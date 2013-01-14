package com.legacytojava.message.ejb.emailaddr;

import java.sql.Timestamp;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;

@Entity
@Table(name="emailaddr")
@TransactionManagement(TransactionManagementType.CONTAINER)
@NamedQuery(name="getByAddress", 
	query=
		"select OBJECT(ea) " +
		" from EmailAddrEntity ea where ea.emailAddr = :emailAddr")
public class EmailAddrEntity implements java.io.Serializable {
	private static final long serialVersionUID = -7968735762309014179L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long emailAddrId;
	@EmbeddedId
	private EmailAddrPK emailAddrPk;
	private String emailAddr;
	private String origEmailAddr;
	@Column(columnDefinition="char")
	private String statusId = StatusIdCode.ACTIVE;
	private Timestamp statusChangeTime;
	private String statusChangeUserId;
	@Column(columnDefinition="decimal")
	private int bounceCount;
	private Timestamp lastBounceTime;
	private Timestamp lastSentTime;
	private Timestamp lastRcptTime;
	@Column(columnDefinition="char")
	private String acceptHtml = Constants.YES_CODE;
	@Column(name="UpdtTime")
	private Timestamp updtTime;
	@Column(name="UpdtUserId", columnDefinition="char")
	private String updtUserId;

	public EmailAddrEntity() {}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	public long getEmailAddrId() {
		return emailAddrId;
	}
	public void setEmailAddrId(long emailAddrId) {
		this.emailAddrId = emailAddrId;
	}
	public EmailAddrPK getEmailAddrPK() {
		return emailAddrPk;
	}
	public void setEmailAddrPK(EmailAddrPK emailAddrPk) {
		this.emailAddrPk = emailAddrPk;
	}
	public String getEmailAddr() {
		return emailAddr;
	}
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}
	public String getOrigEmailAddr() {
		return origEmailAddr;
	}
	public void setOrigEmailAddr(String origEmailAddr) {
		this.origEmailAddr = origEmailAddr;
	}
	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public Timestamp getStatusChangeTime() {
		return statusChangeTime;
	}
	public void setStatusChangeTime(Timestamp statusChangeTime) {
		this.statusChangeTime = statusChangeTime;
	}
	public String getStatusChangeUserId() {
		return statusChangeUserId;
	}
	public void setStatusChangeUserId(String statusChangeUserId) {
		this.statusChangeUserId = statusChangeUserId;
	}
	public int getBounceCount() {
		return bounceCount;
	}
	public void setBounceCount(int bounceCount) {
		this.bounceCount = bounceCount;
	}
	public Timestamp getLastBounceTime() {
		return lastBounceTime;
	}
	public void setLastBounceTime(Timestamp lastBounceTime) {
		this.lastBounceTime = lastBounceTime;
	}
	public Timestamp getLastSentTime() {
		return lastSentTime;
	}
	public void setLastSentTime(Timestamp lastSentTime) {
		this.lastSentTime = lastSentTime;
	}
	public Timestamp getLastRcptTime() {
		return lastRcptTime;
	}
	public void setLastRcptTime(Timestamp lastRcptTime) {
		this.lastRcptTime = lastRcptTime;
	}
	public String getAcceptHtml() {
		return acceptHtml;
	}
	public void setAcceptHtml(String acceptHtml) {
		this.acceptHtml = acceptHtml;
	}
	public Timestamp getUpdtTime() {
		return updtTime;
	}
	public void setUpdtTime(Timestamp updateTime) {
		this.updtTime = updateTime;
	}
	public String getUpdtUserId() {
		return updtUserId;
	}
	public void setUpdtUserId(String updateUserId) {
		this.updtUserId = updateUserId;
	}
}
