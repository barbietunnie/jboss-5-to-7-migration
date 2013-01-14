package com.legacytojava.message.vo;

public class IdTokensVo extends BaseVo implements java.io.Serializable {	
	private static final long serialVersionUID = -2226574685229227694L;
	
	private int rowId = -1;
	private String clientId = "";
	private int clientRowId = -1;
	private String description = null;
	private String bodyBeginToken = "";
	private String bodyEndToken = "";
	private String xHeaderName = null;
	private String xhdrBeginToken = null;
	private String xhdrEndToken = null;
	private int maxLength = -1;
	
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String sentId) {
		this.clientId = sentId;
	}
	public int getClientRowId() {
		return clientRowId;
	}
	public void setClientRowId(int clientRowId) {
		this.clientRowId = clientRowId;
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
