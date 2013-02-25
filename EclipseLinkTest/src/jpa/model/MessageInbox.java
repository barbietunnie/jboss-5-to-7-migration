package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.constant.CarrierCode;
import jpa.constant.MsgDirectionCode;

import org.eclipse.persistence.annotations.CascadeOnDelete;

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

	/*
	 * Define following fields as individual columns instead of Relationships
	 * to simplify the implementation of cascade delete.
	 */
	@org.eclipse.persistence.annotations.Index
	@org.hibernate.annotations.Index(name="ReferringMsgIndex")
	@Column(name="ReferringMsgRowId", nullable=true, columnDefinition="Integer")
	private Integer referringMessageRowId;

	@org.eclipse.persistence.annotations.Index
	@org.hibernate.annotations.Index(name="LeadMsgIndex")
	@Column(name="LeadMsgRowId", nullable=true, columnDefinition="Integer")
	private Integer leadMessageRowId;

	@org.eclipse.persistence.annotations.Index
	@org.hibernate.annotations.Index(name="FromAddressIndex")
	@Column(name="FromAddressRowId", nullable=true, columnDefinition="Integer")
	private Integer fromAddrRowId;

	@Column(name="ReplytoAddressRowId", nullable=true, columnDefinition="Integer")
	private Integer replytoAddrRowId;

	@org.eclipse.persistence.annotations.Index
	@org.hibernate.annotations.Index(name="ToAddressIndex")
	@Column(name="ToAddressRowId", nullable=true, columnDefinition="Integer")
	private Integer toAddrRowId;

	@Column(name="ClientDataRowId", nullable=true, columnDefinition="Integer")
	private Integer clientDataRowId;

	@Column(name="CustomerDataRowId", nullable=true, columnDefinition="Integer")
	private Integer customerDataRowId;

	@Column(name="RuleLogicRowId", nullable=false)
	private int ruleLogicRowId;
	/* end of simplify */
	
	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageHeaderPK.messageInbox", orphanRemoval=true)
	@CascadeOnDelete
	private List<MessageHeader> messageHeaderList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageInbox", orphanRemoval=true)
	@CascadeOnDelete
	private List<MessageAddress> messageAddressList;

	@OneToOne(cascade={CascadeType.ALL},fetch=FetchType.LAZY,mappedBy="messageInbox", orphanRemoval=true, optional=true)
	@CascadeOnDelete
	private MessageStream messageStream;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageRfcFieldPK.messageInbox", orphanRemoval=true)
	@CascadeOnDelete
	private List<MessageRfcField> messageRfcFieldList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageAttachmentPK.messageInbox", orphanRemoval=true)
	@CascadeOnDelete
	private List<MessageAttachment> messageAttachmentList;

	@OneToOne(cascade={CascadeType.ALL},fetch=FetchType.LAZY,mappedBy="messageInbox", orphanRemoval=true, optional=true)
	@CascadeOnDelete
	private MessageUnsubComment messageUnsubComment;

	@OneToOne(cascade={CascadeType.ALL},fetch=FetchType.LAZY,mappedBy="messageInbox", orphanRemoval=true, optional=true)
	@CascadeOnDelete
	private MessageClickCount messageClickCount;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageActionLogPK.messageInbox", orphanRemoval=true)
	@CascadeOnDelete
	private List<MessageActionLog> messageActionLogList;

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

	public Integer getReferringMessageRowId() {
		return referringMessageRowId;
	}

	public void setReferringMessageRowId(Integer referringMessageRowId) {
		this.referringMessageRowId = referringMessageRowId;
	}

	public Integer getLeadMessageRowId() {
		return leadMessageRowId;
	}

	public void setLeadMessageRowId(Integer leadMessageRowId) {
		this.leadMessageRowId = leadMessageRowId;
	}

	public Integer getFromAddrRowId() {
		return fromAddrRowId;
	}

	public void setFromAddrRowId(Integer fromAddrRowId) {
		this.fromAddrRowId = fromAddrRowId;
	}

	public Integer getReplytoAddrRowId() {
		return replytoAddrRowId;
	}

	public void setReplytoAddrRowId(Integer replytoAddrRowId) {
		this.replytoAddrRowId = replytoAddrRowId;
	}

	public Integer getToAddrRowId() {
		return toAddrRowId;
	}

	public void setToAddrRowId(Integer toAddrRowId) {
		this.toAddrRowId = toAddrRowId;
	}

	public Integer getClientDataRowId() {
		return clientDataRowId;
	}

	public void setClientDataRowId(Integer clientDataRowId) {
		this.clientDataRowId = clientDataRowId;
	}

	public Integer getCustomerDataRowId() {
		return customerDataRowId;
	}

	public void setCustomerDataRowId(Integer customerDataRowId) {
		this.customerDataRowId = customerDataRowId;
	}

	public int getRuleLogicRowId() {
		return ruleLogicRowId;
	}

	public void setRuleLogicRowId(int ruleLogicRowId) {
		this.ruleLogicRowId = ruleLogicRowId;
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

	public MessageStream getMessageStream() {
		return messageStream;
	}

	public void setMessageStream(MessageStream messageStream) {
		this.messageStream = messageStream;
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

	public MessageUnsubComment getMessageUnsubComment() {
		return messageUnsubComment;
	}

	public void setMessageUnsubComment(MessageUnsubComment messageUnsubComment) {
		this.messageUnsubComment = messageUnsubComment;
	}

	public MessageClickCount getMessageClickCount() {
		return messageClickCount;
	}

	public void setMessageClickCount(MessageClickCount messageClickCount) {
		this.messageClickCount = messageClickCount;
	}

	public List<MessageActionLog> getMessageActionLogList() {
		return messageActionLogList;
	}

	public void setMessageActionLogList(List<MessageActionLog> messageActionLogList) {
		this.messageActionLogList = messageActionLogList;
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