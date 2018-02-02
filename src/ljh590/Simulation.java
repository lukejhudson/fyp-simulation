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
	private final int maxMeasurements = 100;

	private double activationEnergy = 10;
	private boolean disappearOnActEnergy = false;
	// The number of reactions in this iteration
	private int numReactions;
	// Stores the previously calculated reactions/iteration
	private CopyOnWriteArrayList<Integer> previousNoReactions;

	// Temperature of the walls in Kelvin
	private int T;
	private static int defaultT = 300;
	// Stores the previously calculated temperatures
	private CopyOnWriteArrayList<Double> previousTs;
	// Stores the previously calculated pressures
	private CopyOnWriteArrayList<Double> previousPs;
	// Calculated pressure for one tick
	private double currP;
	// Changes in temperature for this iteration
	private double tChange;
	// Stores the previously calculated changes in temperature
	private CopyOnWriteArrayList<Double> previousTChanges;
	// Buffer of ticks
	private CopyOnWriteArrayList<SimBuffer> buffer;
	// Maximum number of elements in the buffer
	private int bufferMaxSize = 10;
	// Whether we need to roll back the buffer
	boolean rollback = false;
	// List of particles
	private CopyOnWriteArrayList<Particle> particles;
	private Container container;

	private int numParticles;
	private int numActiveParticles;
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

	// If the walls are insulated then heat is not passively transferred between
	// the walls and particles
	private boolean isInsulated = false;

	// Are the particles allowed to push the walls?
	private boolean particlesPushWall = false;

	private Timer timer;

	public Simulation() {
		this(defaultT, defaultNumParticles, defaultDelay);
	}

	public Simulation(int T, int numParticles, int delay) {
		this.T = T;
		this.numParticles = numParticles;
		this.delay = delay;
		pixelSize = particleDiam / (2 * radius);
		container = new Container(defaultWidth, defaultHeight, pixelSize);
		setup();
	}

	private void setup() {
		numActiveParticles = numParticles;
		particles = new CopyOnWriteArrayList<Particle>();
		buffer = new CopyOnWriteArrayList<SimBuffer>();
		pixelSize = particleDiam / (2 * radius);
		container.setPixelSize(pixelSize);
		previousTs = new CopyOnWriteArrayList<Double>();
		previousPs = new CopyOnWriteArrayList<Double>();
		previousTChanges = new CopyOnWriteArrayList<Double>();
		previousNoReactions = new CopyOnWriteArrayList<Integer>();
		if (benchmark) {
			delay = 0;
		}
		for (int i = 0; i < numParticles; i++) {
			particles.add(new Particle(radius));
		}
		spawn();
		double expectedActualMSS = calculateExpectedActualMSS(defaultT);
		double expectedActualRMSS = Math.sqrt(expectedActualMSS);
		double newActualRMSS = Math.sqrt(calculateExpectedActualMSS(T));
		double speedIncrease = newActualRMSS / expectedActualRMSS;
		System.out.println(speedIncrease);
		for (Particle p : particles) {
			// Multiply x and y velocities by (1/sqrt2)
			p.getVel().scale(speedIncrease);
		}
		speedRatio = 1;
		double rmss = Math.sqrt(meanSquareSpeed());
		speedRatio = newActualRMSS / rmss;
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
		if (rollback) {
			if (!buffer.isEmpty()) {
				particles = buffer.get(0).getParticles();
			}
			buffer.clear();
			rollback = false;
		}
		currP = 0;
		// System.out.println(buffer.size());
		if (benchmark || buffer.size() < bufferMaxSize) {
			for (int i = 0; i < numParticles; i++) {
				Particle p = particles.get(i);
				if (p.isActive()) {
					p.move();
					collideWall(p);
					for (int j = 0; j < numParticles; j++) {
						if (i != j && particles.get(j).isActive()) {
							collideParticle(p, particles.get(j));
						}
					}
					if (Math.pow(p.getVel().normalise(), 2) > activationEnergy) {
						numReactions++;
						if (disappearOnActEnergy) {
							p.setActive(false);
							numActiveParticles--;
						}
					}
				}
			}
			iterations++;
			SimBuffer b = new SimBuffer(createCopy(), container.getWidth());
			buffer.add(b);
			// System.out.println(particles);
		}
		calculateCurrT();
		calculateCurrP();

		if (previousTChanges.size() > maxMeasurements) {
			previousTChanges.remove(0);
		}
		previousTChanges.add(tChange);
		// System.out.println("tChange: " + tChange);
		tChange = 0;

		if (previousNoReactions.size() > maxMeasurements) {
			previousNoReactions.remove(0);
		}
		previousNoReactions.add(numReactions);
		numReactions = 0;
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
		assert (p.isActive());
		// Vector ppos = new Vector(p.getPos());
		// Vector qpos = new Vector(q.getPos());
		// System.out.println("before " + p.getPos());
		Vector n = new Vector(p.getPos());
		n.sub(q.getPos());
		// System.out.println("after " + p.getPos());
		// If the two particles are overlapping
		if (n.sqrNorm() < Math.pow((2 * p.getRadius()), 2)) {
			// System.out.println("COLLISION");
			// Make sure particles are not on top of each other
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
		int wallX = (int) container.getWidth();
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
		// The squared speed that we expect for this temperature (in m/s^2)
		double expectedMSS = calculateExpectedActualMSS(T);
		// The actual squared speed of the particle (in m/s^2)
		double actualMSS = p.getVel().sqrNorm() * speedRatio * speedRatio;
		// The energy of the particle before changing its speed
		double prevEnergy = Math.pow(p.getVel().normalise(), 2);

		// Find how much the wall has moved since last iteration
		double wallSpeed = (double) container.getWidthChange() / 3.0;
		// Don't let the particles gain too much energy
		if (wallSpeed < -20) {
			wallSpeed = -20;
		}

		// When insulation is off and a particle collides with any wall, move
		// its speed closer to the expected speed for this temperature (the only
		// debatable part here is the 2.0 and 7.0)
		if (!isInsulated && (wallSpeed == 0 || w != Wall.E)) {
			double difference = expectedMSS - actualMSS;
			double ratioMSS;
			double scaleSpeedUp = 1.5; // default 2
			double scaleSlowDown = 6.125; // default 7
			if (difference > 0) { // Wants to speed up
				ratioMSS = (actualMSS + (difference / scaleSpeedUp)) / actualMSS;
			} else { // Wants to slow down
				ratioMSS = (actualMSS + (difference / scaleSlowDown)) / actualMSS;
			}
			p.getVel().scale(Math.sqrt(Math.abs(ratioMSS)));
		}

		// Calculate initial factor based on how far the right wall has been
		// moved
		// double factor = 1 - ((double) container.getWidthChange() / 10d);
		// Only the right wall can push particles
		// if (w != Wall.E && factor > 1) {
		// factor = 1;
		// }
		// When insulation is off, only particles colliding with the right wall
		// will lose energy when moving the wall outwards (stops the particles
		// from losing too much energy)
		// if (w != Wall.E && !isInsulated) {// factor < 1) {
		// factor = 1;
		// }

		// tempScaleFactor is used to ensure the particles slow down at the
		// correct rate when moving the wall outwards (to return to their
		// original temperature)

		// Approximate values that tempScaleFactor should produce:
		// 4000k --> 0.97
		// 1000k --> 0.95
		// 500k --> 0.89
		// 300k --> 0.875
		// 200k --> 0.8
		// double tempScaleFactor = 0.98 - (200 / (5.5 * (double) T));
		// double tempScaleFactor = 0.5; // Previous value
		// System.out.println(tempScaleFactor);
		// if (factor < tempScaleFactor) {
		// factor = tempScaleFactor;
		// }
		// Don't let particles get too fast
		// if ((actualMSS / expectedMSS) > 20) {
		// factor = 0.5;
		// }
		// So particles won't slow down when moving the wall outward (not
		// insulated only)
		// if (factor < 1 && !isInsulated) {
		// factor = 1;
		// }
		// if (!isInsulated) factor = 1;

		if (particlesPushWall && w == Wall.E && container.getWidth() != container.getMaxWidth() && wallSpeed >= 0) {
			// Mass of wall relative to particle
			int m = 10;

			double vx = p.getVelX();
			double newVX = (vx * (1 - m)) / (1 + m);
			double wallVX = (2 * vx) / (1 + m);

			p.setVelX(Math.abs(newVX));
			container.pushWall(Math.abs(wallVX));

			System.out.println("ux = " + vx + ", vx = " + newVX);
			System.out.println();
			System.out.println("wallVX = " + wallVX + ", width = " + container.getWidth());
		}

		// Scale the particle's speed by the factor. When the right wall is
		// moving in, only particles which are pushed by it are affected (and
		// only their x-velocity is scaled). When the right wall is moving
		// outwards, all particles' velocities are scaled by the factor (except
		// for particles which collide with the right wall, which for some
		// unknown reason only have their x-velocity scaled still).
		double vx, vy;
		switch (w) {
		case N:
			vy = p.getVelY();
			p.setVelY(-vy);
			// p.getVel().scale(factor);
			break;
		case E:
			vx = p.getVelX();
			// If adding the wall speed to the particle (Special and very rare
			// case)
			if (wallSpeed > 0 && Math.abs(vx) < wallSpeed) {
				// System.out.println("!!!!!!!!!");
				p.setVelX(-vx / 2);
			} else if (wallSpeed != 0 && (p.getVel().sqrNorm() < 3 * calculateExpectedMSS(T) || wallSpeed > 0)) {
				// Math.abs(vx) < 5 * Math.abs(wallSpeed)
				// If colliding with a wall moving inwards and the particle
				// isn't moving too fast
				p.setVelX(-Math.abs(vx) + wallSpeed);
			} else {
				p.setVelX(-vx);
			}
			// System.out.println("\nvx = " + vx + "\nwallSpeed = " + wallSpeed
			// + "\nnew vx = " + p.getVelX());
			// p.setVelX(-vx * factor);
			break;
		case S:
			vy = p.getVelY();
			p.setVelY(-vy);
			// p.getVel().scale(factor);
			break;
		case W:
			vx = p.getVelX();
			p.setVelX(-vx);
			// p.getVel().scale(factor);
			break;
		}
		double afterEnergy = Math.pow(p.getVel().normalise(), 2);
		// System.out.println("afterEnergy: " + afterEnergy);
		// Keep track of the change in energy to create the entropy vs
		// temperature graph
		if (w != Wall.E) {
			tChange += (afterEnergy - prevEnergy);
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
		double pressure = (numActiveParticles * particles.get(0).getMass() * meanSquareSpeed())
				/ (2 * container.getActualVolume());
		// System.out.println("numParticles: " + numParticles + "\nmass: " +
		// particles.get(0).getMass() + "\nMSS: "
		// + meanSquareSpeed() + "\nvol: " + container.getActualVolume());
		previousPs.add(pressure);
	}

	private double meanSquareSpeed() {
		double squareSpeed = 0;
		for (Particle p : particles) {
			if (p.isActive()) {
				squareSpeed += p.getVel().sqrNorm() * speedRatio * speedRatio;
			}
		}
		return squareSpeed /= numActiveParticles;
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

		int wallX = (int) container.getWidth();
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
		// calculateExpectedActualMSS(T);
	}

	public int getNumParticles() {
		return numParticles;
	}

	public void setNumParticles(int numParticles) {
		Simulation.defaultNumParticles = numParticles;
	}

	public int getNumActiveParticles() {
		return numActiveParticles;
	}

	public void setNumActiveParticles(int numActiveParticles) {
		this.numActiveParticles = numActiveParticles;
	}

	public int getBufferMaxSize() {
		return bufferMaxSize;
	}

	public void setBufferMaxSize(int b) {
		bufferMaxSize = b;
	}

	public SimBuffer getBuffer() {
		return buffer.remove(0);
	}

	public void rollbackBuffer() {
		rollback = true;
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

	public double getAverageTChange() {
		Iterator<Double> iter = previousTChanges.iterator();
		double tot = 0;
		while (iter.hasNext()) {
			tot += iter.next();
		}
		return tot / previousTChanges.size();
	}

	public double getAverageNoReactions() {
		Iterator<Integer> iter = previousNoReactions.iterator();
		double tot = 0;
		while (iter.hasNext()) {
			tot += iter.next();
		}
		return tot / previousNoReactions.size();
	}

	public double calculateExpectedActualMSS(double temp) {
		return (3 * k * temp) / particles.get(0).getMass();
	}

	public double calculateExpectedMSS(double temp) {
		return ((3 * k * temp) / particles.get(0).getMass()) / (speedRatio * speedRatio);
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
		if (rollback) {
			if (!buffer.isEmpty()) {
				particles = buffer.get(0).getParticles();
			}
			buffer.clear();
			rollback = false;
		}
	}

	public ArrayList<Double> getSpeeds() {
		ArrayList<Double> speeds = new ArrayList<Double>();
		for (Particle p : particles) {
			if (p.isActive()) {
				speeds.add(p.getVel().normalise());
			}
		}
		return speeds;
	}

	public ArrayList<Double> getEnergies() {
		ArrayList<Double> energies = new ArrayList<Double>();
		double energy;
		for (Particle p : particles) {
			if (p.isActive()) {
				energy = Math.pow(p.getVel().normalise(), 2);
				energies.add(energy);
			}
		}
		return energies;
	}

	public void setIsInsulated(boolean b) {
		this.isInsulated = b;
	}

	public boolean getIsInsulated() {
		return this.isInsulated;
	}

	public double getActivationEnergy() {
		return activationEnergy;
	}

	public double getActualActivationEnergy() {
		// System.out.println(0.5 * particles.get(0).getMass() *
		// (activationEnergy * speedRatio * speedRatio));
		return 0.5 * particles.get(0).getMass() * (activationEnergy * speedRatio * speedRatio);
	}

	public void setActivationEnergy(double activationEnergy) {
		this.activationEnergy = activationEnergy;
	}

	public boolean isDisappearOnActEnergy() {
		return disappearOnActEnergy;
	}

	public void setDisappearOnActEnergy(boolean disappearOnActEnergy) {
		this.disappearOnActEnergy = disappearOnActEnergy;
	}

	public void setParticlesPushWall(boolean particlesPushWall) {
		this.particlesPushWall = particlesPushWall;
	}
}
