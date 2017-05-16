/**
 * 
 */
package com.snapscreenapp;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author i
 *
 */
public class NotificationIcon {
	private String deviceId;
	private Image iconImage;
	private TrayIcon trayIcon;

	public NotificationIcon(Image iconImage, String deviceId) {
		this.iconImage = iconImage;
		this.deviceId = deviceId;
	}

	public void remove() {
		SystemTray tray = SystemTray.getSystemTray();

		tray.remove(trayIcon);
	}

	public void place() {
		if (!SystemTray.isSupported()) {
			System.err.println("Must run on a system that supports System Tray.");
			System.exit(1);
		}

		SystemTray tray = SystemTray.getSystemTray();

		PopupMenu menu = new PopupMenu();

		MenuItem deviceIdItem = new MenuItem("Show Device Code");
		deviceIdItem.addActionListener(new ActionListener() {
			private DeviceId id = new DeviceId(deviceId);

			@Override
			public void actionPerformed(ActionEvent e) {
				id.show(iconImage);
			}
		});

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				remove();
				System.exit(0);
			}
		});

		menu.add(deviceIdItem);
		menu.add(exitItem);

		trayIcon = new TrayIcon(iconImage, "Snap Screen", menu);
		trayIcon.setImageAutoSize(true);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.err.println("Error adding System Tray icon.");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
