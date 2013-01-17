package com.legacytojava.jbatch;

import java.io.IOException;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

import com.legacytojava.message.bo.mailreader.DuplicateCheckDao;
import com.legacytojava.message.bo.mailreader.DuplicateCheckJdbcDao;

/**
 * test processor
 */
public class TimerProcessor extends RunnableProcessor {
	static final Logger logger = Logger.getLogger(TimerProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private DuplicateCheckDao duplicateCheck = null;

	// must use constructor without any parameters
	public TimerProcessor() {
		logger.info("Entering Constructor...");
	}

	public void process(Object req) throws JMSException, IOException {
		logger.info("Entering process() method...");
		if (req == null) {
			logger.warn("request object is null.");
			return;
		}
		if (req instanceof java.util.TimerTask) {
			logger.info("process(): A Timertask received.");
			// purge aged records from MSGIDDUP table
			try {
				getDuplicateCheck().process(req);
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				throw new IOException(e.toString());
			}
		}
		else {
			logger.info("process(): received a " + req.getClass().getName());
		}
	}

	private DuplicateCheckDao getDuplicateCheck() {
		if (duplicateCheck == null) {
			duplicateCheck = (DuplicateCheckDao) SpringUtil.getDaoAppContext().getBean("duplicateCheck");
			((DuplicateCheckJdbcDao)duplicateCheck).setPurgeAfter("24");
		}
		return duplicateCheck;
	}
}