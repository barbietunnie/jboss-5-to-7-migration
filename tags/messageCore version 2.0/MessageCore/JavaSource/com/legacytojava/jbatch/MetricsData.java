package com.legacytojava.jbatch;

import java.io.Serializable;

public class MetricsData implements Serializable {
	private static final long serialVersionUID = -6412637880167602637L;
	private String serverType;
	private String serverName;
	private String serverId;
	private int inputCount;
	private int outputCount;
	private int errorCount;
	private int timeCount;
	private int workerCount;
	private int workerRecords;
	private int procRecords;
	
	private int totalInput = 0;
	
	public long getTotalInput() {
		return totalInput;
	}

	public MetricsData() {
		clear();
	}
	
	public void update(int type, int count) {
		update(type, count, 1);
	}
	
	public void update(int type, int count, int procRecords) {
		if (type==MetricsLogger.PROC_INPUT) {
			inputCount += count;
			totalInput += count;
		}
		else if (type==MetricsLogger.PROC_OUTPUT) outputCount += count;
		else if (type==MetricsLogger.PROC_ERROR) errorCount += count;
		else if (type==MetricsLogger.PROC_TIME) {
			timeCount += count;
			this.procRecords += procRecords;
		}
		else if (type==MetricsLogger.PROC_WORKER) {
			workerCount += count;
			workerRecords++;
		}
	}

	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	public int getErrorCount() {
		return errorCount;
	}
	public int getInputCount() {
		return inputCount;
	}
	public int getOutputCount() {
		return outputCount;
	}
	public int getTimeCount() {
		return timeCount;
	}
	public int getWorkerCount() {
		return workerCount;
	}
	public int getWorkerRecords() {
		return workerRecords;
	}
	public int getProcRecords() {
		return procRecords;
	}
	public final void clear() {
		serverType = "";
		serverName = "jbatch";
		serverId = "main";
		inputCount = 0;
		outputCount = 0;
		errorCount = 0;
		timeCount = 0;
		workerCount = 0;
		procRecords = 0;
		workerRecords = 0;
	}
}
