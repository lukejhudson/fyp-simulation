package code;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class SimComponent extends JComponent {

	private SimModel model;
	private JFrame frame;
	private int fps = 60;
	private CopyOnWriteArrayList<Particle> particles;
	// Width of container (from buffer)
	private double contWidth;
	private Container cont;
	private int r;
	private boolean refresh = true;
	private boolean clear = false;

	private boolean draggingWall = false;
	private int mouseX = 0;

	// Are we automatically moving the wall inwards?
	private boolean autoMoveWallIn = false;
	// Are we automatically moving the wall outwards?
	private boolean autoMoveWallOut = false;
	
	private boolean colourParticlesAtActEnergy;

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
						SimBuffer b = model.getBuffer();
						particles = b.getParticles();
						contWidth = b.getContWidth();
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
				double width = cont.getWidth();
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
				double width = cont.getWidth();
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
					int max = cont.getMaxWidth();
					int min = cont.getMinWidth();
					if (x > max) {
						mouseX = max;
					} else if (x < min) {
						mouseX = min;
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
		g.drawRect(0, 0, (int) contWidth, cont.getHeight());

		// System.out.println(particles);
		// double tot = 0;
		double energy;
		for (Particle p : particles) {
			if (p.isActive()) {
				energy = Math.pow(p.getVel().normalise(), 2);
				if (colourParticlesAtActEnergy && energy > model.getActivationEnergy()) {
					g.setColor(Color.RED);
				} else {
					g.setColor(Color.CYAN);
				}
				g.fillOval((int) p.getX() - r, (int) p.getY() - r, 2 * r, 2 * r);
				// tot += p.getVel().normalise();
			}
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

	public void moveWallInAuto(int d, JButton button) {
		int min = cont.getMinWidth();
		Thread t = new Thread(new Runnable() {
			public void run() {
				if (autoMoveWallOut) {
					autoMoveWallOut = false;
				}
				if (autoMoveWallIn) {
					autoMoveWallIn = false;
					return;
				}
				button.setText("Stop movement");
				
				autoMoveWallIn = true;
				model.setBufferMaxSize(1);
				model.rollbackBuffer();
				while (model.getContainer().getWidth() > min && autoMoveWallIn) {
					model.moveWall(model.getContainer().getWidth() - d);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (autoMoveWallIn) {
					model.moveWall(min);
					model.setBufferMaxSize(10);
					autoMoveWallIn = false;
				}
				button.setText("Move wall in");
			}
		});
		t.start();
	}

	public void moveWallOutAuto(int d, JButton button) {
		int max = cont.getMaxWidth();
		Thread t = new Thread(new Runnable() {
			public void run() {
				if (autoMoveWallIn) {
					autoMoveWallIn = false;
				}
				if (autoMoveWallOut) {
					autoMoveWallOut = false;
					return;
				}
				button.setText("Stop movement");
				
				autoMoveWallOut = true;
				model.setBufferMaxSize(1);
				model.rollbackBuffer();
				while (model.getContainer().getWidth() < max && autoMoveWallOut) {
					model.moveWall(model.getContainer().getWidth() + d);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (autoMoveWallOut) {
					model.moveWall(max);
					model.setBufferMaxSize(10);
					autoMoveWallOut = false;
				}
				button.setText("Move wall out");
			}
		});
		t.start();
	}

	public void stopWalls() {
		autoMoveWallIn = false;
		autoMoveWallOut = false;
	}
	
	public void setColouringParticles(boolean b) {
		colourParticlesAtActEnergy = b;
	}
}
