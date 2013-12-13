package com.legacytojava.message.vo.template;

import java.io.Serializable;
import java.sql.Timestamp;

import com.legacytojava.message.vo.BaseVoWithRowId;

public class BodyTemplateVo extends BaseVoWithRowId implements Serializable
{
	private static final long serialVersionUID = -2565627868809110960L;
	private String templateId = "";
	private String clientId = null;
	private Timestamp startTime = new Timestamp(new java.util.Date().getTime());
	private String description = null;
	private String templateValue = null;
	private String contentType = null;
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
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