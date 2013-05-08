package jpa.msgui.bean;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.faces.component.UIInput;
import javax.faces.model.SelectItem;
import javax.persistence.NoResultException;

import jpa.constant.StatusId;
import jpa.data.preload.RuleDataTypeEnum;
import jpa.model.rule.RuleAction;
import jpa.model.rule.RuleActionDetail;
import jpa.model.rule.RuleDataType;
import jpa.model.rule.RuleDataValue;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.BaseVo;
import jpa.service.rule.RuleActionDetailService;
import jpa.service.rule.RuleDataTypeService;

public class RuleActionUIVo extends BaseVo {
	private static final long serialVersionUID = 7955771124737863106L;
	private final RuleAction msgActionVo;

	public RuleActionUIVo(RuleAction vo) {
		msgActionVo = vo;
	}
	
	public RuleAction getRuleAction() {
		return msgActionVo;
	}
	
	/** define properties and methods for UI components */
	private UIInput startDateInput = null;
	public UIInput getStartDateInput() {
		return startDateInput;
	}
	public void setStartDateInput(UIInput startDateInput) {
		this.startDateInput = startDateInput;
	}
	
	private java.util.Date startDate = null;
	private int startHour = -1;
	public java.util.Date getStartDate() {
		if (startDate == null) {
			if (getStartTime() == null) {
				setStartTime(new Timestamp(new java.util.Date().getTime()));
			}
			startDate = new java.util.Date(getStartTime().getTime());
		}
		return startDate;
	}
	public void setStartDate(java.util.Date startDate) {
		this.startDate = startDate;
	}
	public int getStartHour() {
		if (startHour < 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(getStartTime().getTime());
			startHour = cal.get(Calendar.HOUR_OF_DAY);
		}
		return startHour;
	}
	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}
	
	private static RuleDataTypeService msgDataTypeDao = null;
	private static RuleDataTypeService getRuleDataTypeService() {
		if (msgDataTypeDao == null) {
			msgDataTypeDao = (RuleDataTypeService) SpringUtil.getWebAppContext().getBean(
					"ruleDataTypeService");
		}
		return msgDataTypeDao;
	}
	private static RuleActionDetailService msgActionDetailDao = null;
	private static RuleActionDetailService getRuleActionDetailService() {
		if (msgActionDetailDao == null) {
			msgActionDetailDao = (RuleActionDetailService) SpringUtil.getWebAppContext().getBean(
					"ruleActionDetailService");
		}
		return msgActionDetailDao;
	}
	/*
	 * get current data type by Action ID
	 */
	private String getCurrentDataType() {
		try {
			RuleActionDetail vo = getRuleActionDetailService().getByActionId(getActionId());
			if (vo.getRuleDataType()!=null) {
				return vo.getRuleDataType().getDataType();
			}
		}
		catch (NoResultException e) {
		}
		return null;
	}
	
	/**
	 * @return a list of data type values by ActionId
	 */
	public SelectItem[] getDataTypeValuesList() {
		RuleDataType rdt = getRuleDataTypeService().getByDataType(getCurrentDataType());
		List<RuleDataValue> list = rdt.getRuleDataValues();
		SelectItem[] values = new SelectItem[list.size()];
		for (int i = 0; i < list.size(); i++) {
			String dataValue = list.get(i).getRuleDataValuePK().getDataValue();
			values[i] = new SelectItem(dataValue, dataValue);
		}
		return values;
	}
	
	/**
	 * @return true if current ActionId links to a DataType
	 */
	public boolean getHasDataTypeValue() {
		try {
			RuleDataType rdt = getRuleDataTypeService().getByDataType(getCurrentDataType());
			return (rdt.getRuleDataValues() != null && rdt.getRuleDataValues().size() > 0);
		}
		catch (NoResultException e) {
			return false;
		}
	}
	
	/**
	 * @return true if current data type is an email address type
	 */
	public boolean getIsDataTypeEmailAddress() {
		return (RuleDataTypeEnum.EMAIL_ADDRESS.name().equals(getCurrentDataType()));
	}
	
	/**
	 * @return true if current data type is not an email address type
	 */
	public boolean getIsDataTypeNotEmailAddress() {
		return !(RuleDataTypeEnum.EMAIL_ADDRESS.name().equals(getCurrentDataType()));
	}
	
	/**
	 * convert comma delimited string to a string array
	 * @return a string array
	 */
	public String[] getDataTypeValuesUI() {
		if (getDataTypeValues() == null) return new String[0];
		String[] tokens = new String[getDataTypeValues().size()];
		int i=0;
		for (RuleDataValue dtv : getDataTypeValues()) {
			tokens[i++] = dtv.getRuleDataValuePK().getDataValue();
		}
		return tokens;
	}
