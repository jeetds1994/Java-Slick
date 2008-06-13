package org.newdawn.slick.thingle.internal.slick;

import org.newdawn.slick.Image;
import org.newdawn.slick.thingle.spi.ThinletImage;

/**
 * A wrapped round slick images to make them look like thinlet images
 * 
 * @author kevin
 */
public class ImageWrapper implements ThinletImage {
	/** The image being wrapped */
	private Image image;
	
	/**
	 * Create a new wrapper
	 * 
	 * @param image The image being wrapped
	 */
	public ImageWrapper(Image image) {
		this.image = image;
	}

	/**
	 * @see org.newdawn.slick.thingle.spi.ThinletImage#getHeight()
	 */
	public int getHeight() {
		return image.getHeight();
	}

	/**
	 * @see org.newdawn.slick.thingle.spi.ThinletImage#getWidth()
	 */
	public int getWidth() {
		return image.getWidth();
	}
	
	/**
	 * Get the slick image being wrapped
	 * 
	 * @return The slick image being wrapped
	 */
	public Image getSlickImage() {
		return image;
	}
}
