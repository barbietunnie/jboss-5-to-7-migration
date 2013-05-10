package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;

import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.EmailAddressService;
import jpa.service.MailingListService;
import jpa.service.SubscriptionService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SubscriptionBean {
	static final Logger logger = Logger.getLogger(SubscriptionBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private SubscriptionService subscriberDao = null;
	private EmailAddressService emailAddrDao = null;
	private MailingListService mailingListDao = null;
	private DataModel<Subscription> subscribers = null;
	private Subscription subscriber = null;
	private boolean editMode = true;
	private String listId = null;

	private HtmlDataTable dataTable;
	private final PagingVo pagingVo =  new PagingVo();;
	private String searchString = null;
	
	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_FAILED = "subscriberlist.failed";
	static final String TO_PAGING = "subscriberlist.paging";
	static final String TO_DELETED = "subscriberlist.deleted";
	static final String TO_SAVED = "subscriberlist.saved";
	static final String TO_EDIT = "subscriberlist.edit";
	static final String TO_CANCELED = "subscriberlist.canceled";

	@SuppressWarnings("unchecked")
	public DataModel<Subscription> getSubscribers() {
		if (FacesUtil.getRequestParameter("listId") != null) {
			listId = FacesUtil.getRequestParameter("listId");
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getSubscriptionService().getSubscriptionCount(listId, pagingVo);
			pagingVo.setRowCount(rowCount);
		}
		if (subscribers == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<Subscription> subscriberList = getSubscriptionService().getSubscriptionsWithPaging(
					listId, pagingVo);
			/* set keys for paging */
			if (!subscriberList.isEmpty()) {
				Subscription firstRow = (Subscription) subscriberList.get(0);
				pagingVo.setIdFirst(firstRow.getEmailAddr().getRowId());
				Subscription lastRow = (Subscription) subscriberList.get(subscriberList.size() - 1);
				pagingVo.setIdLast(lastRow.getEmailAddr().getRowId());
			}
			else {
				pagingVo.setIdFirst(-1);
				pagingVo.setIdLast(-1);
			}
			logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			//subscribers = new ListDataModel(subscriberList);
			subscribers = new PagedListDataModel(subscriberList, pagingVo.getRowCount(), pagingVo.getPageSize());
		}
		return subscribers;
	}

	public String searchByAddress() {
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
		return null;
	}
	
	public String resetSearch() {
		searchString = null;
		pagingVo.setSearchString(null);
		resetPagingVo();
		return null;
	}
	
	public String pageFirst() {
		dataTable.setFirst(0);
		pagingVo.setPageAction(PagingVo.PageAction.FIRST);
		return TO_PAGING;
	}

	public String pagePrevious() {
		dataTable.setFirst(dataTable.getFirst() - dataTable.getRows());
		pagingVo.setPageAction(PagingVo.PageAction.PREVIOUS);
		return TO_PAGING;
	}

	public String pageNext() {
		dataTable.setFirst(dataTable.getFirst() + dataTable.getRows());
		pagingVo.setPageAction(PagingVo.PageAction.NEXT);
		return TO_PAGING;
	}

	public String pageLast() {
		int count = dataTable.getRowCount();
		int rows = dataTable.getRows();
		dataTable.setFirst(count - ((count % rows != 0) ? count % rows : rows));
		pagingVo.setPageAction(PagingVo.PageAction.LAST);
		return TO_PAGING;
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
	
	public void refresh() {
		subscribers = null;
	}

	public String refreshPage() {
		refresh();
		pagingVo.setRowCount(-1);
		return "";
	}
	
	public void resetPagingVo() {
		pagingVo.resetPageContext();
		if (dataTable != null) dataTable.setFirst(0);
		refresh();
	}
	
	public SubscriptionService getSubscriptionService() {
		if (subscriberDao == null) {
			subscriberDao = (SubscriptionService) SpringUtil.getWebAppContext().getBean(
					"subscriptionService");
		}
		return subscriberDao;
	}

	public void setSubscriptionService(SubscriptionService subscriberDao) {
		this.subscriberDao = subscriberDao;
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

	public MailingListService getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListService) SpringUtil.getWebAppContext().getBean("mailingListService");
		}
		return mailingListDao;
	}

	public String deleteSubscribers() {
		if (isDebugEnabled)
			logger.debug("deleteSubscribers() - Entering...");
		if (subscribers == null) {
			logger.warn("deleteSubscribers() - Subscriber List is null.");
			return TO_FAILED;
		}
		if (listId == null) {
			logger.warn("deleteSubscribers() - ListId is null.");
			return TO_FAILED;
		}
		reset();
		List<Subscription> subrList = getSubscriberList();
		for (int i=0; i<subrList.size(); i++) {
			Subscription vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSubscriptionService().deleteByAddressAndListId(vo.getEmailAddr().getAddress(), listId);
				if (rowsDeleted > 0) {
					logger.info("deleteSubscribers() - Subscriber deleted: " + vo.getEmailAddr());
					pagingVo.setRowCount(pagingVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public String saveSubscribers() {
		if (isDebugEnabled)
			logger.debug("saveSubscribers() - Entering...");
		if (subscribers == null) {
			logger.warn("saveSubscribers() - Subscriber List is null.");
			return TO_FAILED;
		}
		reset();
		List<Subscription> subrList = getSubscriberList();
		for (int i=0; i<subrList.size(); i++) {
			Subscription vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				getSubscriptionService().update(vo);
//				boolean acceptHtml =  CodeType.YES.getValue().equalsIgnoreCase(vo.getAcceptHtmlDesc());
//				EmailAddress emailAddr = getEmailAddressService().getByRowId(vo.getEmailAddr().getRowId());
//				if (acceptHtml!=emailAddr.isAcceptHtml()) {
//					emailAddr.setAcceptHtml(acceptHtml);
//					getEmailAddressService().update(emailAddr);
//				}
				logger.info("saveSubscribers() - Subscriber updated: " + vo.getEmailAddr().getAddress());
			}
		}
		refresh();
		return TO_SAVED;
	}

	public String addSubscriber() {
		if (isDebugEnabled)
			logger.debug("addSubscriber() - Entering...");
		reset();
		this.subscriber = new Subscription();
		MailingList list = getMailingListDao().getByListId(listId);
		subscriber.setMailingList(list);
		subscriber.setSubscribed(true);
		subscriber.setMarkedForEdition(true);
		editMode = false;
		return TO_EDIT;
	}

	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getAnySubscribersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnySubscribersMarkedForDeletion() - Entering...");
		if (subscribers == null) {
			logger.warn("getAnySubscribersMarkedForDeletion() - Subscriber List is null.");
			return false;
		}
		List<Subscription> subrList = getSubscriberList();
		for (Iterator<Subscription> it=subrList.iterator(); it.hasNext();) {
			Subscription vo = it.next();
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
		String subId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - subscriberId: " + subId);
		Subscription vo = getSubscriptionService().getByAddressAndListId(subId, listId);
		if (editMode == true && vo == null) {
			// subscriber does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "subscriberDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// subscriber already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "subscriberAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	void reset() {
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings({ "unchecked" })
	private List<Subscription> getSubscriberList() {
		if (subscribers == null) {
			return new ArrayList<Subscription>();
		}
		else {
			return (List<Subscription>)subscribers.getWrappedData();
		}
	}

	public Subscription getSubscription() {
		return subscriber;
	}

	public void setSubscription(Subscription subscriber) {
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
}
