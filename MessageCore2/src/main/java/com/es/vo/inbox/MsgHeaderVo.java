package com.es.vo.inbox;

import java.io.Serializable;

import com.es.vo.comm.BaseVo;

public class MsgHeaderVo extends BaseVo implements Serializable {
	
	private static final long serialVersionUID = -956215438116104255L;
	private long msgId = -1;
	private int headerSeq = -1;
	private String headerName = null;
	private String headerValue = null;
	
	public String getHeaderName() {
		return headerName;
	}
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}
	public int getHeaderSeq() {
		return headerSeq;
	}
	public void setHeaderSeq(int headerSeq) {
		this.headerSeq = headerSeq;
	}
	public String getHeaderValue() {
		return headerValue;
	}
	public void setHeaderValue(String headerValue) {
		this.headerValue = headerValue;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	
}