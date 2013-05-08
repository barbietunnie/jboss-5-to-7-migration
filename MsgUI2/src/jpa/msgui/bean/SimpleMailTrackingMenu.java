package jpa.msgui.bean;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.UserVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.SearchFieldsVo;
import com.legacytojava.message.vo.inbox.SearchFieldsVo.RuleName;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

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
	private String ruleName = RuleName.All.toString();
	private String fromAddress = null;
	private String toAddress = null;
	private String subject = null;
	private String body = null;

	private String defaultFolder = SearchFieldsVo.MsgType.Received.toString();
	private String defaultRuleName = RuleName.All.toString();
	private String defaultToAddr = null;
	
	private EmailAddrDao emailAddrDao;
	private MsgInboxDao msgInboxDao;
	private static String TO_SELF = "message.search";
	
	public SimpleMailTrackingMenu() {
		setDefaultSearchFields();
		functionKey = defaultFolder;
		ruleName = defaultRuleName;
	}
	
	void setDefaultSearchFields() {
		// initialize search fields from user's default settings.
		UserVo userVo = FacesUtil.getLoginUserVo();
		if (userVo != null) {
			defaultFolder = userVo.getDefaultFolder();
			if (StringUtil.isEmpty(defaultFolder)) {
				defaultFolder = SearchFieldsVo.MsgType.Received.toString();
			}
			defaultRuleName = userVo.getDefaultRuleName();
			if (StringUtil.isEmpty(defaultRuleName)) {
				defaultRuleName = RuleName.All.toString();
			}
			defaultToAddr = userVo.getDefaultToAddr();
			if (StringUtil.isEmpty(defaultToAddr)) {
				defaultToAddr = null;
			}
			else {
				getEmailAddrDao().findByAddress(defaultToAddr);
			}
		}
		else {
			logger.error("constructor - UserVo not found in HTTP session.");
		}
	}
	
	public String selectAll() {
		functionKey = SearchFieldsVo.MsgType.All.toString();
		return TO_SELF;
	}
	
	public String selectReceived() {
		functionKey = SearchFieldsVo.MsgType.Received.toString();
		return TO_SELF;
	}
	
	public String selectSent() {
		functionKey = SearchFieldsVo.MsgType.Sent.toString();
		return TO_SELF;
	}
	
	public String selectDraft() {
		functionKey = SearchFieldsVo.MsgType.Draft.toString();
		return TO_SELF;
	}
	
	public String selectClosed() {
		functionKey = SearchFieldsVo.MsgType.Closed.toString();
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
		
		EmailAddrVo vo = getEmailAddrDao().getByAddress(addr);
		if (vo == null) {
	        FacesMessage message = com.legacytojava.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "emailAddressNotFound", null);
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
	        throw new ValidatorException(message);
		}
	}
	
	public SearchFieldsVo getSearchFieldVo() {
		SearchFieldsVo vo = new SearchFieldsVo();
		SearchFieldsVo.MsgType msgType = null;
		if (SearchFieldsVo.MsgType.All.toString().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.All;
		else if (SearchFieldsVo.MsgType.Received.toString().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Received;
		else if (SearchFieldsVo.MsgType.Sent.toString().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Sent;
		else if (SearchFieldsVo.MsgType.Draft.toString().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Draft;
		else if (SearchFieldsVo.MsgType.Closed.toString().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Closed;
		else if (SearchFieldsVo.MsgType.Trash.toString().equals(functionKey))
			msgType = SearchFieldsVo.MsgType.Trash;
		
		vo.setMsgType(msgType);
		vo.setRuleName(ruleName);
		vo.setFromAddr(fromAddress);
		if (fromAddress != null && fromAddress.trim().length() > 0) {
			EmailAddrVo vo2 = getEmailAddrDao().getByAddress(fromAddress);
			if (vo2 != null) {
				vo.setFromAddrId(vo2.getEmailAddrId());
			}
		}
		vo.setToAddr(toAddress);
		if (toAddress != null && toAddress.trim().length() > 0) {
			EmailAddrVo vo3 = getEmailAddrDao().getByAddress(toAddress);
			if (vo3 != null) {
				vo.setToAddrId(vo3.getEmailAddrId());
			}
		}
		vo.setSubject(subject);
		vo.setBody(body);
		
		return vo;
	}
	
	public int getInboxUnreadCount() {
		return getMsgInboxDao().getInboxUnreadCount();
	}

	public int getSentUnreadCount() {
		return getMsgInboxDao().getSentUnreadCount();
	}

	public int getAllUnreadCount() {
		return getMsgInboxDao().getAllUnreadCount();
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

	public EmailAddrDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddrDao) SpringUtil.getWebAppContext().getBean("emailAddrDao");
		}
		return emailAddrDao;
	}

	public void setEmailAddrDao(EmailAddrDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public MsgInboxDao getMsgInboxDao() {
		if (msgInboxDao == null) {
			msgInboxDao = (MsgInboxDao) SpringUtil.getWebAppContext().getBean("msgInboxDao");
		}
		return msgInboxDao;
	}

	public void setMsgInboxDao(MsgInboxDao msgInboxDao) {
		this.msgInboxDao = msgInboxDao;
	}

}
