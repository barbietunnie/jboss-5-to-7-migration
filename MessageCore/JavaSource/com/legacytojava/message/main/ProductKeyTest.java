package com.legacytojava.message.main;

import com.legacytojava.jbatch.JbMain;
import com.legacytojava.jbatch.common.ProductKey;

public class ProductKeyTest {
	static final String LF = System.getProperty("line.separator", "\n");
	public static void main(String[] args) {
		try {
			String prodKey = JbMain.getProductKeyFromFile();
			System.out.println(prodKey + ", valid? " + ProductKey.validateKey(prodKey));
			System.out.println("Product Key valid? " + JbMain.isProductKeyValid());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
