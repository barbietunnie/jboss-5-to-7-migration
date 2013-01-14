package com.legacytojava.message.vo;

import java.io.Serializable;

public class ServerBaseVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 8994725324194791222L;
	private String serverName = "";
	private String description = null; 
	private boolean allowExtraWorkers = false;
	private String priority;
	private int threads = 1;
	private String processorName = "";
	private int messageCount = 0;

	public String getProcessorName() {
		return processorName;
	}

	public void setProcessorName(String processorName) {
		this.processorName = processorName;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public boolean isAllowExtraWorkers() {
		return allowExtraWorkers;
	}

	public void setAllowExtraWorkers(boolean allowExtraWorkers) {
		this.allowExtraWorkers = allowExtraWorkers;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
