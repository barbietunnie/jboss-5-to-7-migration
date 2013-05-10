package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;

import jpa.constant.Constants;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.EmailAddressService;
import jpa.service.MailingListService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class EmailAddressBean {
	static final Logger logger = Logger.getLogger(EmailAddressBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private EmailAddressService emailAddrDao = null;
	private MailingListService mailingListDao = null;
	private DataModel<EmailAddress> emailAddrs = null;
	private EmailAddress emailAddr = null;
	private boolean editMode = true;

	private HtmlDataTable dataTable;
	private final PagingVo pagingVo =  new PagingVo();;
	private String searchString = null;
	
	private List<MailingList> mailingLists = null;
	private UIInput emailAddrInput = null;
	private String testResult = null;
	private String actionFailure = null;

	private static String TO_FAILED = "emailAddrlist.failed";
	private static String TO_DELETED = "emailAddrlist.deleted";
	private static String TO_EDIT = "emailAddrlist.edit";
	private static String TO_SAVED = "emailAddrlist.saved";
	private static String TO_CANCELED = "emailAddrlist.canceled";
	private static String TO_PAGING = "emailAddrlist.paging";
	private static String TO_SELF = "emailAddrlist.toself";
	
	@SuppressWarnings("unchecked")
	public DataModel<EmailAddress> getEmailAddrs() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getEmailAddressService().getEmailAddressCount(pagingVo);
			pagingVo.setRowCount(rowCount);
		}
		if (emailAddrs == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<EmailAddress> emailAddrList = getEmailAddressService().getEmailAddrsWithPaging(pagingVo);
			/* set keys for paging */
			if (!emailAddrList.isEmpty()) {
				EmailAddress firstRow = (EmailAddress) emailAddrList.get(0);
				EmailAddress lastRow = (EmailAddress) emailAddrList.get(emailAddrList.size() - 1);
				//pagingVo.setIdFirst(firstRow.getEmailAddrId());
				//pagingVo.setIdLast(lastRow.getEmailAddrId());
				pagingVo.setStrIdFirst(firstRow.getAddress());
				pagingVo.setStrIdLast(lastRow.getAddress());
			}
			else {
				//pagingVo.setIdFirst(-1);
				//pagingVo.setIdLast(-1);
				pagingVo.setStrIdFirst(null);
				pagingVo.setStrIdLast(null);
			}
			logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			//emailAddrs = new ListDataModel(emailAddrList);
			emailAddrs = new PagedListDataModel(emailAddrList, pagingVo.getRowCount(), pagingVo.getPageSize());
		}
		return emailAddrs;
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
		return TO_SELF;
	}
	
	public String resetSearch() {
		searchString = null;
		pagingVo.setSearchString(null);
		resetPagingVo();
		return TO_SELF;
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
	
	private void refresh() {
		emailAddrs = null;
	}

	public String refreshPage() {
		refresh();
		pagingVo.setRowCount(-1);
		return TO_SELF;
	}
	
	private void resetPagingVo() {
		pagingVo.resetPageContext();
		if (dataTable != null) dataTable.setFirst(0);
		refresh();
	}
	
	public EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddressService) SpringUtil.getWebAppContext().getBean("emailAddrDao");
		}
		return emailAddrDao;
	}

	public void setEmailAddressService(EmailAddressService emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public MailingListService getMailingListService() {
		if (mailingListDao == null)
			mailingListDao = (MailingListService) SpringUtil.getWebAppContext().getBean("mailingListDao");
		return mailingListDao;
	}

	public void setMailingListService(MailingListService mailingListDao) {
		this.mailingListDao = mailingListDao;
	}

	public String viewEmailAddr() {
		if (isDebugEnabled)
			logger.debug("viewEmailAddr() - Entering...");
		if (emailAddrs == null) {
			logger.warn("viewEmailAddr() - EmailAddr List is null.");
			return TO_FAILED;
		}
		if (!emailAddrs.isRowAvailable()) {
			logger.warn("viewEmailAddr() - EmailAddr Row not available.");
			return TO_FAILED;
		}
		reset();
		this.emailAddr = (EmailAddress) emailAddrs.getRowData();
		logger.info("viewEmailAddr() - EmailAddr to be edited: " + emailAddr.getAddress());
		emailAddr.setMarkedForEdition(true);
		editMode = true;
		mailingLists = getMailingListService().getByEmailAddress(emailAddr.getAddress());
		if (isDebugEnabled) {
			logger.debug("viewEmailAddr() - EmailAddress to be passed to jsp: " + emailAddr);
		}
		return TO_EDIT;
	}

	public String saveEmailAddr() {
		if (isDebugEnabled)
			logger.debug("saveEmailAddr() - Entering...");
		if (emailAddr == null) {
			logger.warn("saveEmailAddr() - EmailAddress is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			emailAddr.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getEmailAddressService().update(emailAddr);
			logger.info("saveEmailAddr() - Rows Updated: " + 1);
		}
		else {
			getEmailAddressService().insert(emailAddr);
			addToList(emailAddr);
			pagingVo.setRowCount(pagingVo.getRowCount() + 1);
			refresh();
			logger.info("saveEmailAddr() - Rows Inserted: " + 1);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(EmailAddress vo) {
		List<EmailAddress> list = (List<EmailAddress>) emailAddrs.getWrappedData();
		list.add(vo);
	}

	public String deleteEmailAddrs() {
		if (isDebugEnabled)
			logger.debug("deleteEmailAddrs() - Entering...");
		if (emailAddrs == null) {
			logger.warn("deleteEmailAddrs() - EmailAddr List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailAddress> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddress vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailAddressService().deleteByRowId(vo.getRowId());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailAddrs() - EmailAddr deleted: " + vo.getAddress());
					pagingVo.setRowCount(pagingVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public String saveEmailAddrs() {
		if (isDebugEnabled)
			logger.debug("saveEmailAddrs() - Entering...");
		if (emailAddrs == null) {
			logger.warn("saveEmailAddrs() - EmailAddr List is null.");
			return TO_FAILED;
		}
		reset();
		List<EmailAddress> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddress vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				if (!vo.getAddress().equals(vo.getCurrAddress())) {
					EmailAddress vo2 = getEmailAddressService().getByAddress(vo.getAddress());
					if (vo2 != null && vo2.getRowId() != vo.getRowId()) {
						actionFailure = "Email address " + vo.getAddress() + " already exists.";
						return TO_SELF;
					}
				}
				getEmailAddressService().update(vo);
				logger.info("saveEmailAddrs() - EmailAddr updated: " + vo.getAddress());
				vo.setMarkedForDeletion(false);
			}
		}
		refresh();
		return TO_SAVED;
	}

	public String addEmailAddr() {
		if (isDebugEnabled)
			logger.debug("addEmailAddr() - Entering...");
		reset();
		this.emailAddr = new EmailAddress();
		emailAddr.setMarkedForEdition(true);
		emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
		if (mailingLists != null)
			mailingLists.clear();
		editMode = false;
		return TO_EDIT;
	}

	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getAnyEmailAddrsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyEmailAddrsMarkedForDeletion() - Entering...");
		if (emailAddrs == null) {
			logger.warn("getAnyEmailAddrsMarkedForDeletion() - EmailAddr List is null.");
			return false;
		}
		List<EmailAddress> addrList = getEmailAddrList();
		for (Iterator<EmailAddress> it=addrList.iterator(); it.hasNext();) {
			EmailAddress vo = it.next();
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
		String address = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - address: " + address);
		EmailAddress vo = getEmailAddressService().getByAddress(address);
		if (editMode == true && vo != null && emailAddr != null
				&& vo.getRowId() != emailAddr.getRowId()) {
			// emailAddr does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					//"com.legacytojava.msgui.messages", "emailAddrDoesNotExist", null);
	        		"com.legacytojava.msgui.messages", "emailAddrAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// emailAddr already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "emailAddrAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	void reset() {
		emailAddrInput = null;
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings({ "unchecked" })
	private List<EmailAddress> getEmailAddrList() {
		if (emailAddrs == null) {
			return new ArrayList<EmailAddress>();
		}
		else {
			return (List<EmailAddress>)emailAddrs.getWrappedData();
		}
	}

	public EmailAddress getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(EmailAddress emailAddr) {
		this.emailAddr = emailAddr;
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

	public UIInput getEmailAddrInput() {
		return emailAddrInput;
	}

	public void setEmailAddrInput(UIInput emailAddrInput) {
		this.emailAddrInput = emailAddrInput;
	}

	public List<MailingList> getMailingLists() {
		return mailingLists;
	}

	public void setMailingLists(List<MailingList> mailingLists) {
		this.mailingLists = mailingLists;
	}

	public boolean isMailingListsEmpty() {
		return (mailingLists == null || mailingLists.isEmpty()); 
	}
}