package com.legacytojava.jbatch.queue;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.JmsException;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.legacytojava.jbatch.JbMain;
import com.legacytojava.jbatch.Processor;

/**
 * Implements spring MessageListener
 */
public class JmsListener implements MessageListener {
	static final Logger logger = Logger.getLogger(JmsListener.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private JmsProcessor jmsProcessor;
	private Processor processor;

	public JmsProcessor getJmsProcessor() {
		return jmsProcessor;
	}

	public void setJmsProcessor(JmsProcessor jmsProcessor) {
		this.jmsProcessor = jmsProcessor;
	}

	public void onMessage(Message message) {
		logger.info("JMS Message Received: "+message);
		try {
			if (message.getJMSRedelivered()) {
				Enumeration<?> enu = message.getPropertyNames();
				while (enu.hasMoreElements()) {
					String name = (String)enu.nextElement();
					logger.info("Propertiy Name: "+name);
					logger.info("String Property: "+message.getStringProperty(name));
					logger.info("Int Property: "+message.getIntProperty(name));
				}
				if (message.getIntProperty("JMSXDeliveryCount")>10) {
					jmsProcessor.writeMsg(message, message.getJMSMessageID(),true);
					return;
				}
			}
			if (message instanceof TextMessage) {
				String msgText = ((TextMessage) message).getText();
				logger.info(msgText);
				try {
					String msgId = jmsProcessor.writeMsg(msgText);
					logger.info("JMSMessageId: "+msgId);
				}
				catch (MessageFormatException mfe) {
					logger.error("MessageFormatException caught, write to error queue...");
					jmsProcessor.writeMsg(msgText,message.getJMSMessageID(),true);
				}
				throw new JMSException("Test");
			}
			else if (message instanceof BytesMessage) {
				BytesMessage byteMsg = (BytesMessage) message;
				int msgLength = 0;
				int BUF_SIZE = 50000;
				byte[] data = new byte[BUF_SIZE];
				while (true) {
					int len = byteMsg.readBytes(data);
					if (len > 0) {
						msgLength += len;
					}
					else {
						break;
					}
				}
				// now we know the message length
				// so reset and read in one go.
				byte[] msg = new byte[msgLength];
				// if msgLength <= BUF_SIZE, then we already
				// have the contents
				if (msgLength <= BUF_SIZE) {
					System.arraycopy(data, 0, msg, 0, msgLength);
				}
				else {
					byteMsg.reset(); // reset cursor to beginning
					byteMsg.readBytes(msg);
				}
				jmsProcessor.writeMsg(byteMsg, null, false);
			}
			else {
				throw new IllegalArgumentException(
						"Message must be of type TextMessage, invalid message type received: "
								+ message.getClass().getName());
			}
		}
		catch (JmsException ex) {
			throw new RuntimeException(ex);
		}
		catch (JMSException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			//
		}
	}
	
	public static void main(String[] args) {
		new JmsListener().startListener();
		boolean start = false;
		if (start) {
			new JmsListener().start();
		}
		System.exit(0);
	}
	
	private void start() {
		try {
			DefaultMessageListenerContainer listener = (DefaultMessageListenerContainer)
				JbMain.getBatchAppContext().getBean("jmsListenerContainer");
			
			//listener.start();
			try {
				while (true) {
					Thread.sleep(60*1000);
				}
			}
			catch (InterruptedException e) {
				logger.info("Listener Interrupted.");
			}
			finally {
				try {
					if (listener.isActive()) {
						listener.stop();
						//listener.shutdown();
						logger.info("Listener stopped.");
					}
				}
				catch (Exception e) {}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startListener() {
		try {
			DefaultMessageListenerContainer listener = (DefaultMessageListenerContainer)
				JbMain.getBatchAppContext().getBean("jmsListenerContainer");
			
			JbMain.getBatchAppContext().getBean("smtpPool");
			JbMain.getBatchAppContext().getBean("exchPool");
			JbMain.getBatchAppContext().getBean("namedPools");
			JbMain.getBatchAppContext().getBean("session");
			//JbMain.getFactory().getBean("ctgPool");
			JbMain.getBatchAppContext().getBean("mailReader");
			JbMain.getBatchAppContext().getBean("queueReader");
			
			listener.start();
			System.out.println("Press any key to stop the Listener...");
			try {
				System.in.read();
			}
			finally {
				listener.stop();
				logger.info("Listener stopped.");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	Processor getProcessor() {
		return processor;
	}

	void setProcessor(Processor processor) {
		this.processor = processor;
	}
}