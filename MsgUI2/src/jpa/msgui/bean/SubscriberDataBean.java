package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;
import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.MobileCarrierEnum;
import jpa.exception.DataValidationException;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingSubscriberData;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.common.SubscriberDataService;
import jpa.util.EmailAddrUtil;
import jpa.util.PhoneNumberUtil;
import jpa.util.SsnNumberUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="subscriberData")
@SessionScoped
public class SubscriberDataBean implements java.io.Serializable {
	private static final long serialVersionUID = 7927665483948452101L;
	static final Logger logger = Logger.getLogger(SubscriberDataBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private SubscriberDataService subscriberDao = null;
	private SenderDataService senderDao = null;
	private EmailAddressService emailAddrDao = null;
	private transient DataModel<SubscriberData> subscribers = null;
	private SubscriberData subscriber = null;
	private boolean editMode = true;

	private transient HtmlDataTable dataTable;
	private final PagingSubscriberData pagingVo =  new PagingSubscriberData();;
	private String searchString = null;
	
	private transient UIInput subscriberIdInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private transient UIInput emailAddrInput = null;
	private transient UIInput ssnNumberInput = null;
	private transient UIInput dayPhoneInput = null;
	private transient UIInput eveningPhoneInput = null;
	private transient UIInput mobilePhoneInput = null;
	private transient UIInput birthDateInput = null;
	private transient UIInput endDateInput = null;
	private transient UIInput mobileCarrierInput = null;
	
	private static String TO_SELF = null;
	private static String TO_EDIT = "subscriberEdit.xhtml";
	private static String TO_FAILED = TO_SELF;
	private static String TO_LIST = "subscribersList.xhtml";
	private static String TO_SAVED = TO_LIST;
	private static String TO_CANCELED = TO_LIST;

	@SuppressWarnings("unchecked")
	public DataModel<SubscriberData> getSubscribers() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (StringUtils.equals(fromPage,"main")) {
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getSubscriberDataService().getSubscriberCount(pagingVo);
			pagingVo.setRowCount(rowCount);
		}
		if (subscribers == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<SubscriberData> subscriberList = getSubscriberDataService().getSubscribersWithPaging(pagingVo);
			/* set keys for paging */
			if (!subscriberList.isEmpty()) {
				SubscriberData firstRow = (SubscriberData) subscriberList.get(0);
				SubscriberData lastRow = (SubscriberData) subscriberList.get(subscriberList.size() - 1);
				pagingVo.setStrIdFirst(firstRow.getSubscriberId());
				pagingVo.setStrIdLast(lastRow.getSubscriberId());
			}
			else {
				pagingVo.setStrIdFirst(null);
				pagingVo.setStrIdLast(null);
			}
			logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			//subscribers = new ListDataModel(subscriberList);
			subscribers = new PagedListDataModel(subscriberList, pagingVo.getRowCount(), pagingVo.getPageSize());
		}
		return subscribers;
	}

	public SenderDataService getSenderDataService() {
		if (senderDao == null) {
			senderDao = SpringUtil.getWebAppContext().getBean(SenderDataService.class);
		}
		return senderDao;
	}

	public void setSenderDataService(SenderDataService senderDao) {
		this.senderDao = senderDao;
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

	public String viewSubscriber() {
		if (isDebugEnabled)
			logger.debug("viewSubscriber() - Entering...");
		if (subscribers == null) {
			logger.warn("viewSubscriber() - Subscriber List is null.");
			return TO_FAILED;
		}
		if (!subscribers.isRowAvailable()) {
			logger.warn("viewSubscriber() - Subscriber Row not available.");
			return TO_FAILED;
		}
		reset();
		this.subscriber = (SubscriberData) subscribers.getRowData();
		logger.info("viewSubscriber() - Subscriber to be edited: " + subscriber.getSubscriberId());
		subscriber.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewSubscriber() - SubscriberData to be passed to jsp: " + subscriber);
		}
		return TO_EDIT;
	}

