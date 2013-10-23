package jpa.model.message;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import jpa.constant.CarrierCode;
import jpa.constant.MsgDirectionCode;
import jpa.message.MessageBodyBuilder;
import jpa.model.BaseModel;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.model.rule.RuleLogic;
import jpa.util.StringUtil;

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
	@Transient
	private MessageInbox referringMessage;
	
	@org.eclipse.persistence.annotations.Index
	@org.hibernate.annotations.Index(name="LeadMsgIndex")
	@Column(name="LeadMsgRowId", nullable=true, columnDefinition="Integer")
	private Integer leadMessageRowId;
	@Transient
	private MessageInbox leadMessage;

	@org.eclipse.persistence.annotations.Index
	@org.hibernate.annotations.Index(name="FromAddressIndex")
	@Column(name="FromAddressRowId", nullable=true, columnDefinition="Integer")
	private Integer fromAddrRowId;
	@Transient
	private EmailAddress fromAddress;

	@Column(name="ReplytoAddressRowId", nullable=true, columnDefinition="Integer")
	private Integer replytoAddrRowId;
	@Transient
	private EmailAddress replytoAddress;

	@org.eclipse.persistence.annotations.Index
	@org.hibernate.annotations.Index(name="ToAddressIndex")
	@Column(name="ToAddressRowId", nullable=true, columnDefinition="Integer")
	private Integer toAddrRowId;
	@Transient
	private EmailAddress toAddress;

	@Column(name="SenderDataRowId", nullable=true, columnDefinition="Integer")
	private Integer senderDataRowId;
	@Transient
	private SenderData senderData;

	@Column(name="SubscriberDataRowId", nullable=true, columnDefinition="Integer")
	private Integer subscriberDataRowId;
	@Transient
	private SubscriberData subscriberData;

	@Column(name="RuleLogicRowId", nullable=false)
	private int ruleLogicRowId;
	@Transient
	private RuleLogic ruleLogic;

	@Column(name="MessageRenderedRowId", nullable=true)
	private Integer messageRenderedRowId;
	@Transient
	private MessageRendered messageRendered;
	/* end of simplify */
	
	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageHeaderPK.messageInbox", orphanRemoval=true)
	@OrderBy
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
	@OrderBy
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

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageDeliveryStatusPK.messageInbox", orphanRemoval=true)
	@CascadeOnDelete
	private List<MessageDeliveryStatus> messageDeliveryStatusList;

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
	@Column(nullable=true, columnDefinition="Integer")
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

	/*
	 * define properties for UI components 
	 */
	@Transient
	private boolean isReply = false;
	@Transient
	private boolean isForward = false;
	@Transient
	private String composeFromAddress = null;
	@Transient
	private String composeToAddress = null;
	@Transient
	private int threadLevel = -1; // don't change
	@Transient
	private boolean showAllHeaders = false;
	@Transient
	private boolean showRawMessage = false;
	
	public boolean isReply() {
		return isReply;
	}

	public void setReply(boolean isReply) {
		this.isReply = isReply;
	}

	public boolean isForward() {
		return isForward;
	}

	public void setForward(boolean isForward) {
		this.isForward = isForward;
	}

	public String getComposeFromAddress() {
		return composeFromAddress;
	}

	public void setComposeFromAddress(String composeFromAddress) {
		this.composeFromAddress = composeFromAddress;
	}

	public String getComposeToAddress() {
		return composeToAddress;
	}

	public void setComposeToAddress(String composeToAddress) {
		this.composeToAddress = composeToAddress;
	}

	public int getThreadLevel() {
		return threadLevel;
	}

	public void setThreadLevel(int threadLevel) {
		this.threadLevel = threadLevel;
	}

	public boolean isShowAllHeaders() {
		return showAllHeaders;
	}

	public void setShowAllHeaders(boolean showAllHeaders) {
		this.showAllHeaders = showAllHeaders;
	}

	public boolean isShowRawMessage() {
		return showRawMessage;
	}

	public void setShowRawMessage(boolean showRawMessage) {
		this.showRawMessage = showRawMessage;
	}

	/**
	 * Email body is displayed in an HTML TextArea field. So HTML tags need to
	 * be removed for HTML message, and PRE tags need to be added for plain text
	 * message.
	 * 
	 * <pre>
	 * check body content type. if text/plain, surround the body text by PRE
	 * tag. if text/html, remove HTML and BODY tags from email body text.
	 * otherwise, return the body text unchanged.
	 * </pre>
	 * 
	 * @return body text
	 */
	public String getDisplayBody() {
		if (bodyContentType == null) bodyContentType = "text/plain";
		if (bodyContentType.toLowerCase().startsWith("text/plain")
				|| bodyContentType.toLowerCase().startsWith("message")) {
			return StringUtil.getHtmlDisplayText(msgBody);
		}
		else if (bodyContentType.toLowerCase().startsWith("text/html")) {
			return MessageBodyBuilder.removeHtmlBodyTags(msgBody);
		}
		else { // unknown type
			return msgBody;
		}
	}

	/**
	 * Always surround raw text by PRE tag as it is displayed in an HTML
	 * TextArea field.
	 * 
	 * @return message raw text
	 */
	public String getRawMessage() {
		if (messageStream == null) {
			// just for safety
			return getDisplayBody();
		}
		else {
			String txt = new String(messageStream.getMsgStream());
			return StringUtil.getHtmlDisplayText(txt);
		}
	}
	
	public int getRows() {
		int rows = msgBodySize / 120;
		return (rows > 20 ? 40 : 20);
	}

	/* end of UI */

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

	public Integer getSenderDataRowId() {
		return senderDataRowId;
	}

	public void setSenderDataRowId(Integer senderDataRowId) {
		this.senderDataRowId = senderDataRowId;
	}

	public Integer getSubscriberDataRowId() {
		return subscriberDataRowId;
	}

	public void setSubscriberDataRowId(Integer subscriberDataRowId) {
		this.subscriberDataRowId = subscriberDataRowId;
	}

	public int getRuleLogicRowId() {
		return ruleLogicRowId;
	}

	public void setRuleLogicRowId(int ruleLogicRowId) {
		this.ruleLogicRowId = ruleLogicRowId;
	}

	public Integer getMessageRenderedRowId() {
		return messageRenderedRowId;
	}

	public void setMessageRenderedRowId(Integer messageRenderedRowId) {
		this.messageRenderedRowId = messageRenderedRowId;
	}

	public List<MessageHeader> getMessageHeaderList() {
		if (messageHeaderList==null) {
			messageHeaderList = new ArrayList<MessageHeader>();
		}
		return messageHeaderList;
	}

	public void setMessageHeaderList(List<MessageHeader> messageHeaderList) {
		this.messageHeaderList = messageHeaderList;
	}

	public List<MessageAddress> getMessageAddressList() {
		if (messageAddressList == null) {
			messageAddressList = new ArrayList<MessageAddress>();
		}
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
		if (messageRfcFieldList==null) {
			messageRfcFieldList = new ArrayList<MessageRfcField>();
		}
		return messageRfcFieldList;
	}

	public void setMessageRfcFieldList(List<MessageRfcField> messageRfcFieldList) {
		this.messageRfcFieldList = messageRfcFieldList;
	}

	public List<MessageAttachment> getMessageAttachmentList() {
		if (messageAttachmentList==null) {
			messageAttachmentList = new ArrayList<MessageAttachment>();
		}
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
		if (messageActionLogList==null) {
			messageActionLogList = new ArrayList<MessageActionLog>();
		}
		return messageActionLogList;
	}

	public void setMessageActionLogList(List<MessageActionLog> messageActionLogList) {
		this.messageActionLogList = messageActionLogList;
	}

	public List<MessageDeliveryStatus> getMessageDeliveryStatusList() {
		if (messageDeliveryStatusList==null) {
			messageDeliveryStatusList = new ArrayList<MessageDeliveryStatus>();
		}
		return messageDeliveryStatusList;
	}

	public void setMessageDeliveryStatusList(
			List<MessageDeliveryStatus> messageDeliveryStatusList) {
		this.messageDeliveryStatusList = messageDeliveryStatusList;
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

	public EmailAddress getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(EmailAddress fromAddress) {
		this.fromAddress = fromAddress;
	}

	public EmailAddress getReplytoAddress() {
		return replytoAddress;
	}

	public void setReplytoAddress(EmailAddress replytoAddress) {
		this.replytoAddress = replytoAddress;
	}

	public EmailAddress getToAddress() {
		return toAddress;
	}

	public void setToAddress(EmailAddress toAddress) {
		this.toAddress = toAddress;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}

	public SubscriberData getSubscriberData() {
		return subscriberData;
	}

	public void setSubscriberData(SubscriberData subscriberData) {
		this.subscriberData = subscriberData;
	}

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public MessageRendered getMessageRendered() {
		return messageRendered;
	}

	public void setMessageRendered(MessageRendered messageRendered) {
		this.messageRendered = messageRendered;
	}
}