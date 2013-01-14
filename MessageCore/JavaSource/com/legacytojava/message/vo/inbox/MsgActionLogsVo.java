package com.legacytojava.message.vo.inbox;

import java.io.Serializable;
import java.sql.Timestamp;

import com.legacytojava.message.vo.BaseVo;

public class MsgActionLogsVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -2253515007664999230L;
	private long msgId = -1;
	private Long msgRefId = null;
	private long leadMsgId = -1;
	private String actionBo = "";
	private String parameters = null;
	private Timestamp addTime;
	
	private MsgInboxVo msgInboxVo = null;
	
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
	public String getActionBo() {
		return actionBo;
	}
	public void setActionBo(String actionId) {
		this.actionBo = actionId;
	}
	public Timestamp getAddTime() {
		return addTime;
	}
	public void setAddTime(Timestamp addTime) {
		this.addTime = addTime;
	}
	public MsgInboxVo getMsgInboxVo() {
		return msgInboxVo;
	}
	public void setMsgInboxVo(MsgInboxVo msgInboxVo) {
		this.msgInboxVo = msgInboxVo;
	}
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
}