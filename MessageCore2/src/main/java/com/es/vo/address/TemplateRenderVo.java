package com.es.vo.address;

import java.io.Serializable;

public class TemplateRenderVo implements Serializable {
	private static final long serialVersionUID = 7218586114850127817L;
	private String subject = null;
	private String body = null;
	private String fromAddr = null;
	private String toAddr = null;
	private String ccAddr = null;
	private String bccAddr = null;
	private String senderId = null;
	private EmailTemplateVo emailTemplateVo = null;
	private MailingListVo mailingListVo = null;
	
	static final String LF = System.getProperty("line.separator", "\n");
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("From: " + fromAddr + LF);
		sb.append("To: " + toAddr + LF);
		if (ccAddr != null) {
			sb.append("Cc: " + ccAddr + LF);
		}
		if (bccAddr != null) {
			sb.append("Bcc: " + bccAddr + LF);
		}
		sb.append("SenderId: " + senderId + LF);
		sb.append("Subject: " + subject + LF);
		sb.append("Body: " + body + LF);
		return sb.toString();
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getFromAddr() {
		return fromAddr;
	}
	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}
	public String getToAddr() {
		return toAddr;
	}
	public void setToAddr(String toAddr) {
		this.toAddr = toAddr;
	}
	public String getCcAddr() {
		return ccAddr;
	}
	public void setCcAddr(String ccAddr) {
		this.ccAddr = ccAddr;
	}
	public String getBccAddr() {
		return bccAddr;
	}
	public void setBccAddr(String bccAddr) {
		this.bccAddr = bccAddr;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public EmailTemplateVo getEmailTemplateVo() {
		return emailTemplateVo;
	}
	public void setEmailTemplateVo(EmailTemplateVo emailTemplateVo) {
		this.emailTemplateVo = emailTemplateVo;
	}
	public MailingListVo getMailingListVo() {
		return mailingListVo;
	}
	public void setMailingListVo(MailingListVo mailingListVo) {
		this.mailingListVo = mailingListVo;
	}
}