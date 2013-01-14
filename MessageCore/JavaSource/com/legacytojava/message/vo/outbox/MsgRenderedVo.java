package com.legacytojava.message.vo.outbox;
	
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.legacytojava.message.vo.BaseVo;

public class MsgRenderedVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -5337491762091825390L;
	private long renderId = -1;
	private String msgSourceId = "";
	private String subjTemplateId = "";
	private String bodyTemplateId = "";
	private Timestamp startTime;
	private String clientId = null;
	private String custId = null;
	private Integer purgeAfter = null;

	private List<RenderVariableVo> renderVariables;
	private List<RenderAttachmentVo> renderAttachments;
	
	public List<RenderVariableVo> getRenderVariables() {
		if (renderVariables==null)
			renderVariables = new ArrayList<RenderVariableVo>();
		return renderVariables;
	}
	public void setRenderVariables(List<RenderVariableVo> renderVariables) {
		this.renderVariables = renderVariables;
	}
	public List<RenderAttachmentVo> getRenderAttachments() {
		if (renderAttachments==null)
			renderAttachments = new ArrayList<RenderAttachmentVo>();
		return renderAttachments;
	}
	public void setRenderAttachments(List<RenderAttachmentVo> renderAttachments) {
		this.renderAttachments = renderAttachments;
	}
	
	public long getRenderId() {
		return renderId;
	}
	public void setRenderId(long renderId) {
		this.renderId = renderId;
	}
	public String getBodyTemplateId() {
		return bodyTemplateId;
	}
	public void setBodyTemplateId(String bodyTemplateId) {
		this.bodyTemplateId = bodyTemplateId;
	}
	public String getMsgSourceId() {
		return msgSourceId;
	}
	public void setMsgSourceId(String msgSourceId) {
		this.msgSourceId = msgSourceId;
	}
	public String getSubjTemplateId() {
		return subjTemplateId;
	}
	public void setSubjTemplateId(String subjTemplateId) {
		this.subjTemplateId = subjTemplateId;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getCustId() {
		return custId;
	}
	public void setCustId(String custId) {
		this.custId = custId;
	}
	public Integer getPurgeAfter() {
		return purgeAfter;
	}
	public void setPurgeAfter(Integer purgeAfter) {
		this.purgeAfter = purgeAfter;
	}
}
