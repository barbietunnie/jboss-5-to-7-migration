package com.es.vo.address;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.es.data.constant.CodeType;
import com.es.data.constant.MailingListDeliveryType;
import com.es.data.constant.MailingListType;
import com.es.vo.comm.BaseVoWithRowId;

public class EmailTemplateVo extends BaseVoWithRowId implements Serializable {
	private static final long serialVersionUID = -5007781927317135437L;
	private String templateId = "";
	private String listId = "";
	private String subject = null;
	private String bodyText = null;
	private boolean isHtml = true;
	private String listType = MailingListType.TRADITIONAL.getValue();
	private String deliveryOption = MailingListDeliveryType.ALL_ON_LIST.getValue();
	private String selectCriteria = null;
	private String embedEmailId = " "; // use system default
	private String isBuiltIn = CodeType.NO_CODE.getValue();
	private SchedulesBlob schedulesBlob = null;
	private String origTemplateId = null;
	private String senderId = "";
	
	/** define components for UI */
	public String getDeliveryOptionDesc() {
		if (MailingListDeliveryType.ALL_ON_LIST.getValue().equals(deliveryOption)) {
			return "All on list";
		}
		else if (MailingListDeliveryType.SUBSCRIBERS_ONLY.getValue().equals(deliveryOption)) {
			return "Subscribers only";
		}
		else if (MailingListDeliveryType.PROSPECTS_ONLY.getValue().equals(deliveryOption)) {
			return "Prospects only";
		}
		return "";
	}
	
	public String getSubjectShort() {
		return StringUtils.left(subject, 50);
	}
	
	public boolean isPersonalized() {
		return MailingListType.PERSONALIZED.getValue().equalsIgnoreCase(listType);
	}
	
	public boolean getIsBuiltInTemplate() {
		return CodeType.YES_CODE.getValue().equals(isBuiltIn);
	}
	/** end of UI */

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public boolean getIsHtml() {
		return isHtml;
	}

	public void setIsHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

	public String getDeliveryOption() {
		return deliveryOption;
	}

	public void setDeliveryOption(String deliveryOption) {
		this.deliveryOption = deliveryOption;
	}

	public String getEmbedEmailId() {
		return embedEmailId;
	}

	public void setEmbedEmailId(String embedEmailId) {
		this.embedEmailId = embedEmailId;
	}

	public String getIsBuiltIn() {
		return isBuiltIn;
	}

	public void setIsBuiltIn(String isBuiltIn) {
		this.isBuiltIn = isBuiltIn;
	}
	
	public String getSelectCriteria() {
		return selectCriteria;
	}

	public void setSelectCriteria(String selectCriteria) {
		this.selectCriteria = selectCriteria;
	}

	public SchedulesBlob getSchedulesBlob() {
		if (schedulesBlob == null)
			schedulesBlob = new SchedulesBlob();
		return schedulesBlob;
	}

	public void setSchedulesBlob(SchedulesBlob schedulesBlob) {
		this.schedulesBlob = schedulesBlob;
	}

	public String getOrigTemplateId() {
		return origTemplateId;
	}

	public void setOrigTemplateId(String origTemplateId) {
		this.origTemplateId = origTemplateId;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
}