package com.legacytojava.jbatch;

import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.QueueReaderVo;

/**
 * Queue Listener receives requests from a input queue and delegates the request
 * to a Processor for processing.
 */
public class QueueListener extends JbThread implements MessageListener {
	private static final long serialVersionUID = -3578869656621657472L;
	static final Logger logger = Logger.getLogger(QueueListener.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private final QueueReaderVo queueReaderVo;
	private JmsTemplate jmsTemplate;
	
	protected QueueListener(QueueReaderVo queueReaderVo) {
		super(queueReaderVo, JbMain.QUEUE_SVR_TYPE);
		this.queueReaderVo = queueReaderVo;
		logger.info("in Constructor() - props:" + LF + queueReaderVo);
	}

	public void onMessage(Message message) {
		if (isDebugEnabled) {
			logger.debug("JMS Message Received: " + message);
		}
		Date start_tms = new Date();
		try {
			if (message.getJMSRedelivered()) {
				Enumeration<?> enu = message.getPropertyNames();
				while (enu.hasMoreElements()) {
					String name = (String)enu.nextElement();
					logger.info("Message Redelivered, Propertiy - " + name + ": "
							+ message.getStringProperty(name));
					logger.info("Int Property Value: "+message.getIntProperty(name));
					String nameLC = name.toLowerCase();
					if (nameLC.indexOf("jms") >= 0
							&& nameLC.indexOf("delivery") > nameLC.indexOf("jms")
							&& nameLC.indexOf("count") > nameLC.indexOf("delivery")) {
						if (message.getIntProperty(name) > 10) {
							// either write it to error queue or stop the listener
							// decision need to be made by the property value:
							if ("stop_listener".equals(queueReaderVo.getDeliveryCountReached())) {
								logger.error("Redelivery Count exceeded 10, stop the listener...");
								keepRunning = false;
								this.interrupt();
							}
							else {
								logger.error("Redelivery Count exceeded 10, send to error queue...");
								jmsTemplate.convertAndSend(message);
							}
							return;
						}
					}
				}
			}
			update(MetricsLogger.PROC_INPUT, 1);
			Processor processor = (Processor) JbMain.getBatchAppContext().getBean(
					queueReaderVo.getProcessorName());
			processor.process(message);
			// assume the processor produces one output per input
			update(MetricsLogger.PROC_OUTPUT, 1);
		}
		catch (InterruptedException e) {
			logger.error("onMessage() - InterruptedException caught", e);
			this.interrupt();
		}
		catch (DataValidationException e) {
			logger.error("onMessage() - DataValidationException caught", e);
			update(MetricsLogger.PROC_ERROR, 1);
			// write to error queue
			jmsTemplate.convertAndSend(message);
		}
		catch (MessageFormatException e) {
			logger.error("onMessage() - MessageFormatException caught", e);
			update(MetricsLogger.PROC_ERROR, 1);
			// write to error queue
			jmsTemplate.convertAndSend(message);
		}
		catch (NumberFormatException e) {
			logger.error("onMessage() - NumberFormatException caught", e);
			update(MetricsLogger.PROC_ERROR, 1);
			// write to error queue
			jmsTemplate.convertAndSend(message);
		}
		catch (JMSException je) {
			logger.error("onMessage() - JMSException caught", je);
			update(MetricsLogger.PROC_ERROR, 1);
			throw new RuntimeException(je);
		}
		catch (SQLException se) {
			logger.error("onMessage() - SQLException caught", se);
			update(MetricsLogger.PROC_ERROR, 1);
			throw new RuntimeException(se);			
		}
		catch (Exception ex) {
			logger.error("onMessage() - Exception caught", ex);
			update(MetricsLogger.PROC_ERROR, 1);
			throw new RuntimeException(ex);
		}
		finally {
			message = null;
			/* Message processed, update processing time */
			long proc_time = new Date().getTime() - start_tms.getTime();
			update(MetricsLogger.PROC_TIME, (int) proc_time, 1);
			logger.info("onMessage() ended. Time spent in milliseconds: " + proc_time);
		}
	}
	
	public static void main(String[] args) {
		QueueListener queListener = (QueueListener)JbMain.getBatchAppContext().getBean("queueListener");
		try {
			queListener.start();
			queListener.join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public void run() {
		logger.info("Entering QueueListener.run() method...");
		keepRunning = true;
		
		DefaultMessageListenerContainer listener = (DefaultMessageListenerContainer)
			JbMain.getBatchAppContext().getBean("queueListenerContainer");
		
		boolean normal_exit = false;
		if (listener.isActive() && !listener.isRunning()) {
			logger.info("starting the queueListener...");
			listener.start();
		}
		
		try {
			while (keepRunning) {
				Thread.sleep(5L*1000L);
			}
		}
		catch (InterruptedException e) {
			logger.info("QueueListener Interrupted, Quitting...");
		}
		finally {
			if (listener.isActive() && listener.isRunning()) {
				try {
					listener.stop();
					logger.info("QueueListener stopped.");
				}
				catch (Exception e) {
					logger.info("Failed to stop QueueListener", e);
				}
			}
		}
		
		if (normal_exit) { // exit without error
			JbMain.getInstance().serverExited(this);
		}
		else { // recycle the server
			JbMain.getInstance().serverAborted(this, queueReaderVo);
		}
		logger.info("QueueListener.run() ended.");
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public QueueReaderVo getQueueReaderVo() {
		return queueReaderVo;
	}
}