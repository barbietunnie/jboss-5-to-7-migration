package com.legacytojava.jbatch;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

/**
 * @deprecated - replaced by QueueListener
 * 
 * load application processor, and call its process() to perform real tasks.
 */
class QueueReaderThread extends Thread implements java.io.Serializable {
	private static final long serialVersionUID = 7418816567428963622L;
	static final Logger logger = Logger.getLogger(QueueReaderThread.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private final QueueReader queServer;
	final JbEventBroker eventBroker;
	private TransactionStatus transactionStatus;
	
	// to be setup by QueueReader every time before run() is invoked.
	javax.jms.Message message = null;
	// end

	/**
	 * create QueueThread instance, initialize application processor class.
	 * 
	 * @param que_server -
	 *            QueueReader instance
	 * @throws Exception
	 *             if any error
	 */
	QueueReaderThread(QueueReader que_server) {
		logger.info("Entering QueueReaderThread Constructor...");
		queServer = que_server;
		eventBroker = queServer.eventBroker;
	}

	/**
	 * set MQ Message
	 * 
	 * @param msg -
	 *            a JMS message
	 */
	void setMessage(javax.jms.Message msg) {
		message = msg;
	}

	/**
	 * process request
	 */
	public void run() {
		if (isDebugEnabled)
			logger.debug("Entering run()...");
		Date start_tms = new Date();
		try {
			queServer.update(MetricsLogger.PROC_INPUT, 1);
			Processor processor = (Processor) JbMain.getBatchAppContext().getBean(
					queServer.getQueueReaderVo().getProcessorName());
			processor.process(message);
			queServer.getTransactionManager().commit(transactionStatus);
		}
		catch (InterruptedException e) {
			logger.error("InterruptedException caught", e);
			if (!transactionStatus.isCompleted()) {
				queServer.getTransactionManager().rollback(transactionStatus);
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during QueueReaderThread.run()", e);
			if (!transactionStatus.isCompleted()) {
				queServer.getTransactionManager().rollback(transactionStatus);
			}
			/* Exception occurred during process, increase error message count */
			queServer.update(MetricsLogger.PROC_ERROR, 1);
			eventBroker.putException(e);
		}
		finally {
			message = null;
			/* Message processed, update processing time */
			long proc_time = new Date().getTime() - start_tms.getTime();
			queServer.update(MetricsLogger.PROC_TIME, (int) proc_time);
			logger.info("QueueThread ended. Time spent in milliseconds: " + proc_time);
		}
	} // end of run()

	void setTransactionStatus(TransactionStatus transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}
}
