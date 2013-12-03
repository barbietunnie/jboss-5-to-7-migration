package com.es.vo.address;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.data.constant.CodeType;
import com.es.data.constant.StatusId;
import com.es.vo.comm.BaseVoWithRowId;

public class MailingListVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = 3125836080929462525L;
	private String listId = "";
	private String displayName = null;
	private String acctUserName = "";
	private String domainName = ""; // from Sender_Data table
	private String description = null;
	private String senderId = "";
	private String isBuiltIn = CodeType.NO_CODE.getValue();
	private String isSendText = null;
	private Timestamp CreateTime = null;
	private String listMasterEmailAddr = null;
	private String origListId = null;
	
	// used when join with Subscription table
	private String subscribed = null;
	private Integer sentCount = null;
	private Integer openCount = null;
	private Integer clickCount = null;
	
	/** define components for UI */
	public boolean isActive() {
		return StatusId.ACTIVE.getValue().equalsIgnoreCase(getStatusId());
	}
	
	public boolean getIsSubscribed() {
		return CodeType.YES_CODE.getValue().equals(subscribed);
	}
	
	public boolean getIsBuiltInList() {
		return CodeType.YES_CODE.getValue().equals(isBuiltIn);
	}
	/** end of UI */
	
	public String getEmailAddr() {
		return acctUserName + "@" + domainName;
	}
	
	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAcctUserName() {
		return acctUserName;
	}
	public void setAcctUserName(String acctUserName) {
		this.acctUserName = acctUserName;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getIsBuiltIn() {
		return isBuiltIn;
	}
	public void setIsBuiltIn(String isBuiltIn) {
		this.isBuiltIn = isBuiltIn;
	}
	public String getIsSendText() {
		return isSendText;
	}

	public void setIsSendText(String isSendText) {
		this.isSendText = isSendText;
	}

	public Timestamp getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getListMasterEmailAddr() {
		return listMasterEmailAddr;
	}
	public void setListMasterEmailAddr(String listMasterEmailAddr) {
		this.listMasterEmailAddr = listMasterEmailAddr;
	}
	public String getOrigListId() {
		return origListId;
	}
	public void setOrigListId(String origListId) {
		this.origListId = origListId;
	}

	public String getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(String subscribed) {
		this.subscribed = subscribed;
	}

	public Integer getSentCount() {
		return sentCount;
	}

	public void setSentCount(Integer sentCount) {
		this.sentCount = sentCount;
	}

	public Integer getOpenCount() {
		return openCount;
	}

	public void setOpenCount(Integer openCount) {
		this.openCount = openCount;
	}

	public Integer getClickCount() {
		return clickCount;
	}

	public void setClickCount(Integer clickCount) {
		this.clickCount = clickCount;
	}
}