/**
 * 
 */
package com.snapscreenapp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

/**
 * @author i
 *
 */
public class Sender {
	private String supervisorId;

	public Sender(String supervisorId) {
		this.supervisorId = supervisorId;
	}

	public void sendScreenshotMessage(BufferedImage screenshot, long interval) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(screenshot, "jpeg", os);

		String filename = "/next_" + interval + ".jpg";
		String server = "https://deferred-transistor.000webhostapp.com/monitor.php";

		doPost(os, "supervisor_id", supervisorId, filename, server);
	}

	/** Source: http://stackoverflow.com/a/35013372/7869628 */
	private void doPost(ByteArrayOutputStream outputStream, String fieldName, String fieldValue, String filename,
			String server) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(server);
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection) con;
		http.setRequestMethod("POST"); // PUT is another valid option
		http.setDoOutput(true);

		String boundary = randomUUID().toString();
		http.setRequestProperty("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + boundary);

		http.setChunkedStreamingMode(0);

		try (OutputStream out = http.getOutputStream()) {
			String partBoundary = "--" + boundary + "\r\n";
			write(out, partBoundary);
			sendField(out, fieldName, fieldValue);
			write(out, partBoundary);

			try (InputStream is = new ByteArrayInputStream(outputStream.toByteArray())) {
				sendFile(out, "identification", is, filename);
			}

			write(out, "--" + boundary + "--");
		}
	}

	private void sendFile(OutputStream out, String name, InputStream in, String fileName) throws IOException {
		write(out, "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name, "UTF-8") + "\"; filename=\""
				+ URLEncoder.encode(fileName, "UTF-8") + "\"\r\n\r\n");

		byte[] buffer = new byte[2048];
		for (int n = 0; n >= 0; n = in.read(buffer))
			out.write(buffer, 0, n);

		write(out, "\r\n");
	}

	private void sendField(OutputStream out, String name, String fieldValue) throws IOException {
		write(out, "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name, "UTF-8") + "\"\r\n\r\n");
		write(out, URLEncoder.encode(fieldValue, "UTF-8"));
		write(out, "\r\n");
	}

	private static void write(OutputStream out, String s) throws IOException {
		out.write(s.getBytes(UTF_8));
	}
}