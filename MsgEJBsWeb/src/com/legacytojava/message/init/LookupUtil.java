package com.legacytojava.message.init;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

public class LookupUtil {
	private static Context ctx = null;
	
	public static Object lookupLocalEjb(String jndiName) {
		try {
			if (ctx == null) {
				Hashtable<String,String> jndiProperties = new Hashtable<String,String>();
				jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.as.naming.InitialContextFactory");
				jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
				ctx = new InitialContext(jndiProperties);
			}
			return ctx.lookup(jndiName);
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
