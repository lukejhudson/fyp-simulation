package ljh590;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

/**
 * @author Luke Runs the simulation side of the program.
 */
public class Model extends Thread {
	
	// Boltzmann constant, k
	private final double k = 1.38E-23;
	
	// Temperature in Kelvin
	private int T;
	private static int defaultT = 300;
	// Buffer of ticks
	private ArrayList<ArrayList<Particle>> buffer;
	private final int bufferMaxSize = 10;
	// List of particles
	private ArrayList<Particle> particles;
	private int wallX = 1200;
	private int wallY = 800;

	private int numParticles;
	private static int defaultNumParticles = 50;

	private int delay;
	private static int defaultDelay = 5;

	private boolean benchmark = false;

	private int iterations = 0;
	private int iterLimit = 2000;

	public Model() {
		this(defaultT, defaultNumParticles, true, defaultDelay);
	}

	public Model(int T, int numParticles, boolean benchmark, int delay) {
		this.T = T;
		this.numParticles = numParticles;
		this.benchmark = benchmark;
		this.delay = delay;
		this.particles = new ArrayList<Particle>();
		this.buffer = new ArrayList<ArrayList<Particle>>();
		setup();
	}

	private void setup() {
		if (benchmark) {
			delay = 0;
		}
		for (int i = 0; i < numParticles; i++) {
			particles.add(new Particle());
		}
		spawn();
	}

	@Override
	public void run() {
		Instant start = Instant.now();
		if (benchmark) {
			System.out.println("STARTING BENCHMARK");
		}
		while (!benchmark || iterations < iterLimit) {
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
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (benchmark) {
			System.out.println("BENCHMARK COMPLETE");
			Instant end = Instant.now();
			System.out.println("TIME:\n" + Duration.between(start, end));
		}
	}

	private ArrayList<Particle> createCopy() {
		ArrayList<Particle> copy = new ArrayList<Particle>();
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
		if (n.normalise() < (2 * p.getRadius())) {
//			System.out.println("COLLISION");
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
//			System.out.println("before: p: " + p.getVel() + ", q: " + q.getVel());
			v1.set(un);
			v1.scale(v1nNew);
			v2.set(ut);
			v2.scale(v1t);
//			System.out.println("P : v1: " + v1 + ", v2: " + v2);
			v1.add(v2);
//			System.out.println(v1);
			
			p.setVel(v1);
			v1 = new Vector(un);
			v1.scale(v2nNew);
			v2 = new Vector(ut);
			v2.scale(v2t);
//			System.out.println("Q : v1: " + v1 + ", v2: " + v2);
			v1.add(v2);
//			System.out.println(v1);
			q.setVel(v1);
//			System.out.println("after: p: " + p.getVel() + ", q: " + q.getVel());
		}
	}

	public void collideWall(Particle p) {
		int r = p.getRadius();
		if (p.getX() < r) {
			p.setX(r);
			p.setVelX(-p.getVelX());
		} else if (p.getX() > (wallX - r)) {
			p.setX(wallX - r);
			p.setVelX(-p.getVelX());
		}
		if (p.getY() < r) {
			p.setY(r);
			p.setVelY(-p.getVelY());
		} else if (p.getY() > (wallY - r)) {
			p.setY(wallY - r);
			p.setVelY(-p.getVelY());
		}
	}

	private void spawn() {
		System.out.println("SPAWNING");
		Random rand = new Random();
		for (Particle p : particles) {
			p.setPos(wallX * rand.nextDouble(), wallY * rand.nextDouble());
			for (Particle q : particles) {
				if (!p.equals(q)) {
					while ((p.getPos().getX() < p.getRadius() || p.getPos().getX() > this.wallX - p.getRadius())
							|| (p.getPos().getY() < p.getRadius() || p.getPos().getY() > this.wallY - p.getRadius())
							|| p.getPos().sqrDist(q.getPos()) < Math.pow(p.getRadius(), 2)) {
						p.setPos(wallX * rand.nextDouble(), wallY * rand.nextDouble());
					}
				}
			}
			// Average speed in m/s
			double averageSpeed = Math.sqrt((3 * k * T) / p.getMass());
			averageSpeed /= 1000;
			System.out.println(averageSpeed);
			p.setVel(1.414 * averageSpeed * (rand.nextDouble() - 0.5), 1.414 * averageSpeed * (rand.nextDouble() - 0.5));
		}
		System.out.println("DONE SPAWNING");
		System.out.println(particles);
	}

	public int getT() {
		return T;
	}

	public void setT(int t) {
		T = t;
	}

	public int getNumParticles() {
		return numParticles;
	}

	public void setNumParticles(int numParticles) {
		this.numParticles = numParticles;
	}

	public int getWallX() {
		return wallX;
	}

	public void setWallX(int wallX) {
		this.wallX = wallX;
	}

	public int getWallY() {
		return wallY;
	}

	public void setWallY(int wallY) {
		this.wallY = wallY;
	}

	public ArrayList<Particle> getBuffer() {
		ArrayList<Particle> tick = buffer.get(0);
		buffer.remove(0);
		return tick;
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
}
