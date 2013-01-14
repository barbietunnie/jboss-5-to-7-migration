package com.legacytojava.jbatch;

import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * scheduled task that updates metrics tables
 */
class MetricsTask extends TimerTask implements java.io.Serializable {
	private static final long serialVersionUID = -1798573102616030282L;
	static final Logger logger = Logger.getLogger(MetricsTask.class);

	/**
	 * perform update.
	 */
	public void run() {
		try {
			JbMain.updateMetrics();
		}
		catch (Exception e) {
			logger.error("Exception caught during serving timer", e);
		}
	}
}