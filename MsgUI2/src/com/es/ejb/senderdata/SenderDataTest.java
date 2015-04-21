package com.es.ejb.senderdata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.persistence.NoResultException;

import jpa.model.SenderData;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.tomee.util.TomeeCtxUtil;

public class SenderDataTest {
	protected final static Logger logger = Logger.getLogger(SenderDataTest.class);
	
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
	public void testSenderDataLocal() {
		try {
			SenderDataLocal senderDao = (SenderDataLocal) TomeeCtxUtil.getLocalContext().lookup(
					"java:global/WebContent/SenderData!com.es.ejb.senderdata.SenderDataLocal");
			
			try {
				senderDao.findBySenderId("");
				fail();
			}
			catch (EJBException e) {
				assertNotNull(e.getCause());
				assert(e.getCause() instanceof NoResultException);
			}
			
			List<SenderData> list = senderDao.findAll();
			assert(!list.isEmpty());
			jpa.model.SenderData sender = senderDao.findBySenderId(list.get(0).getSenderId());
			assertNotNull(sender);
			java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
			sender.setUpdtTime(updtTime);
			senderDao.update(sender);
			jpa.model.SenderData senderUpdated = senderDao.findBySenderId(sender.getSenderId());
			assert(updtTime.equals(senderUpdated.getUpdtTime()));
		}
		catch (NamingException e) {
			fail();
		}
	}
}
