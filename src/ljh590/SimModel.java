package ljh590;

import java.util.ArrayList;
import java.util.Observable;

import ljh590.GraphView.Mode;

public class SimModel extends Observable {

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
	
	public double getAverageTChange() {
		return sim.getAverageTChange();
	}
	
	public double getAverageNoReactions() {
		return sim.getAverageNoReactions();
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
	
	public ArrayList<Double> getEnergies() {
		return sim.getEnergies();
	}
	
	public double calculateExpectedActualMSS(double temp) {
		return sim.calculateExpectedActualMSS(temp);
	}

	public double calculateExpectedMSS(double temp) {
		return sim.calculateExpectedMSS(temp);
	}

	public void moveWall(int x) {
		sim.getContainer().moveWall(x);
		if (sim.getContainer().getWidthChange() != 0) {
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
	
	public void changeMode(Mode mode) {
		setChanged();
		switch (mode) {
		case HeatEngines:
			notifyObservers(Changed.HeatEngines);
		case ActivationEnergy:
			notifyObservers(Changed.ActivationEnergy);
			break;
		default:
			System.out.println("DEFAULT MODE???");
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
