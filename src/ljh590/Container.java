package ljh590;

public class Container {

	private int widthChange;
	private int width;
	private double actualWidth;
	private int height;
	private double actualHeight;
	private double pixelSize;
	
	public Container(int width, int height, double pixelSize) {
		this.setWidth(width);
		this.setHeight(height);
	}
	
	public void setPixelSize(double pixelSize) {
		this.pixelSize = pixelSize;
		setActualWidth(width * pixelSize);
		setActualHeight(height * pixelSize);
	}
	
	public void moveWall(int newWidth) {
		widthChange = newWidth - width;
		if (widthChange > 10) {
			widthChange = 10;
		}
		setWidth(newWidth);
	}
	
	/** Positive when moving to the right
	 * @return
	 */
	public int getWidthChange() {
		return widthChange;
	}
	
	public void setWidthChange(int w) {
		widthChange = w;
	}
	
	public int getVolume() {
		return width * height;
	}
	
	public double getActualVolume() {
		return actualWidth * actualHeight;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
		this.actualWidth = width * pixelSize;
	}

	public double getActualWidth() {
		return actualWidth;
	}

	public void setActualWidth(double actualWidth) {
		this.actualWidth = actualWidth;
	}

	public int getHeight() {
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
}
