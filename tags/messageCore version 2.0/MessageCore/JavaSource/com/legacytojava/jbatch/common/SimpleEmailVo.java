package com.legacytojava.jbatch.common;

public class SimpleEmailVo implements java.io.Serializable {
	private static final long serialVersionUID = 6923436836039056959L;
	private String fromAddr = null;
	private String toAddr = null;
	private String ccAddr = null;
	private String bccAddr = null;
	private String msgSubject = null;
	private String msgBody = null;
	private String xMailer = null;
	private String saveToFolder = null;
	
	public String getXMailer() {
		return xMailer;
	}
	public void setXMailer(String mailer) {
		xMailer = mailer;
	}
	public String getFromAddr() {
		return fromAddr;
	}
	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}
	public String getMsgBody() {
		return msgBody;
	}
	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}
	public String getMsgSubject() {
		return msgSubject;
	}
	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}
	public String getToAddr() {
		return toAddr;
	}
	public void setToAddr(String toAddr) {
		this.toAddr = toAddr;
	}
	public String getBccAddr() {
		return bccAddr;
	}
	public void setBccAddr(String bccAddr) {
		this.bccAddr = bccAddr;
	}
	public String getCcAddr() {
		return ccAddr;
	}
	public void setCcAddr(String ccAddr) {
		this.ccAddr = ccAddr;
	}
	public String getSaveToFolder() {
		return saveToFolder;
	}
	public void setSaveToFolder(String saveToFolder) {
		this.saveToFolder = saveToFolder;
	}
}
