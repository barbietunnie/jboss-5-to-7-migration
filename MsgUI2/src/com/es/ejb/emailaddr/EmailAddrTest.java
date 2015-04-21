package com.es.ejb.emailaddr;

import static org.junit.Assert.*;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import jpa.model.EmailAddress;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.tomee.util.TomeeCtxUtil;

public class EmailAddrTest {
	protected final static Logger logger = Logger.getLogger(EmailAddrTest.class);

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
	public void testEmailAddrRemote() {
		try {
			EmailAddrRemote rmt = (EmailAddrRemote) TomeeCtxUtil.getLocalContext().lookup(
					"java:global/WebContent/EmailAddr!com.es.ejb.emailaddr.EmailAddrRemote");
			
			EmailAddress addr = rmt.findSertAddress("emailaddr@remote.test");
			assertNotNull(addr);
			int rows = rmt.delete(addr.getAddress());
			assert(rows>0);
		}
		catch (NamingException e) {
			fail();
		}
	}
}
