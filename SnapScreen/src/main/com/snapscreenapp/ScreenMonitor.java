/**
 * 
 */
package com.snapscreenapp;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * @author i
 *
 */
public class ScreenMonitor {
	private Robot robot;
	private Toolkit toolkit;

	public ScreenMonitor() {
		toolkit = Toolkit.getDefaultToolkit();
	}

	public BufferedImage captureScreenshot() throws AWTException {
		if (robot == null) {
			robot = new Robot();
		}

		// Screen size may change between screenshots
		Dimension screenSize = toolkit.getScreenSize();
		Rectangle rectangle = new java.awt.Rectangle(screenSize);

		// Capture the whole screen
		return robot.createScreenCapture(rectangle);
	}
}
