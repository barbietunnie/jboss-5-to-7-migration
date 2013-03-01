package com.legacytojava.message.vo.outbox;

import java.io.Serializable;
import java.sql.Timestamp;

import com.legacytojava.message.vo.BaseVo;

public class DeliveryStatusVo extends BaseVo implements Serializable {
	
	private static final long serialVersionUID = -3444704171152514919L;
	private long msgId = -1L;
	private long finalRecipientId = 1L;
	private String finalRecipient = null;
	private Long originalRecipientId = null;
	private String messageId = null;
	private String dsnStatus = null;
	private String dsnReason = null;
	private String dsnText = null;
	private String dsnRfc822 = null;
	private String deliveryStatus = null;
	private Timestamp addTime = null;
	
	public Timestamp getAddTime() {
		return addTime;
	}
	public void setAddTime(Timestamp addTime) {
		this.addTime = addTime;
	}
	public String getDeliveryStatus() {
		return deliveryStatus;
	}
	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
	public String getDsnReason() {
		return dsnReason;
	}
	public void setDsnReason(String dsnReason) {
		this.dsnReason = dsnReason;
	}
	public String getDsnRfc822() {
		return dsnRfc822;
	}
	public void setDsnRfc822(String dsnRfc822) {
		this.dsnRfc822 = dsnRfc822;
	}
	public String getDsnStatus() {
		return dsnStatus;
	}
	public void setDsnStatus(String dsnStatus) {
		this.dsnStatus = dsnStatus;
	}
	public String getDsnText() {
		return dsnText;
	}
	public void setDsnText(String dsnText) {
		this.dsnText = dsnText;
	}
	public String getFinalRecipient() {
		return finalRecipient;
	}
	public void setFinalRecipient(String finalRecipient) {
		this.finalRecipient = finalRecipient;
	}
	public long getFinalRecipientId() {
		return finalRecipientId;
	}
	public void setFinalRecipientId(long finalRecipientId) {
		this.finalRecipientId = finalRecipientId;
	}
	public Long getOriginalRecipientId() {
		return originalRecipientId;
	}
	public void setOriginalRecipientId(Long originalRecipientId) {
		this.originalRecipientId = originalRecipientId;
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
}
