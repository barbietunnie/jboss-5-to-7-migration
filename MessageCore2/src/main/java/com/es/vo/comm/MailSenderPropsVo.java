package com.es.vo.comm;

import java.io.Serializable;

public class MailSenderPropsVo extends ServerBaseVo implements Serializable {
	private static final long serialVersionUID = -6119296927649805655L;
	
	private String internalLoopback = "";
	private String externalLoopback = "";
	private String useTestAddr = "";
	private String testFromAddr = null; 
	private String testToAddr = "";
	private String testReplytoAddr = null;
	private String isVerpEnabled = "";
	
	public String getIsVerpEnabled() {
		return isVerpEnabled;
	}
	public void setIsVerpEnabled(String isVerpEnabled) {
		this.isVerpEnabled = isVerpEnabled;
	}
	public String getExternalLoopback() {
		return externalLoopback;
	}
	public void setExternalLoopback(String externalLoopback) {
		this.externalLoopback = externalLoopback;
	}
	public String getInternalLoopback() {
		return internalLoopback;
	}
	public void setInternalLoopback(String internalLoopback) {
		this.internalLoopback = internalLoopback;
	}
	public String getTestFromAddr() {
		return testFromAddr;
	}
	public void setTestFromAddr(String testFromAddr) {
		this.testFromAddr = testFromAddr;
	}
	public String getTestReplytoAddr() {
		return testReplytoAddr;
	}
	public void setTestReplytoAddr(String testReplytoAddr) {
		this.testReplytoAddr = testReplytoAddr;
	}
	public String getTestToAddr() {
		return testToAddr;
	}
	public void setTestToAddr(String testToAddr) {
		this.testToAddr = testToAddr;
	}
	public String getUseTestAddr() {
		return useTestAddr;
	}
	public void setUseTestAddr(String useTestAddr) {
		this.useTestAddr = useTestAddr;
	}
}