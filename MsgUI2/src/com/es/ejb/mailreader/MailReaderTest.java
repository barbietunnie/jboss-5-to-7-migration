package com.es.ejb.mailreader;

import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

public class MailReaderTest extends TestCase {
	static final Logger logger = Logger.getLogger(MailReaderTest.class);

	 public void testMailReader() throws Exception {
		 
		 final Context context = EJBContainer.createEJBContainer().getContext();
		 
		 Object obj = context.lookup("java:global/WebContent/MailReader");
		 
		 assert(obj instanceof MailReader);
		 
		 MailReader reader = (MailReader) obj;
		 logger.info("MailReader polling interval: " + reader.getInterval());
		 
		 Thread.sleep(TimeUnit.SECONDS.toMillis(6000));
	 }
}
