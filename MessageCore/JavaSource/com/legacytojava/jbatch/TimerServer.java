package com.legacytojava.jbatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.timer.TimerServerDao;
import com.legacytojava.message.vo.TimerServerVo;

/**
 * this class provide a Timer manager that initialize and schedule TimerTasks
 * <b>Timer Job is responsible of scheduling timer tasks
 * 
 * <pre>
 * 1) create a Timer Job based on timer properties
 * 2) create a MetricsLogger instance for metrics logging
 * 3) schedule tasks to run
 * </pre>
 */
class TimerServer extends Timer implements java.io.Serializable {
	private static final long serialVersionUID = -1541104444206505520L;
	static final Logger logger = Logger.getLogger(TimerServer.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final TimerServerVo timerServerVo;
	final String processorName;
	final List<TimerThread> taskPool = new ArrayList<TimerThread>();
		// used to cancel tasks

	// the next two fields to be set up by Main class
	protected String serverName;
	protected String serverId;
	protected final MetricsLogger metricsLogger;

	private int MAX_CLIENTS = 0;
	private int INTERVAL = 0;
	private int DELAY = 0;

	private final Date startTime;

	/**
	 * create a TimerServer instance
	 * 
	 * @param props -
	 *            properties
	 */
	TimerServer(TimerServerVo timerServerVo) {
		this.timerServerVo = timerServerVo;
		logger.info(this.timerServerVo);

		processorName = timerServerVo.getProcessorName();
		if (processorName == null) {
			throw new IllegalArgumentException("processorName must be valued in timerServerVo");
		}
		MAX_CLIENTS = timerServerVo.getThreads();
		if (MAX_CLIENTS < 0) MAX_CLIENTS = 5;
		INTERVAL = timerServerVo.getTimerInterval();
		DELAY = timerServerVo.getInitialDelay();

		String unit = timerServerVo.getTimerIntervalUnit();
		if (unit == null) unit = "minute";
		if ("minute".equalsIgnoreCase(unit)) {
			INTERVAL *= 60;
		}

		startTime = timerServerVo.getStartTime();

		metricsLogger = (MetricsLogger) JbMain.getBatchAppContext().getBean("metricsLogger");
		metricsLogger.getMetricsData().setServerType(JbMain.TIMER_SVR_TYPE);
	}

	public static void main(String[] args) {
		TimerServerDao timerDao = (TimerServerDao) JbMain.getBatchAppContext().getBean(
				"timerServerDao");
		List<TimerServerVo> timerList = timerDao.getAll(true);

		if (timerList==null) return;
		for (Iterator<TimerServerVo> it=timerList.iterator(); it.hasNext(); ) {
			TimerServerVo vo = it.next();
			TimerServer server = new TimerServer(vo);
			try {
				server.schedule();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	/**
	 * release resources
	 * 
	 * @throws Throwable
	 *             if any error
	 */
	protected void finalize() throws Throwable {
		taskPool.clear();
		super.finalize();
	}

	/**
	 * retrieve timer tasks status
	 * 
	 * @return status
	 */
	List<String> getStatus() {
		List<String> v = new ArrayList<String>();
		if (MAX_CLIENTS > 0) {
			v.add("TimerServer: Number of Clients=" + MAX_CLIENTS + ", Interval=" + INTERVAL
					+ ", Delay=" + DELAY);
		}
		if (startTime != null) {
			v.add("Start Time=" + startTime);
		}
		if (taskPool != null) {
			StringBuffer sb = new StringBuffer();
			if (taskPool.size() > 0) {
				TimerThread t = (TimerThread) taskPool.get(0);
				sb.append(t.getName());
			}
			for (int i = 1; i < taskPool.size(); i++) {
				TimerThread t = (TimerThread) taskPool.get(i);
				sb.append(", " + t.getName());
			}
			v.add(sb.toString());
		}
		v.add(timerServerVo.toString());
		return v;
	}

	/**
	 * schedule tasks to run
	 */
	void schedule() {
		logger.info("Entering TimerServer.schedule() method...");
		try {
			logger.info("Creating Timer Threads, # of Threads: " + MAX_CLIENTS);
			// create timers and schedule them
			for (int i = 0; i < MAX_CLIENTS; i++) {
				// input count shows number of timer tasks scheduled
				update(MetricsLogger.PROC_INPUT, 1);
				TimerThread tmrThread = new TimerThread(this);
				tmrThread.setName("TimerTask-" + (i + 1));
				taskPool.add(tmrThread);
				if (startTime != null && INTERVAL > 0)
					schedule(tmrThread, startTime, INTERVAL * 1000);
				else if (startTime != null)
					schedule(tmrThread, startTime);
				else if (INTERVAL > 0)
					schedule(tmrThread, DELAY, INTERVAL * 1000);
				else
					schedule(tmrThread, DELAY);
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during initializing TimerThreads", e);
			update(MetricsLogger.PROC_ERROR, 1);
		}
		logger.info("TimerServer.schedule() ended.");
	}

	/**
	 * cancel timer tasks
	 */
	public void cancel() {
		super.cancel();
		for (int i = 0; i < taskPool.size(); i++) {
			TimerThread tmrThread = (TimerThread) taskPool.get(i);
			tmrThread.cancel();
		}
	}

	/**
	 * update MetricsLogger
	 * 
	 * @param type -
	 *            type of metrics: input, output or error
	 * @param count -
	 *            occurrences
	 */
	void update(int type, int count) {
		metricsLogger.getMetricsData().update(type, count);
	}

	/**
	 * update MetricsLogger
	 * 
	 * @param type -
	 *            type of metrics: input, output or error
	 * @param count -
	 *            occurrences
	 * @param records -
	 *            records processed during the period
	 */
	void update(int type, int count, int records) {
		metricsLogger.getMetricsData().update(type, count, records);
	}

	String getServerId() {
		return serverId;
	}

	void setServerId(String serverId) {
		this.serverId = serverId;
		metricsLogger.getMetricsData().setServerId(serverId);
	}

	String getServerName() {
		return serverName;
	}

	void setServerName(String serverName) {
		this.serverName = serverName;
		metricsLogger.getMetricsData().setServerName(serverName);
	}

	public TimerServerVo getTimerServerVo() {
		return timerServerVo;
	}
}