package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.constant.CarrierCode;
import jpa.constant.MsgDirectionCode;

@Entity
@Table(name="message_inbox")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="MessageInboxNative",
		entities={
		 @EntityResult(entityClass=MessageInbox.class),
	  	}
	  	),
	})
public class MessageInbox extends BaseModel implements Serializable {
	private static final long serialVersionUID = -5868053593529617642L;

	@Transient
	public static final String MAPPING_MESSAGE_INBOX = "MessageInboxNative";

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=MessageInbox.class)
	@JoinColumn(name="ReferringMsgRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private MessageInbox referringMessage;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=MessageInbox.class)
	@JoinColumn(name="LeadMsgRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private MessageInbox leadMessage;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=EmailAddr.class)
	@JoinColumn(name="FromAddressRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private EmailAddr fromAddress;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=EmailAddr.class)
	@JoinColumn(name="ReplytoAddressRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private EmailAddr replytoAddress;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=EmailAddr.class)
	@JoinColumn(name="ToAddressRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private EmailAddr toAddress;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=ClientData.class)
	@JoinColumn(name="ClientDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private ClientData clientData;

	@ManyToOne(fetch=FetchType.LAZY, optional=true, targetEntity=CustomerData.class)
	@JoinColumn(name="CustomerDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=true)
	private CustomerData customerData;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=RuleLogic.class)
	@JoinColumn(name="RuleLogicRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private RuleLogic ruleLogic;
	
	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageHeaderPK.messageInbox", orphanRemoval=true)
	private List<MessageHeader> messageHeaderList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageAddressPK.messageInbox", orphanRemoval=true)
	private List<MessageAddress> messageAddressList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageRfcFieldPK.messageInbox", orphanRemoval=true)
	private List<MessageRfcField> messageRfcFieldList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageAttachmentPK.messageInbox", orphanRemoval=true)
	private List<MessageAttachment> messageAttachmentList;

	@Column(nullable=false, length=1, columnDefinition="char")
	private String carrierCode = CarrierCode.SMTPMAIL.getValue();
	@Column(nullable=false, length=1, columnDefinition="char")
	private String msgDirection = MsgDirectionCode.RECEIVED.getValue();
	@Column(nullable=true, length=255)
	private String msgSubject = null;
	@Column(nullable=true, length=16)
	private String msgPriority = null;
	@Column(nullable=false)
	private Timestamp receivedTime;
	@Column(nullable=true)
	private java.sql.Date purgeDate = null;
	@Column(nullable=true)
	private Timestamp lockTime = null;
	@Column(nullable=true,length=26)
	private String lockId = null;
	@Column(nullable=false,columnDefinition="smallint")
	private int readCount = 0;
	@Column(nullable=false,columnDefinition="smallint")
	private int replyCount = 0;
	@Column(nullable=false,columnDefinition="smallint")
	private int forwardCount = 0;
	@Column(nullable=false, columnDefinition="boolean")
	private boolean isFlagged = false;
	@Column(nullable=true)
	private Timestamp deliveryTime;
	@Column(nullable=true, length=255)
	private String smtpMessageId = null;
	@Column(nullable=true)
	private Integer renderId = null; // TODO
	@Column(nullable=false,columnDefinition="boolean")
	private boolean isOverrideTestAddr = false;
	@Column(nullable=false,columnDefinition="smallint")
	private int attachmentCount = 0;
	@Column(nullable=false)
	private int attachmentSize = 0;
	@Column(nullable=false)
	private int msgBodySize = 0;
	@Column(nullable=false,length=100)
	private String msgContentType = "";
	@Column(nullable=true,length=50)
	private String bodyContentType = null;
	@Lob
	@Column(nullable=true,length=65530)
	private String msgBody = null;

	@Transient
	private int origReadCount = -1;
	@Transient
	private String origStatusId = null;

	public MessageInbox() {
		// must have a no-argument constructor
	}

	public MessageInbox getReferringMessage() {
		return referringMessage;
	}

	public void setReferringMessage(MessageInbox referringMessage) {
		this.referringMessage = referringMessage;
	}

	public MessageInbox getLeadMessage() {
		return leadMessage;
	}

	public void setLeadMessage(MessageInbox leadMessage) {
		this.leadMessage = leadMessage;
	}

	public EmailAddr getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(EmailAddr fromAddress) {
		this.fromAddress = fromAddress;
	}

	public EmailAddr getReplytoAddress() {
		return replytoAddress;
	}

	public void setReplytoAddress(EmailAddr replytoAddress) {
		this.replytoAddress = replytoAddress;
	}

	public EmailAddr getToAddress() {
		return toAddress;
	}

	public void setToAddress(EmailAddr toAddress) {
		this.toAddress = toAddress;
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}

	public CustomerData getCustomerData() {
		return customerData;
	}

	public void setCustomerData(CustomerData customerData) {
		this.customerData = customerData;
	}

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public List<MessageHeader> getMessageHeaderList() {
		return messageHeaderList;
	}

	public void setMessageHeaderList(List<MessageHeader> messageHeaderList) {
		this.messageHeaderList = messageHeaderList;
	}

	public List<MessageAddress> getMessageAddressList() {
		return messageAddressList;
	}

	public void setMessageAddressList(List<MessageAddress> messageAddressList) {
		this.messageAddressList = messageAddressList;
	}

	public List<MessageRfcField> getMessageRfcFieldList() {
		return messageRfcFieldList;
	}

	public void setMessageRfcFieldList(List<MessageRfcField> messageRfcFieldList) {
		this.messageRfcFieldList = messageRfcFieldList;
	}

	public List<MessageAttachment> getMessageAttachmentList() {
		return messageAttachmentList;
	}

	public void setMessageAttachmentList(
			List<MessageAttachment> messageAttachmentList) {
		this.messageAttachmentList = messageAttachmentList;
	}

	public String getCarrierCode() {
		return carrierCode;
	}

	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}

	public String getMsgDirection() {
		return msgDirection;
	}

	public void setMsgDirection(String msgDirection) {
		this.msgDirection = msgDirection;
	}

	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public String getMsgPriority() {
		return msgPriority;
	}

	public void setMsgPriority(String msgPriority) {
		this.msgPriority = msgPriority;
	}

	public Timestamp getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(Timestamp receivedTime) {
		this.receivedTime = receivedTime;
	}

	public java.sql.Date getPurgeDate() {
		return purgeDate;
	}

	public void setPurgeDate(java.sql.Date purgeDate) {
		this.purgeDate = purgeDate;
	}

	public Timestamp getLockTime() {
		return lockTime;
	}

	public void setLockTime(Timestamp lockTime) {
		this.lockTime = lockTime;
	}

	public String getLockId() {
		return lockId;
	}

	public void setLockId(String lockId) {
		this.lockId = lockId;
	}

	public int getReadCount() {
		return readCount;
	}

	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}

	public int getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(int replyCount) {
		this.replyCount = replyCount;
	}

	public int getForwardCount() {
		return forwardCount;
	}

	public void setForwardCount(int forwardCount) {
		this.forwardCount = forwardCount;
	}

	public boolean isFlagged() {
		return isFlagged;
	}

	public void setFlagged(boolean isFlagged) {
		this.isFlagged = isFlagged;
	}

	public Timestamp getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(Timestamp deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public String getSmtpMessageId() {
		return smtpMessageId;
	}

	public void setSmtpMessageId(String smtpMessageId) {
		this.smtpMessageId = smtpMessageId;
	}

	public Integer getRenderId() {
		return renderId;
	}

	public void setRenderId(Integer renderId) {
		this.renderId = renderId;
	}

	public boolean isOverrideTestAddr() {
		return isOverrideTestAddr;
	}

	public void setOverrideTestAddr(boolean isOverrideTestAddr) {
		this.isOverrideTestAddr = isOverrideTestAddr;
	}

	public int getAttachmentCount() {
		return attachmentCount;
	}

	public void setAttachmentCount(int attachmentCount) {
		this.attachmentCount = attachmentCount;
	}

	public int getAttachmentSize() {
		return attachmentSize;
	}

	public void setAttachmentSize(int attachmentSize) {
		this.attachmentSize = attachmentSize;
	}

	public int getMsgBodySize() {
		return msgBodySize;
	}

	public void setMsgBodySize(int msgBodySize) {
		this.msgBodySize = msgBodySize;
	}

	public String getMsgContentType() {
		return msgContentType;
	}

	public void setMsgContentType(String msgContentType) {
		this.msgContentType = msgContentType;
	}

	public String getBodyContentType() {
		return bodyContentType;
	}

	public void setBodyContentType(String bodyContentType) {
		this.bodyContentType = bodyContentType;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public int getOrigReadCount() {
		return origReadCount;
	}

	public void setOrigReadCount(int origReadCount) {
		this.origReadCount = origReadCount;
	}

	public String getOrigStatusId() {
		return origStatusId;
	}

	public void setOrigStatusId(String origStatusId) {
		this.origStatusId = origStatusId;
	}
}