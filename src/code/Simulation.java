package code;

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
 * Runs the simulation side of the program.
 * 
 * @author Luke
 */
public class Simulation extends Thread implements ActionListener {

	// North / East / South / West wall
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

	// The energy required to trigger some process (arbitrary units)
	private double activationEnergy = 10;
	// When a particle reaches the activation energy, should it disappear?
	private boolean disappearOnActEnergy = false;
	// The number of reactions in this iteration
	private int numReactions;
	// Stores the previously calculated reactions/iteration
	private CopyOnWriteArrayList<Integer> previousNumReactions;

	// Temperature of the walls in Kelvin
	private int T;
	private static int defaultT = 300;
	// Stores the previously calculated temperatures
	private CopyOnWriteArrayList<Double> previousTs;
	// Stores the previously calculated pressures
	private CopyOnWriteArrayList<Double> previousPs;
	// Changes in temperature for this iteration
	private double tChange;
	// Stores the previously calculated changes in temperature
	private CopyOnWriteArrayList<Double> previousTChanges;
	// Stores the previously calculated entropy values
	private double entropy;

	// Buffer of ticks
	private CopyOnWriteArrayList<SimBuffer> buffer;
	// Maximum number of elements in the buffer
	private int bufferMaxSize = 10;
	// Whether we need to roll back the buffer
	boolean rollback = false;
	// List of particles
	private CopyOnWriteArrayList<Particle> particles;
	// Container object which holds the particles
	private Container container;
	// Number of particles currently in the simulation
	private int numParticles;
	private static int defaultNumParticles = 250;
	// Has the user requested a change in the number of particles?
	private boolean numParticlesChanged = false;

	// The time to wait between iterations (ms)
	private int delay;
	private static int defaultDelay = 2;

	// Default container info
	private int defaultWidth = 900;
	private int defaultHeight = 700;

	// Are we running a benchmark? (Only used in testing)
	private boolean benchmark = false;
	// Number of iterations calculated so far in the benchmark
	private int iterations = 0;
	// Total number of iterations to calculate in the benchmark
	private int iterLimit = 5000;

	// If the walls are insulated then heat is not passively transferred between
	// the walls and particles
	private boolean isInsulated = false;

	// Are the particles allowed to push the walls?
	private boolean particlesPushWall = false;

	private Timer timer;
	// When true, the simulation will remove the 0th element of the buffer
	private boolean removeBuffer0 = false;

	/**
	 * Create a simulation with default values.
	 */
	public Simulation() {
		this(defaultT, defaultNumParticles, defaultDelay);
	}

	/**
	 * Create a simulation with specified values.
	 * 
	 * @param T
	 *            The initial wall temperature of the simulation
	 * @param numParticles
	 *            The initial number of particles
	 * @param delay
	 *            The time to wait in between iterations
	 */
	public Simulation(int T, int numParticles, int delay) {
		this.T = T;
		this.numParticles = numParticles;
		this.delay = delay;
		pixelSize = particleDiam / (2 * Particle.radius);
		container = new Container(defaultWidth, defaultHeight, pixelSize);
		setup();
	}

	/**
	 * Initialise variables and the particles themselves.
	 */
	private void setup() {
		// Initialise variables
		iterations = 0;
		particles = new CopyOnWriteArrayList<Particle>();
		buffer = new CopyOnWriteArrayList<SimBuffer>();
		pixelSize = particleDiam / (2 * Particle.radius);
		container.setPixelSize(pixelSize);
		previousTs = new CopyOnWriteArrayList<Double>();
		previousPs = new CopyOnWriteArrayList<Double>();
		previousTChanges = new CopyOnWriteArrayList<Double>();
		entropy = 0;
		previousNumReactions = new CopyOnWriteArrayList<Integer>();
		numReactions = 0;
		if (benchmark) {
			delay = 0;
		}
		// Create the particles
		for (int i = 0; i < numParticles; i++) {
			particles.add(new Particle());
		}
		// Calculate the initial positions and velocities of the particles
		spawn();
		// Calculate the ratio between the velocity of the particles in the
		// simulation and what we would expect them to be in real life
		double expectedActualMSS = calculateExpectedActualMSS(defaultT);
		double expectedActualRMSS = Math.sqrt(expectedActualMSS);
		double newActualRMSS = Math.sqrt(calculateExpectedActualMSS(T));
		double speedIncrease = newActualRMSS / expectedActualRMSS;
		for (Particle p : particles) {
			p.getVel().scale(speedIncrease);
		}
		speedRatio = 1;
		double rmss = Math.sqrt(meanSquareSpeed());
		speedRatio = newActualRMSS / rmss;
	}

