package com.es.vo.template;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.vo.comm.BaseVoWithRowId;

public class TemplateDataVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -2565627868809110960L;
	private String templateId = "";
	private String senderId = null;
	private Timestamp startTime = new Timestamp(new java.util.Date().getTime());
	private String description = null;
	private String bodyTemplate = null;
	private String subjTemplate = null;
	private String contentType = null;
	
	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
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

	public String getBodyTemplate() {
		return bodyTemplate;
	}

	public void setBodyTemplate(String templateValue) {
		this.bodyTemplate = templateValue;
	}

	public String getSubjTemplate() {
		return subjTemplate;
	}

	public void setSubjTemplate(String subjTemplate) {
		this.subjTemplate = subjTemplate;
	}
	
}