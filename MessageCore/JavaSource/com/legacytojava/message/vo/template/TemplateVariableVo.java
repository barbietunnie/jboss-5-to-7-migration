package com.legacytojava.message.vo.template;

import java.io.Serializable;

public class TemplateVariableVo extends GlobalVariableVo implements Serializable
{
	private static final long serialVersionUID = 7372301662242870634L;
	private String templateId = "";
	private String clientId = "";

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
}