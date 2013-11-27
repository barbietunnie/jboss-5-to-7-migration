package com.es.vo.inbox;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.es.core.util.SpringUtil;
import com.es.core.util.StringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgStreamDao;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.constant.EmailAddressType;
import com.es.data.constant.MsgDirectionCode;
import com.es.data.constant.MsgStatusCode;
import com.es.msgbean.MessageBodyBuilder;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.comm.BaseVo;
import com.es.vo.outbox.DeliveryStatusVo;
import com.es.vo.outbox.MsgRenderedVo;
import com.es.vo.outbox.MsgStreamVo;

public class MsgInboxVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -6107126104027039811L;
	private long msgId = -1;
	private Long msgRefId = null;
	private long leadMsgId = -1;
	private String carrierCode = CarrierCode.SMTPMAIL.getValue();
	private String msgSubject = null;
	private String msgPriority = null;
	private Timestamp receivedTime;
	private Long fromAddrId = null;
	private Long replyToAddrId = null;
	private Long toAddrId = null;
	private String senderId = null;
	private String subrId = null;
	private java.sql.Date purgeDate = null;
	private Timestamp lockTime = null;
	private String lockId = null;
	private String ruleName = "";
	private int readCount = 0;
	private int replyCount = 0;
	private int forwardCount = 0;
	private String flagged = CodeType.NO_CODE.getValue();
	private String msgDirection = "";
	
	private Timestamp deliveryTime;
	private String smtpMessageId = null;
	private Long renderId = null;
	private String overrideTestAddr = null;
	
	private int attachmentCount = 0;
	private int attachmentSize = 0;
	private int msgBodySize = 0;
	
	private String msgContentType = "";
	private String bodyContentType = null;
	private String msgBody = null;
	private int origReadCount = -1;
	private String origStatusId = null;
	
	private List<MsgAddressVo> msgAddrs;
	private List<MsgHeaderVo> msgHeaders;
	private List<MsgAttachmentVo> attachments;
	private List<MsgRfcFieldVo> rfcFields;
	
	private MsgStreamVo msgStreamVo = null;
	private List<DeliveryStatusVo> deliveryStatus = null;

	// used when joining MsgRendered table
	private MsgRenderedVo msgRenderedVo = null;
	
	/** 
	 * define properties for UI components 
	 */
	private transient EmailAddressDao emailAddrDao = null;
	private transient MsgStreamDao msgStreamDao = null;
	private boolean showAllHeaders = false;
	private boolean showRawMessage = false;
	private String composeFromAddress = null;
	private String composeToAddress = null;
	private int threadLevel = 0;
	private boolean isReply = false;
	private boolean isForward = false;
	
	public MsgInboxVo() {
		setStatusId(MsgStatusCode.OPENED.getValue()); // default in-bound message status
	}

	public int getThreadLevel() {
		return threadLevel;
	}

	public void setThreadLevel(int threadLevel) {
		this.threadLevel = threadLevel;
	}
	
	public boolean getIsReply() {
		return isReply;
	}

	public void setIsReply(boolean isReply) {
		this.isReply = isReply;
	}

	public boolean getIsForward() {
		return isForward;
	}

	public void setIsForward(boolean isForward) {
		this.isForward = isForward;
	}

	public String getLevelDots() {
		return StringUtil.getDots(threadLevel);
	}

	public String getLevelPrefix() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < threadLevel; i++) {
			sb.append("&nbsp;&nbsp;"); //&bull;"); //&sdot;");
		}
		return sb.toString();
	}

	public boolean isHasAttachments() {
		return (attachmentCount > 0 ? true : false);
	}
	
	public boolean isFlaggedMsg() {
		return (CodeType.YES_CODE.getValue().equalsIgnoreCase(flagged));
	}
	
	public boolean isReceivedMsg() {
		return (MsgDirectionCode.RECEIVED.getValue().equalsIgnoreCase(msgDirection));
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
	
	public boolean isClosedStatus() {
		return MsgStatusCode.CLOSED.equals(getStatusId());
	}
	
	public String getFromAddress() {
		if (fromAddrId == null) return "";
		EmailAddressVo vo = getEmailAddrDao().getByAddrId(fromAddrId);
		if (vo == null) return "";
		else return vo.getEmailAddr();
	}
	
	public String getFromDisplayName() {
		return getDisplayName(getFromAddress());
	}
	
	public String getToAddress() {
		// first locate To from header
		List<MsgHeaderVo> headers = getMsgHeaders();
		for (MsgHeaderVo header : headers) {
			if (EmailAddressType.TO_ADDR.getValue().equalsIgnoreCase(header.getHeaderName())) {
				return header.getHeaderValue();
			}
		}
		// if not found from header, get from toAddrId
		if (toAddrId == null) return "";
		EmailAddressVo vo = getEmailAddrDao().getByAddrId(toAddrId);
		if (vo == null) return "";
		else return vo.getEmailAddr();
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

	public String getCcAddress() {
		if (msgAddrs == null || msgAddrs.size() == 0) return "";
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<msgAddrs.size(); i++) {
			MsgAddressVo vo = msgAddrs.get(i);
			if (EmailAddressType.CC_ADDR.getValue().equals(vo.getAddrType())) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(vo.getAddrValue());
			}
		}
		return sb.toString();
	}
	
	public java.util.Date getReceivedDate() {
		if (receivedTime == null) return new java.util.Date();
		else return new java.util.Date(receivedTime.getTime());
	}
	
	public String getSize() {
		int len = (msgBodySize + attachmentSize);
		if (len < 1024) {
			return 1024 + "";
		}
		else {
			return (int) Math.ceil((double)len / 1024.0) + "K";
		}
	}
	
	public int getRows() {
		int rows = getBodylength() / 120;
		return (rows > 25 ? 50 : 25);
	}
	
	private int getBodylength() {
		return msgBodySize;
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
		MsgStreamVo vo = getMsgStreamDao().getByPrimaryKey(msgId);
		if (vo == null || vo.getMsgStream() == null) {
			// just for safety
			return getDisplayBody();
		}
		else {
			String txt = new String(vo.getMsgStream());
			return StringUtil.getHtmlDisplayText(txt);
		}
	}
	
	private EmailAddressDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddrDao;
	}
	
	private MsgStreamDao getMsgStreamDao() {
		if (msgStreamDao == null) {
			msgStreamDao = SpringUtil.getAppContext().getBean(MsgStreamDao.class);
		}
		return msgStreamDao;
	}
	
	private String getDisplayName(String addr) {
		if (addr == null) return addr;
		int left = addr.indexOf("<");
		int right = addr.indexOf(">", left + 1);
		if (left > 0 && right > left) {
			return addr.substring(0, left - 1);
		}
		else {
			return addr;
		}
	}
	/** 
	 * end of UI components 
	 */
	
	public List<MsgAttachmentVo> getAttachments() {
		if (attachments==null)
			attachments = new ArrayList<MsgAttachmentVo>();
		return attachments;
	}
	public void setAttachments(List<MsgAttachmentVo> attachments) {
		this.attachments = attachments;
	}
	public List<MsgAddressVo> getMsgAddrs() {
		if (msgAddrs==null)
			msgAddrs = new ArrayList<MsgAddressVo>();
		return msgAddrs;
	}
	public void setMsgAddrs(List<MsgAddressVo> msgAddrs) {
		this.msgAddrs = msgAddrs;
	}
	public List<MsgHeaderVo> getMsgHeaders() {
		if (msgHeaders==null)
			msgHeaders = new ArrayList<MsgHeaderVo>();
		return msgHeaders;
	}
	public void setMsgHeaders(List<MsgHeaderVo> msgHeaders) {
		this.msgHeaders = msgHeaders;
	}
	public List<MsgRfcFieldVo> getRfcFields() {
		if (rfcFields==null)
			rfcFields = new ArrayList<MsgRfcFieldVo>();
		return rfcFields;
	}
	public void setRfcFields(List<MsgRfcFieldVo> rfcFields) {
		this.rfcFields = rfcFields;
	}
	public List<DeliveryStatusVo> getDeliveryStatus() {
		if (deliveryStatus==null)
			deliveryStatus = new ArrayList<DeliveryStatusVo>();
		return deliveryStatus;
	}
	public void setDeliveryStatus(List<DeliveryStatusVo> deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
	public String getCarrierCode() {
		return carrierCode;
	}
	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getSubrId() {
		return subrId;
	}
	public void setSubrId(String subrId) {
		this.subrId = subrId;
	}
	public Long getFromAddrId() {
		return fromAddrId;
	}
	public void setFromAddrId(Long fromAddrId) {
		this.fromAddrId = fromAddrId;
	}
	public String getLockId() {
		return lockId;
	}
	public void setLockId(String lockId) {
		this.lockId = lockId;
	}
	public Timestamp getLockTime() {
		return lockTime;
	}
	public void setLockTime(Timestamp lockTime) {
		this.lockTime = lockTime;
	}
	public String getMsgBody() {
		return msgBody;
	}
	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
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
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public String getMsgPriority() {
		return msgPriority;
	}
	public void setMsgPriority(String msgPriority) {
		this.msgPriority = msgPriority;
	}
	public Long getMsgRefId() {
		return msgRefId;
	}
	public void setMsgRefId(Long msgRefId) {
		this.msgRefId = msgRefId;
	}
	public long getLeadMsgId() {
		return leadMsgId;
	}
	public void setLeadMsgId(long leadMsgId) {
		this.leadMsgId = leadMsgId;
	}
	public String getMsgSubject() {
		return msgSubject;
	}
	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public java.sql.Date getPurgeDate() {
		return purgeDate;
	}
	public void setPurgeDate(java.sql.Date purgeDate) {
		this.purgeDate = purgeDate;
	}
	public Timestamp getReceivedTime() {
		return receivedTime;
	}
	public void setReceivedTime(Timestamp receivedTime) {
		this.receivedTime = receivedTime;
	}
	public Long getReplyToAddrId() {
		return replyToAddrId;
	}
	public void setReplyToAddrId(Long replyToAddrId) {
		this.replyToAddrId = replyToAddrId;
	}
	public Long getToAddrId() {
		return toAddrId;
	}
	public void setToAddrId(Long toAddrId) {
		this.toAddrId = toAddrId;
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
	public String getFlagged() {
		return flagged;
	}
	public void setFlagged(String flagged) {
		this.flagged = flagged;
	}
	public String getMsgDirection() {
		return msgDirection;
	}
	public void setMsgDirection(String msgDirection) {
		this.msgDirection = msgDirection;
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
	public Long getRenderId() {
		return renderId;
	}
	public void setRenderId(Long renderId) {
		this.renderId = renderId;
	}
	public String getOverrideTestAddr() {
		return overrideTestAddr;
	}
	public void setOverrideTestAddr(String overrideTestAddr) {
		this.overrideTestAddr = overrideTestAddr;
	}
	public MsgRenderedVo getMsgRenderedVo() {
		return msgRenderedVo;
	}
	public void setMsgRenderedVo(MsgRenderedVo msgRenderedVo) {
		this.msgRenderedVo = msgRenderedVo;
	}
	public MsgStreamVo getMsgStreamVo() {
		return msgStreamVo;
	}
	public void setMsgStreamVo(MsgStreamVo msgStreamVo) {
		this.msgStreamVo = msgStreamVo;
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