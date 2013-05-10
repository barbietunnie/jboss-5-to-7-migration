package jpa.msgui.bean;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import javax.persistence.NoResultException;

import jpa.constant.RuleType;
import jpa.model.EmailAddress;
import jpa.model.UserData;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.msgui.vo.SearchFieldsVo.RuleName;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageInboxService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * This is a request scoped bean that holds search fields from HTTP request.
 * Whenever MsgInboxBean.getAll() gets called, it retrieves search fields from
 * this bean and uses them to construct a query to retrieve mails from database.
 * By doing this, if a user clicks browser's back button followed by refresh
 * button, the email list returned will still be okay.
 * 
 * Note: request scoped did not work as expected, changed to session scoped.
 */
public class SimpleMailTrackingMenu {
	static final Logger logger = Logger.getLogger(SimpleMailTrackingMenu.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private String titleKey;
	private String functionKey = null;
	private String ruleName = RuleName.All.name();
	private String fromAddress = null;
	private String toAddress = null;
	private String subject = null;
	private String body = null;

	private String defaultFolder = SearchFieldsVo.MsgType.Received.name();
	private String defaultRuleName = RuleName.All.name();
	private String defaultToAddr = null;
	
	private EmailAddressService emailAddrDao;
	private MessageInboxService msgInboxDao;
	private static String TO_SELF = "message.search";
	
	public SimpleMailTrackingMenu() {
		setDefaultSearchFields();
		functionKey = defaultFolder;
		ruleName = defaultRuleName;
	}
	
	void setDefaultSearchFields() {
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
	
	public String selectAll() {
		functionKey = SearchFieldsVo.MsgType.All.name();
		return TO_SELF;
	}
	
	public String selectReceived() {
		functionKey = SearchFieldsVo.MsgType.Received.name();
		return TO_SELF;
	}
	
	public String selectSent() {
		functionKey = SearchFieldsVo.MsgType.Sent.name();
		return TO_SELF;
	}
	
	public String selectDraft() {
		functionKey = SearchFieldsVo.MsgType.Draft.name();
		return TO_SELF;
	}
	
	public String selectClosed() {
		functionKey = SearchFieldsVo.MsgType.Closed.name();
		return TO_SELF;
	}
	
	/**
	 * This method is designed to go along with following JSF tag:
	 * <h:selectOneMenu value="#{mailtracking.ruleName}" onchange="submit()"
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
		logger.info("Entering ruleNameChanged()...");
		MsgInboxBean bean = (MsgInboxBean) FacesUtil.getSessionMapValue("msgfolder");
		if (bean == null) {
			logger.error("ruleNameChanged() - failed to retrieve MsgInboxBean from HTTP session");
			return;
		}
		String newValue = (String) event.getNewValue();
		String oldValue = (String) event.getOldValue();
		if (newValue != null && !newValue.equals(oldValue)) {
			bean.viewAll(); // reset view search criteria
			bean.getSearchFieldVo().setRuleName(newValue);
		}
	}
	
	public String searchBySearchVo() {
		logger.info("Entering searchBySearchVo()...");
		return TO_SELF;
	}
	
	public String resetSearchFields() {
		ruleName = defaultRuleName;
		fromAddress = null;
		toAddress = defaultToAddr;
		subject = null;
		body = null;
		// the value should be used in navigation rules to point to self to
		// refresh the page
		return TO_SELF;
	}
	
	public void checkEmailAddress(FacesContext context, UIComponent component, Object value) {
		if (value == null) return;
		if (!(value instanceof String)) return;
		
		String addr = (String) value;
		if (addr.trim().length() == 0) return;
		
		EmailAddress vo = getEmailAddressService().getByAddress(addr);
		if (vo == null) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "emailAddressNotFound", null);
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        throw new ValidatorException(message);
		}
	}
	
	public SearchFieldsVo getSearchFieldVo() {
		SearchFieldsVo vo = new SearchFieldsVo();
		SearchFieldsVo.MsgType msgType = null;
		if (SearchFieldsVo.MsgType.All.name().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.All;
		else if (SearchFieldsVo.MsgType.Received.name().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Received;
		else if (SearchFieldsVo.MsgType.Sent.name().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Sent;
		else if (SearchFieldsVo.MsgType.Draft.name().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Draft;
		else if (SearchFieldsVo.MsgType.Closed.name().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Closed;
		else if (SearchFieldsVo.MsgType.Trash.name().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Trash;
		
		vo.setMsgType(msgType);
		vo.setRuleName(ruleName);
		vo.setFromAddr(fromAddress);
		if (fromAddress != null && fromAddress.trim().length() > 0) {
			try {
				EmailAddress vo2 = getEmailAddressService().getByAddress(fromAddress);
				vo.setFromAddrId(vo2.getRowId());
			}
			catch (NoResultException e) {}
		}
//		vo.setToAddr(toAddress);
//		if (toAddress != null && toAddress.trim().length() > 0) {
//			try {
//				EmailAddress vo3 = getEmailAddressService().getByAddress(toAddress);
//				vo.setToAddrId(vo3.getRowId());
//			}
//			catch (NoResultException e) {}
//		}
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
			emailAddrDao = (EmailAddressService) SpringUtil.getWebAppContext().getBean("emailAddressService");
		}
		return emailAddrDao;
	}

	public void setEmailAddressService(EmailAddressService emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public MessageInboxService getMessageInboxService() {
		if (msgInboxDao == null) {
			msgInboxDao = (MessageInboxService) SpringUtil.getWebAppContext().getBean("messageInboxService");
		}
		return msgInboxDao;
	}

	public void setMessageInboxService(MessageInboxService msgInboxDao) {
		this.msgInboxDao = msgInboxDao;
	}

}
