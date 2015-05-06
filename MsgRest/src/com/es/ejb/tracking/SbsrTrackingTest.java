package com.es.ejb.tracking;

import static org.junit.Assert.*;

import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SbsrTrackingTest {
	protected final static Logger logger = Logger.getLogger(SbsrTrackingTest.class);
	
	private static EJBContainer ejbContainer;
	
	private SbsrTrackingLocal sbsrTracking;
	
	@BeforeClass
	public static void startTheContainer() {
		ejbContainer = EJBContainer.createEJBContainer();
	}

	@Before
	public void lookupABean() throws NamingException {
		Object object = ejbContainer.getContext().lookup("java:global/MsgRest/SbsrTracking");

		assert(object instanceof SbsrTracking);

		sbsrTracking = (SbsrTrackingLocal) object;
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSbsrTracking() {
		try {
			int rows = sbsrTracking.updateClickCount(-1);
			assert(rows==0);
		}
		catch (EJBException e) {
			fail();
		}
		
		assertTrue(sbsrTracking.getByMailingListId("").isEmpty());
		assertTrue(sbsrTracking.getByBroadcastMessageRowId(-1).isEmpty());
		
		List<BroadcastMessage> bcstMsgs = sbsrTracking.getByMailingListId("SMPLLST1");
		assertFalse(bcstMsgs.isEmpty());
		
		BroadcastMessage bm = bcstMsgs.get(0);
		
		List<BroadcastTracking> bcstTrks = sbsrTracking.getByBroadcastMessageRowId(bm.getRowId());
		assertFalse(bcstTrks.isEmpty());
		
		BroadcastTracking bt = bcstTrks.get(0);
		
		assert(1<=sbsrTracking.updateClickCount(bt.getRowId()));
		assert(1<=sbsrTracking.updateOpenCount(bt.getRowId()));
		assert(1<=sbsrTracking.updateSentCount(bt.getRowId()));
		
		assert(1<=sbsrTracking.updateClickCount(bt.getEmailAddress().getRowId(), bt.getBroadcastMessage().getMailingList().getListId()));
		assert(1<=sbsrTracking.updateOpenCount(bt.getEmailAddress().getRowId(), bt.getBroadcastMessage().getMailingList().getListId()));
	}
	
}
