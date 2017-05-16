/**
 * 
 */
package com.snapscreenapp;

import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * @author i
 *
 */
public class DeviceId {
	private String deviceId;

	public DeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void show(Image iconImage) {
		ImageIcon icon = new ImageIcon(iconImage);

		JOptionPane.showInputDialog(null,
				"Send the Device Code below to the person who will be your supervisor, to connect your device:",
				"Authorize Snap Screen", JOptionPane.INFORMATION_MESSAGE, icon, null, deviceId);
	}
}