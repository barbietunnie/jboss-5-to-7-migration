package com.es.ejb.mailinglist;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MailingListTest {
	protected final static Logger logger = Logger.getLogger(MailingListTest.class);
	
	private static EJBContainer ejbContainer;
	
	private MailingListLocal mlist;
	 
	@BeforeClass
	public static void startTheContainer() {
		ejbContainer = EJBContainer.createEJBContainer();
	}

	@Before
	public void lookupABean() throws NamingException {
		Object object = ejbContainer.getContext().lookup("java:global/MsgRest/MailingList");

		assert(object instanceof MailingList);

		mlist = (MailingListLocal) object;
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testBroadcast() {
		try {
			int msgsSent = mlist.broadcast("SampleNewsLetter2");
			assert(msgsSent > 0);
		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail("Failed to broadcast");
		}
	}

	@Test
	public void testSendmail() {
		try {
			String toAddr = "testto@localhost";
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("CustomerName", "List Subscriber");
			String templateId = "SampleNewsLetter3";
			int mailsSent = mlist.sendMail(toAddr, vars, templateId);
			logger.info("Number of emails sent: " + mailsSent);
			assert(mailsSent > 0);
		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail("Failed to sendmail");
		}
	}

}
