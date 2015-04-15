package jpa.msgui.bean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import javax.persistence.NoResultException;

import jpa.constant.RuleType;
import jpa.model.EmailAddress;
import jpa.model.UserData;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.FolderType;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.msgui.vo.SearchFieldsVo.RuleName;
import jpa.service.common.EmailAddressService;
import jpa.service.message.MessageInboxService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * This is a request scoped bean that holds search fields from HTTP request.
 * Whenever MessageInboxBean.getAll() gets called, it retrieves search fields from
 * this bean and uses them to construct a query to retrieve mails from database.
 * By doing this, if a user clicks browser's back button followed by refresh
 * button, the email list returned will still be okay.
 * 
 * Note: request scoped did not work as expected, changed to session scoped.
 */
@ManagedBean(name="mailTracking")
@SessionScoped
public class SimpleMailTrackingMenu implements java.io.Serializable {
	private static final long serialVersionUID = -4430208005555443392L;
	static final Logger logger = Logger.getLogger(SimpleMailTrackingMenu.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private String titleKey;
	private String functionKey = null;
	private String ruleName = RuleName.All.name();
	private String fromAddress = null;
	private String toAddress = null;
	private String subject = null;
	private String body = null;

	private final String defaultFolder = FolderType.Received.name();
	private String defaultRuleName = RuleName.All.name();
	private String defaultToAddr = null;
	
	private final static String TO_SELF = null;
	
	private EmailAddressService emailAddrDao;
	private MessageInboxService msgInboxDao;
	
	public SimpleMailTrackingMenu() {
		initDefaultSearchValues();
		functionKey = defaultFolder;
		ruleName = defaultRuleName;
	}
	
	void initDefaultSearchValues() {
		// initialize search fields from user's default settings.
		UserData userVo = FacesUtil.getLoginUserData();
		if (userVo != null) {
			defaultRuleName = userVo.getDefaultRuleName();
			if (StringUtils.isBlank(defaultRuleName)) {
				defaultRuleName = RuleType.ALL.getValue();
			}
			if (userVo.getEmailAddr()!=null) {
				defaultToAddr = userVo.getEmailAddr().getAddress();
				if (StringUtils.isBlank(defaultToAddr)) {
					defaultToAddr = null;
				}
				else {
					getEmailAddressService().findSertAddress(defaultToAddr);
				}
			}
		}
		else {
			logger.error("constructor - UserData not found in HTTP session.");
		}
	}
	
	public String resetSearchFields() {
		ruleName = defaultRuleName;
		fromAddress = null;
		toAddress = defaultToAddr;
		subject = null;
		body = null;
		return TO_SELF;
	}
	
	/*
	 * used by SimpleMailTrackingMenu.xhtml to reset search folder when
	 * navigated from main menu.
	 */
	public void resetFolderIfFromMain() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (StringUtils.equals(fromPage, "main")) {
			functionKey = defaultFolder;
		}
	}

	public void selectAllListener(AjaxBehaviorEvent event) {
		functionKey = FolderType.All.name();
		return; // TO_SELF;
	}
	
	public void selectReceivedListener(AjaxBehaviorEvent event) {
		functionKey = FolderType.Received.name();
		return; // TO_SELF;
	}
	
	public void selectSentListener(AjaxBehaviorEvent event) {
		functionKey = FolderType.Sent.name();
		return; // TO_SELF;
	}
	
	public void selectDraftListener(AjaxBehaviorEvent event) {
		functionKey = FolderType.Draft.name();
		return; // TO_SELF;
	}
	
	public void selectClosedListener(AjaxBehaviorEvent event) {
		functionKey = FolderType.Closed.name();
		return; // TO_SELF;
	}
	
