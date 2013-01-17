package com.legacytojava.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.faces.application.FacesMessage;
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

import org.apache.log4j.Logger;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MailServerType;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.smtp.SmtpServerDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.SmtpConnVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class SmtpServersBean {
	static final Logger logger = Logger.getLogger(SmtpServersBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private SmtpServerDao smtpServerDao = null;
	private DataModel smtpServers = null;
	private SmtpConnVo smtpServer = null;
	private boolean editMode = true;
	
	private UIInput serverNameInput = null;
	private UIInput useSslInput = null;
	private UIInput useAuthInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (smtpServers == null) {
			List<SmtpConnVo> smtpServerList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				smtpServerList = getSmtpServerDao().getAllForTrial(false);
			}
			else {
				smtpServerList = getSmtpServerDao().getAll(false);
			}
			smtpServers = new ListDataModel(smtpServerList);
		}
		return smtpServers;
	}

	public String refresh() {
		smtpServers = null;
		return "";
	}
	
	public SmtpServerDao getSmtpServerDao() {
		if (smtpServerDao == null) {
			smtpServerDao = (SmtpServerDao) SpringUtil.getWebAppContext().getBean("smtpServerDao");
		}
		return smtpServerDao;
	}

	public void setSmtpServerDao(SmtpServerDao smtpServerDao) {
		this.smtpServerDao = smtpServerDao;
	}
	
	public String viewSmtpServer() {
		if (isDebugEnabled)
			logger.debug("viewSmtpServer() - Entering...");
		if (smtpServers == null) {
			logger.warn("viewSmtpServer() - SmtpServer List is null.");
			return "smtpserver.failed";
		}
		if (!smtpServers.isRowAvailable()) {
			logger.warn("viewSmtpServer() - SmtpServer Row not available.");
			return "smtpserver.failed";
		}
		reset();
		this.smtpServer = (SmtpConnVo) smtpServers.getRowData();
		logger.info("viewSmtpServer() - SmtpServer to be edited: " + smtpServer.getSmtpHost());
		smtpServer.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewSmtpServer() - SmtpConnVo to be passed to jsp: " + smtpServer);
		
		return "smtpserver.edit";
	}
	
	public String saveSmtpServer() {
		if (isDebugEnabled)
			logger.debug("saveSmtpServer() - Entering...");
		if (smtpServer == null) {
			logger.warn("saveSmtpServer() - SmtpConnVo is null.");
			return "smtpserver.failed";
		}
		reset();
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			smtpServer.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getSmtpServerDao().update(smtpServer);
			logger.info("saveSmtpServer() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getSmtpServerDao().insert(smtpServer);
			if (rowsInserted > 0)
				addToList(smtpServer);
			logger.info("saveSmtpServer() - Rows Inserted: " + rowsInserted);
		}
		return "smtpserver.saved";
	}

	@SuppressWarnings("unchecked")
	private void addToList(SmtpConnVo vo) {
		List<SmtpConnVo> list = (List<SmtpConnVo>) smtpServers.getWrappedData();
		list.add(vo);
	}
	
	public String deleteSmtpServers() {
		if (isDebugEnabled)
			logger.debug("deleteSmtpServers() - Entering...");
		if (smtpServers == null) {
			logger.warn("deleteSmtpServers() - SmtpServer List is null.");
			return "smtpserver.failed";
		}
		reset();
		List<SmtpConnVo> smtpList = getSmtpServerList();
		for (int i=0; i<smtpList.size(); i++) {
			SmtpConnVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSmtpServerDao().deleteByPrimaryKey(vo.getServerName());
				if (rowsDeleted > 0) {
					logger.info("SmtpServer deleted: " + vo.getServerName());
				}
				smtpList.remove(vo);
			}
		}
		return "smtpserver.deleted";
	}
	
	public String testSmtpServer() {
		if (isDebugEnabled)
			logger.debug("testSmtpServer() - Entering...");
		if (smtpServer == null) {
			logger.warn("testSmtpServer() - SmtpConnVo is null.");
			return "smtpserver.failed";
		}
		String smtpHost = smtpServer.getSmtpHost();
		int smtpPort = smtpServer.getSmtpPort();
		String userId = smtpServer.getUserId();
		String password = smtpServer.getUserPswd();
		
		Session session = null;
		Properties sys_props = (Properties) System.getProperties().clone();
		sys_props.put("mail.smtp.host", smtpHost);
		String protocol = null;
		if ("yes".equalsIgnoreCase(smtpServer.getUseSsl())) {
			sys_props.put("mail.smtps.auth", "true");
			sys_props.put("mail.user", userId);
			protocol = MailServerType.SMTPS;
		}
		else {
			protocol = MailServerType.SMTP;
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
			return "smtpserver.failed";
		}
		reset();
		List<SmtpConnVo> smtpList = getSmtpServerList();
		for (int i=0; i<smtpList.size(); i++) {
			SmtpConnVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.smtpServer = (SmtpConnVo) vo.getClone();
					smtpServer.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					this.smtpServer = new SmtpConnVo();
				}
				smtpServer.setServerName(null);
				smtpServer.setMarkedForEdition(true);
				setDefaultValues(smtpServer);
				editMode = false;
				return "smtpserver.edit";
			}
		}
		return null;
	}
	
	public String addSmtpServer() {
		if (isDebugEnabled)
			logger.debug("addSmtpServer() - Entering...");
		reset();
		this.smtpServer = new SmtpConnVo();
		smtpServer.setMarkedForEdition(true);
		smtpServer.setUpdtUserId(Constants.DEFAULT_USER_ID);
		smtpServer.setUseSsl("No");
		setDefaultValues(smtpServer);
		editMode = false;
		return "smtpserver.edit";
	}
	
	void setDefaultValues(SmtpConnVo smtpServer) {
		smtpServer.setRetryFreq(10); // in seconds
		smtpServer.setMessageCount(0);
		smtpServer.setAlertAfter(5);
		smtpServer.setAlertLevel("error");
	}
	
	public String cancelEdit() {
		refresh();
		return "smtpserver.canceled";
	}
	
	public boolean getAnyServersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyServersMarkedForDeletion() - Entering...");
		if (smtpServers == null) {
			logger.warn("getAnyServersMarkedForDeletion() - SmtpServer List is null.");
			return false;
		}
		List<SmtpConnVo> smtpList = getSmtpServerList();
		for (Iterator<SmtpConnVo> it=smtpList.iterator(); it.hasNext();) {
			SmtpConnVo vo = it.next();
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
		SmtpConnVo vo = getSmtpServerDao().getByPrimaryKey(serverName);
		if (editMode == true && vo == null) {
			// smtpServer does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "smtpServerDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// smtpServer already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "smtpServerAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public boolean getIsUseSslInput() {
		if (useSslInput != null) {
			if (useSslInput.getLocalValue() != null) {
				return Constants.YES.equals(useSslInput.getLocalValue());
			}
			else if (useSslInput.getValue() != null) {
				return Constants.YES.equals(useSslInput.getValue());
			}
		}
		return true; // for safety
	}
	
	public boolean getIsUseAuthInput() {
		if (useAuthInput != null) {
			if (useAuthInput.getLocalValue() != null) {
				return Constants.YES.equals(useAuthInput.getLocalValue());
			}
			else if (useAuthInput.getValue() != null) {
				return Constants.YES.equals(useAuthInput.getValue());
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
	private List<SmtpConnVo> getSmtpServerList() {
		if (smtpServers == null) {
			return new ArrayList<SmtpConnVo>();
		}
		else {
			return (List<SmtpConnVo>)smtpServers.getWrappedData();
		}
	}
	
	public SmtpConnVo getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(SmtpConnVo smtpServer) {
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
