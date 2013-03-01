package com.legacytojava.jbatch;

import java.util.Date;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.legacytojava.message.vo.TimerServerVo;

/**
 * load application processor, and call its process() to perform real tasks.
 */
class TimerThread extends TimerTask implements java.io.Serializable {
	private static final long serialVersionUID = -325424710879386189L;
	static final Logger logger = Logger.getLogger(TimerThread.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final TimerServerVo timerServerVo;
	final Processor processor;
	final TimerServer tmrServer;

	String name = "";

	/**
	 * create a TimerThread instance
	 * 
	 * @param tmr_server -
	 *            reference of TimerServer
	 */
	TimerThread(TimerServer tmr_server) {
		tmrServer = tmr_server;
		timerServerVo = tmrServer.timerServerVo;
		processor = (Processor) JbMain.getBatchAppContext().getBean(tmrServer.processorName);
	}

	/**
	 * perform tasks
	 */
	public void run() {
		if (isDebugEnabled) {
			logger.debug("TimerThread running...");
		}
		Date start_tms = new Date();
		try {
			tmrServer.update(MetricsLogger.PROC_INPUT, 1);
			processor.process(this);
		}
		catch (InterruptedException e) {
			logger.error("InterruptedException caught", e);
		}
		catch (Exception e) {
			logger.error("Exception caught during serving timer", e);
			tmrServer.update(MetricsLogger.PROC_ERROR, 1);
		}
		finally {
			/* Message processed, accumulate processing time */
			long proc_time = new Date().getTime() - start_tms.getTime();
			tmrServer.update(MetricsLogger.PROC_TIME, (int) proc_time);
			logger.info("TimerThread ended. Time spent in milliseconds: " + proc_time);
		}
	}

	/**
	 * set task name
	 * 
	 * @param _name -
	 *            task name
	 */
	void setName(String _name) {
		name = _name;
	}

	/**
	 * retrieve task name
	 * 
	 * @return task name
	 */
	String getName() {
		return name;
	}
}