package com.legacytojava.message.vo.emailaddr;

import java.io.Serializable;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.emailaddr.EmailVariableDao;
import com.legacytojava.message.vo.BaseVoWithRowId;

public class EmailVariableVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -1851563259118001846L;
	private String variableName = "";
	private String variableType = "";
	private String tableName = null;
	private String columnName = null;
	private String isBuiltIn = Constants.NO_CODE;
	private String defaultValue = null;
	private String variableQuery = null;
	private String variableProc = null;
	
	/**
	 * define components for UI
	 */
	public String getVariableQueryShort() {
		if (variableQuery == null || variableQuery.length() <= 40)
			return variableQuery;
		else
			return variableQuery.substring(0,40);
	}
	
	public String getClassNameShort() {
		if (variableProc == null || variableProc.length() <= 20) {
			return variableProc;
		}
		else {
			int lastDot = variableProc.lastIndexOf(".");
			if (lastDot > 0) {
				return variableProc.substring(lastDot);
			}
			else {
				return variableProc.substring(0,20);
			}
		}
	}
	
	public boolean getIsSystemVariable() {
		return EmailVariableDao.SYSTEM_VARIABLE.equals(variableType);
	}
	
	public boolean getIsBuiltInVariable() {
		return Constants.YES_CODE.equals(isBuiltIn);
	}
	/** end of UI components */
	
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
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getVariableQuery() {
		return variableQuery;
	}
	public void setVariableQuery(String variableQuery) {
		this.variableQuery = variableQuery;
	}
	public String getVariableProc() {
		return variableProc;
	}
	public void setVariableProc(String variableProc) {
		this.variableProc = variableProc;
	}
	public String getIsBuiltIn() {
		return isBuiltIn;
	}
	public void setIsBuiltIn(String isBuiltIn) {
		this.isBuiltIn = isBuiltIn;
	}
}