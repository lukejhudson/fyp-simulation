package ljh590;

public class Container {

	private int widthChange;
	private int width;
	private double actualWidth;
	private int height;
	private double actualHeight;
	
	public Container(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
	}
	
	public void setPixelSize(double pixelSize) {
		this.setActualWidth(width * pixelSize);
		this.setActualHeight(height * pixelSize);
	}
	
	public void moveWall(int newWidth) {
		widthChange = newWidth - width;
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
		actualWidth *= (double) width / ((double) width - (double) widthChange);
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
	}

	public double getActualHeight() {
		return actualHeight;
	}

	public void setActualHeight(double actualHeight) {
		this.actualHeight = actualHeight;
	}
}
