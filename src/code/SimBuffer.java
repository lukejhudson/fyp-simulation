package code;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A class to hold both an array of particles as well as the width of the
 * container at that time.
 * 
 * @author Luke
 *
 */
public class SimBuffer {

	private CopyOnWriteArrayList<Particle> particles;
	private double contWidth;

	public SimBuffer(CopyOnWriteArrayList<Particle> particles, double contWidth) {
		this.particles = particles;
		this.contWidth = contWidth;
	}

	/**
	 * @return The list of particles
	 */
	public CopyOnWriteArrayList<Particle> getParticles() {
		return particles;
	}

	/**
	 * @return The width of the container
	 */
	public double getContWidth() {
		return contWidth;
	}
}