	public void searchByAddress(AjaxBehaviorEvent event) {
		boolean changed = false;
		if (this.searchString == null) {
			if (pagingVo.getSearchString() != null) {
				changed = true;
			}
		}
		else {
			if (!this.searchString.equals(pagingVo.getSearchString())) {
				changed = true;
			}
		}
		if (changed) {
			resetPagingVo();
			pagingVo.setSearchString(searchString);
		}
		return; // TO_SELF;
	}
	
	public void resetSearch(AjaxBehaviorEvent event) {
		searchString = null;
		pagingVo.setSearchString(null);
		resetPagingVo();
		return; // TO_SELF;
	}
	
	public void pageFirst(AjaxBehaviorEvent event) {
		dataTable.setFirst(0);
		pagingVo.setPageAction(PagingVo.PageAction.FIRST);
		return; // TO_PAGING;
	}

	public void pagePrevious(AjaxBehaviorEvent event) {
		dataTable.setFirst(dataTable.getFirst() - dataTable.getRows());
		pagingVo.setPageAction(PagingVo.PageAction.PREVIOUS);
		return; // TO_PAGING;
	}

	public void pageNext(AjaxBehaviorEvent event) {
		dataTable.setFirst(dataTable.getFirst() + dataTable.getRows());
		pagingVo.setPageAction(PagingVo.PageAction.NEXT);
		return; // TO_PAGING;
	}

	public void pageLast(AjaxBehaviorEvent event) {
		int count = dataTable.getRowCount();
		int rows = dataTable.getRows();
		dataTable.setFirst(count - ((count % rows != 0) ? count % rows : rows));
		pagingVo.setPageAction(PagingVo.PageAction.LAST);
		return; // TO_PAGING;
	}
    
	public int getLastPageRow() {
		int lastRow = dataTable.getFirst() + dataTable.getRows();
		if (lastRow > dataTable.getRowCount())
			return dataTable.getRowCount();
		else
			return lastRow;
	}
	
	public PagingVo getPagingVo() {
		return pagingVo;
	}
	
	private void refresh() {
		subscribers = null;
	}

	public void refreshPage(AjaxBehaviorEvent event) {
		refresh();
		pagingVo.setRowCount(-1);
		return; // TO_SELF;
	}
	
	public SubscriberData getData() {
		subscriber = getSubscriberDataService().getBySubscriberId(subscriber.getSubscriberId());
		reset();
		return subscriber;
	}
	
	public void refreshSubscriber(AjaxBehaviorEvent event) {
		getData();
		FacesUtil.refreshCurrentJSFPage();
		return; // TO_SELF;
	}

	private void resetPagingVo() {
		pagingVo.resetPageContext();
		if (dataTable != null) dataTable.setFirst(0);
		refresh();
	}
	
	private void reset() {
		testResult = null;
		actionFailure = null;
		subscriberIdInput = null;
		emailAddrInput = null;
		ssnNumberInput = null;
		dayPhoneInput = null;
		eveningPhoneInput = null;
		mobilePhoneInput = null;
		birthDateInput = null;
		endDateInput = null;
		mobileCarrierInput = null;
	}
	
