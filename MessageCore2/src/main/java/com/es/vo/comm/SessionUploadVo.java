package com.es.vo.comm;

import java.io.Serializable;
import java.sql.Timestamp;

public class SessionUploadVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 6484217849141134499L;
	private String sessionId = "";
	private int sessionSeq = -1;
	private String fileName = "";
	private String userId = "";
	private Timestamp createTime;
	private String contentType = null;
	private byte[] sessionValue = null;
	private long fileSize = 0;
	
	// Helpers
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public long getFileSize() {
		return this.fileSize;
	}
	
	// Getters/Setters
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public int getSessionSeq() {
		return sessionSeq;
	}
	public void setSessionSeq(int sessionSeq) {
		this.sessionSeq = sessionSeq;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public byte[] getSessionValue() {
		return sessionValue;
	}
	public void setSessionValue(byte[] sessionValue) {
		this.sessionValue = sessionValue;
	}
}