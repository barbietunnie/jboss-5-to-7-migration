package com.legacytojava.msgui.bean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import com.legacytojava.message.bo.template.RenderUtil;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.emailaddr.EmailTemplateDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.BlobUtil;
import com.legacytojava.message.util.HtmlTags;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;
import com.legacytojava.msgui.util.DynamicCodes;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class EmailTemplatesBean {
	static final Logger logger = Logger.getLogger(EmailTemplatesBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private EmailTemplateDao emailTemplateDao = null;
	private DataModel emailTemplates = null;
	private EmailTemplateVo emailTemplate = null;
	private boolean editMode = true;
	private DataModel dateList = null;
	
	private UIInput templateIdInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	final static String TO_FAILED = "emailtemplate.failed";
	final static String TO_EDIT = "emailtemplate.edit";
	final static String TO_SAVED = "emailtemplate.saved";
	final static String TO_DELETED = "emailtemplate.deleted";
	final static String TO_CANCELED = "emailtemplate.canceled";
	final static String TO_SELF = "emailtemplate.self";
	final static String TO_SCHEDULE_EDIT = "emailschedules.edit";
	final static String TO_SCHEDULE_SAVED = "emailschedules.saved";
	
	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (emailTemplates == null) {
			List<EmailTemplateVo> emailTemplateList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				emailTemplateList = getEmailTemplateDao().getAllForTrial();
			}
			else {
				emailTemplateList = getEmailTemplateDao().getAll();
			}
			emailTemplates = new ListDataModel(emailTemplateList);
		}
		return emailTemplates;
	}

	public String refresh() {
		emailTemplates = null;
		return "";
	}
	
	public EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = (EmailTemplateDao) SpringUtil.getWebAppContext().getBean("emailTemplateDao");
		}
		return emailTemplateDao;
	}

	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}
	
	public String viewEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("viewEmailTemplate() - Entering...");
		if (emailTemplates == null) {
			logger.warn("viewEmailTemplate() - EmailTemplate List is null.");
			return TO_FAILED;
		}
		if (!emailTemplates.isRowAvailable()) {
			logger.warn("viewEmailTemplate() - EmailTemplate Row not available.");
			return TO_FAILED;
		}
		reset();
		this.emailTemplate = (EmailTemplateVo) emailTemplates.getRowData();
		logger.info("viewEmailTemplate() - EmailTemplate to be edited: "
				+ emailTemplate.getTemplateId());
		emailTemplate.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewEmailTemplate() - EmailTemplateVo to be passed to jsp: "
					+ emailTemplate);
		}
		return TO_EDIT;
	}
	
	public String saveEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("saveEmailTemplate() - Entering...");
		if (emailTemplate == null) {
			logger.warn("saveEmailTemplate() - EmailTemplateVo is null.");
			return TO_FAILED;
		}
		reset();
		// validate templates
		try {
			RenderUtil.checkVariableLoop(emailTemplate.getBodyText());
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		try {
			RenderUtil.checkVariableLoop(emailTemplate.getSubject());
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			emailTemplate.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (!emailTemplate.getIsHtml()) { // make sure HTML is set correctly
			emailTemplate.setIsHtml(HtmlTags.isHTML(emailTemplate.getBodyText()));
		}
		if (editMode == true) {
			int rowsUpdated = getEmailTemplateDao().update(emailTemplate);
			logger.info("saveEmailTemplate() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getEmailTemplateDao().insert(emailTemplate);
			if (rowsInserted > 0)
				addToList(emailTemplate);
			logger.info("saveEmailTemplate() - Rows Inserted: " + rowsInserted);
		}
		return TO_SAVED;
	}

	public String editSchedules() {
		if (isDebugEnabled)
			logger.debug("editSchedules() - Entering...");
		this.emailTemplate = (EmailTemplateVo) emailTemplates.getRowData();
		dateList = new ArrayDataModel(emailTemplate.getSchedulesBlob().getDateList());
		return TO_SCHEDULE_EDIT;
	}
	
	public String saveSchedules() {
		if (isDebugEnabled)
			logger.debug("saveSchedules() - Entering...");
		int rowsUpdated = getEmailTemplateDao().update(emailTemplate);
		if (isDebugEnabled)
			logger.debug("saveSchedules() - rows updated: " + rowsUpdated);
		return TO_SCHEDULE_SAVED;
	}
	
	@SuppressWarnings("unchecked")
	private void addToList(EmailTemplateVo vo) {
		List<EmailTemplateVo> list = (List<EmailTemplateVo>) emailTemplates.getWrappedData();
		list.add(vo);
	}
	
	public String deleteEmailTemplates() {
		if (isDebugEnabled)
			logger.debug("deleteEmailTemplates() - Entering...");
		if (emailTemplates == null) {
			logger.warn("deleteEmailTemplates() - EmailTemplate List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailTemplateVo> smtpList = getEmailTemplateList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailTemplateVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailTemplateDao().deleteByTemplateId(vo.getTemplateId());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailTemplates() - EmailTemplate deleted: "
							+ vo.getTemplateId());
				}
				smtpList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public String copyEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("copyEmailTemplate() - Entering...");
		if (emailTemplates == null) {
			logger.warn("copyEmailTemplate() - EmailTemplate List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailTemplateVo> smtpList = getEmailTemplateList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailTemplateVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.emailTemplate = (EmailTemplateVo) BlobUtil.deepCopy(vo);
				emailTemplate.setMarkedForDeletion(false);
				emailTemplate.setTemplateId(null);
				emailTemplate.getSchedulesBlob().reset();
				emailTemplate.setMarkedForEdition(true);
				editMode = false;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public String addEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("addEmailTemplate() - Entering...");
		reset();
		this.emailTemplate = new EmailTemplateVo();
		emailTemplate.setMarkedForEdition(true);
		emailTemplate.setSubject("");
		emailTemplate.setBodyText("");
		editMode = false;
		return TO_EDIT;
	}
	
	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}
	
	public boolean getAnyTemplatesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyTemplatesMarkedForDeletion() - Entering...");
		if (emailTemplates == null) {
			logger.warn("getAnyTemplatesMarkedForDeletion() - EmailTemplate List is null.");
			return false;
		}
		List<EmailTemplateVo> smtpList = getEmailTemplateList();
		for (Iterator<EmailTemplateVo> it=smtpList.iterator(); it.hasNext();) {
			EmailTemplateVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * validate primary key
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String templateId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - templateId: " + templateId);
		EmailTemplateVo vo = getEmailTemplateDao().getByTemplateId(templateId);
		if (editMode == true && vo != null && emailTemplate != null
				&& vo.getRowId() != emailTemplate.getRowId()) {
			// emailTemplate does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					//"com.legacytojava.msgui.messages", "emailTemplateDoesNotExist", null);
	        		"com.legacytojava.msgui.messages", "emailTemplateAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// emailTemplate already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "emailTemplateAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void checkDate(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("checkDate() - date = " + value);
		if (value == null) return;
		if (value instanceof Date) {
		    Calendar cal = Calendar.getInstance();
		    cal.setTime((Date)value);
			return;
		}
		((UIInput)component).setValid(false);
		FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
				"com.legacytojava.msgui.messages", "invalidDate", null);
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		context.addMessage(component.getClientId(context), message);
	}
	
	/**
	 * actionListener
	 * @param e
	 */
	public void actionFired(ActionEvent e) {
		logger.info("actionFired(ActionEvent) - " + e.getComponent().getId());
	}
	
	/**
	 * valueChangeEventListener
	 * @param e
	 */
	public void fieldValueChanged(ValueChangeEvent e) {
		logger.info("fieldValueChanged(ValueChangeEvent) - " + e.getComponent().getId() + ": "
				+ e.getOldValue() + " -> " + e.getNewValue());
		if (emailTemplate != null) {
			emailTemplate.setListType((String)e.getNewValue());
		}
		FacesContext.getCurrentInstance().renderResponse();
	}
	
	public SelectItem[] getEmailVariables() {
		DynamicCodes codes = (DynamicCodes) FacesUtil.getApplicationMapValue("dynacodes");
		SelectItem[] codes1 = codes.getEmailVariableNameItems();
		SelectItem[] codes2 = codes.getGlobalVariableNameItems();
		if (emailTemplate == null || emailTemplate.isPersonalized()) {
			SelectItem[] codesAll = new SelectItem[codes1.length + codes2.length];
			System.arraycopy(codes1, 0, codesAll, 0, codes1.length);
			System.arraycopy(codes2, 0, codesAll, codes1.length, codes2.length);
			return codesAll;
		}
		else {
			return codes2;
		}
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		templateIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<EmailTemplateVo> getEmailTemplateList() {
		if (emailTemplates == null) {
			return new ArrayList<EmailTemplateVo>();
		}
		else {
			return (List<EmailTemplateVo>)emailTemplates.getWrappedData();
		}
	}
	
	public EmailTemplateVo getEmailTemplate() {
		return emailTemplate;
	}

	public void setEmailTemplate(EmailTemplateVo emailTemplate) {
		this.emailTemplate = emailTemplate;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}
	
	public UIInput getTemplateIdInput() {
		return templateIdInput;
	}

	public void setTemplateIdInput(UIInput templateIdInput) {
		this.templateIdInput = templateIdInput;
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}

	public DataModel getDateList() {
		return dateList;
	}

	public void setDateList(DataModel schedulesBlob) {
		this.dateList = schedulesBlob;
	}
}
