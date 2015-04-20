package com.es.ejb.mailsender;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class MailSenderMDBTest extends TestCase {
	protected final static Logger logger = Logger.getLogger(MailSenderMDBTest.class);
	
	@Resource //(name = "connectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(name = "mailOutboxQueue")
    private Queue mailOutboxQueue;

	public void testMDB() {
		logger.info("In MailSenderMDBTest.testMDB()...");
		try {
			EJBContainer.createEJBContainer().getContext().bind("inject", this);
			
			final Connection connection = connectionFactory.createConnection();
			
			connection.start();
			logger.info("In MailSenderMDBTest.testMDB() - JMS Connection started");
			
	        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	
	        final MessageProducer producer = session.createProducer(mailOutboxQueue);
	        
	        sendText("Hello World!", producer, session);
	        logger.info("In MailSenderMDBTest.testMDB() - JMS Message Sent!");
	        
	        producer.close();
			session.close();
			connection.close();
			
	        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
	
	private void sendText(String text, MessageProducer producer,
			Session session) throws JMSException {

		producer.send(session.createTextMessage(text));
	}

}
