package com.legacytojava.jbatch;

import java.io.IOException;
import java.lang.Object;

import javax.jms.JMSException;
import javax.mail.MessagingException;

/**
 * a thread capable processor interface that extends a normal processor
 */
public abstract class RunnableProcessor implements Processor, Runnable {
	Object threadObject = null;

	JbEventBroker eventBroker = null;

	/**
	 * prepare the thread processor
	 * 
	 * @param obj -
	 *            thread object
	 * @param _eventBroker -
	 *            event broker instance
	 */
	public void prepare(Object obj, JbEventBroker _eventBroker) {
		threadObject = obj;
		eventBroker = _eventBroker;
	}

	/**
	 * be a thread
	 */
	public void run() {
		try {
			process(threadObject);
		}
		catch (Exception e) {
			eventBroker.putException(e);
		}
	}

	/**
	 * process request
	 * 
	 * @param req -
	 *            request
	 */
	public abstract void process(Object req) throws IOException, JMSException, MessagingException;
}