//	/**
//	 * convert a string array to a comma delimited string
//	 * @param values a string array
//	 */
//	public void setDataTypeValuesUI(String[] values) {
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; values != null && i < values.length; i++) {
//			if (i == 0)
//				sb.append(values[i]);
//			else
//				sb.append("," + values[i]);
//		}
//		if (sb.length() > 0)
//			setDataTypeValues(sb.toString());
//		else
//			setDataTypeValues(null);
//	}
	/** end of UI components */

	public String getActionId() {
		return msgActionVo.getRuleActionDetail().getActionId();
	}

	public int getActionSeq() {
		return msgActionVo.getRuleActionPK().getActionSequence();
	}

	public String getSenderId() {
		return msgActionVo.getRuleActionPK().getSenderData().getSenderId();
	}

	public String getDataType() {
		if (msgActionVo.getRuleActionDetail().getRuleDataType()!=null) {
			return msgActionVo.getRuleActionDetail().getRuleDataType().getDataType();
		}
		else {
			return null;
		}
	}

	public List<RuleDataValue> getDataTypeValues() {
		if (msgActionVo.getRuleActionDetail().getRuleDataType()!=null) {
			return msgActionVo.getRuleActionDetail().getRuleDataType().getRuleDataValues();
		}
		else {
			return null;
		}
	}

	public String getProcessBeanId() {
		return msgActionVo.getRuleActionDetail().getServiceName();
	}

	public String getProcessClassName() {
		return msgActionVo.getRuleActionDetail().getClassName();
	}

	public int getRowId() {
		return msgActionVo.getRowId();
	}

	public String getRuleName() {
		return msgActionVo.getRuleActionPK().getRuleLogic().getRuleName();
	}

	public Timestamp getStartTime() {
		return msgActionVo.getRuleActionPK().getStartTime();
	}

	public String getStatusIdDesc() {
		return StatusId.getByValue(msgActionVo.getStatusId()).getValue();
	}

	public void setActionId(String actionId) {
		msgActionVo.getRuleActionDetail().setActionId(actionId);
	}

	public void setActionSeq(int actionSeq) {
		msgActionVo.getRuleActionPK().setActionSequence(actionSeq);
	}

	public void setSenderId(String senderId) {
		msgActionVo.getRuleActionPK().getSenderData().setSenderId(senderId);
	}

	public void setDataType(String dataType) {
		if (msgActionVo.getRuleActionDetail().getRuleDataType()!=null) {
			msgActionVo.getRuleActionDetail().getRuleDataType().setDataType(dataType);
		}
	}

	public void setDataTypeValues(List<RuleDataValue> dataTypeValues) {
		if (msgActionVo.getRuleActionDetail().getRuleDataType()!=null) {
			msgActionVo.getRuleActionDetail().getRuleDataType().setRuleDataValues(dataTypeValues);
		}
	}

	public void setProcessBeanId(String processBeanId) {
		 msgActionVo.getRuleActionDetail().setServiceName(processBeanId);
	}

	public void setProcessClassName(String processClassName) {
		 msgActionVo.getRuleActionDetail().setClassName(processClassName);
	}

	public void setRuleName(String ruleName) {
		msgActionVo.getRuleActionPK().getRuleLogic().setRuleName(ruleName);
	}

	public void setStartTime(Timestamp startTime) {
		msgActionVo.getRuleActionPK().setStartTime(startTime);
	}
}
