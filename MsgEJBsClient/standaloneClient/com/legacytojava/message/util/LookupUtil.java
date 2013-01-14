package com.legacytojava.message.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

public class LookupUtil {
	private static Context ctx = null;
	
	/*
	 * Must setup jboss-ejb-client.properties in class path.
	 */
	public static Object lookupRemoteEjb(String jndiName) {
		try {
			if (ctx == null) {
				Hashtable<String,String> jndiProperties = new Hashtable<String,String>();
				jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
				ctx = new InitialContext(jndiProperties);
			}
			return ctx.lookup(jndiName);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

}
