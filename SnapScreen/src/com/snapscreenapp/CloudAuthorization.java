/**
 * 
 */
package com.snapscreenapp;

import static java.nio.file.Files.readAllLines;
import static java.util.UUID.randomUUID;

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
		Path path = Paths.get("authorization.txt");
		if (!Files.exists(path)) {
			authorize();
		}

		List<String> deviceId = readAllLines(path);
		return deviceId.get(0);
	}

	private void authorize() throws IOException {
		String deviceId = randomUUID().toString();

		System.out.println("Authorization complete.");
		System.out.println("- Device ID: " + deviceId);

		Files.write(Paths.get("authorization.txt"), deviceId.getBytes());

		DeviceId id = new DeviceId(deviceId);
		id.show(iconImage);
	}
}