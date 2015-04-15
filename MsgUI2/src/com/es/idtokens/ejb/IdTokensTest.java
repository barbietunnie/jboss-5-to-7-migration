package com.es.idtokens.ejb;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.apache.log4j.Logger;

import jpa.constant.Constants;
import junit.framework.TestCase;

public class IdTokensTest extends TestCase {
	static final Logger logger = Logger.getLogger(IdTokensTest.class);
	
	 public void testIdTokens() throws Exception {
		 
		 final Context context = EJBContainer.createEJBContainer().getContext();
		 
		 Object obj = context.lookup("java:global/WebContent/IdTokens!com.es.idtokens.ejb.IdTokensLocal");
		 
		 assert(obj instanceof IdTokensLocal);
		 
		 IdTokensLocal idTokens = (IdTokensLocal) obj;
		 
		 final CountDownLatch ready = new CountDownLatch(1);
		 final Future<?> cd = idTokens.stayBusy(ready);
		 
		 while (cd.get()==null) {
			 TimeUnit.MILLISECONDS.sleep(10);
			 ready.countDown();
		 }
		 logger.info("Time lapsed: " + cd.get());
		 
		 try {
			 idTokens.findBySenderId(Constants.DEFAULT_SENDER_ID);
		 }
		 catch (Exception e) {
			 fail("Failed to find Sender by Id: " + Constants.DEFAULT_SENDER_ID);
		 }
		 
		 List<jpa.model.IdTokens> list = idTokens.findAll();
		 assertTrue(!list.isEmpty());
		 
		 jpa.model.IdTokens id = list.get(list.size() - 1);
		 long newTime = System.currentTimeMillis();
		 id.setUpdtTime(new java.sql.Timestamp(newTime));
		 idTokens.update(id);
		 id = idTokens.findBySenderId(id.getSenderData().getSenderId());
		 assertEquals(newTime, id.getUpdtTime().getTime());
	 }
}
