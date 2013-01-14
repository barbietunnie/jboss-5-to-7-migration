package com.legacytojava.message.vo.template;

import java.io.Serializable;

public class ClientVariableVo extends GlobalVariableVo implements Serializable
{
	private static final long serialVersionUID = 5123163629276526195L;
	private String clientId = "";

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}