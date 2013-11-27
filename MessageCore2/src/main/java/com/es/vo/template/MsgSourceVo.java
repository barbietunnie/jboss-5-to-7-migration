package com.es.vo.template;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.vo.comm.BaseVo;

public class MsgSourceVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -8801080860617417345L;
	private String msgSourceId = "";
	private String description = null;
	private Long fromAddrId = null;
	private Long replyToAddrId = null;
	private String templateDataId = "";
	private String templateVariableId = null;
	private String excludingIdToken = CodeType.NO_CODE.getValue();
	// Y - No email id will be embedded into message
	private String carrierCode = CarrierCode.SMTPMAIL.getValue();
	// Internet, WebMail, Internal Routing, ...
	private String allowOverride = CodeType.YES_CODE.getValue();
	// allow override templates, addresses to be supplied at runtime
	private String saveMsgStream = CodeType.YES_CODE.getValue();
	// Y - save rendered SMTP message stream to MSGOBSTREAM
	private String archiveInd = CodeType.NO_CODE.getValue();
	// Y - archive the rendered messages
	private Integer purgeAfter = null; // in month
	
	public MsgSourceVo() {
		updtTime = new Timestamp(new java.util.Date().getTime());
	}
	
	public String getAllowOverride() {
		return allowOverride;
	}
	public void setAllowOverride(String allowOverride) {
		this.allowOverride = allowOverride;
	}
	public String getArchiveInd() {
		return archiveInd;
	}
	public void setArchiveInd(String archiveInd) {
		this.archiveInd = archiveInd;
	}
	public String getTemplateDataId() {
		return templateDataId;
	}
	public void setTemplateDataId(String bodyTemplateId) {
		this.templateDataId = bodyTemplateId;
	}
	public String getCarrierCode() {
		return carrierCode;
	}
	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getExcludingIdToken() {
		return excludingIdToken;
	}
	public void setExcludingIdToken(String excludingIdToken) {
		this.excludingIdToken = excludingIdToken;
	}
	public Long getFromAddrId() {
		return fromAddrId;
	}
	public void setFromAddrId(Long fromAddrId) {
		this.fromAddrId = fromAddrId;
	}
	public String getMsgSourceId() {
		return msgSourceId;
	}
	public void setMsgSourceId(String msgSourceId) {
		this.msgSourceId = msgSourceId;
	}
	public Integer getPurgeAfter() {
		return purgeAfter;
	}
	public void setPurgeAfter(Integer purgeAfter) {
		this.purgeAfter = purgeAfter;
	}
	public Long getReplyToAddrId() {
		return replyToAddrId;
	}
	public void setReplyToAddrId(Long replyToAddrId) {
		this.replyToAddrId = replyToAddrId;
	}
	public String getSaveMsgStream() {
		return saveMsgStream;
	}
	public void setSaveMsgStream(String saveMsgStream) {
		this.saveMsgStream = saveMsgStream;
	}
	public String getTemplateVariableId() {
		return templateVariableId;
	}
	public void setTemplateVariableId(String templateVariableId) {
		this.templateVariableId = templateVariableId;
	}
	public String getUpdtUserId() {
		return updtUserId;
	}
}