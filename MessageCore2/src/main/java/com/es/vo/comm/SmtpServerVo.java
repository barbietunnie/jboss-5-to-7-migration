package com.es.vo.comm;

import java.io.Serializable;

public class SmtpServerVo extends ServerBaseVo implements Serializable {
	private static final long serialVersionUID = -1343713327747984527L;
	private String smtpHost = "";
	private int smtpPort = -1;
	private String useSsl = "";
	private String useAuth = null;
	private String userId = ""; 
	private String userPswd = "";
	private String persistence = "";
	private String serverType = null;
	private int retries = -1;
	private int retryFreq = -1;
	private Integer alertAfter = null;
	private String alertLevel = null;
	
	public boolean isSsl() {
		return ("yes".equalsIgnoreCase(useSsl));
	}
	
	public Integer getAlertAfter() {
		return alertAfter;
	}
	public void setAlertAfter(Integer alertAfter) {
		this.alertAfter = alertAfter;
	}
	public String getAlertLevel() {
		return alertLevel;
	}
	public void setAlertLevel(String alertLevel) {
		this.alertLevel = alertLevel;
	}
	public String getPersistence() {
		return persistence;
	}
	public void setPersistence(String persistence) {
		this.persistence = persistence;
	}
	public int getRetries() {
		return retries;
	}
	public void setRetries(int retries) {
		this.retries = retries;
	}
	public int getRetryFreq() {
		return retryFreq;
	}
	public void setRetryFreq(int retryFreq) {
		this.retryFreq = retryFreq;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public String getSmtpHost() {
		return smtpHost;
	}
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	public int getSmtpPort() {
		return smtpPort;
	}
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserPswd() {
		return userPswd;
	}
	public void setUserPswd(String userPswd) {
		this.userPswd = userPswd;
	}
	public String getUseSsl() {
		return useSsl;
	}
	public void setUseSsl(String useSsl) {
		this.useSsl = useSsl;
	}
	public String getUseAuth() {
		return useAuth;
	}
	public void setUseAuth(String useAuth) {
		this.useAuth = useAuth;
	}
}