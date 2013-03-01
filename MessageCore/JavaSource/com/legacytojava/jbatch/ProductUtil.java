package com.legacytojava.jbatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.legacytojava.jbatch.common.ProductKey;

public class ProductUtil {

	public static boolean isProductKeyValid() {
		if (ProductUtil.isKeyValid == null) {
			boolean isValid = ProductKey.validateKey(getProductKeyFromFile());
			ProductUtil.isKeyValid = Boolean.valueOf(isValid);
		}
		return ProductUtil.isKeyValid;
	}

	public static String getProductKeyFromFile() {
		// ThreadContextClassLoader works with both command line JVM and JBoss
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("productkey.txt");
		if (url == null) {
			JbMain.logger.warn("productkey.txt file not found.");
			return null;
		}
		BufferedReader br = null;
		try {
			InputStream is = url.openStream();
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			Pattern pattern = Pattern.compile("\\b((.{5})(-.{5}){4})\\b");
			while ((line=br.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find() && matcher.groupCount() >= 1) {
					return matcher.group(1);
				}
			}
		}
		catch (IOException e) {
			JbMain.logger.error("IOException caught", e);
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (IOException e) {}
			} 
		}
		return null;
	}

	static Boolean isKeyValid = null;

}
