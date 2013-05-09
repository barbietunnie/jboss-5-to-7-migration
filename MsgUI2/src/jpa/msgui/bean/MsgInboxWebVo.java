package jpa.msgui.bean;

import java.io.Serializable;
import java.sql.Timestamp;

import jpa.constant.CodeType;
import jpa.constant.MsgDirectionCode;
import jpa.model.EmailAddress;
import jpa.msgui.vo.BaseVo;
import jpa.service.EmailAddressService;
import jpa.util.SpringUtil;

public class MsgInboxWebVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 4827192283916378782L;
	private int msgId = -1;
	private Integer msgRefId = null;
	private int leadMsgId = -1;
	private String msgSubject = null;
	private Timestamp receivedTime;
	private Integer fromAddrId = null;
	private Integer toAddrId = null;
	private String ruleName = "";
	private int readCount = 0;
	private int replyCount = 0;
	private int forwardCount = 0;
	private String flagged = "";
	private String msgDirection = "";
	
	private int attachmentCount = 0;
	private int attachmentSize = 0;
	private int msgBodySize = 0;
	
	private int origReadCount = -1;
	private String origStatusId = null;
	/** 
	 * define properties for UI components 
	 */
	private transient EmailAddressService emailAddrDao = null;
	private int threadLevel = -1; // don't change

	public MsgInboxWebVo() {
		flagged = "";
		msgDirection = "";
		setStatusId("");		
	}

	public int getThreadLevel() {
		return threadLevel;
	}

	public void setThreadLevel(int threadLevel) {
		this.threadLevel = threadLevel;
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
	
	public String getFromAddress() {
		if (fromAddrId == null) return "";
		EmailAddress vo = getEmailAddressService().getByRowId(fromAddrId);
		if (vo == null) return "";
		else return vo.getAddress();
	}
	
	public String getFromDisplayName() {
		return getDisplayName(getFromAddress());
	}
	
	public String getToAddress() {
		if (toAddrId == null) return "";
		EmailAddress vo = getEmailAddressService().getByRowId(toAddrId);
		if (vo == null) return "";
		else return vo.getAddress();
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
	
	private EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddressService) SpringUtil.getAppContext().getBean("emailAddressService");
		}
		return emailAddrDao;
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
	
	public Integer getFromAddrId() {
		return fromAddrId;
	}
	public void setFromAddrId(Integer fromAddrId) {
		this.fromAddrId = fromAddrId;
	}
	public int getMsgId() {
		return msgId;
	}
	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}
	public Integer getMsgRefId() {
		return msgRefId;
	}
	public void setMsgRefId(Integer msgRefId) {
		this.msgRefId = msgRefId;
	}
	public int getLeadMsgId() {
		return leadMsgId;
	}
	public void setLeadMsgId(int leadMsgId) {
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
	public Timestamp getReceivedTime() {
		return receivedTime;
	}
	public void setReceivedTime(Timestamp receivedTime) {
		this.receivedTime = receivedTime;
	}
	public Integer getToAddrId() {
		return toAddrId;
	}
	public void setToAddrId(Integer toAddrId) {
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