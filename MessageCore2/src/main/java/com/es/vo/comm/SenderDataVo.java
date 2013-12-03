package com.es.vo.comm;

import java.io.Serializable;

import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;

public class SenderDataVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -16795349179937720L;
	private String senderId = "";
	private String senderName = "";
	private String senderType = null;
	private String domainName = "";
	private String irsTaxId = null;
	private String webSiteUrl = null;
	private String saveRawMsg = CodeType.YES_CODE.getValue();
	private String contactName = null;
	private String contactPhone = null;
	private String contactEmail = "";
	private String securityEmail = "";
	private String custcareEmail = "";
	private String rmaDeptEmail = "";
	private String spamCntrlEmail = "";
	private String chaRspHndlrEmail = "";
	private String embedEmailId = "";
	private String returnPathLeft = "";
	private String useTestAddr = CodeType.NO.getValue();
	private String testFromAddr = null; 
	private String testToAddr = null;
	private String testReplytoAddr = null;
	private String isVerpEnabled = CodeType.NO.getValue();
	private String verpSubDomain = null;
	private String verpInboxName = null;
	private String verpRemoveInbox = null;
	private String systemId = "";
	private String systemKey = "";
	private String origSenderId = null;
	
	/** define components for UI */
	public boolean getUseTestAddress() {
		return CodeType.YES.getValue().equalsIgnoreCase(useTestAddr);
	}
	
	public boolean getIsVerpAddressEnabled() {
		return CodeType.YES.getValue().equalsIgnoreCase(isVerpEnabled);
	}
	
	public boolean getIsEmbedEmailId() {
		return CodeType.YES.getValue().equalsIgnoreCase(embedEmailId);
	}
	
	public boolean getIsSystemSender() {
		return Constants.DEFAULT_SENDER_ID.equalsIgnoreCase(senderId);
	}
	/** end of UI components */
	
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public String getSenderType() {
		return senderType;
	}
	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getIrsTaxId() {
		return irsTaxId;
	}
	public void setIrsTaxId(String irsTaxId) {
		this.irsTaxId = irsTaxId;
	}
	public String getSaveRawMsg() {
		return saveRawMsg;
	}
	public void setSaveRawMsg(String saveRawMsg) {
		this.saveRawMsg = saveRawMsg;
	}
	public String getWebSiteUrl() {
		return webSiteUrl;
	}
	public void setWebSiteUrl(String webSiteUrl) {
		this.webSiteUrl = webSiteUrl;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactPhone() {
		return contactPhone;
	}
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public String getContactEmail() {
		return contactEmail;
	}
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
	public String getSecurityEmail() {
		return securityEmail;
	}
	public void setSecurityEmail(String securityEmail) {
		this.securityEmail = securityEmail;
	}
	public String getCustcareEmail() {
		return custcareEmail;
	}
	public void setCustcareEmail(String custcareEmail) {
		this.custcareEmail = custcareEmail;
	}
	public String getRmaDeptEmail() {
		return rmaDeptEmail;
	}
	public void setRmaDeptEmail(String rmaDeptEmail) {
		this.rmaDeptEmail = rmaDeptEmail;
	}
	public String getSpamCntrlEmail() {
		return spamCntrlEmail;
	}
	public void setSpamCntrlEmail(String spamCntrlEmail) {
		this.spamCntrlEmail = spamCntrlEmail;
	}
	public String getChaRspHndlrEmail() {
		return chaRspHndlrEmail;
	}
	public void setChaRspHndlrEmail(String chaRspHndlrEmail) {
		this.chaRspHndlrEmail = chaRspHndlrEmail;
	}
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	public String getSystemKey() {
		return systemKey;
	}
	public void setSystemKey(String systemKey) {
		this.systemKey = systemKey;
	}
	public String getEmbedEmailId() {
		return embedEmailId;
	}
	public void setEmbedEmailId(String embedEmailId) {
		this.embedEmailId = embedEmailId;
	}
	public String getUseTestAddr() {
		return useTestAddr;
	}
	public void setUseTestAddr(String useTestAddr) {
		this.useTestAddr = useTestAddr;
	}
	public String getTestFromAddr() {
		return testFromAddr;
	}
	public void setTestFromAddr(String testFromAddr) {
		this.testFromAddr = testFromAddr;
	}
	public String getTestToAddr() {
		return testToAddr;
	}
	public void setTestToAddr(String testToAddr) {
		this.testToAddr = testToAddr;
	}
	public String getTestReplytoAddr() {
		return testReplytoAddr;
	}
	public void setTestReplytoAddr(String testReplytoAddr) {
		this.testReplytoAddr = testReplytoAddr;
	}
	public String getIsVerpEnabled() {
		return isVerpEnabled;
	}
	public void setIsVerpEnabled(String isVerpEnabled) {
		this.isVerpEnabled = isVerpEnabled;
	}
	public String getVerpSubDomain() {
		return verpSubDomain;
	}
	public void setVerpSubDomain(String verpSubDomain) {
		this.verpSubDomain = verpSubDomain;
	}
	public String getVerpInboxName() {
		return verpInboxName;
	}
	public void setVerpInboxName(String verpInboxName) {
		this.verpInboxName = verpInboxName;
	}
	public String getVerpRemoveInbox() {
		return verpRemoveInbox;
	}
	public void setVerpRemoveInbox(String verpRemoveInbox) {
		this.verpRemoveInbox = verpRemoveInbox;
	}
	public String getOrigSenderId() {
		return origSenderId;
	}
	public void setOrigSenderId(String origSenderId) {
		this.origSenderId = origSenderId;
	}
	public String getReturnPathLeft() {
		return returnPathLeft;
	}
	public void setReturnPathLeft(String returnPathLeft) {
		this.returnPathLeft = returnPathLeft;
	}
}