	public void deleteSubscribers(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("deleteSubscribers() - Entering...");
		if (subscribers == null) {
			logger.warn("deleteSubscribers() - Subscriber List is null.");
			return; //TO_FAILED;
		}
		reset();
		List<SubscriberData> addrList = getSubscriberList();
		for (int i=0; i<addrList.size(); i++) {
			SubscriberData vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSubscriberDataService().deleteBySubscriberId(vo.getSubscriberId());
				if (rowsDeleted > 0) {
					logger.info("deleteSubscribers() - Subscriber deleted: " + vo.getSubscriberId());
					pagingVo.setRowCount(pagingVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return; // TO_DELETED;
	}

	public String saveSubscriber() {
		if (isDebugEnabled)
			logger.debug("saveSubscriber() - Entering...");
		if (subscriber == null) {
			logger.warn("saveSubscriber() - Subscriber Vo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		//emailAddrInput.getValue();
		// TODO need to check email address from input field
		EmailAddress email = getEmailAddressService().findSertAddress(subscriber.getEmailAddr().getAddress());
		subscriber.setEmailAddr(email);
		SenderData sender = getSenderDataService().getBySenderId(subscriber.getSenderData().getSenderId());
		subscriber.setSenderData(sender);
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			subscriber.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		try {
			if (editMode == true) {
				getSubscriberDataService().update(subscriber);
				logger.info("saveSubscriber() - Rows Updated: " + 1);
			}
			else {
				getSubscriberDataService().insert(subscriber);
				addToList(subscriber);
				pagingVo.setRowCount(pagingVo.getRowCount() + 1);
				refresh();
				logger.info("saveSubscriber() - Rows Inserted: " + 1);
			}
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			actionFailure = e.getMessage();
			return TO_FAILED;
		}
		return TO_SAVED;
	}
	
	@SuppressWarnings("unchecked")
	private void addToList(SubscriberData vo) {
		List<SubscriberData> list = (List<SubscriberData>)subscribers.getWrappedData();
		list.add(vo);
	}

	public String copySubscriber() {
		if (isDebugEnabled)
			logger.debug("copySubscriber() - Entering...");
		if (subscribers == null) {
			logger.warn("copySubscriber() - Subscriber List is null.");
			return TO_FAILED;
		}
		reset();
		List<SubscriberData> subrList = getSubscriberList();
		for (int i=0; i<subrList.size(); i++) {
			SubscriberData vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.subscriber = new SubscriberData();
				try {
					vo.copyPropertiesTo(this.subscriber);
					subscriber.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				//subscriber.setLastName(null);
				//subscriber.setFirstName(null);
				subscriber.setSubscriberId(null);
				subscriber.setEmailAddr(new EmailAddress());
				subscriber.setUserPassword(null);
				subscriber.setMarkedForEdition(true);
				editMode = false;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public String addSubscriber() {
		if (isDebugEnabled)
			logger.debug("addSubscriber() - Entering...");
		reset();
		this.subscriber = new SubscriberData();
		SenderData default_sender = getSenderDataService().getBySenderId(Constants.DEFAULT_SENDER_ID);
		subscriber.setEmailAddr(new EmailAddress());
		subscriber.setSenderData(default_sender);
		subscriber.setMarkedForEdition(true);
		subscriber.setUpdtUserId(Constants.DEFAULT_USER_ID);
		editMode = false;
		return TO_EDIT;
	}
	
	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getSubscribersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getSubscribersMarkedForDeletion() - Entering...");
		if (subscribers == null) {
			logger.warn("getSubscribersMarkedForDeletion() - Subscriber List is null.");
			return false;
		}
		List<SubscriberData> addrList = getSubscriberList();
		for (Iterator<SubscriberData> it=addrList.iterator(); it.hasNext();) {
			SubscriberData vo = it.next();
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
		String subrId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - SubscriberId: " + subrId);
		try {
			SubscriberData vo = getSubscriberDataService().getBySubscriberId(subrId);
			if (editMode == true && subscriber != null
					&& vo.getRowId() != subscriber.getRowId()) {
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						//"jpa.msgui.messages", "subscriberDoesNotExist", null);
		        		"jpa.msgui.messages", "subscriberAlreadyExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
			else if (editMode == false) {
				// subscriber already exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "subscriberAlreadyExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
		catch (NoResultException e) {
			// ignore
		}
	}
	
	public void validateEmailAddress(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailAddress() - addr: " + emailAddr);
		if (StringUtils.isNotBlank(emailAddr)) {
			if (!EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
				// invalid email address
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "invalidEmailAddress", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
			else {
				try {
					SubscriberData vo = getSubscriberDataService().getByEmailAddress(emailAddr);
					if (subscriber != null && !vo.getSubscriberId().equals(subscriber.getSubscriberId())) {
						FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
								"jpa.msgui.messages", "emailAddressAlreadyUsed", null);
						message.setSeverity(FacesMessage.SEVERITY_WARN);
						throw new ValidatorException(message);
					}
				}
				catch (NoResultException e) {}
			}
		}
	}
	
	public void validateSsnNumber(FacesContext context, UIComponent component, Object value) {
		String ssn = (String) value;
		if (isDebugEnabled)
			logger.debug("validateSsnNumber() - SSN: " + ssn);
		if (StringUtils.isNotBlank(ssn) && !SsnNumberUtil.isValidSSN(ssn)) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidSsnNumber", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateDate(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateDate() - date = " + value);
		if (value != null && !(value instanceof Date)) {
			FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidDate", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validatePhoneNumber(FacesContext context, UIComponent component, Object value) {
		String phone = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePhoneNumber() - Phone Number: " + phone);
		if (StringUtils.isNotBlank(phone) && !PhoneNumberUtil.isValidPhoneNumber(phone)) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidPhoneNumber", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateMibileCarrier(FacesContext context, UIComponent component, Object value) {
		String carrier = (String) value;
		if (isDebugEnabled)
			logger.debug("validateMibileCarrier() - Phone Number: " + carrier);
		if (StringUtils.isNotBlank(carrier)) {
			try {
				MobileCarrierEnum.getByValue(carrier);
			}
			catch (IllegalArgumentException e) {
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "invalidMobileCarrier", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	public void validateZipCode5(FacesContext context, UIComponent component, Object value) {
		String zip5 = (String) value;
		if (isDebugEnabled)
			logger.debug("validateZipCode5() - Zip Code: " + zip5);
		if (StringUtils.isNotBlank(zip5) && !zip5.matches("\\d{5}")) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
	        		"jpa.msgui.messages", "invalideZipCode", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateZipCode4(FacesContext context, UIComponent component, Object value) {
		String zip4 = (String) value;
		if (isDebugEnabled)
			logger.debug("validateZipCode4() - Zip Code: " + zip4);
		if (StringUtils.isNotBlank(zip4) && !zip4.matches("\\d{4}")) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
	        		"jpa.msgui.messages", "invalideZipCode", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private List<SubscriberData> getSubscriberList() {
		if (subscribers == null) {
			return new ArrayList<SubscriberData>();
		}
		else {
			return (List<SubscriberData>)subscribers.getWrappedData();
		}
	}

	public SubscriberDataService getSubscriberDataService() {
		if (subscriberDao == null) {
			subscriberDao = (SubscriberDataService) SpringUtil.getWebAppContext().getBean(
					"subscriberDataService");
		}
		return subscriberDao;
	}

	public void setSubscriberDataService(SubscriberDataService subscriberDao) {
		this.subscriberDao = subscriberDao;
	}

	public SubscriberData getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(SubscriberData subscriber) {
		this.subscriber = subscriber;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
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

	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
	}

	public UIInput getSubscriberIdInput() {
		return subscriberIdInput;
	}

	public void setSubscriberIdInput(UIInput subscriberIdInput) {
		this.subscriberIdInput = subscriberIdInput;
	}

	public UIInput getEmailAddrInput() {
		return emailAddrInput;
	}

	public void setEmailAddrInput(UIInput emailAddrInput) {
		this.emailAddrInput = emailAddrInput;
	}

	public UIInput getSsnNumberInput() {
		return ssnNumberInput;
	}

	public void setSsnNumberInput(UIInput ssnNumberInput) {
		this.ssnNumberInput = ssnNumberInput;
	}

	public UIInput getDayPhoneInput() {
		return dayPhoneInput;
	}

	public void setDayPhoneInput(UIInput dayPhoneInput) {
		this.dayPhoneInput = dayPhoneInput;
	}

	public UIInput getEveningPhoneInput() {
		return eveningPhoneInput;
	}

	public void setEveningPhoneInput(UIInput eveningPhoneInput) {
		this.eveningPhoneInput = eveningPhoneInput;
	}

	public UIInput getMobilePhoneInput() {
		return mobilePhoneInput;
	}

	public void setMobilePhoneInput(UIInput mobilePhoneInput) {
		this.mobilePhoneInput = mobilePhoneInput;
	}

	public UIInput getBirthDateInput() {
		return birthDateInput;
	}

	public void setBirthDateInput(UIInput birthDateInput) {
		this.birthDateInput = birthDateInput;
	}

	public UIInput getEndDateInput() {
		return endDateInput;
	}

	public void setEndDateInput(UIInput endDateInput) {
		this.endDateInput = endDateInput;
	}

	public UIInput getMobileCarrierInput() {
		return mobileCarrierInput;
	}

	public void setMobileCarrierInput(UIInput mobileCarrierInput) {
		this.mobileCarrierInput = mobileCarrierInput;
	}
}
