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
		frame.setSize(1220, 820);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SimComponent sim = new SimComponent(m, frame);
		
//		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Simulation();
	}
}
