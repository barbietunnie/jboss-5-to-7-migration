package com.legacytojava.message.vo.inbox;

import java.io.Serializable;
import java.sql.Timestamp;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MsgDirectionCode;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.vo.BaseVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

public class MsgInboxWebVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 4827192283916378782L;
	private long msgId = -1;
	private Long msgRefId = null;
	private long leadMsgId = -1;
	private String msgSubject = null;
	private Timestamp receivedTime;
	private Long fromAddrId = null;
	private Long toAddrId = null;
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
	private transient EmailAddrDao emailAddrDao = null;
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
		return (Constants.YES_CODE.equalsIgnoreCase(flagged));
	}
	
	public boolean isReceivedMsg() {
		return (MsgDirectionCode.MSG_RECEIVED.equalsIgnoreCase(msgDirection));
	}
	
	public String getFromAddress() {
		if (fromAddrId == null) return "";
		EmailAddrVo vo = getEmailAddrDao().getByAddrId(fromAddrId);
		if (vo == null) return "";
		else return vo.getEmailAddr();
	}
	
	public String getFromDisplayName() {
		return getDisplayName(getFromAddress());
	}
	
	public String getToAddress() {
		if (toAddrId == null) return "";
		EmailAddrVo vo = getEmailAddrDao().getByAddrId(toAddrId);
		if (vo == null) return "";
		else return vo.getEmailAddr();
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
	
	private EmailAddrDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddrDao) SpringUtil.getDaoAppContext().getBean("emailAddrDao");
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
	
	public Long getFromAddrId() {
		return fromAddrId;
	}
	public void setFromAddrId(Long fromAddrId) {
		this.fromAddrId = fromAddrId;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
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
	public Timestamp getReceivedTime() {
		return receivedTime;
	}
	public void setReceivedTime(Timestamp receivedTime) {
		this.receivedTime = receivedTime;
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