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

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;

/**
 * @author i
 *
 */
public class Sender {
	private String password;
	private DbxClientV2 dropbox;
	private NotificationIcon notificationIcon;

	public Sender(String password, NotificationIcon notificationIcon) {
		this.password = password;
		this.notificationIcon = notificationIcon;
	}

	public void sendScreenshotMessage(BufferedImage screenshot, long interval) throws IOException {
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
	private void uploadFile(DbxClientV2 dbxClient, InputStream in, String dropboxPath) {
		try {
			FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath).withMode(WriteMode.ADD)
					.withClientModified(new Date()).uploadAndFinish(in);

			System.out.println(metadata.toStringMultiline());
		} catch (UploadErrorException ex) {
			System.err.println("Error uploading to Dropbox: " + ex.getMessage());
			notificationIcon.remove();
			System.exit(1);
		} catch (DbxException ex) {
			System.err.println("Error uploading to Dropbox: " + ex.getMessage());
			notificationIcon.remove();
			System.exit(1);
		} catch (IOException ex) {
			System.err.println("Error reading from stream \"" + in + "\": " + ex.getMessage());
			notificationIcon.remove();
			System.exit(1);
		}
	}
}
