package com.es.vo.inbox;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.data.constant.MailingListDeliveryType;
import com.es.vo.comm.BaseVo;

public class MsgClickCountVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 8405577701544401054L;
	private long msgId = -1;
	private String listId = "";
	private String deliveryOption = MailingListDeliveryType.ALL_ON_LIST.getValue();
	private int sentCount = 0;
	private int openCount = 0;
	private int clickCount = 0;
	private Timestamp lastOpenTime = null;
	private Timestamp lastClickTime = null;
	private Timestamp startTime = null;
	private Timestamp endTime = null;
	private int unsubscribeCount = 0;
	private int complaintCount = 0;
	private int referralCount = 0;
	
	/** define components for UI */
	
	public String getDeliveryOptionDesc() {
		if (MailingListDeliveryType.SUBSCRIBERS_ONLY.getValue().equals(deliveryOption)) {
			return "Subscribers only";
		}
		else if (MailingListDeliveryType.PROSPECTS_ONLY.getValue().equals(deliveryOption)) {
			return "Prospects only";
		}
		else {
			return "All on list";
		}
	}
	
	/** end of UI */
	
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public int getSentCount() {
		return sentCount;
	}
	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}
	public int getClickCount() {
		return clickCount;
	}
	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}
	public int getOpenCount() {
		return openCount;
	}
	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}
	public Timestamp getLastOpenTime() {
		return lastOpenTime;
	}
	public void setLastOpenTime(Timestamp lastOpenTime) {
		this.lastOpenTime = lastOpenTime;
	}
	public Timestamp getLastClickTime() {
		return lastClickTime;
	}
	public void setLastClickTime(Timestamp lastClickTime) {
		this.lastClickTime = lastClickTime;
	}
	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public String getDeliveryOption() {
		return deliveryOption;
	}
	public void setDeliveryOption(String deliveryOption) {
		this.deliveryOption = deliveryOption;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
	public int getUnsubscribeCount() {
		return unsubscribeCount;
	}
	public void setUnsubscribeCount(int unsubscribeCount) {
		this.unsubscribeCount = unsubscribeCount;
	}
	public int getComplaintCount() {
		return complaintCount;
	}
	public void setComplaintCount(int complaintCount) {
		this.complaintCount = complaintCount;
	}

	public int getReferralCount() {
		return referralCount;
	}

	public void setReferralCount(int referralCount) {
		this.referralCount = referralCount;
	}
}