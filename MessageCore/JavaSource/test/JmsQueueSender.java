package test;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.legacytojava.jbatch.SpringUtil;

public class JmsQueueSender {

	public static void main(String[] args) {
		AbstractApplicationContext factory = SpringUtil.getAppContext();

		try {
			JmsQueueSender test = new JmsQueueSender();
			test.send(factory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void send(AbstractApplicationContext factory) {
		JmsTemplate template = (JmsTemplate) factory.getBean("jmsTemplate");
		Destination destination = (Destination) factory.getBean("destination");
		//Destination destination = (Destination) factory.getBean("rmaRequestInput");

		template.send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("Test Message");
			}
		});
		System.out.println("Message Sent");
	}
}