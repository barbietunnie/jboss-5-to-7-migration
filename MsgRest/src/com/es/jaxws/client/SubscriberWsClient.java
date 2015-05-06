package com.es.jaxws.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import jpa.model.Subscription;

import org.apache.log4j.Logger;

import com.es.ejb.subscriber.SubscriberWs;
import com.es.tomee.util.TomeeCtxUtil;

public class SubscriberWsClient {
	protected final static Logger logger = Logger.getLogger(SubscriberWsClient.class);
	
	public static void main(String[] args) {
		testSubscriberWs();
	}
	
	static void testSubscriberWs() {
		int port = TomeeCtxUtil.findHttpPort(new int[] {8181, 8080});
		try {
			Service service = Service.create(new URL("http://localhost:" + port + "/MsgRest/webservices/Subscriber?wsdl"),
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
		}
	}
}
