package test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class JmsListener implements MessageListener {
	static final Logger logger = Logger.getLogger(JmsListener.class);
	
	public void onMessage(Message message) {
		logger.info("Invoking onMessage Now ...");
		if (message instanceof TextMessage) {
			try {
				logger.info(((TextMessage) message).getText());
			} catch (JMSException ex) {
				throw new RuntimeException(ex);
			}
		} else {
			throw new IllegalArgumentException(
					"Message must be of type TextMessage");
		}
	}

	public static void main(String[] args) {
		AbstractApplicationContext factory = new ClassPathXmlApplicationContext("spring-jmslistener-config.xml");

		new JmsListener().startListener(factory);
		
		System.exit(0);
	}
	
	private void startListener(AbstractApplicationContext factory) {
		try {
			DefaultMessageListenerContainer listener = (DefaultMessageListenerContainer) factory
					.getBean("listenerContainer");
			
			listener.start();
			System.out.println("Press any key to stop the Listener...");
			try {
				System.in.read();
			}
			finally {
				listener.stop();
				logger.info("Listener stopped.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}