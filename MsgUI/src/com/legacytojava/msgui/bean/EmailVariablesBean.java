package com.legacytojava.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import com.legacytojava.message.bo.template.RenderUtil;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.emailaddr.EmailVariableDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.external.VariableResolver;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.EmailVariableVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class EmailVariablesBean {
	static final Logger logger = Logger.getLogger(EmailVariablesBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private EmailVariableDao emailVariableDao = null;
	private DataModel emailVariables = null;
	private EmailVariableVo emailVariable = null;
	private boolean editMode = true;
	
	private UIInput variableNameInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	final static String TO_EDIT = "emailvariable.edit";
	final static String TO_SELF = "emailvariable.delf";
	final static String TO_SAVED = "emailvariable.saved";
	final static String TO_FAILED = "emailvariable.failed";
	final static String TO_DELETED = "emailvariable.deleted";
	final static String TO_CANCELED = "emailvariable.canceled";
	
	public DataModel getAll() {
		if (emailVariables == null) {
			List<EmailVariableVo> emailVariableList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				emailVariableList = getEmailVariableDao().getAllForTrial();
			}
			else {
				emailVariableList = getEmailVariableDao().getAll();
			}
			emailVariables = new ListDataModel(emailVariableList);
		}
		return emailVariables;
	}

	public String refresh() {
		emailVariables = null;
		return "";
	}
	
	public EmailVariableDao getEmailVariableDao() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableDao) SpringUtil.getWebAppContext().getBean("emailVariableDao");
		}
		return emailVariableDao;
	}

	public void setEmailVariableDao(EmailVariableDao emailVariableDao) {
		this.emailVariableDao = emailVariableDao;
	}
	
	public String viewEmailVariable() {
		if (isDebugEnabled)
			logger.debug("viewEmailVariable() - Entering...");
		if (emailVariables == null) {
			logger.warn("viewEmailVariable() - EmailVariable List is null.");
			return TO_FAILED;
		}
		if (!emailVariables.isRowAvailable()) {
			logger.warn("viewEmailVariable() - EmailVariable Row not available.");
			return TO_FAILED;
		}
		reset();
		this.emailVariable = (EmailVariableVo) emailVariables.getRowData();
		logger.info("viewEmailVariable() - EmailVariable to be edited: "
				+ emailVariable.getVariableName());
		emailVariable.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewEmailVariable() - EmailVariableVo to be passed to jsp: "
					+ emailVariable);
		}
		return TO_EDIT;
	}
	
	public String testEmailVariable() {
		if (isDebugEnabled)
			logger.debug("testEmailVariable() - Entering...");
		if (emailVariable == null) {
			logger.warn("testEmailVariable() - EmailVariableVo is null.");
			return TO_FAILED;
		}
		isQueryValid();
		return TO_SELF;
	}
	
	private boolean isQueryValid() {
		String query = emailVariable.getVariableQuery();
		if (query == null || query.trim().length() == 0) {
			testResult = "variableQueryIsBlank";
			return true;
		}
		else {
			try {
				getEmailVariableDao().getByQuery(query, 1L);
				testResult = "variableQueryTestSuccess";
				return true;
			}
			catch (Exception e) {
				//logger.fatal("Exception caught", e);
				testResult = "variableQueryTestFailure";
				return false;
			}
			finally {
			}
		}
	}
	
	public String saveEmailVariable() {
		if (isDebugEnabled)
			logger.debug("saveEmailVariable() - Entering...");
		if (emailVariable == null) {
			logger.warn("saveEmailVariable() - EmailVariableVo is null.");
			return TO_FAILED;
		}
		reset();
		// validate user input
		if (isQueryValid()==false) {
			return TO_SELF;
		}
		String className = emailVariable.getVariableProc();
		if (className != null && className.trim().length() > 0) {
			Class<?> proc = null;
			try {
				proc = Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				testResult = "variableClassNotFound";
				return TO_SELF;
			}
			try {
				Object obj = proc.newInstance();
				if (!(obj instanceof VariableResolver)) {
					throw new Exception("Variable class is not a VariableResolver");
				}
			}
			catch (Exception e) {
				testResult = "variableClassNotValid";
				return TO_SELF;
			}
		}
		// validate variable loops
		try {
			RenderUtil.checkVariableLoop(emailVariable.getDefaultValue(), emailVariable
					.getVariableName());
		}
		catch (DataValidationException e) {
			actionFailure = e.getMessage();
			return TO_SELF;
		}
		// end of validate
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			emailVariable.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getEmailVariableDao().update(emailVariable);
			logger.info("saveEmailVariable() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getEmailVariableDao().insert(emailVariable);
			if (rowsInserted > 0)
				addToList(emailVariable);
			logger.info("saveEmailVariable() - Rows Inserted: " + rowsInserted);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(EmailVariableVo vo) {
		List<EmailVariableVo> list = (List<EmailVariableVo>) emailVariables.getWrappedData();
		list.add(vo);
	}
	
	public String deleteEmailVariables() {
		if (isDebugEnabled)
			logger.debug("deleteEmailVariables() - Entering...");
		if (emailVariables == null) {
			logger.warn("deleteEmailVariables() - EmailVariable List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailVariableVo> smtpList = getEmailVariableList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailVariableVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailVariableDao().deleteByName(vo.getVariableName());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailVariables() - EmailVariable deleted: "
							+ vo.getVariableName());
				}
				smtpList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public String copyEmailVariable() {
		if (isDebugEnabled)
			logger.debug("copyEmailVariable() - Entering...");
		if (emailVariables == null) {
			logger.warn("copyEmailVariable() - EmailVariable List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailVariableVo> smtpList = getEmailVariableList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailVariableVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.emailVariable = (EmailVariableVo) vo.getClone();
					emailVariable.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					this.emailVariable = new EmailVariableVo();
				}
				emailVariable.setVariableName(null);
				emailVariable.setMarkedForEdition(true);
				editMode = false;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public String addEmailVariable() {
		if (isDebugEnabled)
			logger.debug("addEmailVariable() - Entering...");
		reset();
		this.emailVariable = new EmailVariableVo();
		emailVariable.setMarkedForEdition(true);
		editMode = false;
		return TO_EDIT;
	}
	
	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}
	
	public boolean getAnyListsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyListsMarkedForDeletion() - Entering...");
		if (emailVariables == null) {
			logger.warn("getAnyListsMarkedForDeletion() - EmailVariable List is null.");
			return false;
		}
		List<EmailVariableVo> smtpList = getEmailVariableList();
		for (Iterator<EmailVariableVo> it=smtpList.iterator(); it.hasNext();) {
			EmailVariableVo vo = it.next();
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
		String variableName = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - variableName: " + variableName);
		EmailVariableVo vo = getEmailVariableDao().getByName(variableName);
		if (editMode == true && vo == null) {
			// emailVariable does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "emailVariableDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// emailVariable already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "emailVariableAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
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
		if (isDebugEnabled)
			logger.debug("fieldValueChanged(ValueChangeEvent) - " + e.getComponent().getId() + ": "
					+ e.getOldValue() + " -> " + e.getNewValue());
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		variableNameInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<EmailVariableVo> getEmailVariableList() {
		if (emailVariables == null) {
			return new ArrayList<EmailVariableVo>();
		}
		else {
			return (List<EmailVariableVo>)emailVariables.getWrappedData();
		}
	}
	
	public EmailVariableVo getEmailVariable() {
		return emailVariable;
	}

	public void setEmailVariable(EmailVariableVo emailVariable) {
		this.emailVariable = emailVariable;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public UIInput getVariableNameInput() {
		return variableNameInput;
	}

	public void setVariableNameInput(UIInput variableNameInput) {
		this.variableNameInput = variableNameInput;
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
}
