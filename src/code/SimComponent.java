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

/**
 * Paints the particles and the container they are in onto the screen. Also
 * handles how the user moves the right wall.
 * 
 * @author Luke
 *
 */
@SuppressWarnings("serial")
public class SimComponent extends JComponent {

	private SimModel model;
	private int fps = 60;
	private CopyOnWriteArrayList<Particle> particles;
	// Width of container (from buffer)
	private double contWidth;
	private Container cont;
	// Radius of the particles
	private int r = Particle.radius;

	// Is the wall being moved by the user?
	private boolean draggingWall = false;
	// Current x-position of the user's mouse while they are moving the wall
	private int mouseX = 0;

	// Are we automatically moving the wall inwards?
	private boolean autoMoveWallIn = false;
	// Are we automatically moving the wall outwards?
	private boolean autoMoveWallOut = false;

	private boolean colourParticlesAtActEnergy;

	public SimComponent(SimModel m, JFrame frame, JLabel currT, JLabel currP, ControlPanel controlPanel) {
		super();
		this.model = m;
		this.cont = model.getContainer();

		System.out.println("Starting GUI");

		// New thread which regularly updates important variables used in this
		// class. Also handles movement of the right wall and the temperature
		// and pressure labels.
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (!model.bufferEmpty()) {
						SimBuffer b = model.getBuffer();
						if (b != null) {
							// Update our knowledge of the particles and the
							// container
							particles = b.getParticles();
							contWidth = b.getContWidth();
							// Move the container according to user input
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
						// Control the refresh rate of the particles and wall
						// based on the simulation speed (60 frames per second
						// at 1x speed)
						if (fps != 0) {
							Thread.sleep(1000 / fps);
						}
						while (fps == 0) {
							Thread.sleep(100);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// Update the temperature and pressure labels
					double tValue = model.getAverageT();
					if (Double.isNaN(tValue)) {
						tValue = 0;
					}
					double pValue = model.getAverageP();
					if (Double.isNaN(pValue)) {
						pValue = 0;
					}
					String T = String.format("<html>Average temperature<br>of particles: <b>%6.0f K</b></html>",
							tValue);
					currT.setText(T);
					String P = String.format("<html>Average pressure<br>on container: <b>%6.2f Pa</b></html>", pValue);
					currP.setText(P);
				}
			}
		});

		// New thread to repaint the container 60 times per second
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

		// Mouse listener which listens to click and release events from the
		// mouse. Used to move the right wall.
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				mouseX = e.getX();
				double width = cont.getWidth();
				if (!controlPanel.isPaused() && width - e.getX() < 10 && width - e.getX() > -20) {
					draggingWall = true;
					model.setBufferMaxSize(1);
					model.rollbackBuffer();
				}
			}

			public void mouseReleased(MouseEvent e) {
				draggingWall = false;
				cont.setWidthChange(0);
				model.setBufferMaxSize(10);
			}
		});
		// Mouse listener which listens to mouse movement events. Used to move
		// the right wall.
		addMouseMotionListener(new MouseMotionAdapter() {
			// Whenever the mouse is moved
			public void mouseMoved(MouseEvent e) {
				double width = cont.getWidth();
				if (width - e.getX() < 10 && width - e.getX() > -20) {
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				} else if (!draggingWall) {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}

			// WHenever the mouse is moved while mouse is pressed
			public void mouseDragged(MouseEvent e) {
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

		// Draw particles themselves
		double energy;
		for (Particle p : particles) {
			energy = Math.pow(p.getVel().normalise(), 2);
			// Colour particles red if they are above the activation energy,
			// cyan otherwise
			if (colourParticlesAtActEnergy && energy > model.getActivationEnergy()) {
				g.setColor(Color.RED);
			} else {
				g.setColor(Color.CYAN);
			}
			g.fillOval((int) p.getX() - r, (int) p.getY() - r, 2 * r, 2 * r);
		}
	}

	/**
	 * @param fps
	 *            The new FPS
	 */
	public void setFps(int fps) {
		this.fps = fps;
	}

	/**
	 * @return The current FPS
	 */
	public int getFps() {
		return fps;
	}

	/**
	 * Automatically moves the right wall inwards until the width of the
	 * container is equal to the minimum width.
	 * 
	 * @param d
	 *            The distance to move the wall during each step
	 * @param button
	 *            The button used to activate this function so that its text can
	 *            be updated
	 */
	public void moveWallInAuto(int d, JButton button) {
		int min = cont.getMinWidth();
		Thread t = new Thread(new Runnable() {
			public void run() {
				// Stop moving the wall out
				if (autoMoveWallOut) {
					autoMoveWallOut = false;
				}
				// If we are already moving the wall, pressing the button again
				// should stop the movement
				if (autoMoveWallIn) {
					autoMoveWallIn = false;
					model.getContainer().setWidthChange(0);
					return;
				}
				button.setText("Stop Movement");

				autoMoveWallIn = true;
				// Set the buffer size to 1 to ensure that the position of the
				// wall is updated as soon as the wall is moved
				model.setBufferMaxSize(1);
				model.rollbackBuffer();
				// Move the wall inwards until the container is the smallest it
				// can be
				while (model.getContainer().getWidth() > min && autoMoveWallIn) {
					model.moveWall(model.getContainer().getWidth() - d);
					try {
						// Adjust the rate at which wall moves based on the
						// speed of the simulation
						double sleep = 20.0 / ((double) fps / 60.0);
						Thread.sleep((long) sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// Finish up moving the wall
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

	/**
	 * Automatically moves the right wall outwards until the width of the
	 * container is equal to the maximum width.
	 * 
	 * @param d
	 *            The distance to move the wall during each step
	 * @param button
	 *            The button used to activate this function so that its text can
	 *            be updated
	 */
	public void moveWallOutAuto(int d, JButton button) {
		int max = cont.getMaxWidth();
		Thread t = new Thread(new Runnable() {
			public void run() {
				// Stop moving the wall in
				if (autoMoveWallIn) {
					autoMoveWallIn = false;
				}
				// If we are already moving the wall, pressing the button again
				// should stop the movement
				if (autoMoveWallOut) {
					autoMoveWallOut = false;
					model.getContainer().setWidthChange(0);
					return;
				}
				button.setText("Stop Movement");

				autoMoveWallOut = true;
				// Set the buffer size to 1 to ensure that the position of the
				// wall is updated as soon as the wall is moved
				model.setBufferMaxSize(1);
				model.rollbackBuffer();
				// Move the wall outwards until the container is the largest it
				// can be
				while (model.getContainer().getWidth() < max && autoMoveWallOut) {
					model.moveWall(model.getContainer().getWidth() + d);
					try {
						// Adjust the rate at which wall moves based on the
						// speed of the simulation
						double sleep = 20.0 / ((double) fps / 60.0);
						Thread.sleep((long) sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// Finish up moving the wall
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

	/**
	 * Stops the right wall from moving.
	 */
	public void stopWalls() {
		autoMoveWallIn = false;
		autoMoveWallOut = false;
	}

	/**
	 * @param b
	 *            Should the particles be coloured red when they reach the
	 *            activation energy?
	 */
	public void setColouringParticles(boolean b) {
		colourParticlesAtActEnergy = b;
	}
}
