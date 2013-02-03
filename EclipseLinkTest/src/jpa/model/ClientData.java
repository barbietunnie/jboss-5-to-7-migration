package jpa.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.constant.Constants;

@Entity
@Table(name="client_data")
public class ClientData extends BaseModel implements Serializable {
	private static final long serialVersionUID = 8789436921442107499L;

	//@Index
	@Column(name="ClientId", unique=true, nullable=false, length=16)
	private String clientId = "";
	
	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="ClientData", orphanRemoval=true)
	private List<ClientVariable> clientVariables;

	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="ClientData", orphanRemoval=true)
	private IdTokens idTokens;
	
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
	private String isSaveRawMsg = Constants.Code.YES_CODE.getValue();
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
	private String isEmbedEmailId = "";
	@Column(length=50, nullable=false)
	private String returnPathLeft = "";
	@Column(length=3, nullable=false)
	private String isUseTestAddr = Constants.Code.NO.getValue();
	@Column(length=255)
	private String testFromAddr = null; 
	@Column(length=255)
	private String testToAddr = null;
	@Column(length=255)
	private String testReplytoAddr = null;
	@Column(length=3, nullable=false)
	private String isVerpEnabled = Constants.Code.NO.getValue();
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
	private String isDikm = null;
	@Column(length=1, columnDefinition="char")
	private String domainKey = null;
	@Column(length=200)
	private String keyFilePath = null;
	@Column(length=1, columnDefinition="char")
	private String isSpf = null;

	@Transient
	private String origClientId = null;
	
	public ClientData() {
		// must have a no-argument constructor
	}

	public List<ClientVariable> getClientVariables() {
		return clientVariables;
	}

	public void setClientVariables(List<ClientVariable> clientVariables) {
		this.clientVariables = clientVariables;
	}

	public IdTokens getIdTokens() {
		return idTokens;
	}

	public void setIdTokens(IdTokens idTokens) {
		this.idTokens = idTokens;
	}

	/** define components for UI */
	public boolean isUseTestAddress() {
		return Constants.Code.YES.getValue().equalsIgnoreCase(isUseTestAddr);
	}
	
	public boolean isVerpAddressEnabled() {
		return Constants.Code.YES.getValue().equalsIgnoreCase(isVerpEnabled);
	}
	
	public boolean isEmbedEmailId() {
		return Constants.Code.YES.getValue().equalsIgnoreCase(isEmbedEmailId);
	}
	
	public boolean isSystemClient() {
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
	public String getIsSaveRawMsg() {
		return isSaveRawMsg;
	}
	public void setIsSaveRawMsg(String saveRawMsg) {
		this.isSaveRawMsg = saveRawMsg;
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
	public String getIsEmbedEmailId() {
		return isEmbedEmailId;
	}
	public void setIsEmbedEmailId(String embedEmailId) {
		this.isEmbedEmailId = embedEmailId;
	}
	public String getIsUseTestAddr() {
		return isUseTestAddr;
	}
	public void setIsUseTestAddr(String useTestAddr) {
		this.isUseTestAddr = useTestAddr;
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

	public String getIsDikm() {
		return isDikm;
	}

	public void setIsDikm(String dikm) {
		this.isDikm = dikm;
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

	public String getIsSpf() {
		return isSpf;
	}

	public void setIsSpf(String spf) {
		this.isSpf = spf;
	}
}