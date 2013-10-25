package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

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
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.persistence.NoResultException;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.MailServerType;
import jpa.model.SmtpServer;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.msgout.SmtpServerService;
import jpa.util.SenderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="smtpServer")
@SessionScoped
public class SmtpServerBean implements java.io.Serializable {
	private static final long serialVersionUID = -2610108607170535587L;
	static final Logger logger = Logger.getLogger(SmtpServerBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private SmtpServerService smtpServerDao = null;
	private transient DataModel<SmtpServer> smtpServers = null;
	private SmtpServer smtpServer = null;
	private boolean editMode = true;
	
	private transient UIInput serverNameInput = null;
	private transient UIInput useSslInput = null;
	private transient UIInput useAuthInput = null;

	private String testResult = null;
	private String actionFailure = null;

	private static String TO_EDIT = "smtpServerEdit.xhtml";
	private static String TO_FAILED = null;
	private static String TO_SAVED = "configureSmtpServers.xhtml";
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public DataModel<SmtpServer> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (smtpServers == null) {
			List<SmtpServer> smtpServerList = null;
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				smtpServerList = getSmtpServerService().getAll(false, null);
			}
			else {
				smtpServerList = getSmtpServerService().getAll(false, null);
			}
			smtpServers = new ListDataModel<SmtpServer>(smtpServerList);
		}
		return smtpServers;
	}

	public String refresh() {
		smtpServers = null;
		return "";
	}
	
	public SmtpServerService getSmtpServerService() {
		if (smtpServerDao == null) {
			smtpServerDao = (SmtpServerService) SpringUtil.getWebAppContext().getBean("smtpServerService");
		}
		return smtpServerDao;
	}

	public void setSmtpServerService(SmtpServerService smtpServerDao) {
		this.smtpServerDao = smtpServerDao;
	}
	
	public String viewSmtpServer() {
		if (isDebugEnabled)
			logger.debug("viewSmtpServer() - Entering...");
		if (smtpServers == null) {
			logger.warn("viewSmtpServer() - SmtpServer List is null.");
			return TO_SAVED;
		}
		if (!smtpServers.isRowAvailable()) {
			logger.warn("viewSmtpServer() - SmtpServer Row not available.");
			return TO_FAILED;
		}
		reset();
		this.smtpServer = (SmtpServer) smtpServers.getRowData();
		logger.info("viewSmtpServer() - SmtpServer to be edited: " + smtpServer.getSmtpHostName());
		smtpServer.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewSmtpServer() - SmtpServer to be passed to jsp: " + smtpServer);
		
		return TO_EDIT;
	}
	
	public String saveSmtpServer() {
		if (isDebugEnabled)
			logger.debug("saveSmtpServer() - Entering...");
		if (smtpServer == null) {
			logger.warn("saveSmtpServer() - SmtpServer is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			smtpServer.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getSmtpServerService().update(smtpServer);
			logger.info("saveSmtpServer() - Rows Updated: " + 1);
		}
		else {
			getSmtpServerService().insert(smtpServer);
			addToList(smtpServer);
			logger.info("saveSmtpServer() - Rows Inserted: " + 1);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(SmtpServer vo) {
		List<SmtpServer> list = (List<SmtpServer>) smtpServers.getWrappedData();
		list.add(vo);
	}
	
	public String deleteSmtpServers() {
		if (isDebugEnabled)
			logger.debug("deleteSmtpServers() - Entering...");
		if (smtpServers == null) {
			logger.warn("deleteSmtpServers() - SmtpServer List is null.");
			return TO_FAILED;
		}
		reset();
		List<SmtpServer> smtpList = getSmtpServerList();
		for (int i=0; i<smtpList.size(); i++) {
			SmtpServer vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSmtpServerService().deleteByServerName(vo.getServerName());
				if (rowsDeleted > 0) {
					logger.info("SmtpServer deleted: " + vo.getServerName());
				}
				smtpList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public String testSmtpServer() {
		if (isDebugEnabled)
			logger.debug("testSmtpServer() - Entering...");
		if (smtpServer == null) {
			logger.warn("testSmtpServer() - SmtpServer is null.");
			return TO_FAILED;
		}
		String smtpHost = smtpServer.getSmtpHostName();
		int smtpPort = smtpServer.getSmtpPortNumber();
		String userId = smtpServer.getUserId();
		String password = smtpServer.getUserPswd();
		
		Session session = null;
		Properties sys_props = (Properties) System.getProperties().clone();
		sys_props.put("mail.smtp.host", smtpHost);
		String protocol = null;
		if (smtpServer.isUseSsl()) {
			sys_props.put("mail.smtps.auth", "true");
			sys_props.put("mail.user", userId);
			protocol = MailServerType.SMTPS.getValue();
		}
		else {
			protocol = MailServerType.SMTP.getValue();
		}
		sys_props.put("mail.host", smtpHost);
		sys_props.put("mail.smtp.connectiontimeout", "2000");
			// socket connection timeout value in milliseconds
		sys_props.put("mail.smtp.timeout", "2000");
			// socket I/O timeout value in milliseconds

		// Get a Session object
		session = Session.getInstance(sys_props);
		session.setDebug(true);
		Transport transport = null;
		try {
			transport = session.getTransport(protocol);
			if (smtpPort > 0) {
				transport.connect(smtpHost, smtpPort, userId, password);
			}
			else {
				transport.connect(smtpHost, userId, password);
			}
			testResult = "smtpServerTestSuccess";
		}
		catch (MessagingException e) {
			//logger.fatal("MessagingException caught", e);
			testResult = "smtpServerTestFailure";
		}
		finally {
			if (transport !=null) {
				try {
					transport.close();
				}
				catch (MessagingException e) {}
			}
		}
		
		return null;
	}
	
	public String copySmtpServer() {
		if (isDebugEnabled)
			logger.debug("copySmtpServer() - Entering...");
		if (smtpServers == null) {
			logger.warn("copySmtpServer() - SmtpServer List is null.");
			return TO_FAILED;
		}
		reset();
		List<SmtpServer> smtpList = getSmtpServerList();
		for (int i=0; i<smtpList.size(); i++) {
			SmtpServer vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.smtpServer = new SmtpServer();
				try {
					vo.copyPropertiesTo(this.smtpServer);
					smtpServer.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				smtpServer.setServerName(null);
				smtpServer.setMarkedForEdition(true);
				setDefaultValues(smtpServer);
				editMode = false;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public String addSmtpServer() {
		if (isDebugEnabled)
			logger.debug("addSmtpServer() - Entering...");
		reset();
		this.smtpServer = new SmtpServer();
		smtpServer.setMarkedForEdition(true);
		smtpServer.setUpdtUserId(Constants.DEFAULT_USER_ID);
		smtpServer.setUseSsl(false);
		setDefaultValues(smtpServer);
		editMode = false;
		return TO_EDIT;
	}
	
	void setDefaultValues(SmtpServer smtpServer) {
		smtpServer.setRetryFrequence(10); // in seconds
		smtpServer.setMessageCount(0);
		smtpServer.setAlertAfter(5);
		smtpServer.setAlertLevel("error");
	}
	
	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}
	
	public boolean getAnyServersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyServersMarkedForDeletion() - Entering...");
		if (smtpServers == null) {
			logger.warn("getAnyServersMarkedForDeletion() - SmtpServer List is null.");
			return false;
		}
		List<SmtpServer> smtpList = getSmtpServerList();
		for (Iterator<SmtpServer> it=smtpList.iterator(); it.hasNext();) {
			SmtpServer vo = it.next();
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
		String serverName = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - serverName: " + serverName);
		try {
			SmtpServer vo = getSmtpServerService().getByServerName(serverName);
			if (editMode == false && vo != null) {
				// smtpServer already exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "smtpServerAlreadyExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
		catch (NoResultException e) {
			if (editMode == true) {
				// smtpServer does not exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "smtpServerDoesNotExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	public boolean getIsUseSslInput() {
		if (useSslInput != null) {
			if (useSslInput.getLocalValue() != null) {
				return CodeType.YES.getValue().equals(useSslInput.getLocalValue());
			}
			else if (useSslInput.getValue() != null) {
				return CodeType.YES.getValue().equals(useSslInput.getValue());
			}
		}
		return true; // for safety
	}
	
	public boolean getIsUseAuthInput() {
		if (useAuthInput != null) {
			if (useAuthInput.getLocalValue() != null) {
				return CodeType.YES.getValue().equals(useAuthInput.getLocalValue());
			}
			else if (useAuthInput.getValue() != null) {
				return CodeType.YES.getValue().equals(useAuthInput.getValue());
			}
		}
		return true; // for safety
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
		serverNameInput = null;
		useSslInput = null;
		useAuthInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<SmtpServer> getSmtpServerList() {
		if (smtpServers == null) {
			return new ArrayList<SmtpServer>();
		}
		else {
			return (List<SmtpServer>)smtpServers.getWrappedData();
		}
	}
	
	public SmtpServer getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(SmtpServer smtpServer) {
		this.smtpServer = smtpServer;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public UIInput getServerNameInput() {
		return serverNameInput;
	}

	public void setServerNameInput(UIInput smtpHostInput) {
		this.serverNameInput = smtpHostInput;
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

	public UIInput getUseSslInput() {
		return useSslInput;
	}

	public void setUseSslInput(UIInput useSslInput) {
		this.useSslInput = useSslInput;
	}

	public UIInput getUseAuthInput() {
		return useAuthInput;
	}

	public void setUseAuthInput(UIInput useAuthInput) {
		this.useAuthInput = useAuthInput;
	}
}
