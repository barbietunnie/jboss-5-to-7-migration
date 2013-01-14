package com.legacytojava.message.vo.inbox;

import java.io.Serializable;

import com.legacytojava.message.vo.BaseVo;

public class RfcFieldsVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -993729602203029412L;
	private long msgId = -1;
	private String rfcType = null;
	private String rfcStatus = null;
	private String rfcAction = null;
	private String finalRcpt = null;
	private Long finalRcptId = null;
	private String origRcpt = null;
	private String origMsgSubject = null;
	private String messageId = null;
	private String dsnText = null;
	private String dsnRfc822 = null;
	private String dlvrStatus = null;
	
	public String getDlvrStatus() {
		return dlvrStatus;
	}
	public void setDlvrStatus(String dlvrStatus) {
		this.dlvrStatus = dlvrStatus;
	}
	public String getDsnRfc822() {
		return dsnRfc822;
	}
	public void setDsnRfc822(String dsnRfc822) {
		this.dsnRfc822 = dsnRfc822;
	}
	public String getDsnText() {
		return dsnText;
	}
	public void setDsnText(String dsnText) {
		this.dsnText = dsnText;
	}
	public String getFinalRcpt() {
		return finalRcpt;
	}
	public void setFinalRcpt(String finalRcpt) {
		this.finalRcpt = finalRcpt;
	}
	public Long getFinalRcptId() {
		return finalRcptId;
	}
	public void setFinalRcptId(Long finalRcptId) {
		this.finalRcptId = finalRcptId;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public String getOrigMsgSubject() {
		return origMsgSubject;
	}
	public void setOrigMsgSubject(String origMsgSubject) {
		this.origMsgSubject = origMsgSubject;
	}
	public String getOrigRcpt() {
		return origRcpt;
	}
	public void setOrigRcpt(String origRcpt) {
		this.origRcpt = origRcpt;
	}
	public String getRfcAction() {
		return rfcAction;
	}
	public void setRfcAction(String rfcAction) {
		this.rfcAction = rfcAction;
	}
	public String getRfcStatus() {
		return rfcStatus;
	}
	public void setRfcStatus(String rfcStatus) {
		this.rfcStatus = rfcStatus;
	}
	public String getRfcType() {
		return rfcType;
	}
	public void setRfcType(String rfcType) {
		this.rfcType = rfcType;
	}
}