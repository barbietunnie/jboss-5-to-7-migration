package com.es.ejb.subscriber;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.es.ejb.vo.BaseWsVo;
import com.es.tomee.util.TimestampAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SubscriptionVo")
public class SubscriptionVo extends BaseWsVo {
	private static final long serialVersionUID = 5490896686324913331L;
	
	@XmlElement(required=true)
	private int emailAddrRowId;
	private String address;
	@XmlElement(required=true)
	private int mailingListRowId;
	private String listId;
	@XmlElement(required=true)
	private boolean isSubscribed = true;
	private Boolean isOptIn;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp CreateTime;
	@XmlElement(required=true)
	private int sentCount = 0;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastSentTime;
	@XmlElement(required=true)
	private int openCount = 0;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastOpenTime;
	@XmlElement(required=true)
	private int clickCount = 0;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastClickTime;
	
	public int getEmailAddrRowId() {
		return emailAddrRowId;
	}
	public void setEmailAddrRowId(int emailAddrRowId) {
		this.emailAddrRowId = emailAddrRowId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getMailingListRowId() {
		return mailingListRowId;
	}
	public void setMailingListRowId(int mailingListRowId) {
		this.mailingListRowId = mailingListRowId;
	}
	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public boolean isSubscribed() {
		return isSubscribed;
	}
	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}
	public Boolean getIsOptIn() {
		return isOptIn;
	}
	public void setIsOptIn(Boolean isOptIn) {
		this.isOptIn = isOptIn;
	}
	public Timestamp getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
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

}
