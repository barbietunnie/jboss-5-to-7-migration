package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;
import javax.persistence.NoResultException;

import jpa.exception.DataValidationException;
import jpa.model.EmailVariable;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.EmailVariableService;
import jpa.service.external.VariableResolver;
import jpa.util.SenderUtil;
import jpa.variable.RenderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="emailVariable")
@SessionScoped
public class EmailVariableBean implements java.io.Serializable {
	private static final long serialVersionUID = 8620743959575480890L;
	static final Logger logger = Logger.getLogger(EmailVariableBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private EmailVariableService emailVariableDao = null;
	private DataModel<EmailVariable> emailVariables = null;
	private EmailVariable emailVariable = null;
	private boolean editMode = true;
	
	private UIInput variableNameInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	final static String TO_EDIT = "emailVariableEdit";
	final static String TO_SELF = TO_EDIT;
	final static String TO_SAVED = "configureEmailVariables";
	final static String TO_FAILED = null;
	final static String TO_DELETED = TO_SAVED;
	final static String TO_CANCELED = TO_SAVED;
	
	public DataModel<EmailVariable> getAll() {
		if (emailVariables == null) {
			List<EmailVariable> emailVariableList = null;
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				emailVariableList = getEmailVariableService().getAll();
			}
			else {
				emailVariableList = getEmailVariableService().getAll();
			}
			emailVariables = new ListDataModel<EmailVariable>(emailVariableList);
		}
		return emailVariables;
	}

	public String refresh() {
		emailVariables = null;
		return "";
	}
	
	public EmailVariableService getEmailVariableService() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableService) SpringUtil.getWebAppContext().getBean(
					"emailVariableService");
		}
		return emailVariableDao;
	}

	public void setEmailVariableService(EmailVariableService emailVariableDao) {
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
		this.emailVariable = (EmailVariable) emailVariables.getRowData();
		logger.info("viewEmailVariable() - EmailVariable to be edited: "
				+ emailVariable.getVariableName());
		emailVariable.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewEmailVariable() - EmailVariable to be passed to jsp: "
					+ emailVariable);
		}
		return TO_EDIT;
	}
	
	public String testEmailVariable() {
		if (isDebugEnabled)
			logger.debug("testEmailVariable() - Entering...");
		if (emailVariable == null) {
			logger.warn("testEmailVariable() - EmailVariable is null.");
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
				getEmailVariableService().getByQuery(query, 1);
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
			logger.warn("saveEmailVariable() - EmailVariable is null.");
			return TO_FAILED;
		}
		reset();
		// validate user input
		if (isQueryValid()==false) {
			return TO_SELF;
		}
		String className = emailVariable.getVariableProcName();
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
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			emailVariable.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getEmailVariableService().update(emailVariable);
			logger.info("saveEmailVariable() - Rows Updated: " + 1);
		}
		else {
			getEmailVariableService().insert(emailVariable);
			addToList(emailVariable);
			logger.info("saveEmailVariable() - Rows Inserted: " + 1);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(EmailVariable vo) {
		List<EmailVariable> list = (List<EmailVariable>) emailVariables.getWrappedData();
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
		List<EmailVariable> smtpList = getEmailVariableList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailVariable vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailVariableService().deleteByVariableName(vo.getVariableName());
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
		List<EmailVariable> smtpList = getEmailVariableList();
		for (int i=0; i<smtpList.size(); i++) {
			EmailVariable vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.emailVariable = new EmailVariable();
				try {
					vo.copyPropertiesTo(this.emailVariable);
					emailVariable.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
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
		this.emailVariable = new EmailVariable();
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
		List<EmailVariable> smtpList = getEmailVariableList();
		for (Iterator<EmailVariable> it=smtpList.iterator(); it.hasNext();) {
			EmailVariable vo = it.next();
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
		try {
			getEmailVariableService().getByVariableName(variableName);
			if (editMode == false) {
				// emailVariable already exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "emailVariableAlreadyExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
		catch (NoResultException e) {
			if (editMode == true) {
				// emailVariable does not exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "emailVariableDoesNotExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
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
	private List<EmailVariable> getEmailVariableList() {
		if (emailVariables == null) {
			return new ArrayList<EmailVariable>();
		}
		else {
			return (List<EmailVariable>)emailVariables.getWrappedData();
		}
	}
	
	public EmailVariable getEmailVariable() {
		return emailVariable;
	}

	public void setEmailVariable(EmailVariable emailVariable) {
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
