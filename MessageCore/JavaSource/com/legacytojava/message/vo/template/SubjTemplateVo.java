package com.legacytojava.message.vo.template;

import java.io.Serializable;
import java.sql.Timestamp;

import com.legacytojava.message.vo.BaseVoWithRowId;

public class SubjTemplateVo extends BaseVoWithRowId implements Serializable
{
	private static final long serialVersionUID = -1913573551779574674L;
	private String templateId = "";
	private String clientId = null;
	private Timestamp startTime = new Timestamp(new java.util.Date().getTime());
	private String description = null;
	private String templateValue = null;
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTemplateValue() {
		return templateValue;
	}

	public void setTemplateValue(String templateValue) {
		this.templateValue = templateValue;
	}
	
}