package com.es.tomee.util;

import java.net.InetSocketAddress;
import java.net.Socket;
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

	public static Context getLocalContext() throws NamingException {
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
	
	public static Context getRemoteContext() throws NamingException {
		Properties p = new Properties();
		p.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
		//p.put("java.naming.provider.url", "ejbd://localhost:4201"); // OpenEjb
		int port = findHttpPort(new int[] {8181,8080});
		p.put("java.naming.provider.url", "http://127.0.0.1:" + port + "/tomee/ejb"); // TomEE
		// user and pass optional
		p.put("java.naming.security.principal", "tomee");
		p.put("java.naming.security.credentials", "tomee");
		try {
			InitialContext context = new InitialContext(p);
			return context;
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw e;
		}
	}

	public static Context getActiveMQContext(String... queueNames) throws NamingException {
		Properties props = new Properties();
		props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty(Context.PROVIDER_URL, "tcp://127.0.0.1:61616");
		
		//specify queue property name as queue.jndiname
		for (String queueName : queueNames) {
			props.setProperty("queue."+ queueName, queueName);
		}
		
		try {
			Context context = new InitialContext(props);
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
			logger.error("NamingException: " + e.getMessage());
		}
    }

	private static Integer tomcat_port = null;
	
	public static int findHttpPort(int... ports) {
		if (tomcat_port != null) {
			return tomcat_port;
		}
		String hostIP = "127.0.0.1";
		Socket socket = null;
		long start = System.currentTimeMillis();
		for (int port : ports) {
			socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(hostIP, port), 1000);
				logger.info("Port (" + port + ") reachable.");
				tomcat_port = port;
				break;
			}
			catch (java.io.IOException e) {
				logger.info("Port (" + port + ") unreachable, time spent: " + (System.currentTimeMillis() - start));
			}
			finally {
				if (socket != null) {
					try {
						socket.close();
					}
					catch (java.io.IOException e) {}
				}
			}
		}
		return (tomcat_port==null?80:tomcat_port);
	}

	public static void main(String[] args) {
		try {
			logger.info("port found: " + findHttpPort(new int[] {8181,8080}));
			// test EJB remote access
//			Context ctx = getRemoteContext();
//			listContext(ctx, "");
//			MailSenderRemote sender = (MailSenderRemote) ctx.lookup("MailSenderRemote");
//			logger.info("MailSenderRemote instance: " + sender);
//			EmailAddress ea = sender.findByAddress("test@test.com");
//			logger.info(StringUtil.prettyPrint(ea, 1));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}
