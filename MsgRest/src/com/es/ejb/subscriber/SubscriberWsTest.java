package com.es.ejb.subscriber;

import static org.junit.Assert.*;

import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import jpa.model.Subscription;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SubscriberWsTest {
	protected final static Logger logger = Logger.getLogger(SubscriberWsTest.class);
	
	private static EJBContainer ejbContainer;
	
	@BeforeClass
	public static void startTheContainer() {
		Properties properties = new Properties();
        properties.setProperty("openejb.embedded.remotable", "true");
        //properties.setProperty("httpejbd.print", "true");
        //properties.setProperty("httpejbd.indent.xml", "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSubscriberWs() {
		try {
			Service service = Service.create(new URL("http://127.0.0.1:4204/MsgRest/Subscriber?wsdl"),
				new QName("http://com.es.ws.subscriber/wsdl", "SubscriberService"));
			assertNotNull(service);
			SubscriberWs sbsr = service.getPort(SubscriberWs.class);
			
			Subscription sub = sbsr.subscribe("test@test.com", "SMPLLST1");
			assertNotNull(sub);
			assertTrue(sub.isSubscribed());
			
			sub = sbsr.unSubscribe("test@test.com", "SMPLLST1");
			assertNotNull(sub);
			assertFalse(sub.isSubscribed());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
		
		try {
			TimeUnit.SECONDS.sleep(2);
		}
		catch (InterruptedException e) {}
	}
}
