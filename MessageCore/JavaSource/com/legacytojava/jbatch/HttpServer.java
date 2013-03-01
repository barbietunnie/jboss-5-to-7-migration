package com.legacytojava.jbatch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.URLConnection;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * A simple HTTP static file processor
 */
public final class HttpServer implements Processor, Serializable {
	private static final long serialVersionUID = 4857593043104131177L;
	static final Logger logger = Logger.getLogger(HttpServer.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	// private Properties props;
	private static String documentRoot;
	private static File docRoot = null;

	/**
	 * create a HttpServer instance
	 */
	public HttpServer(String _documentRoot) throws Exception {
		documentRoot = _documentRoot;
		setDocRoot();
	}

	private void setDocRoot() throws IOException {
		if (docRoot != null) {
			return;
		}
		ClassLoader loader = this.getClass().getClassLoader();
		java.net.URL url = loader.getResource(documentRoot);
		docRoot = new File(url.getPath());
		if (isDebugEnabled)
			logger.info("Document Root: " + url.getPath());
		else if (docRoot == null || !docRoot.exists() || !docRoot.isDirectory()) {
			throw new IOException("specified documentRoot is null "
					+ "or does not exist or is not a directory");
		}
	}

	/**
	 * invoked by container to process the request
	 * 
	 * @param req
	 *            a Socket
	 * @throws Exception
	 *             if error occurred
	 */
	public void process(Object req) throws Exception {
		if (isDebugEnabled)
			logger.debug("Entering process()...");

		Socket s = null;
		if (req != null && req instanceof Socket) {
			if (isDebugEnabled)
				logger.debug("process() - A Socket received.");
			s = (Socket) req;
			InputStream in = null;
			OutputStream out = null;

			try {
				in = s.getInputStream();
				out = s.getOutputStream();
				generateResponse(in, out);
				out.flush();
			}
			catch (IOException iox) {
				logger.error("I/O error while processing request, ignored.");
			}
			finally {
				// Try to close everything, ignoring
				// any IOExceptions that might occur.
				if (in != null) {
					try {
						in.close();
					}
					catch (IOException iox) {
						// ignore
					}
					finally {
						in = null;
					}
				}

				if (out != null) {
					try {
						out.close();
					}
					catch (IOException iox) {
						// ignore
					}
					finally {
						out = null;
					}
				}

				if (s != null) {
					try {
						s.close();
					}
					catch (IOException iox) {
						// ignore
					}
					finally {
						s = null;
					}
				}
			}
		}
		else {
			logger.error("Request received was not a Socket as expected, ignored.");
		}
	}

	private void generateResponse(InputStream in, OutputStream out)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		String requestLine = reader.readLine();
		if (requestLine == null || requestLine.length() < 1) {
			throw new IOException("could not read request");
		}
		if (isDebugEnabled)
			logger.debug("requestLine=" + requestLine);

		StringTokenizer st = new StringTokenizer(requestLine);
		String filename = null;

		try {
			// request method, typically 'GET', but ignored
			st.nextToken();

			// the second token should be the filename
			filename = st.nextToken();
		}
		catch (NoSuchElementException x) {
			throw new IOException("could not parse request line");
		}

		BufferedOutputStream buffOut = new BufferedOutputStream(out);

		File requestedFile = generateFile(filename);

		if (requestedFile.exists()) {
			if (isDebugEnabled)
				logger.debug("200 OK: " + filename);
			int fileLen = (int) requestedFile.length();
			BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(requestedFile));

			// Use this utility to make a guess about the
			// content type based on the first few bytes
			// in the stream.
			String contentType = URLConnection.guessContentTypeFromStream(fileIn);

			byte[] headerBytes = createHeaderBytes("HTTP/1.0 200 OK", fileLen, contentType);
			buffOut.write(headerBytes);

			byte[] buf = new byte[2048];
			int blockLen = 0;

			while ((blockLen = fileIn.read(buf)) != -1) {
				buffOut.write(buf, 0, blockLen);
			}
			fileIn.close();
		}
		// test only, to show server status
		else if (filename.startsWith("/status")) {
			if (isDebugEnabled)
				logger.debug("200 OK: show server status, " + filename);

			StringTokenizer st2 = new StringTokenizer(filename, "?&");
			String server_name = null, goback_days = null;
			while (st2.hasMoreTokens()) {
				String pair = st2.nextToken();
				if (isDebugEnabled)
					logger.debug("token: " + pair);
				if (pair.startsWith("name="))
					server_name = pair.substring(5);
				if (pair.startsWith("days="))
					goback_days = pair.substring(5);
			}

			// get application generated status
			String app_status = showAppStatus(buffOut, goback_days);
			String text = "<html><body>" + JbMain.getMetricsReport(server_name, goback_days)
					+ "<br>" + app_status + "</body></html>";

			int len = text.length();
			byte[] headerBytes = createHeaderBytes("HTTP/1.0 200 OK", len, "text/html");
			buffOut.write(headerBytes);
			byte[] bodyBytes = text.getBytes();

			buffOut.write(bodyBytes);
		}
		// end test
		else {
			if (isDebugEnabled)
				logger.debug("404 Not Found: " + filename);
			byte[] headerBytes = createHeaderBytes("HTTP/1.0 404 Not Found", -1, null);
			buffOut.write(headerBytes);
		}
		buffOut.flush();
	}

	/**
	 * to be overwritten by application processor
	 * 
	 * @param buffOut
	 *            used by container
	 * @param goback_days
	 *            from when
	 * @return always a blank string.
	 */
	public String showAppStatus(BufferedOutputStream buffOut, String goback_days) {
		return ""; // TODO
	}

	private File generateFile(String filename) {
		File requestedFile = docRoot; // start at the base

		// Build up the path to the requested file in a
		// platform independent way. URL's use '/' in their
		// path, but this platform may not.
		StringTokenizer st = new StringTokenizer(filename, "/");
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("..")) {
				// Silently ignore parts of path that might
				// lead out of the document root area.
				continue;
			}
			requestedFile = new File(requestedFile, tok);
		}
		if (requestedFile.exists() && requestedFile.isDirectory()) {
			// If a directory was requested, modify the request
			// to look for the "index.html" file in that
			// directory.
			requestedFile = new File(requestedFile, "index.html");
		}
		return requestedFile;
	}

	private byte[] createHeaderBytes(String resp, int contentLen, String contentType)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

		// Write the first line of the response, followed by
		// the RFC-specified line termination sequence.
		writer.write(resp + "\r\n");

		// If a length was specified, add it to the header
		if (contentLen != -1) {
			writer.write("Content-Length: " + contentLen + "\r\n");
		}

		// If a type was specified, add it to the header
		if (contentType != null) {
			writer.write("Content-Type: " + contentType + "\r\n");
		}

		// A blank line is required after the header.
		writer.write("\r\n");
		writer.flush();
		byte[] data = baos.toByteArray();
		writer.close();

		return data;
	}
}
