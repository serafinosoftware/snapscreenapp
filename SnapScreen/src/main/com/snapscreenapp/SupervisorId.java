/**
 * 
 */
package com.snapscreenapp;

import java.awt.Image;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * @author i
 *
 */
public class SupervisorId {
	private String deviceId;

	public void retrieve(Image iconImage) {
		ImageIcon icon = new ImageIcon(iconImage);

		deviceId = (String) JOptionPane.showInputDialog(null,
				"Please get the Supervisor ID from the person who will be your supervisor, and enter it below to connect your device:",
				"Authorize Snap Screen", JOptionPane.QUESTION_MESSAGE, icon, null, "");

		if (deviceId == null)
			return;

		String id = deviceId.toString();

		System.out.println("Authorization complete.");
		System.out.println("- Device ID: " + id);

		try {
			Files.write(Paths.get(System.getProperty("user.home"), ".snapscreen_authorization.txt"), id.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** @return a string representation of the ID */
	@Override
	public String toString() {
		return deviceId;
	}
}