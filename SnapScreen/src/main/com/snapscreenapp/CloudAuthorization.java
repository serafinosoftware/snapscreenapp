/**
 * 
 */
package com.snapscreenapp;

import static java.nio.file.Files.readAllLines;

import java.awt.Image;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author i
 *
 */
public class CloudAuthorization {
	private Image iconImage;

	public CloudAuthorization(Image iconImage) {
		this.iconImage = iconImage;
	}

	public String retrievePassword() throws IOException {
		Path path = Paths.get(System.getProperty("user.home"), ".snapscreen_authorization.txt");
		if (!Files.exists(path)) {
			authorize();
		}

		List<String> deviceId = readAllLines(path);
		return deviceId.get(0);
	}

	private void authorize() throws IOException {
		SupervisorId deviceId = new SupervisorId();
		// TODO: (IMS) What if they cancel?
		deviceId.retrieve(iconImage);
	}
}