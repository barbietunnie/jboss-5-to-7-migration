package com.es.tomee.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.log4j.Logger;

public class JaxrsUtil {
	final static Logger logger = Logger.getLogger(JaxrsUtil.class);
	
	public static byte[] getBytesFromDataHandler(DataHandler dh) throws IOException {
		if (dh == null) {
			return "DataHandler is null.".getBytes();
		}
		CachedOutputStream bos = new CachedOutputStream();
		try {
			IOUtils.copy(dh.getInputStream(), bos);
			byte[] out = bos.getBytes();
			return out;
		}
		finally{
			try {
				dh.getInputStream().close();
			}
			catch (IOException e) {}
			try {
				bos.close();
			}
			catch (IOException e) {
			}
		}
	}
	
	public static void printOutHttpHeaders(HttpHeaders hh) {
		if (hh == null) {
			return;
		}
		// print out request headers
		MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
		for (Iterator<String> it = headerParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("HTTP Header: " + key + " => " + headerParams.get(key));
		}
		// print out cookies
		Map<String, Cookie> cookieParams = hh.getCookies();
		for (Iterator<String> it = cookieParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Cookie: " + key + " => " + cookieParams.get(key));
		}
	}

}
