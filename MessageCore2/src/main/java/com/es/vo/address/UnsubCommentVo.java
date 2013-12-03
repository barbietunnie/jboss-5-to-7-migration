package com.es.vo.address;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.vo.comm.BaseVoWithRowId;

public class UnsubCommentVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -8214632550517452561L;
	private long emailAddrId = -1;
	private String listId = null;
	private String comments = "";
	private Timestamp addTime = null;
	
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