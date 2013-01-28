package com.pra.rave.xml.util;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;

import com.pra.util.logger.LoggerHelper;

public class JaxbUtil {
	static Logger logger = LoggerHelper.getLogger();
	static boolean isDebugEnabled = logger.isDebugEnabled();

	private static final Map<String, JAXBContext> ctxs = new HashMap<String, JAXBContext>();

	public synchronized static JAXBContext getJAXBContext(String pkgName) throws JAXBException {
		// JAXBContext is thread safe
		if (!ctxs.containsKey(pkgName)) {
			long start = new java.util.Date().getTime();
			JAXBContext ctx = JAXBContext.newInstance(pkgName);
			ctxs.put(pkgName, ctx);
			logger.info("getJAXBContext() - JAXBContext created for " + pkgName); 
			logger.info("getJAXBContext() - Time taken " + (new java.util.Date().getTime() - start) + " ms"); 
		}
		return (JAXBContext) ctxs.get(pkgName);
	}
	
}
