package com.es.bo.render;
	
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

import com.es.bo.render.ErrorVariable;
import com.es.bo.render.RenderVariable;
import com.es.msgbean.MessageBean;
import com.es.vo.template.MsgSourceVo;

public class RenderResponse implements Serializable {
	private static final long serialVersionUID = -735532735569912023L;
	MsgSourceVo msgSourceVo;
	String senderId;
	Timestamp startTime;
	Map<String, RenderVariable> variableFinal;
	Map<String, ErrorVariable> variableErrors;
	MessageBean messageBean;

	public RenderResponse(
			MsgSourceVo msgSourceVo,
			String senderId,
			Timestamp startTime,
			Map<String, RenderVariable> variableFinal,
			Map<String, ErrorVariable> variableErrors,
			MessageBean messageBean)
		{
			this.msgSourceVo=msgSourceVo;
			this.senderId=senderId;
			this.startTime=startTime;
			this.variableFinal=variableFinal;
			this.variableErrors=variableErrors;
			this.messageBean=messageBean;
    }

	public String toString()
	{
		String LF = System.getProperty("line.separator","\n");
		StringBuffer sb = new StringBuffer();
		sb.append("========== Display RenderResponse Fields =========="+LF);
		if (msgSourceVo!=null) {
			sb.append(msgSourceVo.toString());
		}
		else {
			sb.append("MsgSourceReq:     "+"null"+LF);
		}
		sb.append("SenderId:        "+senderId+LF);
		sb.append("StartTime:       "+startTime+LF+LF);
		if (variableFinal!=null && !variableFinal.isEmpty()) {
			sb.append("Display Final Variables.........."+LF);
			Collection<RenderVariable> c = variableFinal.values();
			for (Iterator<RenderVariable> it=c.iterator(); it.hasNext();) {
				RenderVariable req = it.next();
				sb.append(req.toString());
			}
		}
		else {
			sb.append("VariableFinal:    "+"null"+LF);
		}
		if (variableErrors!=null && !variableErrors.isEmpty()) {
			sb.append("Display Error Variables.........."+LF);
			Collection<ErrorVariable> c = variableErrors.values();
			for (Iterator<ErrorVariable> it=c.iterator(); it.hasNext();) {
				ErrorVariable req = it.next();
				sb.append(req.toString());
			}
		}
		else {
			sb.append("VariableErrors:   "+"null"+LF);
		}
		if (messageBean!=null)
			sb.append(LF+messageBean.toString());
		return sb.toString();
	}
	   
    public String getSenderId() {
		return senderId;
	}

	public MessageBean getMessageBean() {
		return messageBean;
	}

	public MsgSourceVo getMsgSourceVo() {
		return msgSourceVo;
	}

	public Map<String, ErrorVariable> getVariableErrors() {
		return variableErrors;
	}

	public Map<String, RenderVariable> getVariableFinal() {
		return variableFinal;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setMsgSourceVo(MsgSourceVo msgSourceVo) {
		this.msgSourceVo = msgSourceVo;
	}
}
