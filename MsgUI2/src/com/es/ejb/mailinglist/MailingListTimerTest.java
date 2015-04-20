package com.es.ejb.mailinglist;

import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class MailingListTimerTest extends TestCase {
	static final Logger logger = Logger.getLogger(MailingListTimerTest.class);

	 public void testMailingListTimer() throws Exception {
		 
		 final Context context = EJBContainer.createEJBContainer().getContext();
		 
		 Object obj = context.lookup("java:global/WebContent/MailingListTimer");
		 
		 assert(obj instanceof MailingListTimer);
		 
		 MailingListTimer reader = (MailingListTimer) obj;
		 logger.info("MailingListTimer current hour: " + reader.currentHour());
		 
		 reader.scheduleSingleTask("SampleNewsLetter1", 5);
		 
		 Thread.sleep(TimeUnit.SECONDS.toMillis(30));
	 }

}
