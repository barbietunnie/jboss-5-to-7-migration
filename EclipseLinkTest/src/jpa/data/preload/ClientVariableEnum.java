package jpa.data.preload;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.VariableType;

/*
 * define sample client variables
 */
public enum ClientVariableEnum {
	CurrentDateTime(null,null,VariableType.DATETIME,CodeType.YES_CODE),
	CurrentDate(null,"yyyy-MM-dd",VariableType.DATETIME,CodeType.YES_CODE),
	CurrentTime(null,"hh:mm:ss a",VariableType.DATETIME,CodeType.YES_CODE),
	ClientId(Constants.DEFAULT_CLIENTID,null,VariableType.TEXT,CodeType.YES_CODE);
	
	private String value;
	private String format;
	private VariableType type;
	private CodeType allowOverride;
	private ClientVariableEnum(String value, String format, VariableType type, CodeType allowOverride) {
		this.value=value;
		this.format=format;
		this.type=type;
		this.allowOverride=allowOverride;
	}

	public String getDefaultValue() {
		return value;
	}
	public String getVariableFormat() {
		return format;
	}
	public VariableType getVariableType() {
		return type;
	}
	public CodeType getAllowOverride() {
		return allowOverride;
	}
}
