package ljh590;

import javax.swing.JFrame;

/**
 * @author Luke Starts the GUI and simulation.
 */
public class SimulationGUI {

	public SimulationGUI() {
		Simulation sim = new Simulation();
		sim.start();
		JFrame frame = new JFrame("Simulation");
		frame.setSize(1500, 880);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ControlPanel pan = new ControlPanel(sim, frame);
		
//		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new SimulationGUI();
	}
}
