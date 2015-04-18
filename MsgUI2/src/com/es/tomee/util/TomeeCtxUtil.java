package com.es.tomee.util;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import jpa.model.EmailAddress;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.es.mailsender.ejb.MailSenderRemote;

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
		p.put("java.naming.provider.url", "http://127.0.0.1:8080/tomee/ejb"); // TomEE
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

	public static void main(String[] args) {
		try {
			// test EJB remote access
			Context ctx = getRemoteContext();
			listContext(ctx, "");
			MailSenderRemote sender = (MailSenderRemote) ctx.lookup("MailSenderRemote");
			logger.info("MailSenderRemote instance: " + sender);
			EmailAddress ea = sender.findByAddress("test@test.com");
			logger.info(StringUtil.prettyPrint(ea, 1));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}
