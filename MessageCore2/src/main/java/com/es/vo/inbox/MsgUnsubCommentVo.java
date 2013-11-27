package com.es.vo.inbox;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.vo.comm.BaseVo;

public class MsgUnsubCommentVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -3365866865718039073L;
	private int rowId = -1;
	private long msgId = -1;
	private long emailAddrId = -1;
	private String listId = null;
	private String comments = "";
	private Timestamp addTime = null;
	
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public long getEmailAddrId() {
		return emailAddrId;
	}
	public void setEmailAddrId(long emailAddrId) {
		this.emailAddrId = emailAddrId;
	}
	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Timestamp getAddTime() {
		return addTime;
	}
	public void setAddTime(Timestamp addTime) {
		this.addTime = addTime;
	}
}