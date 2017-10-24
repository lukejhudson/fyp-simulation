package ljh590;

/**
 * @author Luke Holds vector information and helper functions.
 */
public class Vector {

	private double x;
	private double y;

	public Vector() {
		this.x = -1;
		this.y = -1;
	}

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vector(Vector v) {
		this.x = v.getX();
		this.y = v.getY();
	}

	public double dot(Vector v) {
		return (this.x * v.getX()) + (this.y * v.getY());
	}

	/**
	 * Scale the vector by scale factor s.
	 * 
	 * @param s
	 */
	public Vector scale(double s) {
		this.x *= s;
		this.y *= s;
		return this;
	}

	/**
	 * Find the squared distance between this vector and another given vector.
	 * 
	 * @param v
	 * @return
	 */
	public double sqrDist(Vector v) {
		return Math.pow((this.x - v.getX()), 2) + Math.pow((this.y - v.getY()), 2);
	}

	public double normalise() {
		return Math.hypot(x, y);
	}

	public void unitVector() {
		double d = normalise();
		if (d != 0) {
			this.x /= d;
			this.y /= d;
		}
	}

	public Vector add(Vector v) {
		this.x += v.getX();
		this.y += v.getY();
		return this;
	}

	public Vector sub(Vector v) {
		this.x -= v.getX();
		this.y -= v.getY();
		return this;
	}

	/**
	 * Seems to copy the vector rather than copying its values
	 * 
	 * @param v
	 */
	public void set(Vector v) {
		this.x = v.getX();
		this.y = v.getY();
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}
}
