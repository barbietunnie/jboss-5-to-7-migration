package com.es.mailreader.ejb;

import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import junit.framework.TestCase;

public class MailReaderTest extends TestCase {

	 public void testMailReader() throws Exception {
		 
		 final Context context = EJBContainer.createEJBContainer().getContext();
		 
		 Object obj = context.lookup("java:global/WebContent/MailReader");
		 
		 assert(obj instanceof MailReader);
		 
		 MailReader reader = (MailReader) obj;
		 
		 Thread.sleep(TimeUnit.SECONDS.toMillis(6000));
	 }
}
