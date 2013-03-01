/*
 * com/legacytojava/message/util/ServiceLocator.java
 * 
 * Copyright (C) 2008 Jack W.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.legacytojava.message.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public final class ServiceLocator {
	static final Logger logger = Logger.getLogger(ServiceLocator.class);

	private ServiceLocator() {
		// static only
	}
	
	/**
	 * Works with JNDI name: "java:/comp/env/jdbc/msgdb_pool" in JBoss
	 * @param dataSourceJndiName
	 * @return a dataSource instance
	 * @throws NamingException
	 */
	public static DataSource getDataSource(String dataSourceJndiName) throws NamingException {
		Context ctx = getInitialContext();
		DataSource dataSource = (DataSource) ctx.lookup(dataSourceJndiName);
		return dataSource;
	}

	public static Context getInitialContext() throws NamingException {
		Hashtable<String,String> jndiProps = new Hashtable<String,String>();
		/*
		 * XXX Must use this property to load local context. JBoss 7.1 loads remote context by default 
		 * which caused following NamingException:
		 * 
		 * JBAS011843: Failed instantiate InitialContextFactory org.jboss.naming.remote.client.InitialContextFactory from classloader ModuleClassLoader for Module...
		 */
        jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.as.naming.InitialContextFactory");
        // TODO find the location of the class
//        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        URL url = loader.getResource("org/jboss/as/naming/InitialContextFactory.class");
//        if (url == null) {
//        	url = ClassLoader.getSystemResource("org/jboss/as/naming/InitialContextFactory.class");
//        	if (url == null) {
//        		throw new NamingException("Not running in JBoss, ClassNotFoundException caught.");
//        	}
//        }
        return new InitialContext(jndiProps);
	}

	/**
	 * Works with following JNDI names in JBoss
	 *		ServiceLocator.listContext("java:/comp/");
	 *		ServiceLocator.listContext("java:/comp/env");
	 *		ServiceLocator.listContext("java:/comp/env/jdbc");
	 * @throws NamingException
	 */
	public static void listContext(String jndiName) throws NamingException {
		logger.info("===== List Context - JNDI ====> " + jndiName);
		Context initCtx = null;
		try {
			initCtx = getInitialContext();
		}
		catch (NamingException e) {
			if (e.getMessage().indexOf("Not running in JBoss")>=0) {
				logger.error("Initial Context failed" + e);
			}
			else {
				logger.error("Initial Context failed", e);
			}
			return;
		}
		
		// method 1
		try
		{
			logger.info("===== List Context - Method 1 =====");
			NamingEnumeration<?> enu = initCtx.list(jndiName);
			while (enu.hasMore()) {
				logger.info("Binding : " + enu.next());
			}
		}
		catch (NamingException e) {
			logger.error("JNDI lookup failed : " + e);
		}
		
		// method 2
		try {
			logger.info("===== List Context - Method 2 =====");
			NamingEnumeration<?> enu = initCtx.listBindings(jndiName);
			while (enu.hasMore()) {
				try {
					logger.info("Binding : " + enu.next());
				}
				catch (Throwable t) {
					logger.error("Throwable caught : " + t);
				}
				
			}
		}
		catch (NamingException e) {
			logger.error("JNDI lookup failed : " + e);
		}
		
		// method 3
		try {
			logger.info("===== List Context - Method 3 =====");
			listContext(initCtx, jndiName);
		}
		catch (NamingException e) {
			logger.error("JNDI lookup failed : " + e);
		}
	}
	
	/*
	 * list contents of a Context
	 */
	private static void listContext(Context ctx, String name) throws NamingException {
		NamingEnumeration<?> enu = ctx.listBindings(name);
		while (enu.hasMore()) {
			Object obj = enu.next();
			logger.info("Binding : " + StringUtil.prettyPrint(obj));
			if (obj instanceof NameClassPair) {
				NameClassPair ncp = (NameClassPair)obj;
				//System.out.println("NameClassPair classname/name: "+ncp.getClassName()+"/"+ncp.getName());
				if (ncp.getClassName().indexOf("Context")>=0)
					listContext(ctx, name+"/"+ncp.getName());
			}
		}
	}
}
