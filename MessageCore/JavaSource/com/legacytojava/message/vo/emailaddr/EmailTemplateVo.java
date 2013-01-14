package com.legacytojava.message.vo.emailaddr;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MailingListDeliveryOption;
import com.legacytojava.message.constant.MailingListType;
import com.legacytojava.message.dao.emailaddr.SchedulesBlob;
import com.legacytojava.message.vo.BaseVo;

public class EmailTemplateVo extends BaseVo implements Serializable {
	private static final long serialVersionUID = -5007781927317135437L;
	private String templateId = "";
	private String listId = "";
	private String subject = null;
	private String bodyText = null;
	private boolean isHtml = true;
	private String listType = MailingListType.TRADITIONAL;
	private String deliveryOption = MailingListDeliveryOption.ALL_ON_LIST;
	private String selectCriteria = null;
	private String embedEmailId = " "; // use system default
	private String isBuiltIn = Constants.NO_CODE;
	private SchedulesBlob schedulesBlob = null;
	private String origTemplateId = null;
	private String clientId = "";
	
	/** define components for UI */
	public String getDeliveryOptionDesc() {
		if (MailingListDeliveryOption.ALL_ON_LIST.equals(deliveryOption)) {
			return "All on list";
		}
		else if (MailingListDeliveryOption.CUSTOMERS_ONLY.equals(deliveryOption)) {
			return "Customers only";
		}
		else if (MailingListDeliveryOption.PROSPECTS_ONLY.equals(deliveryOption)) {
			return "Prospects only";
		}
		return "";
	}
	
	public String getSubjectShort() {
		return StringUtils.left(subject, 50);
	}
	
	public boolean isPersonalized() {
		return MailingListType.PERSONALIZED.equalsIgnoreCase(listType);
	}
	
	public boolean getIsBuiltInTemplate() {
		return Constants.YES_CODE.equals(isBuiltIn);
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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}