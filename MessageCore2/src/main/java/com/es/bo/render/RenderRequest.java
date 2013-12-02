package com.es.bo.render;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

import com.es.bo.render.RenderVariable;

public class RenderRequest implements Serializable {
	private static final long serialVersionUID = 1682554017067987597L;
	String msgSourceId;
	String senderId;
	Timestamp startTime;
	Map<String, RenderVariable> variableOverrides;

	public RenderRequest(
			String msgSourceId,
			String senderId,
			Timestamp effectiveDate,
			Map<String, RenderVariable> variableOverrides) {
		this.msgSourceId = msgSourceId;
		this.senderId = senderId;
		this.startTime = effectiveDate;
		this.variableOverrides = variableOverrides;
	}

	public String getMsgSourceId() {
		return msgSourceId;
	}

	public String getSenderId() {
		return senderId;
	}

	public Timestamp getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Map<String, RenderVariable> getVariableOverrides() {
		return variableOverrides;
	}

	public String toString() {
		String LF = System.getProperty("line.separator", "\n");
		StringBuffer sb = new StringBuffer();
		sb.append("========== Display RenderRequest Fields ==========" + LF);
		sb.append("MsgSourceId:       " + msgSourceId + LF);
		sb.append("SenderId:		  " + senderId + LF);
		sb.append("EffectiveDate:     " + (startTime == null ? "null" : startTime.toString())
				+ LF);
		sb.append("VariableOverrides: " + variableOverrides + LF);
		return sb.toString();
	}
}
