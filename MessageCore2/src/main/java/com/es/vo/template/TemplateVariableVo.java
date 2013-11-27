package com.es.vo.template;

import java.io.Serializable;

public class TemplateVariableVo extends GlobalVariableVo implements Serializable
{
	private static final long serialVersionUID = 7372301662242870634L;
	private String templateId = "";
	private String senderId = "";

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
}