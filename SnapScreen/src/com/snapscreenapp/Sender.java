/**
 * 
 */
package com.snapscreenapp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.imageio.ImageIO;

/**
 * @author i
 *
 */
public class Sender {
	private String password;
	private NotificationIcon notificationIcon;

	public Sender(String password, NotificationIcon notificationIcon) {
		this.password = password;
		this.notificationIcon = notificationIcon;
	}

	public void sendScreenshotMessage(BufferedImage screenshot, long interval) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(screenshot, "jpeg", os);
		InputStream is = new ByteArrayInputStream(os.toByteArray());

//		if (dropbox == null) {
//			DbxRequestConfig requestConfig = new DbxRequestConfig("SnapScreen/0.1");
//			dropbox = new DbxClientV2(requestConfig, password);
//		}
//
//		uploadFile(dropbox, is, "/next_" + interval + ".jpg");
	}
}
