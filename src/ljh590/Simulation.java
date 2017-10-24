package ljh590;

import javax.swing.JFrame;

/**
 * @author Luke Starts the GUI and simulation.
 */
public class Simulation {

	public Simulation() {
		Model m = new Model();
		m.start();
		JFrame frame = new JFrame("Simulation");
		frame.setSize(1220, 850);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SimComponent sim = new SimComponent(m, frame);
		ModelComponent mod = new ModelComponent(m, frame);
		
		frame.add(sim);
		frame.add(mod);
		
//		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Simulation();
	}
}
