package com.legacytojava.message.vo.action;

import java.io.Serializable;
import java.sql.Timestamp;

import com.legacytojava.message.vo.BaseVo;

public class MsgActionVo extends BaseVo implements Serializable
{
	private static final long serialVersionUID = 5576050695865725099L;
	private int rowId = -1;
	private String ruleName = "";
	private int actionSeq = -1;
	private Timestamp startTime;
	private String clientId = null;
	private String actionId = "";
	private String dataTypeValues = null;
	
	private String processBeanId = "";
	private String processClassName = null;
	private String dataType = null;
	
	public MsgActionVo() {}
	
	public MsgActionVo(
			String ruleName,
			int actionSeq,
			Timestamp startTime,
			String clientId,
			String actionId,
			String statusId,
			String dataTypeValues) {
		super();
		this.ruleName = ruleName;
		this.actionSeq = actionSeq;
		this.startTime = startTime;
		this.clientId = clientId;
		this.actionId = actionId;
		setStatusId(statusId);
		this.dataTypeValues = dataTypeValues;
	}
	
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
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
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
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