package ljh590;

/**
 * @author Luke Holds particle information and helper functions.
 */
public class Particle {

	private Vector pos;
	private Vector vel;
	private int radius;
	// Mass of hydrogen atom
	private double mass = 1.67E-27;

	public Particle(int x, int y, int vx, int vy) {
		this.pos = new Vector(x, y);
		this.vel = new Vector(vx, vy);
	}

	public Particle(int x, int y) {
		this.pos = new Vector(x,y);
		this.vel = new Vector(-1, -1);
	}
	
	public Particle(int r) {
		this.radius = r;
		this.pos = new Vector(-1, -1);
		this.vel = new Vector(0, 0);
	}

	public Particle() {
		this.pos = new Vector(-1, -1);
		this.vel = new Vector(0, 0);
	}

	public Particle(Vector pos, Vector vel) {
		this.pos = new Vector(pos);
		this.vel = new Vector(vel);
	}
	
	public Particle(Vector pos, Vector vel, int r) {
		this.pos = new Vector(pos);
		this.vel = new Vector(vel);
		this.radius = r;
	}

	public void move() {
		this.pos.add(vel);
	}
	
	public void set(Vector pos, Vector vel) {
		this.pos = pos;
		this.vel = vel;
	}
	
	public double getX() {
		return this.pos.getX();
	}
	
	public double getY() {
		return this.pos.getY();
	}

	public Vector getPos() {
		return pos;
	}
	
	public void setX(double x) {
		this.pos.setX(x);
	}
	
	public void setY(double y) {
		this.pos.setY(y);
	}

	public void setPos(double x, double y) {
		this.pos = new Vector(x, y);
	}

	public void setPos(Vector pos) {
		this.pos = pos;
	}

	public Vector getVel() {
		return vel;
	}

	public void setVel(double vx, double vy) {
		this.vel = new Vector(vx, vy);
	}

	public void setVel(Vector vel) {
		this.vel.setX(vel.getX());
		this.vel.setY(vel.getY());
	}
	
	public void setVelX(double vx) {
		this.vel.setX(vx);
	}
	
	public void setVelY(double vy) {
		this.vel.setY(vy);
	}

	public double getVelX() {
		return this.vel.getX();
	}
	
	public double getVelY() {
		return this.vel.getY();
	}
	
	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(int mass) {
		this.mass = mass;
	}
	
	public String toString() {
		return "<P:(" + pos.getX() + ", " + pos.getY() + ") V:(" + vel.getX() + ", " + vel.getY() +")>";
	}

	public Particle createCopy() {
		return new Particle(pos, vel, radius);
	}
}
