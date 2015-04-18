package com.es.mailsender.ejb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.message.MessageBean;
import jpa.model.EmailAddress;
import jpa.service.msgin.MailFileReader;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.tomee.util.TomeeCtxUtil;

public class MailSenderTest {
	protected final static Logger logger = Logger.getLogger(MailSenderTest.class);
	
	private static EJBContainer ejbContainer;
	
	@BeforeClass
	public static void startTheContainer() {
		ejbContainer = EJBContainer.createEJBContainer();
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testMailSenderRemote() {
		try {
			MailSenderRemote rmt = (MailSenderRemote) TomeeCtxUtil.getInitialContext().lookup(
					"java:global/WebContent/MailSender!com.es.mailsender.ejb.MailSenderRemote");
			EmailAddress addr = rmt.findByAddress("test@test.com");
			assertNotNull(addr);
			
			try {
				EmailAddress ea = rmt.findByAddress("test@test.com");
				assertNotNull(ea);
				//fail();
			}
			catch (EJBException e) {
				assertNotNull(e.getCause());
				assert(e.getCause() instanceof NoResultException);
			}
		}
		catch (NamingException e) {
			fail();
		}
	}

	@Test
	public void testMailSenderLocal() {
		try {
			MailSenderLocal lcl = (MailSenderLocal) TomeeCtxUtil.getInitialContext().lookup(
					"java:global/WebContent/MailSender!com.es.mailsender.ejb.MailSenderLocal");
			EmailAddress addr = lcl.findByAddress("test@test.com");
			assertNotNull(addr);
			
			String filePath = "bouncedmails";
			String fileName = "BouncedMail_1.txt";
			MailFileReader fReader = new MailFileReader();
			try {
				MessageBean msgBean = fReader.read(filePath, fileName);
				msgBean.setSenderId(Constants.DEFAULT_SENDER_ID);
				lcl.send(msgBean);
			}
			catch (Exception te) {
				logger.error("Exception caught", te);
			}
		}
		catch (NamingException e) {
			fail();
		}
	}

}
