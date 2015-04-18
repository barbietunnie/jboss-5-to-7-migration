package com.es.ejb.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.es.tomee.util.TomeeCtxUtil;

public class JmsProducerCleint {
	static Logger logger = Logger.getLogger(JmsProducerCleint.class);
	
	public static void main(String[] args) {
		try {
			JmsProducerCleint client = new JmsProducerCleint();
			client.testProducer();
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}

	void testProducer() throws NamingException, JMSException {
		Context ctx = TomeeCtxUtil.getActiveMQContext("mailOutboxQueue");
		//TomeeCtxUtil.listContext(ctx, "");
		ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		logger.info("ConnectionFactory instance: " + cf);
		
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		try {
			connection = cf.createConnection();
			
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Destination queue = (Destination)ctx.lookup("mailOutboxQueue");
			logger.info("Queue instance: " + queue);
			
			producer = session.createProducer(queue);
			
			producer.send(session.createTextMessage("Hello from remote client!"));
			logger.info("Text message is sent to " + queue);
		}
		catch (JMSException e) {
			throw e;
		}
		finally {
			if (producer!=null) {
				try {
					producer.close();
				}
				catch (JMSException e) {}
			}
			if (session!=null) {
				try {
					session.close();
				}
				catch (JMSException e) {}
			}
			if (connection!=null) {
				try {
					connection.close();
				}
				catch (JMSException e) {}
			}
		}
	}
}
