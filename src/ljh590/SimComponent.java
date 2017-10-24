package ljh590;

import javax.swing.JComponent;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class SimComponent extends JComponent {

	private Model model;
	private JFrame frame;

	public SimComponent(Model m, JFrame frame) {
		this.model = m;
		this.frame = frame;
	}
	
	
}
