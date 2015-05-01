package com.es.ejb.subscriber;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import jpa.model.SubscriberData;
import jpa.util.ExceptionUtil;
import jpa.util.StringUtil;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.tomee.util.TomeeCtxUtil;

public class SubscriberTest {
	protected final static Logger logger = Logger.getLogger(SubscriberTest.class);
	
	private static EJBContainer ejbContainer;
	
	private Subscriber subscriber;
	 
	@BeforeClass
	public static void startTheContainer() {
		ejbContainer = EJBContainer.createEJBContainer();
	}

	@Before
	public void lookupABean() throws NamingException {
		Object object = ejbContainer.getContext().lookup("java:global/MsgRest/subscriber"); //"java:global/MsgRest/subscriber!com.es.ejb.subscriber.Subscriber");

		assert(object instanceof Subscriber);

		subscriber = (Subscriber) object;
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSubscriber() {
		try {
			assertNull(subscriber.getSubscriberById(""));
		}
		catch (EJBException e) {
			//assertNotNull(e.getCause());
			//assert(e.getCause() instanceof NoResultException);
			fail();
		}
		
		List<SubscriberData> subrList = subscriber.getAllSubscribers();
		assert(!subrList.isEmpty());
		
		SubscriberData subr1 = subscriber.getSubscriberById(subrList.get(0).getSubscriberId());
		assertNotNull(subr1);
		
		SubscriberData subr2 = subscriber.getSubscriberByEmailAddress(subrList.get(0).getEmailAddr().getAddress());
		assertNotNull(subr2);
		
		logger.info(StringUtil.prettyPrint(subr2, 1));
	}
	
	@Test
	public void testSubscriberRemote() {
		try {
			SubscriberRemote rmt = (SubscriberRemote) TomeeCtxUtil.getLocalContext().lookup(
					"java:global/MsgRest/subscriber!com.es.ejb.subscriber.SubscriberRemote");
			List<SubscriberData> subrList = rmt.getAllSubscribers();
			assert(!subrList.isEmpty());
		}
		catch (NamingException e) {
			fail();
		}
	}

	@Test
	public void testSubscriberLocal() {
		try {
			SubscriberLocal subr = (SubscriberLocal) TomeeCtxUtil.getLocalContext().lookup(
					"java:global/MsgRest/subscriber!com.es.ejb.subscriber.SubscriberLocal");
			List<SubscriberData> subrList = subr.getAllSubscribers();
			assert(!subrList.isEmpty());
			
			try {
				subr.subscribe("", "");
				fail();
			}
			catch (EJBException e) {
				assert(ExceptionUtil.findRootCause(e) instanceof IllegalArgumentException);
			}
		}
		catch (NamingException e) {
			fail();
		}
	}


}
