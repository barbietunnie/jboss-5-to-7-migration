package com.legacytojava.jbatch;

import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * watch status of started servers and jobs. restart failed servers and jobs.
 */
class ServerWatch extends TimerTask {
	static final Logger logger = Logger.getLogger(ServerWatch.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final JbMain jbMain;

	/**
	 * create a ServerWatch instance
	 * 
	 * @param _aMain -
	 *            reference to JbMain instance
	 */
	ServerWatch(JbMain _aMain) {
		jbMain = _aMain;
	}

	/**
	 * perform tasks
	 */
	public void run() {
		// check queue servers
		if (jbMain.queueThreadFailed.size() > 0) {
			try {
				jbMain.startFailedQueueServers();
			}
			catch (Exception e) {
				logger.error("Exception caught during restarting Queue Servers", e);
			}
		}

		// check socket servers
		if (jbMain.socketThreadFailed.size() > 0) {
			try {
				jbMain.startFailedSocketServers();
			}
			catch (Exception e) {
				logger.error("Exception caught during restarting Socket Servers", e);
			}
		}

		// check timers
		if (jbMain.timerThreadFailed.size() > 0) {
			try {
				jbMain.startFailedTimerTasks();
			}
			catch (Exception e) {
				logger.error("Exception caught during restarting Timer Servers", e);
			}
		}

		// check Mail Readers
		if (jbMain.mailboxThreadFailed.size() > 0) {
			try {
				jbMain.startFailedMailReaders();
			}
			catch (Exception e) {
				logger.error("Exception caught during restarting Mail Readers", e);
			}
		}
	}
}