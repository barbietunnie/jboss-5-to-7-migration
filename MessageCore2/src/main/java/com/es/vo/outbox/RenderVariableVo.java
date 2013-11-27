package com.es.vo.outbox;

import java.io.Serializable;

import com.es.vo.comm.BaseVo;

public class RenderVariableVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = 3093934334206038940L;
	private long renderId = -1;
	private String variableName = null;
	private String variableFormat = null;
	private String variableType = null;
	private String variableValue = null;
	
	public long getRenderId() {
		return renderId;
	}
	public void setRenderId(long renderId) {
		this.renderId = renderId;
	}
	public String getVariableFormat() {
		return variableFormat;
	}
	public void setVariableFormat(String variableFormat) {
		this.variableFormat = variableFormat;
	}
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	public String getVariableType() {
		return variableType;
	}
	public void setVariableType(String variableType) {
		this.variableType = variableType;
	}
	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}
