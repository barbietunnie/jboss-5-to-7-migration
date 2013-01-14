package com.legacytojava.jbatch.common;

import java.security.DigestException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Random;

public class KeyGenerator {

	/*
	 * 32 Numerics and alphabetics - I, O, S and Z were removed as we only want
	 * 32 characters, and we could be mistaken I for number 1, O for 0, S for 5
	 * and Z for 2.
	 */
	static String ValidChars = ProductKey.ValidChars;
	static String prodName = ProductKey.prodName;
	static int RDM_LEN = ProductKey.RDM_LEN;
	final static String dburl = "jdbc:mysql://localhost:3306/esphere";
	
	public static void main(String[] args) {
		try {
			String prodKey = generateKey();
			System.out.println("Product Key: " + prodKey);
			System.out.println("Valid Key? " + ProductKey.validateKey(prodKey));
			insertProductKeys();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * generate a product key using Digest-MD5 algorithm.
	 * 
	 * @param prodName -
	 *            product name the key to be generated from
	 * @return a product key, for example: 3E4J7-7RU79-G2CUG-3R5N4-6FF4G
	 * @throws DigestException
	 *             if failed
	 */
	public static String generateKey() throws DigestException {
		String random = generateRandom(RDM_LEN);
		String prodKey = random + ProductKey.generateMD5(random, prodName);
		return insertDashes(prodKey);
	}
	
	private static String generateRandom(int len) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < len; i++) {
			int r = random.nextInt(ValidChars.length());
			sb.append(ValidChars.charAt(r));
		}
		return sb.toString();
	}
	
	private static String insertDashes(String key) {
		StringBuffer sb = new StringBuffer();
		char[] chars = key.toCharArray();
		sb.append(chars[0]);
		for (int i = 1; i < chars.length; i++) {
			if (i % 5 == 0) {
				sb.append("-");
			}
			sb.append(chars[i]);
		}
		return sb.toString();
	}
	
	static void insertProductKeys() throws Exception {
		Connection conn = null;
		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (dburl, "jwang", "jwang");
			Statement stmt = conn.createStatement();
			int count = 100;
			for (int i = 0; i < count; i++) {
				String prodKey = generateKey();
				stmt.executeUpdate("insert into productkeys (ProductKey, AddTime)" + 
						" values ('" + prodKey + "',now())");
			}
			System.out.println("Number of keys generated: " + count);
			stmt.close();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (Exception e) {
				}
			}
		}
	}
}
