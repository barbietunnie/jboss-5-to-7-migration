package com.es.vo.action;

import java.io.Serializable;

import com.es.vo.comm.BaseVoWithRowId;

public class RuleDataTypeVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = 1257470921464461217L;
	private String dataType = "";
	private String dataTypeValue = "";
	private String miscProperties = null;
	
	public RuleDataTypeVo() {
	}
	
	public RuleDataTypeVo(String dataType, String dataTypeValue, String miscProperties) {
		super();
		this.dataType = dataType;
		this.dataTypeValue = dataTypeValue;
		this.miscProperties = miscProperties;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataTypeValue() {
		return dataTypeValue;
	}

	public void setDataTypeValue(String dataTypeValue) {
		this.dataTypeValue = dataTypeValue;
	}
	
	public String getMiscProperties() {
		return miscProperties;
	}

	public void setMiscProperties(String miscProperties) {
		this.miscProperties = miscProperties;
	}
}