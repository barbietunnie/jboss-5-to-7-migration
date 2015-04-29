package com.es.jaxrs.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="errorResponse")
public class ErrorResponse implements java.io.Serializable {
	private static final long serialVersionUID = -5392895881239058633L;

	@XmlElement(required=true)
	private int httpStatus;
	@XmlElement(required=true)
	private int errorCode;
	@XmlElement(required=true)
	private String errorMessage;
	
	public ErrorResponse() {}
	
	public int getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
}
