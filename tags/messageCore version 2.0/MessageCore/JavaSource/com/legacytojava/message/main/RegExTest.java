package com.legacytojava.message.main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RegExTest {
	static final Logger logger = Logger.getLogger(RegExTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	static final String LF = System.getProperty("line.separator", "\n");
	public static void main(String[] args) {
		try {
			RegExTest test = new RegExTest();
			//test.general();
			System.out.println(test.isHTML("plain no html text <nonhtml abc> tag<a abc>"));
			System.out.println(test.isHTML("plain no html text <a> tag</a>"));
			test.convertUrlBraces("Web Beacon<img src='wsmopen.php?msgid=$%7BBroadcastMsgId%7D&amp;listid=$%7BListId%7D' width='1' height='1' alt=''>");
			System.out.println(test.extractDate("Thu, 09 Oct 2008 07:58:55 -0400 (EDT)"));
			System.out.println(test.extractDate("9 Oct 2008 15:20:20 -0400"));
			test.general();
			Pattern p = Pattern.compile("^user_name=([\\w\\s]{1,50});");
			Matcher m = p.matcher("user_name=test user;\\nuser_phone=;\\nkey please.");
			if (m.find() && m.groupCount() >= 1) {
				for (int i = 0; i <= m.groupCount(); i++) {
					System.out.println(i + " - " + m.group(i));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	java.util.Date extractDate(String text) {
		if (text == null) return null;
		String regex = "^(?:\\w{3},\\s+)??(\\d{1,2}\\s+\\w{3}\\s+\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\s+[+-]\\d{4})";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(text);
		if (m.find() && m.groupCount() >= 1) {
			for (int i = 0; i <= m.groupCount(); i++) {
				System.out.println(i + " - " + m.group(i));
			}
			String tsStr = m.group(1);
			SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z");
			try {
				java.util.Date date = sdf.parse(tsStr);
				return date;
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/*
	 * convert %7B %7D pair to {}
	 */
	void convertUrlBraces(String text) {
		String regex = "\\$\\%7B(.{1,26}?)\\%7D";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			for (int i = 0; i <= m.groupCount(); i++) {
				System.out.println(m.group(i));
			}
			m.appendReplacement(sb, "\\$\\{" + m.group(1)+"\\}");
		}
		m.appendTail(sb);
		System.out.println(sb.toString());
	}
	
	boolean isHTML(String text) {
		Pattern pattern0 = Pattern.compile("<([a-zA-Z]{1,10})(?:\\b.{1,1024}?)?>(.*?)</\\1>", Pattern.DOTALL);
		Matcher m0 = pattern0.matcher(text);
		if (m0.find()) {
			System.out.println("Pattern0: " + m0.group(m0.groupCount()));
			System.out.println("Pattern0: " + m0.group(0));
			System.out.println("Pattern0: " + m0.group(1));
		}
		Pattern pattern1 = Pattern.compile("<([a-z]{1,10})[\\s]?[\\/]?>", Pattern.CASE_INSENSITIVE);
		Matcher m1 = pattern1.matcher(text);
		boolean f1 = false, f2 = false;
		if ((f1 = m1.find())) {
			System.out.println("Pattern1: " + m1.group(m1.groupCount()));
			System.out.println("Pattern1: " + m1.group(0));
		}
		Pattern pattern2 = Pattern.compile("<([a-z]{1,10})(?:\\b.{0,1024}?[\\/]?)?>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m2 = pattern2.matcher(text);
		if ((f2 = m2.find())) {
			System.out.println("Pattern2: " + m2.group(m2.groupCount()));
			System.out.println("Pattern2: " + m2.group(0));
		}
		return f1 | f2;
	}
	
	void general() {
		Pattern pattern = Pattern.compile("^[\\[<\"](.+)[\\]>\"]$");
		String addr = "[subscribe]";
		Matcher m = pattern.matcher(addr);
		if (m.find(0)) {
			addr = m.group(m.groupCount());
			System.out.println(m.find(0) + ", " + addr);
		}
		System.out.println("abc\r\ndef".replaceAll("\n", " ").replaceAll("\r", " "));
		
		String ssnRegex = "^([0-6]\\d{2}|7[0-6]\\d|77[0-2])([ \\-]?)(\\d{2})\\2(\\d{4})$";
		ssnRegex = "^(?!000)(?:[0-6]\\d{2}|7(?:[0-6]\\d|7[0-2]))([ -]?)(?!00)\\d{2}\\1(?!0000)\\d{4}$";
		pattern = Pattern.compile(ssnRegex);
		m = pattern.matcher("078-05-1120");
		System.out.println(m.matches());
		
		String phoneRegex = "^(?:1[ -]?)?(?:\\(\\d{3}\\)|\\d{3})[ -]?(?:\\d{3}|[a-z]{3})[ -]?(?:\\d{4}|[a-z]{4})$";
		pattern = Pattern.compile(phoneRegex, Pattern.CASE_INSENSITIVE);
		m = pattern.matcher("1(614)123-CELL");
		System.out.println(m.matches());
		
		pattern = Pattern.compile("(?i)^<?[a-z0-9-~#&\\_]+(?:\\.[a-z0-9-~#&\\_]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,5}>?\\s*$");
		pattern = Pattern.compile("^<?.+@.+>?$");
		m = pattern.matcher("<>");
		System.out.println(m.matches());
	}
}
