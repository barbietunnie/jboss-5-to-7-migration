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
import javax.mail.Store;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.model.MailInbox;
import jpa.model.MailInboxPK;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.msgin.MailInboxService;
import jpa.util.SenderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="mailInbox")
@SessionScoped
public class MailInboxBean implements java.io.Serializable {
	private static final long serialVersionUID = 2069189605831996367L;
	static final Logger logger = Logger.getLogger(MailInboxBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();

	private MailInboxService mailBoxDao = null;
	private DataModel<MailInbox> mailBoxes = null;
	private MailInbox mailbox = null;
	private boolean editMode = true;
	
	private UIInput userIdInput = null;
	private UIInput hostNameInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "mailboxEdit.xhtml";
	private static String TO_FAILED = null;
	private static String TO_SAVED = "configureMailboxes.xhtml";
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public String refresh() {
		mailBoxes = null;
		return "";
	}
	
	public MailInboxService getMailInboxService() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (mailBoxDao == null) {
			mailBoxDao = (MailInboxService) SpringUtil.getWebAppContext().getBean("mailInboxService");
		}
		return mailBoxDao;
	}

	public void setMailInboxService(MailInboxService mailBoxDao) {
		this.mailBoxDao = mailBoxDao;
	}
	
	public DataModel<MailInbox> getAll() {
		if (mailBoxes == null) {
			List<MailInbox> mailBoxList = null;
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				mailBoxList = getMailInboxService().getAll(false);
			}
			else {
				mailBoxList = getMailInboxService().getAll(false);
			}
			mailBoxes = new ListDataModel<MailInbox>(mailBoxList);
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
		this.mailbox = (MailInbox) mailBoxes.getRowData();
		if (isInfoEnabled) {
			logger.info("viewMailBox() - Mailbox to be edited: " + mailbox.getMailInboxPK());
		}
		mailbox.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewMailBox() - MailInbox to be passed to jsp: " + mailbox);
		
		return TO_EDIT;
	}
	
	public String saveMailbox() {
		if (isDebugEnabled)
			logger.debug("saveMailbox() - Entering...");
		if (mailbox == null) {
			logger.warn("saveMailbox() - MailInbox is null.");
			return TO_FAILED;
		}
		reset();
		if (validatePrimaryKey(mailbox.getMailInboxPK()) != null) {
			return TO_FAILED;
		}
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			mailbox.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getMailInboxService().update(mailbox);
			logger.info("saveMailBox() - Rows Updated: " + 1);
		}
		else {
			getMailInboxService().insert(mailbox);
			addToList(mailbox);
			logger.info("saveMailBox() - Rows Inserted: " + 1);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(MailInbox vo) {
		List<MailInbox> list = (List<MailInbox>) mailBoxes.getWrappedData();
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
		List<MailInbox> mboxList = getMailBoxList();
		for (int i=0; i<mboxList.size(); i++) {
			MailInbox vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMailInboxService().deleteByPrimaryKey(vo.getMailInboxPK());
				if (rowsDeleted > 0) {
					logger.info("deleteMailBoxes() - Mailbox deleted: " + vo.getMailInboxPK());
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
			logger.warn("testMailbox() - MailInbox is null.");
			return TO_FAILED;
		}
		Properties m_props = (Properties) System.getProperties().clone();
		Session session = null;
		// Get a Session object
		if (mailbox.isUseSsl()) {
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
			store.connect(mailbox.getMailInboxPK().getHostName(), mailbox.getPortNumber(),
					mailbox.getMailInboxPK().getUserId(), mailbox.getUserPswd());
			
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
        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
				"jpa.msgui.messages", testResult, null);
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
		List<MailInbox> mboxList = getMailBoxList();
		for (int i=0; i<mboxList.size(); i++) {
			MailInbox vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				mailbox = new MailInbox();
				try {
					vo.copyPropertiesTo(mailbox);
					mailbox.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
					setDefaultValues(mailbox);
				}
				mailbox.getMailInboxPK().setHostName(null);
				mailbox.getMailInboxPK().setUserId(null);
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
		this.mailbox = new MailInbox();
		mailbox.setMarkedForEdition(true);
		mailbox.setUpdtUserId(Constants.DEFAULT_USER_ID);
		mailbox.setUseSsl(false);
		mailbox.setIsToPlainText(false);
		mailbox.setReadPerPass(5);
		setDefaultValues(mailbox);
		editMode = false;
		return TO_EDIT;
	}
	
	private void setDefaultValues(MailInbox mailbox) {
		// default values, not present on screen
		mailbox.setIsCheckDuplicate(true);
		mailbox.setIsAlertDuplicate(true);
		mailbox.setIsLogDuplicate(true);
		mailbox.setPurgeDupsAfter(24); // in hours
		mailbox.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		mailbox.setIsInternalOnly(false);
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
		List<MailInbox> mboxList = getMailBoxList();
		for (Iterator<MailInbox> it=mboxList.iterator(); it.hasNext();) {
			MailInbox vo = it.next();
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
		MailInbox vo = (MailInbox) getMailInboxService().getByPrimaryKey(mailbox.getMailInboxPK());
		if (editMode == true && vo != null && mailbox != null
				&& vo.getRowId() != mailbox.getRowId()) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "mailboxAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "mailboxAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	private String validatePrimaryKey(MailInboxPK pk) {
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - hostName/userId: " + pk.getHostName()+ "/" + pk.getUserId());
		MailInbox vo = (MailInbox) getMailInboxService().getByPrimaryKey(pk);
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
	private List<MailInbox> getMailBoxList() {
		if (mailBoxes == null) {
			return new ArrayList<MailInbox>();
		}
		else {
			return (List<MailInbox>)mailBoxes.getWrappedData();
		}
	}
	
	public MailInbox getMailbox() {
		return mailbox;
	}

	public void setMailbox(MailInbox mailbox) {
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
