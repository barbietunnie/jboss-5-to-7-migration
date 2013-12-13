package com.legacytojava.message.vo.template;

import java.io.Serializable;
import java.sql.Timestamp;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.vo.BaseVoWithRowId;

public class GlobalVariableVo extends BaseVoWithRowId implements Serializable
{
	private static final long serialVersionUID = 8887386091033102579L;
	private String variableName = "";
	private Timestamp startTime = new Timestamp(new java.util.Date().getTime());
	private String variableValue = null;
	private String variableFormat= null;
	private String variableType = "";
	// T - text, N - numeric, D - DateField/time,
	// A - address, X - Xheader, L - LOB(Attachment)
	//private String statusId = Constants.ACTIVE;
	// A - Active, I - Inactive
	private String allowOverride = Constants.YES_CODE;
	// allow override value to be supplied at runtime, Y/N/M, M=Mandatory
	private String required = Constants.NO_CODE;
	
	public String getAllowOverride() {
		return allowOverride;
	}
	public void setAllowOverride(String allowOverride) {
		this.allowOverride = allowOverride;
	}
	public String getRequired() {
		return required;
	}
	public void setRequired(String required) {
		this.required = required;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public String getVariableFormat() {
		return variableFormat;
	}
	public void setVariableFormat(String variableFormat) {
		this.variableFormat = variableFormat;
	}
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	public String getVariableType() {
		return variableType;
	}
	public void setVariableType(String variableType) {
		this.variableType = variableType;
	}
	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}