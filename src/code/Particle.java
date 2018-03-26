package code;

/**
 * Holds particle information and helper functions.
 * 
 * @author Luke
 */
public class Particle {

	private Vector pos;
	private Vector vel;
	public static int radius = 10;
	// Mass of hydrogen atom
	public static double mass = 1.67E-27;

	public Particle() {
		this.pos = new Vector(-1, -1);
		this.vel = new Vector(0, 0);
	}

	public Particle(Vector pos, Vector vel) {
		this.pos = new Vector(pos);
		this.vel = new Vector(vel);
	}

	/**
	 * "Moves" the particle. Adds its current velocity to its current position.
	 */
	public void move() {
		this.pos.add(vel);
	}

	/**
	 * @return The x position of the particle
	 */
	public double getX() {
		return this.pos.getX();
	}

	/**
	 * @return The y position of the particle
	 */
	public double getY() {
		return this.pos.getY();
	}

	/**
	 * @return The Vector of the particle's position
	 */
	public Vector getPos() {
		return pos;
	}

	/**
	 * @param x
	 *            The new x position of the particle
	 */
	public void setX(double x) {
		this.pos.setX(x);
	}

	/**
	 * @param y
	 *            The new y position of the particle
	 */
	public void setY(double y) {
		this.pos.setY(y);
	}

	/**
	 * @param x
	 *            The new x position of the particle
	 * @param y
	 *            The new y position of the particle
	 */
	public void setPos(double x, double y) {
		this.pos = new Vector(x, y);
	}

	/**
	 * @return The particle's velocity Vector
	 */
	public Vector getVel() {
		return vel;
	}

	/**
	 * @param vx
	 *            The new x velocity of the particle
	 * @param vy
	 *            The new y velocity of the particle
	 */
	public void setVel(double vx, double vy) {
		this.vel = new Vector(vx, vy);
	}

	/**
	 * @param vel
	 *            The particle's new velocity Vector
	 */
	public void setVel(Vector vel) {
		this.vel.setX(vel.getX());
		this.vel.setY(vel.getY());
	}

	/**
	 * @param vx
	 *            The new x velocity
	 */
	public void setVelX(double vx) {
		this.vel.setX(vx);
	}

	/**
	 * @param vy
	 *            The new y velocity
	 */
	public void setVelY(double vy) {
		this.vel.setY(vy);
	}

	/**
	 * @return The current x velocity
	 */
	public double getVelX() {
		return this.vel.getX();
	}

	/**
	 * @return The current y velocity
	 */
	public double getVelY() {
		return this.vel.getY();
	}

	/*
	 * A string formatting of a particle.
	 */
	public String toString() {
		return "<P:" + pos + " V:" + vel + ">";
	}

	/**
	 * Creates a copy of this particle which, when changed, will not have any
	 * effect on the particle it was copied from or vice versa.
	 * 
	 * @return A copy of this particle
	 */
	public Particle createCopy() {
		return new Particle(pos, vel);
	}
}
