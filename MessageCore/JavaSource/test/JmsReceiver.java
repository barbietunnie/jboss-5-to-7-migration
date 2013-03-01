package test;

import javax.jms.Message;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import com.legacytojava.jbatch.SpringUtil;

public class JmsReceiver {

	public static void main(String[] args){
		AbstractApplicationContext factory = SpringUtil.getAppContext();
		
		javax.jms.Queue queue = (javax.jms.Queue) factory.getBean("customerCareInput");
		queue = (javax.jms.Queue) factory.getBean("destination");
		
		try {
			JmsReceiver test = (JmsReceiver)factory.getBean("jmsReceiver");
			for (int i=0; i<10; i++) {
				test.receive(queue);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			//
		}
		System.exit(0);
	}
	
	private JmsTemplate jmsTemplate;

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	private void receive(javax.jms.Queue queue) {
		jmsTemplate.setDefaultDestination(queue);
		System.out.println("Will wait "+(this.jmsTemplate.getReceiveTimeout()/1000)+" seconds...");
		
		Message msg = this.jmsTemplate.receive(this.jmsTemplate.getDefaultDestination());
		System.out.println("Received Message: "+msg);
		//System.out.println(jmsTemplate.isSessionTransacted());
	}
}