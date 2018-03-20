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
	private ControlPanel controlPanel;
	private JFrame frame;
	private int fps = 60;
	private CopyOnWriteArrayList<Particle> particles;
	// Width of container (from buffer)
	private double contWidth;
	private Container cont;
	private int r;

	private boolean draggingWall = false;
	private int mouseX = 0;

	// Are we automatically moving the wall inwards?
	private boolean autoMoveWallIn = false;
	// Are we automatically moving the wall outwards?
	private boolean autoMoveWallOut = false;

	private boolean colourParticlesAtActEnergy;


	public SimComponent(SimModel m, JFrame frame, JLabel currT, JLabel currP, ControlPanel controlPanel) {
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
						if (b != null) {
							particles = b.getParticles();
							contWidth = b.getContWidth();
							r = Particle.radius;
							if (draggingWall) {
								int max = cont.getMaxWidth();
								int min = cont.getMinWidth();
								if (mouseX > max) {
									mouseX = max;
								} else if (mouseX < min) {
									mouseX = min;
								}
								model.moveWall(mouseX);
							}
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
					double tValue = model.getAverageT();
					if (Double.isNaN(tValue)) {
						tValue = 0;
					}
					double pValue = model.getAverageP();
					if (Double.isNaN(pValue)) {
						pValue = 0;
					}
					String T = String.format("<html>Average temperature<br>of particles: %6.0f K</html>", tValue);
					currT.setText(T);
					String P = String.format("<html>Average pressure<br>on container: %6.2f Pa</html>", pValue);
					currP.setText(P);
				}
			}
		});

		Thread timer = new Thread(new Runnable() {
			public void run() {
				while (true) {
					frame.repaint();
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
				if (!controlPanel.isPaused() && width - e.getX() < 10 && width - e.getX() > -20) {// Math.abs(width
																									// -
																									// e.getX())
																									// <
																									// 10)
																									// {
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
				if (width - e.getX() < 10 && width - e.getX() > -20) {// Math.abs(width
																		// -
																		// e.getX())
																		// < 10)
																		// {
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
		int contHeight = (int) cont.getHeight();
		// Draw container
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, (int) contWidth, contHeight);
		// Draw "handle" of container
		g.setColor(Color.DARK_GRAY);
		g.fillRect((int) contWidth, (contHeight / 2) - 5, 10, 10);
		g.fillRect((int) contWidth + 10, (contHeight / 2) - 75, 7, 150);

		// System.out.println(particles);
		// double tot = 0;
		double energy;
		for (Particle p : particles) {
			energy = Math.pow(p.getVel().normalise(), 2);
			if (colourParticlesAtActEnergy && energy > model.getActivationEnergy()) {
				g.setColor(Color.RED);
			} else {
				g.setColor(Color.CYAN);
			}
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
		return fps;
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
					model.getContainer().setWidthChange(0);
					return;
				}
				button.setText("Stop Movement");

				autoMoveWallIn = true;
				model.setBufferMaxSize(1);
				model.rollbackBuffer();
				while (model.getContainer().getWidth() > min && autoMoveWallIn) {
					model.moveWall(model.getContainer().getWidth() - d);
					try {
						double sleep = 20.0 / (fps / 60.0);
						Thread.sleep((long) sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (autoMoveWallIn) {
					model.moveWall(min);
					model.setBufferMaxSize(10);
					autoMoveWallIn = false;
				}
				button.setText("Move Wall In");
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
					model.getContainer().setWidthChange(0);
					return;
				}
				button.setText("Stop Movement");

				autoMoveWallOut = true;
				model.setBufferMaxSize(1);
				model.rollbackBuffer();
				while (model.getContainer().getWidth() < max && autoMoveWallOut) {
					model.moveWall(model.getContainer().getWidth() + d);
					try {
						double sleep = 20.0 / (fps / 60.0);
						Thread.sleep((long) sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (autoMoveWallOut) {
					model.moveWall(max);
					model.setBufferMaxSize(10);
					autoMoveWallOut = false;
				}
				button.setText("Move Wall Out");
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
