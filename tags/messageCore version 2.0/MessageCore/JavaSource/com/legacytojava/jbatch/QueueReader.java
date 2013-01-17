/*
 * @(#)QueueReader.java	1.0	2002/02/20
 *
 *	@author:	Jack Wang
 * 
 */
package com.legacytojava.jbatch;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.springframework.jms.JmsException;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.legacytojava.jbatch.smtp.SmtpException;
import com.legacytojava.message.vo.QueueReaderVo;

/**
 * @deprecated - replaced by QueueListener
 * 
 * Queue Reader receives requests from a input queue and delegates the requests
 * to a QueueReaderThread for processing.
 * 
 * <pre>
 * 1) register an event listener to handle Exceptions
 * 3) create a thread pool, the number of threads is defined by properties
 * 4) create workers, each worker is loaded with a QueueReaderThread
 * 5) the number of workers is greater than or equals to the number of threads
 * 6) invoke run() method to perform following:
 * 6.1) read message from input queue
 * 6.2) retrieve a worker from workers pool
 * 6.3) pass the message to the worker and execute the thread
 * 7) repeat step 6.1 to 6.3 until a shutdown request or a Exception is received
 * 7.1) shut down all threads and clean up the thread pool
 * 7.2) register itself as an exited or aborted server to the server monitor
 * </pre>
 */
