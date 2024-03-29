package com.es.ejb.idtokens;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.apache.log4j.Logger;

import jpa.constant.Constants;
import jpa.util.StringUtil;
import junit.framework.TestCase;

public class IdTokensTest extends TestCase {
	static final Logger logger = Logger.getLogger(IdTokensTest.class);
	
	 public void testIdTokens() throws Exception {
		 
		 final Context context = EJBContainer.createEJBContainer().getContext();
		 
		 Object obj = context.lookup("java:global/MsgRest/IdTokens!com.es.ejb.idtokens.IdTokensLocal");
		 
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
			 assertNotNull(idTokens.findBySenderId(Constants.DEFAULT_SENDER_ID));
		 }
		 catch (Exception e) {
			 fail("Failed to find Sender by Id: " + Constants.DEFAULT_SENDER_ID);
		 }
		 
		 try {
			 assertNull(idTokens.findBySenderId(""));
		 }
		 catch (Exception e) {
			 fail();
		 }
		 
		 List<jpa.model.IdTokens> list = idTokens.findAll();
		 assertTrue(!list.isEmpty());
		 logger.info(StringUtil.prettyPrint(list.get(0)));
		 
		 jpa.model.IdTokens idvo = list.get(list.size() -1);
		 Timestamp ts = new Timestamp(System.currentTimeMillis());
		 idvo.setUpdtTime(ts);
		 idTokens.update(idvo);
		 idvo = idTokens.findBySenderId(idvo.getSenderData().getSenderId());
		 assert(ts.equals(idvo.getUpdtTime()));
	 }
}
