package com.snapscreenapp;
/**
 * Snap Screen
 * Runs in the background capturing screenshots and random intervals and sends them to predetermined recipient
 Copyright (C) 2005 Isaac Serafino <isaac.serafino@serafinosoftware.com>

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.awt.AWTException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.imageio.ImageIO;

public class SnapScreen {
	public static void main(String[] args) throws IOException {
		icon = retrieveIcon();
		CloudAuthorization authorization = new CloudAuthorization(icon);
		String password = authorization.retrievePassword();

		NotificationIcon notificationIcon = new NotificationIcon(icon, password);
		Sender sender = new Sender(password, notificationIcon);

		ScreenMonitor screenMonitor = new ScreenMonitor();
		SnapScreen snapScreen = new SnapScreen(sender, notificationIcon, screenMonitor);
		snapScreen.start();
	}

	private Sender sender;
	private NotificationIcon notificationIcon;
	private Random random;
	private ScreenMonitor screenMonitor;
	private static Image icon;

	public SnapScreen(Sender sender, NotificationIcon notificationIcon, ScreenMonitor screenMonitor) {
		super();
		this.sender = sender;
		this.notificationIcon = notificationIcon;
		this.screenMonitor = screenMonitor;
	}

	private long generateInterval() {
		if (random == null) {
			random = new Random();
		}

		// A number of milliseconds from 1 second to 10 minutes
		return random.nextInt(599001) + 1000;
	}

	private void start() {
		BufferedImage screenshot;
		long interval;

		icon = retrieveIcon();
		notificationIcon.place();

		while (true) {
			try {
				screenshot = screenMonitor.captureScreenshot();
			} catch (AWTException e1) {
				e1.printStackTrace();
				break;
			}

			interval = generateInterval();

			try {
				sender.sendScreenshotMessage(screenshot, interval);
			} catch (IOException e1) {
				e1.printStackTrace();
				break;
			}

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				break;
			}
		}

		notificationIcon.remove();
	}

	private static Image retrieveIcon() {
		InputStream stream = SnapScreen.class.getResourceAsStream("icon_48.png");
		if (icon == null) {
			try {
				icon = ImageIO.read(stream);
			} catch (IOException e1) {
				System.err.println("Error loading icon image.");
				e1.printStackTrace();
				System.exit(1);
			}
		}

		return icon;
	}
}