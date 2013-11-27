package com.es.vo.template;

import java.io.Serializable;

public class SenderVariableVo extends GlobalVariableVo implements Serializable
{
	private static final long serialVersionUID = 5123163629276526195L;
	private String senderId = "";

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
}