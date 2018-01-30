package ljh590;

import java.util.concurrent.CopyOnWriteArrayList;

public class SimBuffer {

	private CopyOnWriteArrayList<Particle> particles;
	private int contWidth;
	
	public SimBuffer(CopyOnWriteArrayList<Particle> particles, int contWidth) {
		this.particles = particles;
		this.contWidth = contWidth;
	}

	public CopyOnWriteArrayList<Particle> getParticles() {
		return particles;
	}

	public int getContWidth() {
		return contWidth;
	}
}
