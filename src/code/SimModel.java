package code;

import java.util.ArrayList;
import java.util.Observable;

import code.GraphView.Mode;

/**
 * Model used in the Model-View-Controller architecture.
 * 
 * @author Luke
 *
 */
public class SimModel extends Observable {

	/*
	 * Enum to communicate which aspect of the simulation has changed.
	 */
	public enum Changed {
		NumParticles, ParticleSize, T, Restart, WallMoved, HeatEngines, ActivationEnergy
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

	public SimBuffer getBuffer() {
		return sim.getBuffer();
	}

	public double getAverageT() {
		return sim.getAverageT();
	}

	public double getAverageP() {
		return sim.getAverageP();
	}

	public double getEntropy() {
		return sim.getEntropy();
	}

	public double getAverageNumReactions() {
		return sim.getAverageNumReactions();
	}

	public void setBufferMaxSize(int i) {
		sim.setBufferMaxSize(i);
	}

	public void rollbackBuffer() {
		sim.rollbackBuffer();
	}

	public void setNumParticles(int n) {
		sim.setNumParticles(n);
		// Notify the observers (GraphView) that the number of particles has
		// changed
		setChanged();
		notifyObservers(Changed.NumParticles);
	}

	public int getNumParticles() {
		return sim.getNumParticles();
	}

	public void setT(int t) {
		sim.setT(t);
		// Notify the observers (GraphView) that the temperature has changed
		setChanged();
		notifyObservers(Changed.T);
	}

	public void restartSim() {
		sim.restartSim();
		// Notify the observers (GraphView) that the simulation has been
		// restarted
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

	public ArrayList<Double> getEnergies() {
		return sim.getEnergies();
	}

	public double calculateExpectedActualMSS(double temp) {
		return sim.calculateExpectedActualMSS(temp);
	}

	public double calculateExpectedMSS(double temp) {
		return sim.calculateExpectedMSS(temp);
	}

	public void moveWall(double x) {
		sim.getContainer().moveWall(x);
		if (sim.getContainer().getWidthChange() != 0) {
			// Notify the observers (GraphView) that the right wall has moved
			setChanged();
			notifyObservers(Changed.WallMoved);
		}
	}

	public void setIsInsulated(boolean b) {
		sim.setIsInsulated(b);
	}

	public boolean getIsInsulated() {
		return sim.getIsInsulated();
	}

	/**
	 * @param mode
	 *            The new mode of the simulation (Changed.HeatEngines or
	 *            Changed.ActivationEnergy)
	 */
	public void changeMode(Mode mode) {
		// Notify the observers (GraphView) that the mode has changed
		setChanged();
		switch (mode) {
		case HeatEngines:
			notifyObservers(Changed.HeatEngines);
		case ActivationEnergy:
			notifyObservers(Changed.ActivationEnergy);
			break;
		default:
			break;
		}
	}

	public void setActivationEnergy(double e) {
		sim.setActivationEnergy(e);
	}

	public double getActivationEnergy() {
		return sim.getActivationEnergy();
	}

	public double getActualActivationEnergy() {
		return sim.getActualActivationEnergy();
	}

	public void setDisappearOnActEnergy(boolean b) {
		sim.setDisappearOnActEnergy(b);
	}

	public boolean isDisappearOnActEnergy() {
		return sim.isDisappearOnActEnergy();
	}

	public void setParticlesPushWall(boolean b) {
		sim.setParticlesPushWall(b);
	}
}
