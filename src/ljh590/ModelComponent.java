package ljh590;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ModelComponent extends JPanel {

	private Model model;
	private JFrame frame;

	public ModelComponent(Model m, JFrame frame) {
		super();
		this.model = m;
		this.frame = frame;

		System.out.println("Starting GUI");
		// setLayout(new BorderLayout());
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (!model.bufferEmpty()) {
						frame.repaint();
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, model.getWallX(), model.getWallY());
		g.setColor(Color.CYAN);

		ArrayList<Particle> particles = model.getBuffer();
		// System.out.println(particles);
		int r = particles.get(0).getRadius();
		for (Particle p : particles) {
			g.fillOval((int) p.getX() - r, (int) p.getY() - r, 2 * r, 2 * r);
		}
	}
}
