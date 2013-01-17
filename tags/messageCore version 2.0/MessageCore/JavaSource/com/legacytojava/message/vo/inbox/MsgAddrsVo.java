package com.legacytojava.message.vo.inbox;

import java.io.Serializable;

import com.legacytojava.message.vo.BaseVo;

public class MsgAddrsVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 719252349271405308L;
	private long msgId = -1;
	private String addrType = "";
	private int addrSeq = -1;
	private String addrValue = null;
	
	public int getAddrSeq() {
		return addrSeq;
	}
	public void setAddrSeq(int addrSeq) {
		this.addrSeq = addrSeq;
	}
	public String getAddrType() {
		return addrType;
	}
	public void setAddrType(String addrType) {
		this.addrType = addrType;
	}
	public String getAddrValue() {
		return addrValue;
	}
	public void setAddrValue(String addrValue) {
		this.addrValue = addrValue;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}