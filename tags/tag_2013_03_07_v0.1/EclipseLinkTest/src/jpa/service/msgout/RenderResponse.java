package jpa.service.msgout;
	
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

import jpa.message.MessageBean;
import jpa.model.message.MessageSource;
import jpa.variable.ErrorVariableVo;
import jpa.variable.RenderVariableVo;

public class RenderResponse implements Serializable {
	private static final long serialVersionUID = -735532735569912023L;
	MessageSource msgSourceVo;
	String clientId;
	Timestamp startTime;
	Map<String, RenderVariableVo> variableFinal;
	Map<String, ErrorVariableVo> variableErrors;
	MessageBean messageBean;

	RenderResponse(
			MessageSource msgSourceVo,
			String clientId,
			Timestamp startTime,
			Map<String, RenderVariableVo> variableFinal,
			Map<String, ErrorVariableVo> variableErrors,
			MessageBean messageBean)
		{
			this.msgSourceVo=msgSourceVo;
			this.clientId=clientId;
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
		sb.append("ClientId:        "+clientId+LF);
		sb.append("StartTime:       "+startTime+LF+LF);
		if (variableFinal!=null && !variableFinal.isEmpty()) {
			sb.append("Display Final Variables.........."+LF);
			Collection<RenderVariableVo> c = variableFinal.values();
			for (Iterator<RenderVariableVo> it=c.iterator(); it.hasNext();) {
				RenderVariableVo req = it.next();
				sb.append(req.toString());
			}
		}
		else {
			sb.append("VariableFinal:    "+"null"+LF);
		}
		if (variableErrors!=null && !variableErrors.isEmpty()) {
			sb.append("Display Error Variables.........."+LF);
			Collection<ErrorVariableVo> c = variableErrors.values();
			for (Iterator<ErrorVariableVo> it=c.iterator(); it.hasNext();) {
				ErrorVariableVo req = it.next();
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
	   
    public String getClientId() {
		return clientId;
	}

	public MessageBean getMessageBean() {
		return messageBean;
	}

	public MessageSource getMessageSource() {
		return msgSourceVo;
	}

	public Map<String, ErrorVariableVo> getVariableErrors() {
		return variableErrors;
	}

	public Map<String, RenderVariableVo> getVariableFinal() {
		return variableFinal;
	}

	public Timestamp getStartTime() {
		return startTime;
	}
}
