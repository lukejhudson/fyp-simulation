package code;

/**
 * Holds vector information and helper functions.
 * 
 * @author Luke
 */
public class Vector {

	private double x;
	private double y;

	/**
	 * Creates a new Vector with no specified x or y values.
	 */
	public Vector() {
		this.x = -1;
		this.y = -1;
	}

	/**
	 * Creates a new Vector with the specified x and y values (doubles)
	 * 
	 * @param x
	 *            The Vector's x value
	 * @param y
	 *            The Vector's y value
	 */
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a new Vector with the specified x and y values (ints)
	 * 
	 * @param x
	 *            The Vector's x value
	 * @param y
	 *            The Vector's y value
	 */
	public Vector(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a new Vector with x and y values equal to those in the given
	 * Vector v.
	 * 
	 * @param v
	 *            The Vector to extract x and y values from
	 */
	public Vector(Vector v) {
		this.x = v.getX();
		this.y = v.getY();
	}

	/**
	 * Calculates the dot product of this Vector and the Vector v.
	 * 
	 * @param v
	 *            The Vector to calculate the dot product with
	 * @return The dot product of this Vector and the Vector v
	 */
	public double dot(Vector v) {
		return (this.x * v.getX()) + (this.y * v.getY());
	}

	/**
	 * Scale the vector by scale factor s.
	 * 
	 * @param s
	 *            The scale factor to scale this Vector by
	 */
	public Vector scale(double s) {
		this.x *= s;
		this.y *= s;
		return this;
	}

	/**
	 * Find the squared distance between this Vector and another given Vector v.
	 * 
	 * @param v
	 *            The Vector to calculate the squared distance between
	 * @return The squared distance between this Vector and the Vector v
	 */
	public double sqrDist(Vector v) {
		return Math.pow((this.x - v.getX()), 2) + Math.pow((this.y - v.getY()), 2);
	}

	/**
	 * Calculates the scalar distance represented by this Vector. This function
	 * is costly due to its use of square root.
	 * 
	 * @return The scalar distance represented by this Vector
	 */
	public double normalise() {
		return Math.hypot(x, y);
	}

	/**
	 * Calculates the scalar distance represented by this Vector. This function
	 * is much more efficient than normalise().
	 * 
	 * @return The squared scalar distance represented by this Vector
	 */
	public double sqrNorm() {
		return x * x + y * y;
	}

	/**
	 * Transforms this Vector into its unit vector.
	 */
	public void unitVector() {
		double d = normalise();
		if (d != 0) {
			this.x /= d;
			this.y /= d;
		}
	}

	/**
	 * Adds this Vector to the given Vector v.
	 * 
	 * @param v
	 *            The Vector to add to this Vector
	 * @return This Vector, which holds the result of the calculation in its x
	 *         and y values
	 */
	public Vector add(Vector v) {
		this.x += v.getX();
		this.y += v.getY();
		return this;
	}

	/**
	 * Subtracts the given Vector v from this Vector.
	 * 
	 * @param v
	 *            The Vector to subtract from this Vector
	 * @return This Vector, which holds the result of the calculation in its x
	 *         and y values
	 */
	public Vector sub(Vector v) {
		this.x -= v.getX();
		this.y -= v.getY();
		return this;
	}

	/**
	 * Sets this Vector'x x and y values equal to those of the given Vector v.
	 * Seems to copy the vector rather than copying its values.
	 * 
	 * @param v
	 *            The Vector whose x and y values will be copied
	 */
	public void set(Vector v) {
		this.x = v.getX();
		this.y = v.getY();
	}

	/**
	 * @param x
	 *            The new x value
	 * @param y
	 *            The new y value
	 */
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * @return The current x value
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x
	 *            The new x value
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return The current y value
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y
	 *            The new y value
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Checks whether this Vector and the given Vector v are equal.
	 * 
	 * @param v
	 *            The Vector to check against
	 * @return True if the two Vectors are equal
	 */
	public boolean equals(Vector v) {
		return x == v.x && y == v.y;
	}

	/* 
	 * A string representation of this Vector.
	 */
	public String toString() {
		return "(" + (Math.round(x * 100.0) / 100.0) + ", " + (Math.round(y * 100.0) / 100.0) + ")";
	}
}
