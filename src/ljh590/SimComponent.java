package ljh590;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class SimComponent extends JComponent {

	private SimModel model;
	private JFrame frame;
	private int fps = 60;
	private CopyOnWriteArrayList<Particle> particles;
	private Container cont;
	private int r;
	private boolean refresh = true;
	private boolean clear = false;

	private boolean draggingWall = false;
	private int mouseX = 0;

	public SimComponent(SimModel m, JFrame frame, JLabel currT, JLabel currP) {
		super();
		this.model = m;
		this.frame = frame;
		this.cont = model.getContainer();

		System.out.println("Starting GUI");

		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (!model.bufferEmpty()) {
						particles = model.getBuffer();
						r = particles.get(0).getRadius();
						if (refresh) {
							frame.repaint();
							refresh = false;
						}
						if (draggingWall) {
							model.moveWall(mouseX);
						}
					}
					try {
						if (fps != 0) {
							Thread.sleep(1000 / fps);
						}
						while (fps == 0) {
							Thread.sleep(100);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String T = String.format("<html>Average temperature<br>of particles: %6.0f K</html>",
							model.getAverageT());
					currT.setText(T);
					String P = String.format("<html>Average pressure<br>on container: %6.2f Pa</html>",
							model.getAverageP());
					currP.setText(P);
				}
			}
		});

		Thread timer = new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (!refresh) {
						refresh = true;
					}
					try {
						Thread.sleep(1000 / 60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		timer.start();

		t.start();

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				System.out.println("PRESSED: " + e.getPoint());
				mouseX = e.getX();
				int width = cont.getWidth();
				if (Math.abs(width - e.getX()) < 10) {
					draggingWall = true;
					model.setBufferMaxSize(1);
					model.rollbackBuffer();
				}
			}

			public void mouseReleased(MouseEvent e) {
				System.out.println("RELEASED: " + e.getPoint());
				draggingWall = false;
				cont.setWidthChange(0);
				model.setBufferMaxSize(10);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			// Whenever the mouse is moved
			public void mouseMoved(MouseEvent e) {
				// System.out.println("MOVED: " + e.getPoint());
				int width = cont.getWidth();
				if (Math.abs(width - e.getX()) < 10) {
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				} else if (!draggingWall) {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}

			// WHenever the mouse is moved while mouse is pressed
			public void mouseDragged(MouseEvent e) {
				// System.out.println("DRAGGED: " + e.getPoint());
				int x = e.getX();
				if (draggingWall) {
					if (x > 1200) {
						mouseX = 1200;
					} else if (x < 500) {
						mouseX = 500;
					} else {
						mouseX = e.getX();
					}
				}
			}
		});
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, cont.getWidth(), cont.getHeight());
		g.setColor(Color.CYAN);

		// System.out.println(particles);
		// double tot = 0;
		for (Particle p : particles) {
			g.fillOval((int) p.getX() - r, (int) p.getY() - r, 2 * r, 2 * r);
			// tot += p.getVel().normalise();
		}
		// tot /= particles.size();
		// System.out.println("Average speed: " + tot);
	}

	public void setFps(int fps) {
		this.fps = fps;
	}

	public int getFps() {
		return this.fps;
	}
}
