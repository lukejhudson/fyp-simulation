package code;

public class Container {

	private double widthChange;
	private double width;
	private double actualWidth;
	private double height;
	private double actualHeight;
	private double pixelSize;

	private int maxWidth = 1200;
	private int minWidth = 300;

	public Container(int width, int height, double pixelSize) {
		this.setWidth(width);
		this.setHeight(height);
	}

	/**
	 * Sets the pixel size, i.e. how many metres a pixel represents.
	 * 
	 * @param pixelSize
	 *            The number of metres represented by one pixel
	 */
	public void setPixelSize(double pixelSize) {
		this.pixelSize = pixelSize;
		setActualWidth(width * pixelSize);
		setActualHeight(height * pixelSize);
	}

	/**
	 * Sets the width of the container to new Width.
	 * 
	 * @param newWidth
	 *            The new width of the container
	 */
	public void moveWall(double newWidth) {
		widthChange = newWidth - width;
		setWidth(newWidth);
	}

	/**
	 * Changes the width of the container by dist.
	 * 
	 * @param dist
	 *            The distance to move the wall in pixels
	 */
	public void pushWall(double dist) {
		double newWidth = width + dist;
		if (newWidth + dist > maxWidth) {
			newWidth = maxWidth;
			widthChange = 0;
		} else {
			widthChange = dist;
		}
		setWidth(newWidth);
	}

	/**
	 * @return The current widthChange
	 */
	public double getWidthChange() {
		return widthChange;
	}

	/**
	 * @param w
	 *            The new widthChange
	 */
	public void setWidthChange(double w) {
		widthChange = w;
	}

	/**
	 * @return The current volume of the container (in pixels)
	 */
	public double getVolume() {
		return width * height;
	}

	/**
	 * @return The actual volume of the container (in metres)
	 */
	public double getActualVolume() {
		return actualWidth * actualHeight;
	}

	/**
	 * @return The current width of the container
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            The new width of the container
	 */
	public void setWidth(double width) {
		this.width = width;
		this.actualWidth = width * pixelSize;
	}

	/**
	 * @return The actual width of the container (in pixels)
	 */
	public double getActualWidth() {
		return actualWidth;
	}

	/**
	 * @param actualWidth
	 *            The new actual width of the container (in pixels)
	 */
	public void setActualWidth(double actualWidth) {
		this.actualWidth = actualWidth;
	}

	/**
	 * @return The current height of the container
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            The new height of the container
	 */
	public void setHeight(int height) {
		this.height = height;
		this.actualHeight = height * pixelSize;
	}

	/**
	 * @return The current actual height of the container (in metres)
	 */
	public double getActualHeight() {
		return actualHeight;
	}

	/**
	 * @param actualHeight
	 *            The new actual height of the container (in metres)
	 */
	public void setActualHeight(double actualHeight) {
		this.actualHeight = actualHeight;
	}

	/**
	 * @param newWidth
	 *            The new maximum width of the container
	 */
	public void setMaxWidth(int newWidth) {
		this.maxWidth = newWidth;
	}

	/**
	 * @return The current maximum width of the container
	 */
	public int getMaxWidth() {
		return this.maxWidth;
	}

	/**
	 * @param newWidth
	 *            The new minimum width of the container
	 */
	public void setMinWidth(int newWidth) {
		this.minWidth = newWidth;
	}

	/**
	 * @return The current minimum width of the container
	 */
	public int getMinWidth() {
		return this.minWidth;
	}
}
