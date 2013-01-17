package test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.legacytojava.jbatch.SpringUtil;

public class JmsSender {

	public static void main(String[] args) {
		AbstractApplicationContext factory = SpringUtil.getAppContext();

		try {
			JmsSender test = (JmsSender)factory.getBean("jmsSender");
			test.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private JmsTemplate jmsTemplate;

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void send() {
		this.jmsTemplate.send(this.jmsTemplate.getDefaultDestination(), new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage("Test Message");
			}
		});
		System.out.println("Message Sent");
	}
}