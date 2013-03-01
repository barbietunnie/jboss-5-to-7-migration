package com.legacytojava.jbatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.legacytojava.message.vo.ServerBaseVo;

/**
 * provide common threading methods needed by JBatch servers
 */
public abstract class JbThread extends Thread implements java.io.Serializable {
	private static final long serialVersionUID = 4237270399644566809L;
	protected static final Logger logger = Logger.getLogger(JbThread.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();
	
	protected final ServerBaseVo serverBaseVo;
	protected final String LF = System.getProperty("line.separator", "\n");

	// the next two fields to be set up by Main class
	protected String serverName;
	protected String serverId;
	
	private final String serverType;
	private MetricsLogger metricsLogger = null;

	protected ExecutorService jbPool = null;
	protected int MAX_CLIENTS = 0;
	protected int MSG2PROC = 0;

	protected boolean ALLOW_EXTRA_WORKERS = true;
	protected volatile boolean keepRunning = true;

	/**
	 * invoked by subclass, to initialize fields that are used by all
	 * subclasses.
	 * 
	 * @param _props -
	 *            server properties
	 */
	protected JbThread(ServerBaseVo serverBaseVo, String serverType) {
		super();
		this.serverBaseVo = serverBaseVo;
		if (!serverBaseVo.isAllowExtraWorkers()) {
			ALLOW_EXTRA_WORKERS = false;
		}
		this.serverType = serverType;
	}

	protected MetricsLogger getMetricsLogger() {
		if (metricsLogger == null) {
			metricsLogger = (MetricsLogger)JbMain.getBatchAppContext().getBean("metricsLogger");
			metricsLogger.getMetricsData().setServerType(serverType);
		}
		return metricsLogger;
	}

	/**
	 * return Server status, invoked by MBean
	 * 
	 * @return a List containing server status
	 */
	protected List<String> getStatus() {
		List<String> v = new ArrayList<String>();
		if (jbPool != null) {
			v.add(jbPool.isTerminated() ? "Terminated" : "Alive");
		}
		v.add(serverBaseVo.toString());
		return v;
	}

	/**
	 * override finalize to release resources.
	 * @throws Throwable 
	 */
//	protected void finalize() throws Throwable {
//		super.finalize();
//	}

	void shutDownPool() {
		if (jbPool != null) { // clean up pool
			jbPool.shutdown();
			try {
				if (!jbPool.awaitTermination(JbMain.shutDownDelay, TimeUnit.SECONDS)) {
					jbPool.shutdownNow();
				}
			}
			catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				jbPool.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * update Metrics using MetricsLogger
	 * 
	 * @param type -
	 *            type of metrics: input, output or error
	 * @param count -
	 *            occurrences
	 */
	protected void update(int type, int count) {
		getMetricsLogger().getMetricsData().update(type, count);
	}

	/**
	 * update Metrics using MetricsLogger
	 * 
	 * @param type -
	 *            type of metrics: input, output or error
	 * @param count -
	 *            occurrences
	 * @param records -
	 *            number of records processed during the period
	 */
	protected void update(int type, int count, int records) {
		getMetricsLogger().getMetricsData().update(type, count, records);
	}

	String getServerId() {
		return serverId;
	}

	void setServerId(String serverId) {
		this.serverId = serverId;
		getMetricsLogger().getMetricsData().setServerId(serverId);
	}

	String getServerName() {
		return serverName;
	}

	void setServerName(String serverName) {
		this.serverName = serverName;
		getMetricsLogger().getMetricsData().setServerName(serverName);
	}

	public ServerBaseVo getServerBaseVo() {
		return serverBaseVo;
	}
}