/**
 * 
 */
package com.snapscreenapp;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Scanner;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.json.JsonReader.FileLoadException;

/**
 * @author i
 *
 */
public class CloudAuthorization {
	private Image iconImage;

	public CloudAuthorization(Image iconImage) {
		this.iconImage = iconImage;
	}

	public String retrievePassword() throws IOException, FileLoadException {
		File file = new File("authorization.json");
		if (!file.exists()) {
			authorize();
		}

		DbxAuthInfo authorization = DbxAuthInfo.Reader.readFromFile(file);

		return authorization.getAccessToken();
	}

	private void authorize() throws IOException {
		// Read app info file (contains app key and app secret)
		DbxAppInfo appInfo;
		try {
			InputStream stream = SnapScreen.class.getResourceAsStream("snap_screen.app");
			Scanner scanner = new Scanner(stream);
			scanner.useDelimiter("\\A");
			String encoded;
			try {
				encoded = scanner.next();
			} finally {
				scanner.close();
			}
			Decoder decoder = Base64.getDecoder();
			byte[] json = decoder.decode(encoded);
			appInfo = DbxAppInfo.Reader.readFully(json);
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
				"Follow these steps to authorize Snap Screen to send screenshots to your desired Dropbox account.*\n"
						+ "You should only have to do this the first time.\n\n"
						+ "*Dropbox and the Dropbox logo are trademarks of Dropbox, Inc.\n"
						+ "Snap Screen is not affiliated with or otherwise sponsored by Dropbox, Inc.\n\n"
						+ "1. Go to the Internet address in the box below.\n"
						+ "2. Click \"Allow\" (you might have to log in first).\n" + "3. Copy the authorization code.\n"
						+ "Enter the authorization code here, in place of the Internet address: ",
				"Authorize Snap Screen", JOptionPane.QUESTION_MESSAGE, (Icon) iconImage, null, authorizeUrl);

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
}