package com.legacytojava.message.vo.action;

import java.io.Serializable;

import com.legacytojava.message.vo.BaseVo;

public class MsgDataTypeVo extends BaseVo implements Serializable
{
	private static final long serialVersionUID = 1257470921464461217L;
	private int rowId = -1;
	private String dataType = "";
	private String dataTypeValue = "";
	private String miscProperties = null;
	
	public MsgDataTypeVo() {
	}
	
	public MsgDataTypeVo(String dataType, String dataTypeValue, String miscProperties) {
		super();
		this.dataType = dataType;
		this.dataTypeValue = dataTypeValue;
		this.miscProperties = miscProperties;
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
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