package com.es.vo.outbox;
	
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.es.vo.comm.BaseVo;

public class MsgRenderedVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -5337491762091825390L;
	private long renderId = -1;
	private String msgSourceId = "";
	private String templateId = "";
	private Timestamp startTime;
	private String senderId = null;
	private String subrId = null;
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
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getMsgSourceId() {
		return msgSourceId;
	}
	public void setMsgSourceId(String msgSourceId) {
		this.msgSourceId = msgSourceId;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getSubrId() {
		return subrId;
	}
	public void setSubrId(String subrId) {
		this.subrId = subrId;
	}
	public Integer getPurgeAfter() {
		return purgeAfter;
	}
	public void setPurgeAfter(Integer purgeAfter) {
		this.purgeAfter = purgeAfter;
	}
}
