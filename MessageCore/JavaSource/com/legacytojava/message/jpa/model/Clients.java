package com.legacytojava.message.jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.legacytojava.message.constant.Constants;

@Entity
@Table(name="Clients")
public class Clients extends BaseModel implements Serializable {
	private static final long serialVersionUID = 8789436921442107499L;

	//@Index(name="ClientId")
	@Column(name="clientId", unique=true, nullable=false, length=16)
	private String clientId = "";

	@Column(length=40, nullable=false)
	private String clientName = "";
	@Column(length=1, columnDefinition="char")
	private String clientType = null;
	@Column(length=100, nullable=false)
	private String domainName = "";
	@Column(length=1, nullable=false, columnDefinition="char")
	private String statusId = "";
	@Column(length=10)
	private String irsTaxId = null;
	@Column(length=100)
	private String webSiteUrl = null;
	@Column(length=1, nullable=false, columnDefinition="char")
	private String saveRawMsg = Constants.YES_CODE;
	@Column(length=60)
	private String contactName = null;
	@Column(length=18)
	private String contactPhone = null;
	@Column(length=255, nullable=false)
	private String contactEmail = "";
	@Column(length=255, nullable=false)
	private String securityEmail = "";
	@Column(length=255, nullable=false)
	private String custcareEmail = "";
	@Column(length=255, nullable=false)
	private String rmaDeptEmail = "";
	@Column(length=255, nullable=false)
	private String spamCntrlEmail = "";
	@Column(length=255, nullable=false)
	private String chaRspHndlrEmail = "";
	@Column(length=3, nullable=false)
	private String embedEmailId = "";
	@Column(length=50, nullable=false)
	private String returnPathLeft = "";
	@Column(length=3, nullable=false)
	private String useTestAddr = Constants.NO;
	@Column(length=255)
	private String testFromAddr = null; 
	@Column(length=255)
	private String testToAddr = null;
	@Column(length=255)
	private String testReplytoAddr = null;
	@Column(length=3, nullable=false)
	private String isVerpEnabled = Constants.NO;
	@Column(length=50)
	private String verpSubDomain = null;
	@Column(length=50)
	private String verpInboxName = null;
	@Column(length=50)
	private String verpRemoveInbox = null;
	@Column(length=40, nullable=false)
	private String systemId = "";
	@Column(length=30)
	private String systemKey = null;
	@Column(length=1, columnDefinition="char")
	private String dikm = null;
	@Column(length=1, columnDefinition="char")
	private String domainKey = null;
	@Column(length=200)
	private String keyFilePath = null;
	@Column(length=1, columnDefinition="char")
	private String spf = null;

	@Transient
	private String origClientId = null;
	
	public Clients() {
		// must have a no-argument constructor
	}

	/** define components for UI */
	public boolean getUseTestAddress() {
		return Constants.YES.equalsIgnoreCase(useTestAddr);
	}
	
	public boolean getIsVerpAddressEnabled() {
		return Constants.YES.equalsIgnoreCase(isVerpEnabled);
	}
	
	public boolean getIsEmbedEmailId() {
		return Constants.YES.equalsIgnoreCase(embedEmailId);
	}
	
	public boolean getIsSystemClient() {
		return Constants.DEFAULT_CLIENTID.equalsIgnoreCase(clientId);
	}
	/** end of UI components */
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getClientType() {
		return clientType;
	}
	public void setClientType(String clientType) {
		this.clientType = clientType;
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
	public String getOrigClientId() {
		return origClientId;
	}
	public void setOrigClientId(String origClientId) {
		this.origClientId = origClientId;
	}
	public String getReturnPathLeft() {
		return returnPathLeft;
	}
	public void setReturnPathLeft(String returnPathLeft) {
		this.returnPathLeft = returnPathLeft;
	}

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	public String getSystemKey() {
		return systemKey;
	}

	public void setSystemKey(String systemKey) {
		this.systemKey = systemKey;
	}

	public String getDikm() {
		return dikm;
	}

	public void setDikm(String dikm) {
		this.dikm = dikm;
	}

	public String getDomainKey() {
		return domainKey;
	}

	public void setDomainKey(String domainKey) {
		this.domainKey = domainKey;
	}

	public String getKeyFilePath() {
		return keyFilePath;
	}

	public void setKeyFilePath(String keyFilePath) {
		this.keyFilePath = keyFilePath;
	}

	public String getSpf() {
		return spf;
	}

	public void setSpf(String spf) {
		this.spf = spf;
	}
}