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

	public void setPixelSize(double pixelSize) {
		this.pixelSize = pixelSize;
		setActualWidth(width * pixelSize);
		setActualHeight(height * pixelSize);
	}

	public void moveWall(double newWidth) {
		widthChange = newWidth - width;
		setWidth(newWidth);
	}

	/**
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
	 * Positive when moving to the right
	 * 
	 * @return
	 */
	public double getWidthChange() {
		return widthChange;
	}

	public void setWidthChange(double w) {
		widthChange = w;
	}

	public double getVolume() {
		return width * height;
	}

	public double getActualVolume() {
		return actualWidth * actualHeight;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
		this.actualWidth = width * pixelSize;
	}

	public double getActualWidth() {
		return actualWidth;
	}

	public void setActualWidth(double actualWidth) {
		this.actualWidth = actualWidth;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
		this.actualHeight = height * pixelSize;
	}

	public double getActualHeight() {
		return actualHeight;
	}

	public void setActualHeight(double actualHeight) {
		this.actualHeight = actualHeight;
	}

	public void setMaxWidth(int newWidth) {
		this.maxWidth = newWidth;
	}

	public int getMaxWidth() {
		return this.maxWidth;
	}

	public void setMinWidth(int newWidth) {
		this.minWidth = newWidth;
	}

	public int getMinWidth() {
		return this.minWidth;
	}
}
