package com.legacytojava.msgui.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * HttpServlet utilities.
 */
public class HttpServletUtil {
	static final Logger logger = Logger.getLogger(HttpServletUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

    private HttpServletUtil() {
        // static class
    }

    /**
	 * Retrieve cookie value by cookie name.
	 * 
	 * @param request
	 *            an HttpServletRequest instance
	 * @param name
	 *            cookie name
	 * @return cookie value
	 */
    public static String getCookieValue(HttpServletRequest request, String name) {
    	if (request == null || name == null) return null; // just for safety
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie != null && name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
	 * Set a cookie value by name and expiration period.
	 * 
	 * @param response
	 *            An HttpServletResponse instance
	 * @param name
	 *            cookie name
	 * @param value
	 *            cookie value
	 * @param maxAge
	 *            expiration period in seconds. expire immediately if set to 0.
	 */
    public static void setCookieValue(HttpServletResponse response, String name, String value,
			int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

    /**
	 * Remove a cookie from a response by cookie name, by setting the expiration
	 * period to 0.
	 * 
	 * @param response
	 *            An HttpServletResponse instance
	 * @param name
	 *            The name of the cookie to be removed.
	 */
    public static void removeCookie(HttpServletResponse response, String name) {
        setCookieValue(response, name, null, 0);
    }

    /**
	 * Send a file to a servlet response for down-load. If isAttachment is true,
	 * pop-up a "Save as" dialogue window, else display the file contents in the
	 * browser or have the OS open it with an associated program.
	 * 
	 * @param response
	 *            an HttpServletResponse
	 * @param bytes
	 *            a file contents wrapped in a byte array.
	 * @param fileName
	 *            the file name
	 * @param isAttachment
	 *            down load as attachment?
	 */
    public static void downloadFile(HttpServletResponse response, byte[] bytes, String fileName,
			boolean isAttachment) throws IOException {
		// Wrap the byte array in a ByteArrayInputStream
		downloadFile(response, new ByteArrayInputStream(bytes), fileName,isAttachment);
	}

    /**
	 * Send a file to a servlet response for down-load. If isAttachment is true,
	 * pop-up a "Save as" dialogue window, else display the file contents in the
	 * browser or have the OS open it with an associated program.
	 * 
	 * @param response
	 *            an HttpServletResponse
	 * @param file
	 *            a File object
	 * @param isAttachment
	 *            down load as attachment?
	 */
    public static void downloadFile(HttpServletResponse response, File file, boolean isAttachment)
			throws IOException {
		BufferedInputStream input = null;
		try {
			// Wrap the file in a BufferedInputStream
			input = new BufferedInputStream(new FileInputStream(file));
			downloadFile(response, input, file.getName(), isAttachment);
		}
		finally {
			// make sure to close the stream
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException e) {
					logger.error("Faild to close file: " + file.getPath(), e);
				}
			}
		}
	}

    /**
	 * Send a file to a servlet response for down-load. If isAttachment is true,
	 * pop-up a "Save as" dialogue window, else display the file contents in the
	 * browser or have the OS open it with an associated program.
	 * 
	 * @param response
	 *            an HttpServletResponse
	 * @param input
	 *            a file contents in an InputStream
	 * @param fileName
	 *            file name.
	 * @param iaAttachment
	 *            down load as attachment?
	 */
    public static void downloadFile(HttpServletResponse response, InputStream input,
			String fileName, boolean isAttachment) throws IOException {
		BufferedOutputStream output = null;

		try {
			int contentLength = input.available();
			String contentType = URLConnection.guessContentTypeFromName(fileName);
			String disposition = isAttachment ? "attachment" : "inline";

			// If content type is unknown, set to "application/octet-stream"
			// For a complete list of content types, see:
			// http://www.w3schools.com/media/media_mimeref.asp
			if (contentType == null) {
				contentType = "application/octet-stream";
			}

			// initialize servlet response
			response.reset();
			response.setContentLength(contentLength);
			response.setContentType(contentType);
			response.setHeader("Content-disposition", disposition + "; filename=\"" + fileName
					+ "\"");
			output = new BufferedOutputStream(response.getOutputStream());

			// Write file contents to response
			while (contentLength-- > 0) {
				output.write(input.read());
			}
			output.flush();
		}
		finally {
			// make sure to close stream.
			if (output != null) {
				try {
					output.close();
				}
				catch (IOException e) {
					logger.error("Failed to close HttpServletResponse.outputStream", e);
				}
			}
		}
	}

    /**
	 * Send a GET request to the given URL with the given parameters that will
	 * be encoded as UTF-8. It is highly recommended to close the returned input
	 * stream after processing!
	 * 
	 * @param url
	 *            The URL to be invoked
	 * @param parameters
	 *            query parameters.
	 * @return The result of the GET request as an InputStream
	 * @throws MalformedURLException
	 *             If the given URL is invalid
	 * @throws IOException
	 *             If the given URL cannot be connected or written
	 */
    public static InputStream doGet(String url, Map<String, String[]> parameters)
			throws MalformedURLException, IOException {
		return doGet(url, parameters, "UTF-8");
	}

    /**
	 * Send a POST request to the given URL with the parameters and the charset
	 * encoding. It is highly recommended to close the returned input stream
	 * after processing!
	 * 
	 * @param url
	 *            The URL to be invoked
	 * @param parameters
	 *            query parameters.
	 * @param charset
	 *            The encoding to be applied
	 * @return The result of the POST request as an InputStream
	 * @throws MalformedURLException
	 *             If the given URL is invalid
	 * @throws IOException
	 *             If the given URL cannot be connected or written
	 * @throws UnsupportedEncodingException
	 *             If the given charset is not supported
	 */
	public static InputStream doGet(String url, Map<String, String[]> parameters, String charset)
			throws MalformedURLException, IOException, UnsupportedEncodingException {
		String query = createQuery(parameters, charset);
		URLConnection urlConnection = new URL(url + "?" + query).openConnection();
		urlConnection.setUseCaches(false);

		return urlConnection.getInputStream();
	}

    /**
	 * Send a GET request to the given URL with the given parameters that will
	 * be encoded as UTF-8. It is highly recommended to close the returned input
	 * stream after processing!
	 * 
	 * @param url
	 *            The URL to be invoked
	 * @param parameters
	 *            query parameters.
	 * @return The result of the POST request as an InputStream
	 * @throws MalformedURLException
	 *             If the given URL is invalid
	 * @throws IOException
	 *             If the given URL cannot be connected or written
	 */
    public static InputStream doPost(String url, Map<String, String[]> parameters)
			throws MalformedURLException, IOException {
		return doPost(url, parameters, "UTF-8");
	}

    /**
	 * Send a POST request to the given URL with the parameters and the charset
	 * encoding. It is highly recommended to close the returned input stream
	 * after processing!
	 * 
	 * @param url
	 *            The URL to be invoked
	 * @param parameters
	 *            query parameters.
	 * @param charset
	 *            The encoding to be applied
	 * @return The result of the POST request as an InputStream
	 * @throws MalformedURLException
	 *             If the given URL is invalid
	 * @throws IOException
	 *             If the given URL cannot be connected or written
	 * @throws UnsupportedEncodingException
	 *             If the given charset is not supported
	 */
	public static InputStream doPost(String url, Map<String, String[]> parameters, String charset)
			throws MalformedURLException, IOException, UnsupportedEncodingException {
		String query = createQuery(parameters, charset);
		URLConnection urlConnection = new URL(url).openConnection();
		urlConnection.setUseCaches(false);
		urlConnection.setDoOutput(true); // triggers POST
		urlConnection.setRequestProperty("accept-charset", charset);
		urlConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(urlConnection.getOutputStream());
			writer.write(query);
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				}
				catch (IOException e) {
					logger.error("Failed to close URLConnection.outputStream for " + url, e);
				}
			}
		}
		return urlConnection.getInputStream();
	}

    /**
	 * Create a query string by the given parameter map and charset encoding.
	 * 
	 * @param parameters
	 *            contains query parameters
	 * @param charset
	 *            the encoding to be applied
	 * @return A parameter map as query string
	 * @throws UnsupportedEncodingException
	 *             If the given charset is not supported.
	 */
    public static String createQuery(Map<String, String[]> parameters, String charset)
			throws UnsupportedEncodingException {
		StringBuilder query = new StringBuilder();
		if (parameters == null) return query.toString(); // just for safety
		for (Iterator<String> names = parameters.keySet().iterator(); names.hasNext();) {
			String name = names.next();
			String[] values = parameters.get(name);
			for (int i = 0; i < values.length;) {
				query.append(URLEncoder.encode(name, charset));
				query.append("=");
				query.append(URLEncoder.encode(values[i], charset));
				if (++i < values.length) {
					query.append("&");
				}
			}
			if (names.hasNext()) {
				query.append("&");
			}
		}
		return query.toString();
	}
}