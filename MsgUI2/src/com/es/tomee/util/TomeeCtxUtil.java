package com.es.tomee.util;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class TomeeCtxUtil {
	protected final static Logger logger = Logger.getLogger(TomeeCtxUtil.class);

	public static Context getInitialContext() throws NamingException {
		Properties p = new Properties();
		p.put("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory");
		try {
			InitialContext context = new InitialContext(p);
			return context;
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw e;
		}
	}
	
    public static void listContext(Context context, String listName) {
    	try {
			NamingEnumeration<NameClassPair> list = context.list(listName);
			while (list!=null && list.hasMore()) {
				String name = list.next().getName();
				logger.info("Name: " + listName + "/" + name);
				if (StringUtils.isNotBlank(name)) {
					listContext(context, listName + "/" + name);
				}
			}
		} catch (NamingException e) {
			logger.error(e.getMessage());
		}
    }

}