	/*
	 * Start the simulation
	 */
	@Override
	public void run() {
		// Store starting time, for benchmark
		Instant start = Instant.now();
		if (benchmark) {
			System.out.println("STARTING BENCHMARK");
		}
		// Start the timer, which runs the actionPerformed function below
		// repeatedly after a delay of delay
		timer = new Timer(delay, this);
		timer.start();
		// If we're running a benchmark, start it
		if (benchmark) {
			while (iterations < iterLimit) {
				try {
					Thread.sleep(1);
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
		// Do we need to edit the buffer?
		if (removeBuffer0 && !buffer.isEmpty()) {
			buffer.remove(0);
			removeBuffer0 = false;
		}
		// Do we need to empty the buffer?
		// (Need low response rates, e.g. when moving a wall manually)
		if (rollback) {
			if (!buffer.isEmpty()) {
				particles = buffer.get(0).getParticles();
			}
			buffer.clear();
			rollback = false;
		}
		// Do we need to update the number of particles?
		if (numParticlesChanged && Simulation.defaultNumParticles != numParticles) {
			int n = Simulation.defaultNumParticles - numParticles;
			if (n > 0) {
				// Add particles
				addParticles(n);
			} else {
				// Remove particles
				removeParticles(Math.abs(n));
			}
			numParticles = Simulation.defaultNumParticles;
			numParticlesChanged = false;
		}
		// Do we need to update all the particles?
		if (benchmark || buffer.size() < bufferMaxSize) {
			// Move the particles and resolve and collisions
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
			// Check for new reactions
			for (int i = 0; i < numParticles; i++) {
				Particle p = particles.get(i);
				if (Math.pow(p.getVel().normalise(), 2) > activationEnergy) {
					numReactions++;
					if (disappearOnActEnergy) {
						Simulation.defaultNumParticles--;
						numParticles = Simulation.defaultNumParticles;
						particles.remove(i);
					}
				}
			}
			iterations++;
			// Update the buffer
			SimBuffer b = new SimBuffer(createCopy(), container.getWidth());
			buffer.add(b);

			// Calculate updates values of temperature and pressure
			calculateCurrT();
			calculateCurrP();

			// Keep track of the change in temperature
			if (previousTChanges.size() > maxMeasurements) {
				previousTChanges.remove(0);
			}
			previousTChanges.add(tChange);
			// Update the entropy value
			if (!isInsulated) {
				entropy += tChange / previousTs.get(previousTs.size() - 1);
			}
			tChange = 0;
			// Keep track of the number of reactions
			if (previousNumReactions.size() > maxMeasurements) {
				previousNumReactions.remove(0);
			}
			previousNumReactions.add(numReactions);
			numReactions = 0;
		}
	}

	/**
	 * Creates a copy of the particles list with all new particles, so that
	 * updating one list does not affect the other.
	 * 
	 * @return The copy of the list of particles
	 */
	private CopyOnWriteArrayList<Particle> createCopy() {
		CopyOnWriteArrayList<Particle> copy = new CopyOnWriteArrayList<Particle>();
		Particle q;
		for (Particle p : particles) {
			q = p.createCopy();
			copy.add(q);
		}
		return copy;
	}

	/**
	 * Checks if there has been a collision between two particles, p and q. If
	 * there has been a collision, update their positions and velocities
	 * accordingly.
	 * 
	 * The physics was adapted from the collideAtoms code at
	 * https://sites.google.com/site/drjohnbmatthews/kineticmodel/code#Ensemble,
	 * which references a very helpful explanation of the physics at
	 * http://www.vobarian.com/collisions/2dcollisions2.pdf.
	 * 
	 * @param p
	 *            The first particle
	 * @param q
	 *            The second particle
	 */
	public void collideParticle(Particle p, Particle q) {
		// Calculate the distance between p and q in vector form
		Vector dist = new Vector(p.getPos());
		dist.sub(q.getPos());
		// If the two particles are overlapping
		if (dist.sqrNorm() < Math.pow((2 * Particle.radius), 2)) {
			// Make sure particles are not exactly on top of each other, and
			// move them slightly if they are
			if (p.getPos().equals(q.getPos())) {
				// Move p a tiny bit
				Vector tinyVel = new Vector(p.getVel());
				p.getPos().add(tinyVel.scale(0.1));
				// Move q a tiny bit
				tinyVel = new Vector(q.getVel());
				q.getPos().add(tinyVel.scale(0.1));
				// Recalculate distance
				dist = new Vector(p.getPos());
				dist.sub(q.getPos());
			}
			// Move particles back to where they collided
			// u = unit, t = tangent, n = normal
			double un = ((2 * Particle.radius) - dist.normalise()) / 2;
			Vector distUnit = new Vector(dist);
			distUnit.unitVector();
			p.getPos().add(distUnit.scale(un));
			distUnit = new Vector(dist);
			distUnit.unitVector();
			q.getPos().add(distUnit.scale(-un));
			// Find normal and tangential components of v1 and v2
			distUnit = new Vector(dist);
			distUnit.unitVector();

			Vector ut = new Vector(-distUnit.getY(), distUnit.getX());
			Vector v1 = new Vector(p.getVel());
			Vector v2 = new Vector(q.getVel());

			double v1n = distUnit.dot(v1);
			double v1t = ut.dot(v1);
			double v2n = distUnit.dot(v2);
			double v2t = ut.dot(v2);

			// Calculate new v1 and v2 in normal direction
			double v1nNew = (v2n);
			double v2nNew = (v1n);

			// Update velocities with the sum of normal and tangential
			// components
			v1.set(distUnit);
			v1.scale(v1nNew);
			v2.set(ut);
			v2.scale(v1t);
			v1.add(v2);
			p.setVel(v1);

			v1 = new Vector(distUnit);
			v1.scale(v2nNew);
			v2 = new Vector(ut);
			v2.scale(v2t);
			v1.add(v2);
			q.setVel(v1);
		}
	}

	/**
	 * Checks if a particle p has collided with a wall. If it has, update its
	 * velocity based on which wall it has collided with.
	 * 
	 * @param p
	 *            The particle to check
	 */
	public void collideWall(Particle p) {
		int wallX = (int) container.getWidth();
		int wallY = (int) container.getHeight();
		int r = Particle.radius;
		if (p.getX() < r) { // Left wall
			p.setX(r); // Move particle back into container
			collideWallSpeed(p, Wall.W); // Update particle's speed
		} else if (p.getX() > (wallX - r)) { // Right wall
			p.setX(wallX - r);
			collideWallSpeed(p, Wall.E);
		}
		if (p.getY() < r) { // Top wall
			p.setY(r);
			collideWallSpeed(p, Wall.N);
		} else if (p.getY() > (wallY - r)) { // Bottom wall
			p.setY(wallY - r);
			collideWallSpeed(p, Wall.S);
		}
	}

	/**
	 * Given a particle p and a wall w, calculate the velocity the particle
	 * should have after colliding with the wall.
	 * 
	 * @param p
	 *            The particle colliding with the wall
	 * @param w
	 *            The wall that the particle has collided with
	 */
	private void collideWallSpeed(Particle p, Wall w) {
		// The energy of the particle before changing its speed
		double prevEnergy = Math.pow(p.getVel().normalise(), 2);

		// Find how much the wall has moved since last iteration
		double wallSpeed = (double) container.getWidthChange();
		// Don't let the particles gain too much energy
		if (isInsulated && wallSpeed < -5) {
			wallSpeed = -5;
		} else if (!isInsulated && wallSpeed < -10) {
			wallSpeed = -10;
		}

		// When particles are allowed to push the right wall
		if (particlesPushWall && w == Wall.E && container.getWidth() != container.getMaxWidth()) {
			double wallM;
			double partM = 1;
			double fact;
			if (isInsulated) {
				// Mass of wall relative to particle
				wallM = 10;
				// Scaling factor for wallM when calculating the particle's
				// velocity
				fact = 5;
			} else {
				wallM = 12;
				partM = 25;
				fact = 6;
			}
			// 1D momentum calculation to calculate the particle's new velocity
			double vx = p.getVelX();
			double newVX;
			if (wallSpeed < 0) {
				newVX = ((vx * (partM - (wallM / fact))) + (2 * wallM * wallSpeed)) / (partM + (wallM / fact));
			} else {
				newVX = (vx * (partM - (wallM / fact))) / (partM + (wallM / fact));
			}
			double wallVX = 2 * ((2 * vx) / (1 + wallM));

			// Update the particle based on the wall's insulation
			if (isInsulated) {
				p.setVelX(Math.abs(newVX));
				p.setVelY(p.getVelY() * 0.5);
			} else {
				p.setVelX(Math.abs(newVX));
			}
			container.pushWall(Math.abs(wallVX));
		}
		// When insulation is off and a particle collides with any wall, move
		// its speed closer to the expected speed for this temperature
		if (!isInsulated && (wallSpeed == 0 || w != Wall.E)) {
			// The squared speed that we expect for this temperature (in
			// m/iteration^2)
			double expectedMSS = calculateExpectedActualMSS(T);
			// The actual squared speed of the particle (in m/iteration^2)
			double actualMSS = p.getVel().sqrNorm() * speedRatio * speedRatio;
			double difference = expectedMSS - actualMSS;
			double ratioMSS;
			double scaleSpeedUp;
			double scaleSlowDown;
			// Scale the particle's speed to try to keep the average temperature
			// of the particles approximately equal to the wall temperature
			if (wallSpeed > 0) {
				scaleSpeedUp = 1;
				scaleSlowDown = 15;
			} else if (wallSpeed < 0) {
				scaleSpeedUp = 1.75;
				scaleSlowDown = 1.75;
			} else {
				scaleSpeedUp = 1;
				scaleSlowDown = 4.1;
			}
			if (difference > 0) { // Wants to speed up
				ratioMSS = (actualMSS + (difference / scaleSpeedUp)) / actualMSS;
			} else { // Wants to slow down
				ratioMSS = (actualMSS + (difference / scaleSlowDown)) / actualMSS;
			}
			p.getVel().scale(Math.sqrt(Math.abs(ratioMSS)));
		}

		// Update the particle's velocity based on which wall it collided with
		double vx, vy;
		switch (w) {
		case N:
			vy = p.getVelY();
			p.setVelY(-vy);
			break;
		case E:
			vx = p.getVelX();
			// If the wall is moving right and the particle is moving slower
			// than the wall but still collides with the wall
			if (!particlesPushWall && wallSpeed > 0 && Math.abs(vx) < wallSpeed) {
				p.setVelX(-vx / 2);
			} else if (!particlesPushWall && wallSpeed != 0) {
				// If colliding with a moving wall
				double wallM;
				double partM = 1;
				if (isInsulated) {
					// Mass of wall relative to particle
					wallM = 2.75;
					// Scaling factor for wallM when calculating the particle's
					// velocity
				} else {
					wallM = 1;
					partM = 5;
				}
				// 1D momentum calculation to calculate the particle's new
				// velocity
				double newVX;
				if (wallSpeed > 0) {
					newVX = ((vx * (1 - 5)) + (2 * 5 * wallSpeed)) / (1 + 5);
				} else {
					newVX = ((vx * (partM - wallM)) + (3 * wallM * wallSpeed)) / (partM + wallM);
				}

				if (isInsulated) {
					p.setVelX(newVX);
				} else {
					p.setVelX(newVX);
				}
			} else {
				// Wall is stationary
				p.setVelX(-vx);
			}
			break;
		case S:
			vy = p.getVelY();
			p.setVelY(-vy);
			break;
		case W:
			vx = p.getVelX();
			p.setVelX(-vx);
			break;
		}
		double afterEnergy = Math.pow(p.getVel().normalise(), 2);
		// Keep track of the change in energy to create the entropy vs
		// temperature graph
		if (w != Wall.E) {
			tChange += (afterEnergy - prevEnergy);
		}
	}

	/**
	 * Calculates the current temperature using the current speed of the
	 * particles.
	 */
	private void calculateCurrT() {
		if (previousTs.size() > maxMeasurements) {
			previousTs.remove(0);
		}
		// T = (m * mss) / 3k
		double temp = (Particle.mass * meanSquareSpeed()) / (3 * k);
		previousTs.add(temp);
	}

	/**
	 * Calculates the current pressure using the current speed of the particles
	 * and the volume of the container.
	 */
	private void calculateCurrP() {
		if (previousPs.size() >= maxMeasurements) {
			previousPs.remove(0);
		}
		// P = (N * m * mss) / (2 * V)
		double pressure = (numParticles * Particle.mass * meanSquareSpeed()) / (2 * container.getActualVolume());
		previousPs.add(pressure);
	}

	/**
	 * Calculates the mean square speed of the particles.
	 * 
	 * @return The mean square speed of the particles
	 */
	private double meanSquareSpeed() {
		// (speedRatio is the ratio of simulated and actual particle speeds)
		double squareSpeed = 0;
		for (Particle p : particles) {
			squareSpeed += p.getVel().sqrNorm() * speedRatio * speedRatio;
		}
		return squareSpeed /= numParticles;
	}

	/**
	 * Generates a valid starting position and velocity for each particle.
	 */
	private void spawn() {
		double wallX = container.getWidth();
		double wallY = container.getHeight();

		Random rand = new Random();
		for (Particle p : particles) {
			// Give the particle a random position
			p.setPos(wallX * rand.nextDouble(), wallY * rand.nextDouble());
			for (Particle q : particles) {
				if (!p.equals(q)) {
					// Check for problems with the given position (such as
					// overlapping, outside of container)
					while ((p.getPos().getX() < Particle.radius || p.getPos().getX() > wallX - Particle.radius)
							|| (p.getPos().getY() < Particle.radius || p.getPos().getY() > wallY - Particle.radius)
							|| p.getPos().sqrDist(q.getPos()) < Math.pow(Particle.radius, 2)) {
						// If there are problems, generate a new random position
						p.setPos(wallX * rand.nextDouble(), wallY * rand.nextDouble());
					}
				}
			}
			// Give the particle a random velocity
			p.setVel(4 * (rand.nextDouble() - 0.5), 4 * (rand.nextDouble() - 0.5));
		}
	}

	/**
	 * Adds the specified number of particles to the simulation.
	 * 
	 * @param num
	 *            The number of particles to add to the simulation
	 */
	private void addParticles(int num) {
		double wallX = container.getWidth();
		double wallY = container.getHeight();
		double mss = meanSquareSpeed();
		double newMSS;
		double ratioMSS;

		Random rand = new Random();
		// Randomly spawn the particle using the same method as the spawn()
		// function above
		for (int i = 0; i < num; i++) {
			Particle newP = new Particle();
			newP.setPos(wallX * rand.nextDouble(), wallY * rand.nextDouble());
			if (particles.size() != 0) {
				for (Particle p : particles) {
					if (!p.equals(newP)) {
						// Check for problems with the given position (such as
						// overlapping, outside of container)
						while ((newP.getPos().getX() < Particle.radius
								|| newP.getPos().getX() > wallX - Particle.radius)
								|| (newP.getPos().getY() < Particle.radius
										|| newP.getPos().getY() > wallY - Particle.radius)
								|| newP.getPos().sqrDist(p.getPos()) < Math.pow(Particle.radius, 2)) {
							// If there are problems, generate a new random
							// position
							newP.setPos(wallX * rand.nextDouble(), wallY * rand.nextDouble());
						}
					}
				}
			}
			// Give the particle a random velocity
			newP.setVel(4 * (rand.nextDouble() - 0.5), 4 * (rand.nextDouble() - 0.5));
			if (particles.size() == 0) {
				// If it's the first particle added, scale the speed of the
				// particle to the current wall temperature
				double expectedActualMSS = calculateExpectedActualMSS(defaultT);
				double expectedActualRMSS = Math.sqrt(expectedActualMSS);
				double newActualRMSS = Math.sqrt(calculateExpectedActualMSS(T));
				double speedIncrease = newActualRMSS / expectedActualRMSS;
				newP.getVel().scale(speedIncrease);
			} else {
				// If it's not the first particle added, scale the speed of the
				// particle to that of the other particles
				newMSS = newP.getVel().sqrNorm() * speedRatio * speedRatio;
				ratioMSS = mss / newMSS;
				newP.getVel().scale(Math.sqrt(ratioMSS));
			}
			particles.add(newP);
			numParticles++;
			// Recalculate mss for next particle
			if (particles.size() == 1) {
				mss = meanSquareSpeed();
			}
		}
	}

	/**
	 * Removes the specified number of particles from the simulation.
	 * 
	 * @param num
	 *            The number of particles to remove from the simulation
	 */
	private void removeParticles(int num) {
		Random rand = new Random();
		for (int i = 0; i < num; i++) {
			if (Simulation.defaultNumParticles == 0) {
				// If there's only 1 particle left, remove that particle
				// (Simulation.defaultNumParticles is updated prior to calling
				// this function)
				particles.remove(0);
			} else {
				// Otherwise, remove a random particle
				int r = rand.nextInt(Simulation.defaultNumParticles);
				particles.remove(r);
			}
		}
	}

	/**
	 * @return The current temperature
	 */
	public int getT() {
		return T;
	}

	/**
	 * @param newT
	 *            The new current temperature
	 */
	public void setT(int newT) {
		T = newT;
	}

	/**
	 * @return The current number of particles
	 */
	public int getNumParticles() {
		return numParticles;
	}

	/**
	 * @param numParticles
	 *            The new number of particles
	 */
	public void setNumParticles(int numParticles) {
		Simulation.defaultNumParticles = numParticles;
		numParticlesChanged = true;
	}

	/**
	 * @return The maximum size of the buffer
	 */
	public int getBufferMaxSize() {
		return bufferMaxSize;
	}

	/**
	 * @param b
	 *            The new maximum size of the buffer
	 */
	public void setBufferMaxSize(int b) {
		bufferMaxSize = b;
	}

	/**
	 * @return The oldest SimBuffer in the buffer, or null if the buffer is
	 *         empty
	 */
	public SimBuffer getBuffer() {
		try {
			SimBuffer b = buffer.get(0);
			removeBuffer0 = true;
			return b;
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Request that the buffer be emptied
	 */
	public void rollbackBuffer() {
		rollback = true;
	}

	/**
	 * @return True if the buffer is empty
	 */
	public boolean bufferEmpty() {
		return buffer.isEmpty();
	}

	/**
	 * @return The container being used in the simulation
	 */
	public Container getContainer() {
		return this.container;
	}

	/**
	 * @return The average pressure over the last maxMeasurements iterations
	 */
	public double getAverageP() {
		Iterator<Double> iter = previousPs.iterator();
		double tot = 0;
		while (iter.hasNext()) {
			tot += iter.next();
		}
		return tot / previousPs.size();
	}

	/**
	 * @return The average temperature over the last maxMeasurements iterations
	 */
	public double getAverageT() {
		Iterator<Double> iter = previousTs.iterator();
		double tot = 0;
		while (iter.hasNext()) {
			tot += iter.next();
		}
		return tot / previousTs.size();
	}

	/**
	 * @return The current entropy value
	 */
	public double getEntropy() {
		return entropy;
	}

	/**
	 * @return The average number of reactions over the last maxMeasurements
	 *         iterations
	 */
	public double getAverageNumReactions() {
		Iterator<Integer> iter = previousNumReactions.iterator();
		double tot = 0;
		while (iter.hasNext()) {
			tot += iter.next();
		}
		return tot / previousNumReactions.size();
	}

	/**
	 * @param temp
	 *            The temperature to calculate the expected speed
	 * @return The expected mean squared speed for the given temperature, in
	 *         m/iteration ^2
	 */
	public double calculateExpectedActualMSS(double temp) {
		return (3 * k * temp) / Particle.mass;
	}

	/**
	 * @param temp
	 *            The temperature to calculate the expected speed
	 * @return The expected mean squared speed for the given temperature, in
	 *         pixels/iteration ^2
	 */
	public double calculateExpectedMSS(double temp) {
		return ((3 * k * temp) / Particle.mass) / (speedRatio * speedRatio);
	}

	/**
	 * Restarts the simulation to its default parameters
	 */
	public void restartSim() {
		timer.stop();
		numParticles = defaultNumParticles;
		if (numParticles != 0) {
			setup();
		}
		timer.start();
	}

	/**
	 * Resumes a paused simulation
	 */
	public void resumeSim() {
		timer.start();
	}

	/**
	 * Pauses the simulation
	 */
	public void pauseSim() {
		timer.stop();
	}

	/**
	 * @return An ArrayList<Double> of the speeds of all the particles (in
	 *         pixels/iteration)
	 */
	public ArrayList<Double> getSpeeds() {
		ArrayList<Double> speeds = new ArrayList<Double>();
		for (Particle p : particles) {
			speeds.add(p.getVel().normalise());
		}
		return speeds;
	}

	/**
	 * @return An ArrayList<Double> of the energies of all the particles
	 *         (arbitrary units)
	 */
	public ArrayList<Double> getEnergies() {
		ArrayList<Double> energies = new ArrayList<Double>();
		double energy;
		for (Particle p : particles) {
			energy = Math.pow(p.getVel().normalise(), 2);
			energies.add(energy);
		}
		return energies;
	}

	/**
	 * @param b
	 *            The new value for isInsulated
	 */
	public void setIsInsulated(boolean b) {
		this.isInsulated = b;
	}

	/**
	 * @return True if the container is insulated
	 */
	public boolean getIsInsulated() {
		return this.isInsulated;
	}

	/**
	 * @return The current activation energy of the particles
	 */
	public double getActivationEnergy() {
		return activationEnergy;
	}

	/**
	 * @return The current actual activation energy of the particles
	 */
	public double getActualActivationEnergy() {
		return 0.5 * Particle.mass * (activationEnergy * speedRatio * speedRatio);
	}

	/**
	 * @param activationEnergy
	 *            The new activation energy of the particles
	 */
	public void setActivationEnergy(double activationEnergy) {
		this.activationEnergy = activationEnergy;
	}

	/**
	 * @return True if the particles will disappear upon reaching the activation
	 *         energy
	 */
	public boolean isDisappearOnActEnergy() {
		return disappearOnActEnergy;
	}

	/**
	 * @param disappearOnActEnergy
	 *            The new value of disappearOnActEnergy
	 */
	public void setDisappearOnActEnergy(boolean disappearOnActEnergy) {
		this.disappearOnActEnergy = disappearOnActEnergy;
	}

	/**
	 * @param particlesPushWall
	 *            The new value of particlesPushWall
	 */
	public void setParticlesPushWall(boolean particlesPushWall) {
		this.particlesPushWall = particlesPushWall;
	}
}
