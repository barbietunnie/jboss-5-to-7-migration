package com.legacytojava.message.vo.outbox;
	
import java.io.Serializable;
import java.sql.Timestamp;

import com.legacytojava.message.vo.BaseVo;

public class MsgStreamVo extends BaseVo implements Serializable {
	
	private static final long serialVersionUID = 9078211382541294227L;
	private long msgId = -1;
	private Long fromAddrId = null;
	private Long toAddrId = null;
	private String msgSubject = null;
	private Timestamp addTime = null;
	private byte[] msgStream = null;
	
	public Timestamp getAddTime() {
		return addTime;
	}
	public void setAddTime(Timestamp addTime) {
		this.addTime = addTime;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public byte[] getMsgStream() {
		return msgStream;
	}
	public void setMsgStream(byte[] msgText) {
		this.msgStream = msgText;
	}
	public Long getFromAddrId() {
		return fromAddrId;
	}
	public void setFromAddrId(Long fromAddrId) {
		this.fromAddrId = fromAddrId;
	}
	public String getMsgSubject() {
		return msgSubject;
	}
	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}
	public Long getToAddrId() {
		return toAddrId;
	}
	public void setToAddrId(Long toAddrId) {
		this.toAddrId = toAddrId;
	}
}
