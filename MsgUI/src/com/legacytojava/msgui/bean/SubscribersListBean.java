package com.legacytojava.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MsgStatusCode;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.emailaddr.SubscriptionDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.emailaddr.SubscriptionVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class SubscribersListBean {
	static final Logger logger = Logger.getLogger(SubscribersListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private SubscriptionDao subscriberDao = null;
	private EmailAddrDao emailAddrDao = null;
	private DataModel subscribers = null;
	private SubscriptionVo subscriber = null;
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

	public DataModel getSubscribers() {
		if (FacesUtil.getRequestParameter("listId") != null) {
			listId = FacesUtil.getRequestParameter("listId");
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getSubscriptionDao().getSubscriberCount(listId, pagingVo);
			pagingVo.setRowCount(rowCount);
		}
		if (subscribers == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<SubscriptionVo> subscriberList = getSubscriptionDao().getSubscribersWithPaging(
					listId, pagingVo);
			/* set keys for paging */
			if (!subscriberList.isEmpty()) {
				SubscriptionVo firstRow = (SubscriptionVo) subscriberList.get(0);
				pagingVo.setIdFirst(firstRow.getEmailAddrId());
				SubscriptionVo lastRow = (SubscriptionVo) subscriberList.get(subscriberList.size() - 1);
				pagingVo.setIdLast(lastRow.getEmailAddrId());
			}
			else {
				pagingVo.setIdFirst(-1);
				pagingVo.setIdLast(-1);
			}
			logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			//subscribers = new ListDataModel(subscriberList);
			subscribers = new PagedListDataModel(subscriberList, pagingVo.getRowCount(), pagingVo
					.getPageSize());
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
	
	public SubscriptionDao getSubscriptionDao() {
		if (subscriberDao == null) {
			subscriberDao = (SubscriptionDao) SpringUtil.getWebAppContext().getBean(
					"subscriptionDao");
		}
		return subscriberDao;
	}

	public void setSubscriptionDao(SubscriptionDao subscriberDao) {
		this.subscriberDao = subscriberDao;
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
		List<SubscriptionVo> subrList = getSubscriberList();
		for (int i=0; i<subrList.size(); i++) {
			SubscriptionVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSubscriptionDao().deleteByPrimaryKey(vo.getEmailAddrId(), listId);
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
		List<SubscriptionVo> subrList = getSubscriberList();
		for (int i=0; i<subrList.size(); i++) {
			SubscriptionVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				int rowsUpdated = getSubscriptionDao().update(vo);
				boolean acceptHtml =  Constants.YES_CODE.equalsIgnoreCase(vo.getAcceptHtml());
				rowsUpdated += getEmailAddrDao().updateAcceptHtml(vo.getEmailAddrId(), acceptHtml);
				if (rowsUpdated > 0) {
					logger.info("saveSubscribers() - Subscriber updated: " + vo.getEmailAddr());
				}
			}
		}
		refresh();
		return TO_SAVED;
	}

	public String addSubscriber() {
		if (isDebugEnabled)
			logger.debug("addSubscriber() - Entering...");
		reset();
		this.subscriber = new SubscriptionVo();
		subscriber.setListId(listId);
		subscriber.setSubscribed(MsgStatusCode.PENDING);
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
		List<SubscriptionVo> subrList = getSubscriberList();
		for (Iterator<SubscriptionVo> it=subrList.iterator(); it.hasNext();) {
			SubscriptionVo vo = it.next();
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
		SubscriptionVo vo = getSubscriptionDao().getByAddrAndListId(subId, listId);
		if (editMode == true && vo == null) {
			// subscriber does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "subscriberDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// subscriber already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
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
	private List<SubscriptionVo> getSubscriberList() {
		if (subscribers == null) {
			return new ArrayList<SubscriptionVo>();
		}
		else {
			return (List<SubscriptionVo>)subscribers.getWrappedData();
		}
	}

	public SubscriptionVo getSubscription() {
		return subscriber;
	}

	public void setSubscription(SubscriptionVo subscriber) {
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
