package com.es.vo.address;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.core.util.StringUtil;
import com.es.data.constant.CodeType;
import com.es.vo.comm.BaseVo;

public class SubscriptionVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 5343435037125506312L;
	private long emailAddrId = -1;
	private String listId = "";
	private String subscribed = "";
	private Timestamp CreateTime = null;
	private int sentCount = 0;
	private Timestamp lastSentTime = null;
	private int openCount = 0;
	private Timestamp lastOpenTime = null;
	private int clickCount = 0;
	private Timestamp lastClickTime = null;
	
	// used when joining Email_Address table to get Email Address
	private String acceptHtml = "";
	private String emailAddr = null;
	// used when joining Subscriber table to get subscriber name
	private String firstName = null;
	private String lastName = null;
	private String middleName = null;
	
	/** define components for UI */
	public String getEmailAddrShort() {
		return StringUtil.cutWithDots(emailAddr, 100);
	}
	
	public String getSubscribedDesc() {
		return CodeType.NO_CODE.getValue().equals(subscribed) ? CodeType.NO.getValue() : CodeType.YES.getValue();
	}
	
	public String getAcceptHtmlDesc() {
		return CodeType.NO_CODE.getValue().equals(acceptHtml) ? CodeType.NO.getValue() : CodeType.YES.getValue();
	}
	
	public String getCustomerName() {
		return (firstName == null ? "" : firstName + " ") + (lastName == null ? "" : lastName);
	}
	/** end of UI */
	
	public long getEmailAddrId() {
		return emailAddrId;
	}
	public void setEmailAddrId(long emailAddrId) {
		this.emailAddrId = emailAddrId;
	}
	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public String getSubscribed() {
		return subscribed;
	}
	public void setSubscribed(String subscribed) {
		this.subscribed = subscribed;
	}
	public Timestamp getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}
	public String getEmailAddr() {
		return emailAddr;
	}
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}
	public String getAcceptHtml() {
		return acceptHtml;
	}
	public void setAcceptHtml(String acceptHtml) {
		this.acceptHtml = acceptHtml;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public int getSentCount() {
		return sentCount;
	}

	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}

	public Timestamp getLastSentTime() {
		return lastSentTime;
	}

	public void setLastSentTime(Timestamp lastSentTime) {
		this.lastSentTime = lastSentTime;
	}

	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}

	public Timestamp getLastClickTime() {
		return lastClickTime;
	}

	public void setLastClickTime(Timestamp lastClickTime) {
		this.lastClickTime = lastClickTime;
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
}