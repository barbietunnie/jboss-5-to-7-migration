package com.es.vo.inbox;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.vo.comm.BaseVo;

public class MsgActionLogVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -2253515007664999230L;
	private long msgId = -1;
	private int ActionSeq = -1;
	private Timestamp addTime;
	private String actionBo = "";
	private String parameters = null;
	
	private MsgInboxVo msgInboxVo = null;
	
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public Timestamp getAddTime() {
		return addTime;
	}
	public void setAddTime(Timestamp addTime) {
		this.addTime = addTime;
	}
	public int getActionSeq() {
		return ActionSeq;
	}
	public void setActionSeq(int actionSeq) {
		ActionSeq = actionSeq;
	}
	public String getActionBo() {
		return actionBo;
	}
	public void setActionBo(String actionId) {
		this.actionBo = actionId;
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