package jpa.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TestUtil {

	public static byte[] loadFromFile(String fileName) {
		return loadFromFile("jpa/test/data/", fileName);
	}

	public static byte[] loadFromFile(String filePath, String fileName) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (!filePath.endsWith(File.separator)) {
			filePath += File.separator;
		}
		InputStream is = loader.getResourceAsStream(filePath + fileName);
		if (is == null) {
			throw new RuntimeException("File (" + filePath + fileName + ") not found!");
		}
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		try {
			while ((len=bis.read(buffer))>0) {
				baos.write(buffer, 0, len);
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught: " + e.getMessage());
		}
	}
}
