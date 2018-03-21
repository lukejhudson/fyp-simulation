package code;

import javax.swing.JFrame;

/**
 * Starts the GUI and simulation.
 * 
 * @author Luke
 */
public class SimulationGUI {

	public SimulationGUI() {
		Simulation sim = new Simulation();
		sim.start();
		JFrame frame = new JFrame("Simulation");
		frame.setSize(1565, 925);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		new ControlPanel(sim, frame);

		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new SimulationGUI();
	}
}
