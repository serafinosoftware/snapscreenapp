/**
 * 
 */
package com.snapscreenapp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
	public static final String CRLF = "\r\n";

	private String supervisorId;

	public Sender(String supervisorId) {
		this.supervisorId = supervisorId;
	}

	public void sendScreenshotMessage(BufferedImage screenshot, long interval) throws IOException {
		String filename = "next_" + interval + ".jpg";
		// String server = "http://super.snapscreenapp.com/monitor.php";
		String server = "https://isaacserafino.pythonanywhere.com/monitor/";
		//String server = "http://127.0.0.1:8000/monitor/";

		doPost(screenshot, "supervisor_id", supervisorId, filename, server);
	}

	/** Referenced: http://stackoverflow.com/a/35013372/7869628 */
	private void doPost(RenderedImage image, String fieldName, String fieldValue, String filename, String server)
			throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(server);
		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection) con;
		http.setRequestMethod("POST"); // PUT is another valid option
		http.setDoOutput(true);

		String boundary = randomUUID().toString();
		http.setRequestProperty("Content-Type", "multipart/form-data; charset=UTF-8; boundary=" + boundary);

		OutputStream out = http.getOutputStream();
		Writer streamWriter = new OutputStreamWriter(out, UTF_8);
		try (Writer writer = new BufferedWriter(streamWriter)) {
			String partBoundary = "--" + boundary + CRLF;
			writer.append(partBoundary);
			sendField(writer, fieldName, fieldValue);
			writer.append(partBoundary);

			sendImage(out, writer, "activity", image, filename);

			writer.append("--" + boundary + "--");
		}

		// Although response ignored, this call is required to actually finish sending request
		http.getResponseCode();
	}

	private void sendImage(OutputStream out, Writer writer, String name, RenderedImage contents, String fileName)
			throws IOException {
		writer.append("Content-Disposition: form-data; name=\"" + escape(name) + "\"; filename=\"" + escape(fileName)
				+ "\"\r\n\r\n");
		writer.flush();

		ImageIO.write(contents, "jpeg", out);

		writer.append(CRLF);
	}

	private void sendField(Writer writer, String name, String fieldValue) throws IOException {
		writer.append("Content-Disposition: form-data; name=\"" + escape(name) + "\"\r\n\r\n");
		writer.append(escape(fieldValue));
		writer.append(CRLF);
	}

	private static String escape(String s) throws UnsupportedEncodingException {
		return URLEncoder.encode(s, "UTF-8");
	}
}