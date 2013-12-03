package com.es.vo.action;

import java.io.Serializable;
import java.sql.Timestamp;

import com.es.core.util.StringUtil;
import com.es.vo.comm.BaseVoWithRowId;

public class RuleActionDetailVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -3441560088153348986L;
	private String actionId = "";
	private String description = null;
	private String processBeanId = "";
	private String processClassName = null;
	private String dataType = null;
	
	public RuleActionDetailVo() {
	}
	
	public RuleActionDetailVo(
			String actionId,
			String description,
			String processBeanId,
			String processClassName,
			String dataType,
			Timestamp updtTime,
			String updtUserId) {
		super();
		this.actionId = actionId;
		this.description = description;
		this.processBeanId = processBeanId;
		this.processClassName = processClassName;
		this.dataType = dataType;
		this.updtTime = updtTime;
		this.updtUserId = updtUserId;
	}
	
	/** define properties and methods for UI component */
	public String getDescriptionShort() {
		return StringUtil.cutWithDots(description,30);
	}
	
	/** end of UI component */

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getProcessClassName() {
		return processClassName;
	}

	public void setProcessClassName(String processClassName) {
		this.processClassName = processClassName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getProcessBeanId() {
		return processBeanId;
	}

	public void setProcessBeanId(String processBeanId) {
		this.processBeanId = processBeanId;
	}
}