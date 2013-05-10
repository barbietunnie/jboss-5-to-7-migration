package jpa.msgui.bean;

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

import jpa.exception.DataValidationException;
import jpa.model.EmailTemplate;
import jpa.model.EmailVariable;
import jpa.msgui.util.DynamicCodes;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.EmailTemplateService;
import jpa.service.EmailVariableService;
import jpa.util.BlobUtil;
import jpa.util.HtmlUtil;
import jpa.util.SenderUtil;
import jpa.variable.RenderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class EmailTemplateBean {
	static final Logger logger = Logger.getLogger(EmailTemplateBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private EmailTemplateService emailTemplateDao = null;
	private EmailVariableService emailVariableDao = null;
	private DataModel<EmailTemplate> emailTemplates = null;
	private EmailTemplate emailTemplate = null;
	private boolean editMode = true;
	private DataModel<Object> dateList = null;
	
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
	
	public DataModel<EmailTemplate> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (emailTemplates == null) {
			List<EmailTemplate> emailTemplateList = null;
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				emailTemplateList = getEmailTemplateService().getAll();
			}
			else {
				emailTemplateList = getEmailTemplateService().getAll();
			}
			emailTemplates = new ListDataModel<EmailTemplate>(emailTemplateList);
		}
		return emailTemplates;
	}

	public String refresh() {
		emailTemplates = null;
		return "";
	}
	
	public EmailTemplateService getEmailTemplateService() {
		if (emailTemplateDao == null) {
			emailTemplateDao = (EmailTemplateService) SpringUtil.getWebAppContext().getBean("emailTemplateService");
		}
		return emailTemplateDao;
	}

	public void setEmailTemplateService(EmailTemplateService emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}
	
	public EmailVariableService getEmailVariableService() {
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableService) SpringUtil.getWebAppContext().getBean(
					"emailVariableService");
		}
		return emailVariableDao;
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
		this.emailTemplate = (EmailTemplate) emailTemplates.getRowData();
		logger.info("viewEmailTemplate() - EmailTemplate to be edited: "
				+ emailTemplate.getTemplateId());
		emailTemplate.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewEmailTemplate() - EmailTemplate to be passed to jsp: "
					+ emailTemplate);
		}
		return TO_EDIT;
	}

	private void checkVariableLoop(String text) throws DataValidationException {
		List<String> varNames = RenderUtil.retrieveVariableNames(text);
		for (String loopName : varNames) {
			EmailVariable vo = getEmailVariableService().getByVariableName(loopName);
			if (vo != null) {
				RenderUtil.checkVariableLoop(vo.getDefaultValue(), loopName);
			}
		}
	}

	public String saveEmailTemplate() {
		if (isDebugEnabled)
			logger.debug("saveEmailTemplate() - Entering...");
		if (emailTemplate == null) {
			logger.warn("saveEmailTemplate() - EmailTemplate is null.");
			return TO_FAILED;
		}
		reset();
		// validate templates
		try {
			checkVariableLoop(emailTemplate.getBodyText());
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		try {
			checkVariableLoop(emailTemplate.getSubject());
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			emailTemplate.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (!emailTemplate.isHtml()) { // make sure HTML is set correctly
			emailTemplate.setHtml(HtmlUtil.isHTML(emailTemplate.getBodyText()));
		}
		if (editMode == true) {
			getEmailTemplateService().update(emailTemplate);
			logger.info("saveEmailTemplate() - Rows Updated: " + 1);
		}
		else {
			getEmailTemplateService().insert(emailTemplate);
			addToList(emailTemplate);
			logger.info("saveEmailTemplate() - Rows Inserted: " + 1);
		}
		return TO_SAVED;
	}

	public String editSchedules() {
		if (isDebugEnabled)
			logger.debug("editSchedules() - Entering...");
		this.emailTemplate = (EmailTemplate) emailTemplates.getRowData();
		dateList = new ArrayDataModel<Object>(emailTemplate.getSchedulesBlob().getDateList());
		return TO_SCHEDULE_EDIT;
	}
	
	public String saveSchedules() {
		if (isDebugEnabled)
			logger.debug("saveSchedules() - Entering...");
		getEmailTemplateService().update(emailTemplate);
		if (isDebugEnabled)
			logger.debug("saveSchedules() - rows updated: " + 1);
		return TO_SCHEDULE_SAVED;
	}
	
	@SuppressWarnings("unchecked")
	private void addToList(EmailTemplate vo) {
		List<EmailTemplate> list = (List<EmailTemplate>) emailTemplates.getWrappedData();
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
		List<EmailTemplate> smtpList = getEmailTemplateList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailTemplate vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailTemplateService().deleteByTemplateId(vo.getTemplateId());
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
		List<EmailTemplate> smtpList = getEmailTemplateList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailTemplate vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.emailTemplate = (EmailTemplate) BlobUtil.deepCopy(vo);
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
		this.emailTemplate = new EmailTemplate();
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
		List<EmailTemplate> smtpList = getEmailTemplateList();
		for (Iterator<EmailTemplate> it=smtpList.iterator(); it.hasNext();) {
			EmailTemplate vo = it.next();
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
		EmailTemplate vo = getEmailTemplateService().getByTemplateId(templateId);
		if (editMode == true && vo != null && emailTemplate != null
				&& vo.getRowId() != emailTemplate.getRowId()) {
			// emailTemplate does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					//"com.legacytojava.msgui.messages", "emailTemplateDoesNotExist", null);
	        		"com.legacytojava.msgui.messages", "emailTemplateAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// emailTemplate already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
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
		FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
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
		if (emailTemplate == null) {
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
	private List<EmailTemplate> getEmailTemplateList() {
		if (emailTemplates == null) {
			return new ArrayList<EmailTemplate>();
		}
		else {
			return (List<EmailTemplate>)emailTemplates.getWrappedData();
		}
	}
	
	public EmailTemplate getEmailTemplate() {
		return emailTemplate;
	}

	public void setEmailTemplate(EmailTemplate emailTemplate) {
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

	public DataModel<Object> getDateList() {
		return dateList;
	}

	public void setDateList(DataModel<Object> schedulesBlob) {
		this.dateList = schedulesBlob;
	}
}