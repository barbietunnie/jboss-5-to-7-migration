package com.es.jaxws.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import jpa.util.StringUtil;

import org.apache.log4j.Logger;

import com.es.ejb.subscriber.SubscriberWs;
import com.es.ejb.ws.vo.SubscriptionVo;
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
			
			SubscriptionVo sub = sbsr.addEmailToList("test@test.com", "SMPLLST1");
			assertNotNull(sub);
			assertTrue(sub.isSubscribed());
			logger.info("Subscription subed:" + StringUtil.prettyPrint(sub));
			
			sub = sbsr.removeEmailFromList("test@test.com", "SMPLLST1");
			assertNotNull(sub);
			assertFalse(sub.isSubscribed());
			logger.info("Subscription unsubed:" + StringUtil.prettyPrint(sub));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}
