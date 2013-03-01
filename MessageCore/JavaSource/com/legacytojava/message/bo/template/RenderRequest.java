package com.legacytojava.message.bo.template;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

public class RenderRequest implements Serializable {
	private static final long serialVersionUID = 1682554017067987597L;
	String msgSourceId;
	String clientId;
	Timestamp startTime;
	HashMap<String, RenderVariable> variableOverrides;

	public RenderRequest(
			String msgSourceId,
			String clientId,
			Timestamp effectiveDate,
			HashMap<String, RenderVariable> variableOverrides) {
		this.msgSourceId = msgSourceId;
		this.clientId = clientId;
		this.startTime = effectiveDate;
		this.variableOverrides = variableOverrides;
	}

	public String toString() {
		String LF = System.getProperty("line.separator", "\n");
		StringBuffer sb = new StringBuffer();
		sb.append("========== Display RenderRequest Fields ==========" + LF);
		sb.append("MsgSourceId:       " + msgSourceId + LF);
		sb.append("ClientId:		  " + clientId + LF);
		sb.append("EffectiveDate:     " + (startTime == null ? "null" : startTime.toString())
				+ LF);
		sb.append("VariableOverrides: " + variableOverrides + LF);
		return sb.toString();
	}
}
