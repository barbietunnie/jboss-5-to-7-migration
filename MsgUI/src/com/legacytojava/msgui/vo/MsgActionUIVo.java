package com.legacytojava.msgui.vo;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.component.UIInput;
import javax.faces.model.SelectItem;

import com.legacytojava.message.constant.MsgDataType;
import com.legacytojava.message.dao.action.MsgActionDetailDao;
import com.legacytojava.message.dao.action.MsgDataTypeDao;
import com.legacytojava.message.vo.BaseVo;
import com.legacytojava.message.vo.action.MsgActionDetailVo;
import com.legacytojava.message.vo.action.MsgActionVo;
import com.legacytojava.message.vo.action.MsgDataTypeVo;
import com.legacytojava.msgui.util.SpringUtil;

public class MsgActionUIVo extends BaseVo {
	private static final long serialVersionUID = 7955771124737863106L;
	private final MsgActionVo msgActionVo;

	public MsgActionUIVo(MsgActionVo vo) {
		msgActionVo = vo;
	}
	
	public MsgActionVo getMsgActionVo() {
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
	
	private static MsgDataTypeDao msgDataTypeDao = null;
	private static MsgDataTypeDao getMsgDataTypeDao() {
		if (msgDataTypeDao == null) {
			msgDataTypeDao = (MsgDataTypeDao) SpringUtil.getWebAppContext().getBean(
					"msgDataTypeDao");
		}
		return msgDataTypeDao;
	}
	private static MsgActionDetailDao msgActionDetailDao = null;
	private static MsgActionDetailDao getMsgActionDetailDao() {
		if (msgActionDetailDao == null) {
			msgActionDetailDao = (MsgActionDetailDao) SpringUtil.getWebAppContext().getBean(
					"msgActionDetailDao");
		}
		return msgActionDetailDao;
	}
	/*
	 * get current data type by Action ID
	 */
	private String getCurrentDataType() {
		MsgActionDetailVo vo = getMsgActionDetailDao().getByActionId(getActionId());
		if (vo != null) {
			return vo.getDataType();
		}
		else {
			return null;
		}
	}
	
	/**
	 * @return a list of data type values by AcyionId
	 */
	public SelectItem[] getDataTypeValuesList() {
		List<MsgDataTypeVo> list = getMsgDataTypeDao().getByDataType(getCurrentDataType());
		SelectItem[] values = new SelectItem[list.size()];
		for (int i = 0; i < list.size(); i++) {
			String dataValue = list.get(i).getDataTypeValue();
			values[i] = new SelectItem(dataValue, dataValue);
		}
		return values;
	}
	
	/**
	 * @return true if current ActionId links to a DataType
	 */
	public boolean getHasDataTypeValues() {
		List<MsgDataTypeVo> list = getMsgDataTypeDao().getByDataType(getCurrentDataType());
		return (list.size() > 0);
	}
	
	/**
	 * @return true if current data type is an email address type
	 */
	public boolean getIsDataTypeEmailAddress() {
		return (MsgDataType.EMAIL_ADDRESS.equals(getCurrentDataType()));
	}
	
	/**
	 * @return true if current data type is not an email address type
	 */
	public boolean getIsDataTypeNotEmailAddress() {
		return !(MsgDataType.EMAIL_ADDRESS.equals(getCurrentDataType()));
	}
	
	/**
	 * convert comma delimited string to a string array
	 * @return a string array
	 */
	public String[] getDataTypeValuesUI() {
		if (getDataTypeValues() == null) return new String[0];
		StringTokenizer st = new StringTokenizer(getDataTypeValues(), ",");
		String[] tokens = new String[st.countTokens()];
		int i=0;
		while (st.hasMoreTokens()) {
			tokens[i++] = st.nextToken();
		}
		return tokens;
	}
	/**
	 * convert a string array to a comma delimited string
	 * @param values a string array
	 */
	public void setDataTypeValuesUI(String[] values) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; values != null && i < values.length; i++) {
			if (i == 0)
				sb.append(values[i]);
			else
				sb.append("," + values[i]);
		}
		if (sb.length() > 0)
			setDataTypeValues(sb.toString());
		else
			setDataTypeValues(null);
	}
	/** end of UI components */

	public String getActionId() {
		return msgActionVo.getActionId();
	}

	public int getActionSeq() {
		return msgActionVo.getActionSeq();
	}

	public String getClientId() {
		return msgActionVo.getClientId();
	}

	public String getDataType() {
		return msgActionVo.getDataType();
	}

	public String getDataTypeValues() {
		return msgActionVo.getDataTypeValues();
	}

	public String getProcessBeanId() {
		return msgActionVo.getProcessBeanId();
	}

	public String getProcessClassName() {
		return msgActionVo.getProcessClassName();
	}

	public int getRowId() {
		return msgActionVo.getRowId();
	}

	public String getRuleName() {
		return msgActionVo.getRuleName();
	}

	public Timestamp getStartTime() {
		return msgActionVo.getStartTime();
	}

	public String getStatusIdDesc() {
		return msgActionVo.getStatusIdDesc();
	}

	public void setActionId(String actionId) {
		msgActionVo.setActionId(actionId);
	}

	public void setActionSeq(int actionSeq) {
		msgActionVo.setActionSeq(actionSeq);
	}

	public void setClientId(String clientId) {
		msgActionVo.setClientId(clientId);
	}

	public void setDataType(String dataType) {
		msgActionVo.setDataType(dataType);
	}

	public void setDataTypeValues(String dataTypeValues) {
		msgActionVo.setDataTypeValues(dataTypeValues);
	}

	public void setProcessBeanId(String processBeanId) {
		msgActionVo.setProcessBeanId(processBeanId);
	}

	public void setProcessClassName(String processClassName) {
		msgActionVo.setProcessClassName(processClassName);
	}

	public void setRowId(int rowId) {
		msgActionVo.setRowId(rowId);
	}

	public void setRuleName(String ruleName) {
		msgActionVo.setRuleName(ruleName);
	}

	public void setStartTime(Timestamp startTime) {
		msgActionVo.setStartTime(startTime);
	}
}
