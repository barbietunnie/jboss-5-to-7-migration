package com.es.vo.action;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.vo.comm.BaseVoWithRowId;

public class RuleActionVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = 5576050695865725099L;
	private String ruleName = "";
	private int actionSeq = -1;
	private Timestamp startTime;
	private String senderId = null;
	private String actionId = "";
	private String dataTypeValues = null;
	
	private String processBeanId = "";
	private String processClassName = null;
	private String dataType = null;
	
	public RuleActionVo() {}
	
	public RuleActionVo(
			String ruleName,
			int actionSeq,
			Timestamp startTime,
			String senderId,
			String actionId,
			String statusId,
			String dataTypeValues) {
		super();
		this.ruleName = ruleName;
		this.actionSeq = actionSeq;
		this.startTime = startTime;
		this.senderId = senderId;
		this.actionId = actionId;
		setStatusId(statusId);
		this.dataTypeValues = dataTypeValues;
	}
	
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public int getActionSeq() {
		return actionSeq;
	}
	public void setActionSeq(int actionSeq) {
		this.actionSeq = actionSeq;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getActionId() {
		return actionId;
	}
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	public String getDataTypeValues() {
		return dataTypeValues;
	}
	public void setDataTypeValues(String dataTypeValues) {
		this.dataTypeValues = dataTypeValues;
	}

	public String getProcessBeanId() {
		return processBeanId;
	}
	public void setProcessBeanId(String processBeanId) {
		this.processBeanId = processBeanId;
	}
	public String getProcessClassName() {
		return processClassName;
	}
	public void setProcessClassName(String processClassName) {
		this.processClassName = processClassName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}