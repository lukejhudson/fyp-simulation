package ljh590;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Timer;

/**
 * @author Luke Runs the simulation side of the program.
 */
public class Simulation extends Thread implements ActionListener {

	private enum Wall {
		N, E, S, W
	};

	// Boltzmann constant, k
	private final double k = 1.38E-23;
	// Diameter of hydrogen atom
	private double particleDiam = 5.29E-11;
	// Size of 1 pixel in metres
	private double pixelSize;
	// Ratio of simulated and actual particle speeds
	private double speedRatio;
	// Maximum number of pressure and temperature measurements to consider when
	// calculating the average values
	private final int maxMeasurements = 10;

	// Temperature of the walls in Kelvin
	private int T;
	private static int defaultT = 300;
	// Stores the previously calculated temperatures
	private CopyOnWriteArrayList<Double> previousTs;
	// Stores the previously calculated pressures
	private CopyOnWriteArrayList<Double> previousPs;
	// Calculated pressure for one tick
	private double currP;
	// Buffer of ticks
	private CopyOnWriteArrayList<CopyOnWriteArrayList<Particle>> buffer;
	// Maximum number of elements in the buffer
	private int bufferMaxSize = 10;
	// List of particles
	private CopyOnWriteArrayList<Particle> particles;
	private Container container;

	private int numParticles;
	private static int defaultNumParticles = 250;

	private int delay;
	private static int defaultDelay = 2;

	// Default container info
	private int defaultWidth = 900;
	private int defaultHeight = 700;
	// Default particle info
	private int radius = 10;

	private boolean changeSize = false;
	private int newSize = 10;

	private boolean benchmark = false;

	private int iterations = 0;
	private int iterLimit = 5000;

	private Timer timer;

	public Simulation() {
		this(defaultT, defaultNumParticles, defaultDelay);
	}

	public Simulation(int T, int numParticles, int delay) {
		this.T = T;
		this.numParticles = numParticles;
		this.delay = delay;
		pixelSize = particleDiam / (2 * radius);
		this.container = new Container(defaultWidth, defaultHeight, pixelSize);
		setup();
	}

	private void setup() {
		this.particles = new CopyOnWriteArrayList<Particle>();
		this.buffer = new CopyOnWriteArrayList<CopyOnWriteArrayList<Particle>>();
		pixelSize = particleDiam / (2 * radius);
		container.setPixelSize(pixelSize);
		this.previousTs = new CopyOnWriteArrayList<Double>();
		this.previousPs = new CopyOnWriteArrayList<Double>();
		if (benchmark) {
			delay = 0;
		}
		for (int i = 0; i < numParticles; i++) {
			particles.add(new Particle(radius));
		}
		spawn();
		double expectedActualMSS = calculateExpectedActualMSS(defaultT);
		double expectedActualRMSS = Math.sqrt(expectedActualMSS);
		double newActualRMSS = Math.sqrt((3 * k * T) / particles.get(0).getMass());
		double speedIncrease = newActualRMSS / expectedActualRMSS;
		System.out.println(speedIncrease);
		for (Particle p : particles) {
			// Multiply x and y velocities by (1/sqrt2)
			p.getVel().scale(speedIncrease);
		}
		this.speedRatio = 1;
		double rmss = Math.sqrt(meanSquareSpeed());
		this.speedRatio = newActualRMSS / rmss;
		System.out.println("rmss: " + rmss + "\nnewActualRMSS: " + newActualRMSS + "\nspeedRatio: " + speedRatio);

		// double actualRms = Math.sqrt((3 * k * T) /
		// particles.get(0).getMass());
		// this.speedRatio = 1;
		// double rms = Math.sqrt(meanSquareSpeed());
		// this.speedRatio = actualRms / rms;
		// System.out.println("rms: " + rms + "\nactualRms: " + actualRms +
		// "\nspeedRatio: " + speedRatio);
	}

	@Override
	public void run() {
		Instant start = Instant.now();
		if (benchmark) {
			System.out.println("STARTING BENCHMARK");
		}
		timer = new Timer(delay, this);
		timer.start();
		if (benchmark) {
			while (iterations < iterLimit) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("BENCHMARK COMPLETE");
			Instant end = Instant.now();
			System.out.println("TIME:\n" + Duration.between(start, end));
		}
	}

