package ljh590;

import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimModel extends Observable {

	public enum Changed {
		NumParticles, ParticleSize, T, Restart
	};

	private Simulation sim;

	public SimModel(Simulation sim) {
		this.sim = sim;
	}

	public Container getContainer() {
		return sim.getContainer();
	}

	public boolean bufferEmpty() {
		return sim.bufferEmpty();
	}

	public CopyOnWriteArrayList<Particle> getBuffer() {
		return sim.getBuffer();
	}

	public double getAverageT() {
		return sim.getAverageT();
	}

	public double getAverageP() {
		return sim.getAverageP();
	}

	public void setBufferMaxSize(int i) {
		sim.setBufferMaxSize(i);
	}

	public void rollbackBuffer() {
		sim.rollbackBuffer();
	}

	public void setNumParticles(int n) {
		sim.setNumParticles(n);
		setChanged();
		notifyObservers(Changed.NumParticles);
	}

	public void setParticleSize(int i) {
		sim.setParticleSize(i);
		setChanged();
		notifyObservers(Changed.ParticleSize);
	}

	public void setT(int t) {
		sim.setT(t);
		setChanged();
		notifyObservers(Changed.T);
	}

	public void restartSim() {
		sim.restartSim();
		setChanged();
		notifyObservers(Changed.Restart);
	}

	public void pauseSim() {
		sim.pauseSim();
	}

	public void resumeSim() {
		sim.resumeSim();
	}
	
	public ArrayList<Double> getSpeeds() {
		return sim.getSpeeds();
	}
}
