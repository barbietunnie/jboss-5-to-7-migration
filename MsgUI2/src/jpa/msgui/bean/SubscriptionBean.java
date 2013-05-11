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

	private SubscriptionService subscriptionDao = null;
	private EmailAddressService emailAddrDao = null;
	private MailingListService mailingListDao = null;
	private DataModel<Subscription> subscriptions = null;
	private Subscription subscription = null;
	private boolean editMode = true;
	private String listId = null;

	private HtmlDataTable dataTable;
	private final PagingVo pagingVo =  new PagingVo();;
	private String searchString = null;
	
	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_FAILED = "subscription.failed";
	static final String TO_PAGING = "subscription.paging";
	static final String TO_DELETED = "subscription.deleted";
	static final String TO_SAVED = "subscription.saved";
	static final String TO_EDIT = "subscription.edit";
	static final String TO_CANCELED = "subscription.canceled";

	@SuppressWarnings("unchecked")
	public DataModel<Subscription> getSubscriptions() {
		if (FacesUtil.getRequestParameter("listId") != null) {
			listId = FacesUtil.getRequestParameter("listId");
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getSubscriptionService().getSubscriptionCount(listId, pagingVo);
			pagingVo.setRowCount(rowCount);
		}
		if (subscriptions == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<Subscription> subscriptionList = getSubscriptionService().getSubscriptionsWithPaging(
					listId, pagingVo);
			/* set keys for paging */
			if (!subscriptionList.isEmpty()) {
				Subscription firstRow = (Subscription) subscriptionList.get(0);
				pagingVo.setIdFirst(firstRow.getEmailAddr().getRowId());
				Subscription lastRow = (Subscription) subscriptionList.get(subscriptionList.size() - 1);
				pagingVo.setIdLast(lastRow.getEmailAddr().getRowId());
			}
			else {
				pagingVo.setIdFirst(-1);
				pagingVo.setIdLast(-1);
			}
			logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			//subscriptions = new ListDataModel(subscriptionList);
			subscriptions = new PagedListDataModel(subscriptionList, pagingVo.getRowCount(), pagingVo.getPageSize());
		}
		return subscriptions;
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
		subscriptions = null;
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
		if (subscriptionDao == null) {
			subscriptionDao = (SubscriptionService) SpringUtil.getWebAppContext().getBean(
					"subscriptionService");
		}
		return subscriptionDao;
	}

	public void setSubscriptionService(SubscriptionService subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
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

	public String deleteSubscriptions() {
		if (isDebugEnabled)
			logger.debug("deleteSubscriptions() - Entering...");
		if (subscriptions == null) {
			logger.warn("deleteSubscriptions() - Subscription List is null.");
			return TO_FAILED;
		}
		if (listId == null) {
			logger.warn("deleteSubscriptions() - ListId is null.");
			return TO_FAILED;
		}
		reset();
		List<Subscription> subrList = getSubscriptionList();
		for (int i=0; i<subrList.size(); i++) {
			Subscription vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSubscriptionService().deleteByAddressAndListId(vo.getEmailAddr().getAddress(), listId);
				if (rowsDeleted > 0) {
					logger.info("deleteSubscriptions() - Subscription deleted: " + vo.getEmailAddr());
					pagingVo.setRowCount(pagingVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public String saveSubscriptions() {
		if (isDebugEnabled)
			logger.debug("saveSubscriptions() - Entering...");
		if (subscriptions == null) {
			logger.warn("saveSubscriptions() - Subscription List is null.");
			return TO_FAILED;
		}
		reset();
		List<Subscription> subrList = getSubscriptionList();
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
				logger.info("saveSubscriptions() - Subscription updated: " + vo.getEmailAddr().getAddress());
			}
		}
		refresh();
		return TO_SAVED;
	}

	public String addSubscription() {
		if (isDebugEnabled)
			logger.debug("addSubscription() - Entering...");
		reset();
		this.subscription = new Subscription();
		MailingList list = getMailingListDao().getByListId(listId);
		subscription.setMailingList(list);
		subscription.setSubscribed(true);
		subscription.setMarkedForEdition(true);
		editMode = false;
		return TO_EDIT;
	}

	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getSubscriptionsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getSubscriptionsMarkedForDeletion() - Entering...");
		if (subscriptions == null) {
			logger.warn("getSubscriptionsMarkedForDeletion() - Subscription List is null.");
			return false;
		}
		List<Subscription> subrList = getSubscriptionList();
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
			logger.debug("validatePrimaryKey() - subscriptionId: " + subId);
		Subscription vo = getSubscriptionService().getByAddressAndListId(subId, listId);
		if (editMode == true && vo == null) {
			// subscription does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "subscriptionDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// subscription already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "subscriptionAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	void reset() {
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings({ "unchecked" })
	private List<Subscription> getSubscriptionList() {
		if (subscriptions == null) {
			return new ArrayList<Subscription>();
		}
		else {
			return (List<Subscription>)subscriptions.getWrappedData();
		}
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
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
