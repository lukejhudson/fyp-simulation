package ljh590;

import java.util.concurrent.CopyOnWriteArrayList;

public class SimBuffer {

	private CopyOnWriteArrayList<Particle> particles;
	private double contWidth;
	
	public SimBuffer(CopyOnWriteArrayList<Particle> particles, double contWidth) {
		this.particles = particles;
		this.contWidth = contWidth;
	}

	public CopyOnWriteArrayList<Particle> getParticles() {
		return particles;
	}

	public double getContWidth() {
		return contWidth;
	}
}
