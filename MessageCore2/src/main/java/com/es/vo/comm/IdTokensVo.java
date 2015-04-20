package com.es.vo.comm;


public class IdTokensVo extends BaseVoWithRowId implements java.io.Serializable {	
	private static final long serialVersionUID = -2226574685229227694L;
	
	private String senderId = "";
	private int senderRowId = -1;
	private String description = null;
	private String bodyBeginToken = "";
	private String bodyEndToken = "";
	private String xHeaderName = null;
	private String xhdrBeginToken = null;
	private String xhdrEndToken = null;
	private int maxLength = -1;
	
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String sentId) {
		this.senderId = sentId;
	}
	public int getSenderRowId() {
		return senderRowId;
	}
	public void setSenderRowId(int senderRowId) {
		this.senderRowId = senderRowId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getBodyBeginToken() {
		return bodyBeginToken;
	}
	public void setBodyBeginToken(String bodyBeginToken) {
		this.bodyBeginToken = bodyBeginToken;
	}
	public String getBodyEndToken() {
		return bodyEndToken;
	}
	public void setBodyEndToken(String bodyEndToken) {
		this.bodyEndToken = bodyEndToken;
	}
	public int getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	public String getXhdrBeginToken() {
		return xhdrBeginToken;
	}
	public void setXhdrBeginToken(String xhdrBeginToken) {
		this.xhdrBeginToken = xhdrBeginToken;
	}
	public String getXhdrEndToken() {
		return xhdrEndToken;
	}
	public void setXhdrEndToken(String xhdrEndToken) {
		this.xhdrEndToken = xhdrEndToken;
	}
	public String getXHeaderName() {
		return xHeaderName;
	}
	public void setXHeaderName(String headerName) {
		xHeaderName = headerName;
	}
}