	/**
	 * This method is designed to go along with following JSF tag:
	 * <h:selectOneMenu value="#{mailtracking.ruleName}" onclick="submit()"
	 * valueChangeListener="#{mailtracking.ruleNameChanged}"/>
	 * 
	 * @param event
	 * 
	 * @deprecated "Search" button is now used to submit rule name changes. The
	 *             value change listener gets executed every time in JSF life
	 *             cycle, so method bean.viewAll() gets called every time which
	 *             resets "folder" to null. This behavior has caused all other
	 *             methods that rely on "folder" to fail.
	 */
	public void ruleNameChanged(ValueChangeEvent event) {
		/*
		 * <h:selectOneMenu value="#{mailTracking.ruleName}" onchange="submit()"
		 *	valueChangeListener="#{mailTracking.ruleNameChanged}"/>
		 */
		logger.info("Entering ruleNameChanged()...");
		MessageInboxBean bean = (MessageInboxBean) FacesUtil.getSessionMapValue("msgfolder");
		if (bean == null) {
			logger.error("ruleNameChanged() - failed to retrieve MessageInboxBean from HTTP session");
			return;
		}
		String newValue = (String) event.getNewValue();
		String oldValue = (String) event.getOldValue();
		if (newValue != null && !newValue.equals(oldValue)) {
			bean.viewAll(); // reset view search criteria
			bean.getSearchFieldVo().setRuleName(newValue);
		}
	}
	
	public void searchBySearchVoListener(AjaxBehaviorEvent event) {
		logger.info("Entering searchBySearchVo()...");
		return; // TO_SELF;
	}
	
	public void resetSearchFieldsListener(AjaxBehaviorEvent event) {
		resetSearchFields();
	}
	
	public void checkEmailAddress(FacesContext context, UIComponent component, Object value) {
		if (value == null) return;
		if (!(value instanceof String)) return;
		
		String addr = (String) value;
		if (addr.trim().length() == 0) return;
		
		EmailAddress vo = getEmailAddressService().getByAddress(addr);
		if (vo == null) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "emailAddressNotFound", null);
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        throw new ValidatorException(message);
		}
	}
	
	public SearchFieldsVo getSearchFieldVo() {
		SearchFieldsVo vo = new SearchFieldsVo();
		FolderType msgType = null;
		try {
			msgType = FolderType.getByName(functionKey);
		}
		catch (IllegalArgumentException e) {
			msgType = FolderType.Received;
		}
		vo.setFolderType(msgType);
		vo.setRuleName(ruleName);
		vo.setFromAddr(fromAddress);
		if (StringUtils.isNotBlank(fromAddress)) {
			try {
				EmailAddress from = getEmailAddressService().getByAddress(fromAddress);
				vo.setFromAddrId(from.getRowId());
			}
			catch (NoResultException e) {}
		}
		if (StringUtils.isNotBlank(toAddress)) {
			try {
				EmailAddress to = getEmailAddressService().getByAddress(toAddress);
				vo.setToAddrId(to.getRowId());
			}
			catch (NoResultException e) {}
		}
		vo.setSubject(subject);
		vo.setBody(body);
		
		return vo;
	}
	
	public int getInboxUnreadCount() {
		return getMessageInboxService().getReceivedUnreadCount();
	}

	public int getSentUnreadCount() {
		return getMessageInboxService().getSentUnreadCount();
	}

	public int getAllUnreadCount() {
		return getMessageInboxService().getAllUnreadCount();
	}

	// PROPERTY: titleKey
	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

	public String getTitleKey() {
		return titleKey;
	}

	public String getFunctionKey() {
		return functionKey;
	}

	public void setFunctionKey(String function) {
		this.functionKey = function;
	}
	
	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getToAddress() {
		return toAddress;
	}

	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressService.class);
		}
		return emailAddrDao;
	}

	public void setEmailAddressService(EmailAddressService emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public MessageInboxService getMessageInboxService() {
		if (msgInboxDao == null) {
			msgInboxDao = SpringUtil.getWebAppContext().getBean(MessageInboxService.class);
		}
		return msgInboxDao;
	}

	public void setMessageInboxService(MessageInboxService msgInboxDao) {
		this.msgInboxDao = msgInboxDao;
	}

}
