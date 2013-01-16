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
import javax.mail.Store;

import org.apache.log4j.Logger;

import com.legacytojava.message.constant.CarrierCode;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.mailbox.MailBoxDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.MailBoxVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class MailboxesBean {
	static final Logger logger = Logger.getLogger(MailboxesBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();

	private MailBoxDao mailBoxDao = null;
	private DataModel mailBoxes = null;
	private MailBoxVo mailbox = null;
	private boolean editMode = true;
	
	private UIInput userIdInput = null;
	private UIInput hostNameInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "mailbox.edit";
	private static String TO_FAILED = "mailbox.failed";
	private static String TO_SAVED = "mailbox.saved";
	private static String TO_DELETED = "mailbox.deleted";
	private static String TO_CANCELED = "mailbox.canceled";

	public String refresh() {
		mailBoxes = null;
		return "";
	}
	
	public MailBoxDao getMailBoxDao() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (mailBoxDao == null) {
			mailBoxDao = (MailBoxDao) SpringUtil.getWebAppContext().getBean("mailBoxDao");
		}
		return mailBoxDao;
	}

	public void setMailBoxDao(MailBoxDao mailBoxDao) {
		this.mailBoxDao = mailBoxDao;
	}
	
	public DataModel getAll() {
		if (mailBoxes == null) {
			List<MailBoxVo> mailBoxList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				mailBoxList = getMailBoxDao().getAllForTrial(false);
			}
			else {
				mailBoxList = getMailBoxDao().getAll(false);
			}
			mailBoxes = new ListDataModel(mailBoxList);
		}
		return mailBoxes;
	}

	public String viewMailBox() {
		if (isDebugEnabled)
			logger.debug("viewMailBox() - Entering...");
		if (mailBoxes == null) {
			logger.warn("viewMailBox() - MailBox List is null.");
			return TO_FAILED;
		}
		if (!mailBoxes.isRowAvailable()) {
			logger.warn("viewMailBox() - MailBox Row not available.");
			return TO_FAILED;
		}
		reset();
		this.mailbox = (MailBoxVo) mailBoxes.getRowData();
		if (isInfoEnabled) {
			logger.info("viewMailBox() - Mailbox to be edited: " + mailbox.getUserId() + "@"
					+ mailbox.getHostName());
		}
		mailbox.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewMailBox() - MailBoxVo to be passed to jsp: " + mailbox);
		
		return TO_EDIT;
	}
	
	public String saveMailbox() {
		if (isDebugEnabled)
			logger.debug("saveMailbox() - Entering...");
		if (mailbox == null) {
			logger.warn("saveMailbox() - MailBoxVo is null.");
			return TO_FAILED;
		}
		reset();
		if (validatePrimaryKey(mailbox.getHostName(), mailbox.getUserId()) != null) {
			return TO_FAILED;
		}
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			mailbox.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getMailBoxDao().update(mailbox);
			logger.info("saveMailBox() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getMailBoxDao().insert(mailbox);
			if (rowsInserted > 0)
				addToList(mailbox);
			logger.info("saveMailBox() - Rows Inserted: " + rowsInserted);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(MailBoxVo vo) {
		List<MailBoxVo> list = (List<MailBoxVo>) mailBoxes.getWrappedData();
		list.add(vo);
	}
	
	public String deleteMailBoxes() {
		if (isDebugEnabled)
			logger.debug("deleteMailBoxes() - Entering...");
		if (mailBoxes == null) {
			logger.warn("deleteMailBoxes() - MailBox List is null.");
			return TO_FAILED;
		}
		reset();
		List<MailBoxVo> mboxList = getMailBoxList();
		for (int i=0; i<mboxList.size(); i++) {
			MailBoxVo vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMailBoxDao().deleteByPrimaryKey(vo.getUserId(),
						vo.getHostName());
				if (rowsDeleted > 0) {
					logger.info("deleteMailBoxes() - Mailbox deleted: " + vo.getUserId() + "@"
							+ vo.getHostName());
				}
				mboxList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public String testMailbox() {
		if (isDebugEnabled)
			logger.debug("testMailbox() - Entering...");
		if (mailbox == null) {
			logger.warn("testMailbox() - MailBoxVo is null.");
			return TO_FAILED;
		}
		Properties m_props = (Properties) System.getProperties().clone();
		Session session = null;
		// Get a Session object
		if ("yes".equalsIgnoreCase(mailbox.getUseSsl())) {
			m_props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			m_props.setProperty("mail.pop3.socketFactory.fallback", "false");
			m_props.setProperty("mail.pop3.port", mailbox.getPortNumber()+"");
			m_props.setProperty("mail.pop3.socketFactory.port", mailbox.getPortNumber()+"");
			session = Session.getInstance(m_props);
		}
		else {
			session = Session.getInstance(m_props, null);
		}
		session.setDebug(true);
		Store store = null;
		try {
			// Get a Store object
			store = session.getStore(mailbox.getProtocol());
			// connect to the store
			store.connect(mailbox.getHostName(), mailbox.getPortNumber(),
					mailbox.getUserId(), mailbox.getUserPswd());
			
			testResult = "mailboxTestSuccess";
		}
		catch (MessagingException me) {
			//logger.fatal("MessagingException caught", me);
			testResult = "mailboxTestFailure";
		}
		finally {
			if (store != null) {
				try {
					store.close();
				}
				catch (Exception e) {}
			}
		}
		/* Add to Face message queue. Not working. */
        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
				"com.legacytojava.msgui.messages", testResult, null);
		FacesContext.getCurrentInstance().addMessage(null, message);
		
		return null;
	}
	
	public String copyMailbox() {
		if (isDebugEnabled)
			logger.debug("copyMailbox() - Entering...");
		if (mailBoxes == null) {
			logger.warn("copyMailbox() - MailBox List is null.");
			return TO_FAILED;
		}
		reset();
		List<MailBoxVo> mboxList = getMailBoxList();
		for (int i=0; i<mboxList.size(); i++) {
			MailBoxVo vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.mailbox = (MailBoxVo) vo.getClone();
					mailbox.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					mailbox = new MailBoxVo();
					setDefaultValues(mailbox);
				}
				mailbox.setHostName(null);
				mailbox.setUserId(null);
				mailbox.setMarkedForEdition(true);
				editMode = false;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public String addMailbox() {
		if (isDebugEnabled)
			logger.debug("addMailbox() - Entering...");
		reset();
		this.mailbox = new MailBoxVo();
		mailbox.setMarkedForEdition(true);
		mailbox.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mailbox.setUseSsl("No");
		mailbox.setToPlainText("No");
		mailbox.setReadPerPass(5);
		setDefaultValues(mailbox);
		editMode = false;
		return TO_EDIT;
	}
	
	private void setDefaultValues(MailBoxVo mailbox) {
		// default values, not present on screen
		mailbox.setProcessorName("mailProcessor");
		mailbox.setCheckDuplicate(Constants.YES);
		mailbox.setAlertDuplicate(Constants.YES);
		mailbox.setLogDuplicate(Constants.YES);
		mailbox.setPurgeDupsAfter(24); // in hours
		mailbox.setCarrierCode(CarrierCode.SMTPMAIL);
		mailbox.setInternalOnly(Constants.NO);
		mailbox.setMessageCount(-1);
	}
	
	public String cancelEdit() {
		return TO_CANCELED;
	}
	
	public boolean getAnyMailBoxsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMailBoxsMarkedForDeletion() - Entering...");
		if (mailBoxes == null) {
			logger.warn("getAnyMailBoxsMarkedForDeletion() - MailBox List is null.");
			return false;
		}
		List<MailBoxVo> mboxList = getMailBoxList();
		for (Iterator<MailBoxVo> it=mboxList.iterator(); it.hasNext();) {
			MailBoxVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String userId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - UserId: " + userId);
		MailBoxVo vo = (MailBoxVo) getMailBoxDao().getByPrimaryKey(userId, mailbox.getHostName());
		if (editMode == true && vo != null && mailbox != null
				&& vo.getRowId() != mailbox.getRowId()) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "mailboxAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "mailboxAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	private String validatePrimaryKey(String hostName, String userId) {
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - hostName/userId: " + hostName + "/" + userId);
		MailBoxVo vo = (MailBoxVo) getMailBoxDao().getByPrimaryKey(userId, hostName);
		if (editMode == true && vo != null && vo.getRowId() != mailbox.getRowId()) {
			// mailbox does not exist
			testResult = "mailboxAlreadyExist"; //"mailboxDoesNotExist";
		}
		else if (editMode == false && vo != null) {
			// mailbox already exist
			testResult = "mailboxAlreadyExist";
		}
		return testResult;
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
	public void hostNameOrUserIdChanged(ValueChangeEvent e) {
		logger.info("hostNameOrUserIdChanged(ValueChangeEvent) - " + e.getComponent().getId()
				+ ": " + e.getOldValue() + " -> " + e.getNewValue());
		//FacesContext.getCurrentInstance().renderResponse();
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		userIdInput = null;
		hostNameInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<MailBoxVo> getMailBoxList() {
		if (mailBoxes == null) {
			return new ArrayList<MailBoxVo>();
		}
		else {
			return (List<MailBoxVo>)mailBoxes.getWrappedData();
		}
	}
	
	public MailBoxVo getMailbox() {
		return mailbox;
	}

	public void setMailbox(MailBoxVo mailbox) {
		this.mailbox = mailbox;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
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

	public UIInput getUserIdInput() {
		return userIdInput;
	}

	public void setUserIdInput(UIInput userIdInput) {
		this.userIdInput = userIdInput;
	}

	public UIInput getHostNameInput() {
		return hostNameInput;
	}

	public void setHostNameInput(UIInput hostNameInput) {
		this.hostNameInput = hostNameInput;
	}
}