class QueueReader extends JbThread implements java.io.Serializable,
		JbEventListener {
	private static final long serialVersionUID = -7345655559355669343L;
	protected static final Logger logger = Logger.getLogger(QueueReader.class);

	private final QueueReaderVo queueReaderVo;
	private JmsTemplate jmsTemplate;
	private JmsTransactionManager transactionManager;

	final JbEventBroker eventBroker;

	final String queueName;
	int PRIORITY = Thread.NORM_PRIORITY;

	/**
	 * create a QueueReader instance
	 * 
	 * @param props -
	 *            queue properties
	 * @throws Exception
	 *             if any error
	 */
	QueueReader(QueueReaderVo queueReaderVo) {
		super(queueReaderVo, JbMain.QUEUE_SVR_TYPE);
		this.queueReaderVo = queueReaderVo;
		logger.info("Entering QueueReader Constructor..." + queueReaderVo);
		eventBroker = new JbEventBroker();
		eventBroker.addAgentEventListener(this);
		queueName = queueReaderVo.getQueueName();

		init();
	}

	/**
	 * initialize the QueueReader
	 * 
	 * @throws Exception
	 *             if any error
	 */
	final void init() {
		createThreadPool();
	}

	/**
	 * create a thread pool
	 * 
	 * @throws Exception
	 *             if any error
	 */
	final void createThreadPool() {
		// retrieve PRIORITY
		// Have the queueThread run at a slightly lower priority so that new
		// messages are handled with higher priority than in-progress messages.
		PRIORITY = getPriority() - 1;
		String priority = queueReaderVo.getPriority();
		if ("high".equalsIgnoreCase(priority))
			PRIORITY = Thread.MAX_PRIORITY - 1;
		else if ("low".equalsIgnoreCase(priority))
			PRIORITY = Thread.MIN_PRIORITY + 1;
		else if ("medium".equalsIgnoreCase(priority))
			PRIORITY = Thread.NORM_PRIORITY;

		MSG2PROC = queueReaderVo.getMessageCount();

		// retrieve the number of threads to be started
		MAX_CLIENTS = queueReaderVo.getThreads();
		MAX_CLIENTS = MAX_CLIENTS > JbMain.MAX_THREADS ? JbMain.MAX_THREADS : MAX_CLIENTS;

		logger.info("Creating Queue Threads for " + queueName + ", # of Threads: " + MAX_CLIENTS
				+ ", PRIORITY: " + PRIORITY);
		jbPool = Executors.newFixedThreadPool(MAX_CLIENTS);
	}

	/**
	 * perform task
	 */
	public void run() {
		logger.info("Entering QueueReader.run() method...");

		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		
		boolean normal_exit = false;
		ThreadPoolExecutor tPool = (ThreadPoolExecutor) jbPool;
		while (keepRunning) {
			Thread.yield(); // give other threads chance to run
			// for test only
			if (MSG2PROC > 0 && getMetricsLogger().getMetricsData().getInputCount() > MSG2PROC) {
				normal_exit = true;
				break;
			}
			// end

			QueueReaderThread queThread = new QueueReaderThread(this);
			javax.jms.Message message = null;
			
			while (message == null && keepRunning) {
				TransactionStatus tStatus = null;
				try {
					tStatus = transactionManager.getTransaction(definition);
					queThread.setTransactionStatus(tStatus);
					/* Read next JMS Message, timeout in 5 seconds */
					jmsTemplate.setReceiveTimeout(5000);
					message = jmsTemplate.receive();
					if (message != null) {
						if (isDebugEnabled)
							logger.debug("Message Received: " + LF + message);
						queThread.setMessage(message);
						jbPool.execute(queThread);
						update(MetricsLogger.PROC_WORKER, tPool.getPoolSize());
					}
				}
				catch (JmsException je) {
					logger.error("JmsException caught during QueueReader run()",je);
					keepRunning = false;
					//transactionManager.rollback(tStatus);
					transactionManager.rollback(queThread.getTransactionStatus());
				}
				catch (Throwable e) {
					// other exception, exiting Agent
					logger.error("Exception caught during jms receive", e);
					keepRunning = false; // really bad thing happened
					transactionManager.rollback(queThread.getTransactionStatus());
				}
			} // end of while(message==null)
		} // end while(keepRunning)

		shutDownPool();
		
		if (normal_exit) // exit without error
			JbMain.getInstance().serverExited(this);
		else  // recycle the server
			JbMain.getInstance().serverAborted(this, queueReaderVo);

		logger.info("QueueReader.run() ended.");
	}

	/**
	 * implement JbEventListener methods
	 * 
	 * @param e -
	 *            event
	 */
	public void exceptionCaught(JbEvent e) {
		Exception excep = e.getException();
		logger.error("EventListener: Exception caught by QueueReaderThread. "
				+ excep);
		if (excep instanceof javax.jms.JMSException) {
			logger.error("Stopping the queue server due to JMSException "
					+ excep);
			keepRunning = false;
			JbMain.getEventAlert().issueExcepAlert(JbMain.QUEUE_SVR,
					"QueueReaderThread caught JMSException, stopping server "
							+ queueReaderVo.getQueueName(), excep);

			Object source = e.getSource();
			String class_name = source.getClass().getName();
			if (isDebugEnabled)
				logger.debug("The Class that generated this event: " + class_name);
		}
		else if (excep instanceof java.sql.SQLException) {
			// recycle the queue thread and retry
			JbMain.getEventAlert().issueExcepAlert(JbMain.QUEUE_SVR,
					"QueueReaderThread caught SQLException, server "
							+ queueReaderVo.getQueueName(), excep);
		}
		else if (excep instanceof javax.mail.MessagingException) {
			// TODO: handle MessagingException from java mail. recycle the
			// thread for now
			JbMain.getEventAlert().issueExcepAlert(JbMain.QUEUE_SVR,
					"QueueReaderThread caught MessagingException, server "
							+ queueReaderVo.getQueueName(), excep);
		}
		else if (excep instanceof SmtpException) {
			// TODO: handle SmtpException from SmtpConnection. recycle the
			// thread for now
			JbMain.getEventAlert().issueExcepAlert(JbMain.QUEUE_SVR,
					"QueueReaderThread caught SmtpException, server "
							+ queueReaderVo.getQueueName(), excep);
		}
		else if (excep instanceof Exception) {
			// unchecked Exception, stop the queue server
			logger
					.fatal("Stopping the queue server due to unchecked Exception "
							+ excep);
			keepRunning = false;
			JbMain.getEventAlert().issueExcepAlert(JbMain.QUEUE_SVR,
					"QueueReaderThread caught Unchecked Exception, stopping server "
							+ queueReaderVo.getQueueName(), excep);
		}
	}

	/**
	 * implement JbEventListener methods
	 * 
	 * @param e -
	 *            event
	 */
	public void errorOccured(JbEvent e) {
		Exception excep = e.getException();
		logger.error("Error caught by QueueReaderThread. " + excep);
	}

	public static void main(String[] args) {
		QueueReader reader = (QueueReader)JbMain.getBatchAppContext().getBean("queueReader");
		try {
			reader.start();
			reader.join();
		}
		catch (Exception e) {
			logger.error(e);
		}
		System.exit(0);
	}
	

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(JmsTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public QueueReaderVo getQueueReaderVo() {
		return queueReaderVo;
	}

}