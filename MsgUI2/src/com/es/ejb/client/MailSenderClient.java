package com.es.ejb.client;

import javax.naming.Context;
import javax.naming.NamingException;

import jpa.model.EmailAddress;
import jpa.util.StringUtil;

import org.apache.log4j.Logger;

import com.es.mailsender.ejb.MailSenderRemote;
import com.es.tomee.util.TomeeCtxUtil;

public class MailSenderClient {
	static Logger logger = Logger.getLogger(MailSenderClient.class);
	
	public static void main(String[] args) {
		try {
			MailSenderClient client = new MailSenderClient();
			client.testMailSender();
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
	
	void testMailSender() {
		MailSenderRemote sender = null;
		Context ctx = null;
		try {
			ctx = TomeeCtxUtil.getRemoteContext();
			TomeeCtxUtil.listContext(ctx, "");
			sender = (MailSenderRemote) ctx.lookup("MailSenderRemote");
		}
		catch (NamingException e) {
			logger.error("NamingException", e);
			return;
		}

		// test EJB remote access
		logger.info("MailSenderRemote instance: " + sender);
		EmailAddress ea = sender.findByAddress("test@test.com");
		logger.info(StringUtil.prettyPrint(ea, 1));
	}
}
