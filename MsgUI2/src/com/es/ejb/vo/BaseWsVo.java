package com.es.ejb.vo;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.es.tomee.util.TimestampAdapter;

import jpa.constant.Constants;

@XmlTransient
public abstract class BaseWsVo implements java.io.Serializable {
	private static final long serialVersionUID = -9076018726215757829L;
	
	@XmlElement(required=true)
	protected int rowId = 0;
	@XmlElement()
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	protected Timestamp updtTime = new Timestamp(System.currentTimeMillis());
	@XmlElement()
	protected String updtUserId = Constants.DEFAULT_USER_ID;

	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public Timestamp getUpdtTime() {
		return updtTime;
	}
	public void setUpdtTime(Timestamp updtTime) {
		this.updtTime = updtTime;
	}
	public String getUpdtUserId() {
		return updtUserId;
	}
	public void setUpdtUserId(String updtUserId) {
		this.updtUserId = updtUserId;
	}

}