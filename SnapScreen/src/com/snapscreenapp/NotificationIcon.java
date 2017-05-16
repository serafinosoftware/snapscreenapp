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
	private Image iconImage;
	private TrayIcon trayIcon;

	public NotificationIcon(Image iconImage) {
		this.iconImage = iconImage;
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

		MenuItem deviceIdItem = new MenuItem("Change Supervisor ID");
		deviceIdItem.addActionListener(new ActionListener() {
			private SupervisorId id = new SupervisorId();

			@Override
			public void actionPerformed(ActionEvent e) {
				id.retrieve(iconImage);
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