	/*
	 * Performs one iteration of the simulation
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (changeSize) {
			for (Particle p : particles) {
				p.setRadius(newSize);
			}
			radius = newSize;
			changeSize = false;
		}
		currP = 0;
		// System.out.println(buffer.size());
		if (benchmark || buffer.size() < bufferMaxSize) {
			for (int i = 0; i < numParticles; i++) {
				Particle p = particles.get(i);
				p.move();
				collideWall(p);
				for (int j = 0; j < numParticles; j++) {
					if (i != j) {
						collideParticle(p, particles.get(j));
					}
				}
			}
			iterations++;
			buffer.add(createCopy());
			// System.out.println(particles);
		}
		calculateCurrT();
		calculateCurrP();
	}

	private CopyOnWriteArrayList<Particle> createCopy() {
		CopyOnWriteArrayList<Particle> copy = new CopyOnWriteArrayList<Particle>();
		Particle q;
		for (Particle p : particles) {
			q = p.createCopy();
			copy.add(q);
		}
		return copy;
	}

	public void collideParticle(Particle p, Particle q) {
		// Vector ppos = new Vector(p.getPos());
		// Vector qpos = new Vector(q.getPos());
		// System.out.println("before " + p.getPos());
		Vector n = new Vector(p.getPos());
		n.sub(q.getPos());
		// System.out.println("after " + p.getPos());
		// If the two particles are overlapping
		if (n.sqrNorm() < Math.pow((2 * p.getRadius()), 2)) {
//			System.out.println("COLLISION");
			if (p.getPos().equals(q.getPos())) {
				// Move p a tiny bit
				Vector tinyVel = new Vector(p.getVel());
				p.getPos().add(tinyVel.scale(0.1));
				// Move q a tiny bit
				tinyVel = new Vector(q.getVel());
				q.getPos().add(tinyVel.scale(0.1));
				// Recalculate distance
				n = new Vector(p.getPos());
				n.sub(q.getPos());
			}
			// Move particles back to where they collided
			double dr = ((2 * p.getRadius()) - n.normalise()) / 2;
			Vector un = new Vector(n);
			un.unitVector();
			// System.out.println("p before: " + p.getPos());
			p.getPos().add(un.scale(dr));
			// System.out.println("p after: " + p.getPos());
			un = new Vector(n);
			un.unitVector();
			// System.out.println("q before: " + q.getPos());
			q.getPos().add(un.scale(-dr));
			// System.out.println("q after: " + q.getPos());
			// Find normal and tangential components of v1 and v2
			un = new Vector(n);
			un.unitVector();
			Vector ut = new Vector(-un.getY(), un.getX());
			Vector v1 = new Vector(p.getVel());
			Vector v2 = new Vector(q.getVel());
			double v1n = un.dot(v1);
			double v1t = ut.dot(v1);
			double v2n = un.dot(v2);
			double v2t = ut.dot(v2);
			// Calculate new v1 and v2 in normal direction
			double v1nNew = (v2n);
			double v2nNew = (v1n);
			// Update velocities with the sum of normal and tangential
			// components
			// System.out.println("before: p: " + p.getVel() + ", q: " +
			// q.getVel());
			v1.set(un);
			v1.scale(v1nNew);
			v2.set(ut);
			v2.scale(v1t);
			// System.out.println("P : v1: " + v1 + ", v2: " + v2);
			v1.add(v2);
			// System.out.println(v1);
			p.setVel(v1);
			v1 = new Vector(un);
			v1.scale(v2nNew);
			v2 = new Vector(ut);
			v2.scale(v2t);
			// System.out.println("Q : v1: " + v1 + ", v2: " + v2);
			v1.add(v2);
			// System.out.println(v1);
			q.setVel(v1);
			// System.out.println("after: p: " + p.getVel() + ", q: " +
			// q.getVel());
		}
	}

	public void collideWall(Particle p) {
		int wallX = container.getWidth();
		int wallY = container.getHeight();
		int r = p.getRadius();
		if (p.getX() < r) { // Left wall
			p.setX(r);
			collideWallSpeed(p, Wall.W);
			currP += (p.getMass() * Math.abs(p.getVelX() * speedRatio)) / container.getActualVolume();
		} else if (p.getX() > (wallX - r)) { // Right wall
			p.setX(wallX - r);
			collideWallSpeed(p, Wall.E);
			currP += (p.getMass() * Math.abs(p.getVelX() * speedRatio)) / container.getActualVolume();
		}
		if (p.getY() < r) { // Top wall
			p.setY(r);
			collideWallSpeed(p, Wall.N);
			currP += (p.getMass() * Math.abs(p.getVelY() * speedRatio)) / container.getActualVolume();
		} else if (p.getY() > (wallY - r)) { // Bottom wall
			p.setY(wallY - r);
			collideWallSpeed(p, Wall.S);
			currP += (p.getMass() * Math.abs(p.getVelY() * speedRatio)) / container.getActualVolume();
		}
	}

	private void collideWallSpeed(Particle p, Wall w) {
		double expectedMSS = calculateExpectedActualMSS(T);
		// System.out.println("expected: " + expectedMSS + "\nactual: " +
		// meanSquareSpeed());
		double actualMSS = p.getVel().sqrNorm() * speedRatio * speedRatio;
		// System.out.println("expected: " + expectedMSS + "\nactual: " + actualMSS);
		double difference = expectedMSS - actualMSS;
		double ratioMSS;
		if (difference > 0) { // Wants to speed up
			ratioMSS = (actualMSS + (difference / 4d)) / actualMSS;
//			System.out.println(">0: " + ratioMSS);
		} else { // Wants to slow down
			ratioMSS = (actualMSS + (difference / 15d)) / actualMSS;
//			System.out.println("<0: " + ratioMSS);
		}
		// System.out.println("\nexpected: " + Math.sqrt(expectedMSS)/speedRatio +
		// "\nactual: " + Math.sqrt(meanSquareSpeed())/speedRatio);
//		System.out.println("\nexpected RMS: " + expectedMSS + "\nactual RMS:   " + meanSquareSpeed());
//		System.out.println("difference: " + difference);
		// System.out.println("average: " + averageSpeed());
		// System.out.println("before: " + p.getVel().normalise());
//		double before = actualMSS;
//		System.out.println("before:   " + before);
		p.getVel().scale(Math.sqrt(Math.abs(ratioMSS)));// * 1.0363);// + 0.03d);
		// System.out.println("after: " + p.getVel().normalise());
//		double after = p.getVel().sqrNorm() * speedRatio * speedRatio;
//		System.out.println("after:    " + after);
//		System.out.println("diff2: " + (before - after));
//		System.out.println("ratio: " + (difference / (before - after)));
		// System.out.println("expected: " + expectedMss);
		// System.out.println("actual: " + actualMss);
		// System.out.println("diff: " + difference);
		// System.out.println("ratio: " + ratioMss);

		double factor = 1 - ((double) container.getWidthChange() / 10d);
		// Only the right wall can push particles
		if (w != Wall.E && factor > 1) {
			factor = 1;
		}
		if (factor < 0.5) {
			factor = 0.5;
		}
		// Don't let particles get too fast
		if ((actualMSS / expectedMSS) > 30) {
			factor = 0.8;
		}
		// System.out.println(container.getWidthChange() + " " + factor);
		assert (factor > 0);
		double vx, vy;
		switch (w) {
		case N:
			vy = p.getVelY();
			p.setVelY(-vy);
			p.getVel().scale(factor);
			break;
		case E:
			vx = p.getVelX();
			p.setVelX(-vx * factor);
			break;
		case S:
			vy = p.getVelY();
			p.setVelY(-vy);
			p.getVel().scale(factor);
			break;
		case W:
			vx = p.getVelX();
			p.setVelX(-vx);
			p.getVel().scale(factor);
			break;
		}
	}

	private void calculateCurrT() {
		// T = (m * rms) / 3k

		if (previousTs.size() > maxMeasurements) {
			previousTs.remove(0);
		}

		double temp = (particles.get(0).getMass() * meanSquareSpeed()) / (3 * k);
		previousTs.add(temp);

		// double m = particles.get(0).getMass();
		// double tot = 0;
		// for (Particle p : particles) {
		// tot += 0.5 * m * Math.pow(p.getVel().normalise() * speedRatio, 2);
		// }
		// tot /= numParticles;
		// previousTs.add(tot);
		// System.out.println("T: " + tot);
	}

	private void calculateCurrP() {
		if (previousPs.size() >= maxMeasurements) {
			previousPs.remove(0);
		}
		// previousPs.add(currP * speedRatio);
		double pressure = (numParticles * particles.get(0).getMass() * meanSquareSpeed())
				/ (2 * container.getActualVolume());
		// System.out.println("numParticles: " + numParticles + "\nmass: " +
		// particles.get(0).getMass() + "\nMSS: "
		// + meanSquareSpeed() + "\nvol: " + container.getActualVolume());
		previousPs.add(pressure);
	}

	private double meanSquareSpeed() {
		double squareSpeed = 0;
		for (Particle p : particles) {
			squareSpeed += p.getVel().sqrNorm() * speedRatio * speedRatio;
		}
		return squareSpeed /= numParticles;
	}

	private double averageSpeed() {
		double speed = 0;
		for (Particle p : particles) {
			speed += p.getVel().normalise();
		}
		return speed /= numParticles;
	}

	private void spawn() {
		System.out.println("SPAWNING");

		int wallX = container.getWidth();
		int wallY = container.getHeight();

		Random rand = new Random();
		for (Particle p : particles) {
			p.setPos(wallX * rand.nextDouble(), wallY * rand.nextDouble());
			for (Particle q : particles) {
				if (!p.equals(q)) {
					while ((p.getPos().getX() < p.getRadius() || p.getPos().getX() > wallX - p.getRadius())
							|| (p.getPos().getY() < p.getRadius() || p.getPos().getY() > wallY - p.getRadius())
							|| p.getPos().sqrDist(q.getPos()) < Math.pow(p.getRadius(), 2)) {
						p.setPos(wallX * rand.nextDouble(), wallY * rand.nextDouble());
					}
				}
			}
			p.setVel(4 * (rand.nextDouble() - 0.5), 4 * (rand.nextDouble() - 0.5));
			// System.out.println(p.getVel().normalise());
		}
		System.out.println("DONE SPAWNING");
		System.out.println(particles);
	}

	public int getT() {
		return T;
	}

	public void setT(int newT) {
		// double prevActualRms = Math.sqrt((3 * k * T) /
		// particles.get(0).getMass());
		// double newActualRms = Math.sqrt((3 * k * newT) /
		// particles.get(0).getMass());
		// double speedIncrease = newActualRms / prevActualRms;
		// System.out.println(speedIncrease);
		// for (Particle p : particles) {
		// // Multiply x and y velocities by (1/sqrt2)
		// p.getVel().scale(speedIncrease);
		// }
		// this.speedRatio = 1;
		// double rms = Math.sqrt(meanSquareSpeed());
		// this.speedRatio = newActualRms / rms;
		// System.out.println("rms: " + rms + "\nnewActualRms: " + newActualRms
		// + "\nspeedRatio: " + speedRatio);
		T = newT;
		calculateExpectedActualMSS(T);
	}

	public int getNumParticles() {
		return numParticles;
	}

	public void setNumParticles(int numParticles) {
		Simulation.defaultNumParticles = numParticles;
	}

	public int getBufferMaxSize() {
		return bufferMaxSize;
	}

	public void setBufferMaxSize(int b) {
		bufferMaxSize = b;
	}

	public CopyOnWriteArrayList<Particle> getBuffer() {
		return buffer.remove(0);
	}

	public void rollbackBuffer() {
		timer.stop();
		if (!buffer.isEmpty()) {
			particles = buffer.get(0);
		}
		buffer.clear();
		timer.start();
	}

	public boolean bufferEmpty() {
		return buffer.isEmpty();
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getIterations() {
		return iterations;
	}

	public Container getContainer() {
		return this.container;
	}

	public double getAverageP() {
		// PV = NkT
		// P = NkT / V
		// double pressure = (numParticles * k * getAverageT()) /
		// container.getActualVolume();
		// System.out.println("1: " + pressure);
		//
		// pressure = (numParticles * particles.get(0).getMass() *
		// meanSquareSpeed()) / (3 * container.getActualVolume());
		// System.out.println("2: " + pressure);

		Iterator<Double> iter = previousPs.iterator();
		double tot = 0;
		while (iter.hasNext()) {
			tot += iter.next();
		}
		return tot / previousPs.size();
	}

	public double getAverageT() {
		Iterator<Double> iter = previousTs.iterator();
		double tot = 0;
		while (iter.hasNext()) {
			tot += iter.next();
		}
		return tot / previousTs.size();
	}

	private double calculateExpectedActualMSS(double temp) {
		return (3 * k * temp) / particles.get(0).getMass();
	}

	public void setParticleSize(int size) {
		changeSize = true;
		newSize = size;
		pixelSize = particleDiam / (2 * radius);
		container.setPixelSize(pixelSize);
	}

	public void restartSim() {
		timer.stop();
		numParticles = defaultNumParticles;
		setup();
		timer.start();
	}

	public void resumeSim() {
		timer.start();
	}

	public void pauseSim() {
		timer.stop();
	}

	public ArrayList<Double> getSpeeds() {
		ArrayList<Double> speeds = new ArrayList<Double>();
		for (Particle p : particles) {
			speeds.add(p.getVel().normalise());
		}
		return speeds;
	}
}
