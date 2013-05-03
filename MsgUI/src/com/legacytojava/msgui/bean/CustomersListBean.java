package com.legacytojava.msgui.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import com.legacytojava.message.bo.customer.CustomerBo;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MobileCarrier;
import com.legacytojava.message.dao.customer.CustomerDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.PhoneNumberUtil;
import com.legacytojava.message.util.SsnNumberUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.CustomerVo;
import com.legacytojava.message.vo.PagingCustomerVo;
import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class CustomersListBean {
	static final Logger logger = Logger.getLogger(CustomersListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private CustomerDao customerDao = null;
	private CustomerBo customerBo = null;
	private DataModel customers = null;
	private CustomerVo customer = null;
	private boolean editMode = true;

	private HtmlDataTable dataTable;
	private final PagingCustomerVo pagingVo =  new PagingCustomerVo();;
	private String searchString = null;
	
	private UIInput custIdInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private UIInput emailAddrInput = null;
	private UIInput ssnNumberInput = null;
	private UIInput dayPhoneInput = null;
	private UIInput eveningPhoneInput = null;
	private UIInput mobilePhoneInput = null;
	private UIInput birthDateInput = null;
	private UIInput endDateInput = null;
	private UIInput mobileCarrierInput = null;
	
	private final CustomerVo custMeta = new CustomerVo();
	
	private static String TO_EDIT = "customerlist.edit";
	private static String TO_FAILED = "customerlist.failed";
	private static String TO_DELETED = "customerlist.deleted";
	private static String TO_SAVED = "customerlist.saved";
	private static String TO_CANCELED = "customerlist.canceled";
	private static String TO_SELF = "customerlist.toself";
	private static String TO_PAGING = "customerlist.paging";

	public DataModel getCustomers() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getCustomerDao().getCustomerCount(pagingVo);
			pagingVo.setRowCount(rowCount);
		}
		if (customers == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<CustomerVo> customerList = getCustomerDao().getCustomersWithPaging(pagingVo);
			/* set keys for paging */
			if (!customerList.isEmpty()) {
				CustomerVo firstRow = (CustomerVo) customerList.get(0);
				pagingVo.setStrIdFirst(firstRow.getCustId());
				CustomerVo lastRow = (CustomerVo) customerList.get(customerList.size() - 1);
				pagingVo.setStrIdLast(lastRow.getCustId());
			}
			else {
				pagingVo.setStrIdFirst(null);
				pagingVo.setStrIdLast(null);
			}
			logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			//customers = new ListDataModel(customerList);
			customers = new PagedListDataModel(customerList, pagingVo.getRowCount(), pagingVo
					.getPageSize());
		}
		return customers;
	}

	public String viewCustomer() {
		if (isDebugEnabled)
			logger.debug("viewCustomer() - Entering...");
		if (customers == null) {
			logger.warn("viewCustomer() - Customer List is null.");
			return TO_FAILED;
		}
		if (!customers.isRowAvailable()) {
			logger.warn("viewCustomer() - Customer Row not available.");
			return TO_FAILED;
		}
		reset();
		this.customer = (CustomerVo) customers.getRowData();
		logger.info("viewCustomer() - Customer to be edited: " + customer.getCustId());
		customer.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewCustomer() - CustomerVo to be passed to jsp: " + customer);
		}
		return TO_EDIT;
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
		customers = null;
	}

	public String refreshPage() {
		refresh();
		pagingVo.setRowCount(-1);
		return TO_SELF;
	}
	
	public CustomerVo getData() {
		customer = getCustomerDao().getByCustId(customer.getCustId());
		reset();
		return customer;
	}
	
	public String refreshCustomer() {
		getData();
		FacesUtil.refreshCurrentJSFPage();
		return TO_SELF;
	}

	private void resetPagingVo() {
		pagingVo.resetPageContext();
		if (dataTable != null) dataTable.setFirst(0);
		refresh();
	}
	
	private void reset() {
		testResult = null;
		actionFailure = null;
		custIdInput = null;
		emailAddrInput = null;
		ssnNumberInput = null;
		dayPhoneInput = null;
		eveningPhoneInput = null;
		mobilePhoneInput = null;
		birthDateInput = null;
		endDateInput = null;
		mobileCarrierInput = null;
	}
	
	public String deleteCustomers() {
		if (isDebugEnabled)
			logger.debug("deleteCustomers() - Entering...");
		if (customers == null) {
			logger.warn("deleteCustomers() - Customer List is null.");
			return TO_FAILED;
		}
		reset();
		List<CustomerVo> addrList = getCustomerList();
		for (int i=0; i<addrList.size(); i++) {
			CustomerVo vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getCustomerDao().delete(vo.getCustId());
				if (rowsDeleted > 0) {
					logger.info("deleteCustomers() - Customer deleted: " + vo.getCustId());
					pagingVo.setRowCount(pagingVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public String saveCustomer() {
		if (isDebugEnabled)
			logger.debug("saveCustomer() - Entering...");
		if (customer == null) {
			logger.warn("saveCustomer() - Customer Vo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			customer.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		try {
			if (editMode == true) {
				int rowsUpdated = getCustomerBo().update(customer);
				logger.info("saveCustomer() - Rows Updated: " + rowsUpdated);
			}
			else {
				int rowsInserted = getCustomerBo().insert(customer);
				if (rowsInserted > 0) {
					addToList(customer);
					pagingVo.setRowCount(pagingVo.getRowCount() + rowsInserted);
					refresh();
				}
				logger.info("saveCustomer() - Rows Inserted: " + rowsInserted);
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
	private void addToList(CustomerVo vo) {
		List<CustomerVo> list = (List<CustomerVo>)customers.getWrappedData();
		list.add(vo);
	}

	public String copyCustomer() {
		if (isDebugEnabled)
			logger.debug("copyCustomer() - Entering...");
		if (customers == null) {
			logger.warn("copyCustomer() - Customer List is null.");
			return TO_FAILED;
		}
		reset();
		List<CustomerVo> custList = getCustomerList();
		for (int i=0; i<custList.size(); i++) {
			CustomerVo vo = custList.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.customer = (CustomerVo) vo.getClone();
					customer.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					this.customer = new CustomerVo();
				}
				//customer.setLastName(null);
				//customer.setFirstName(null);
				customer.setCustId(null);
				customer.setEmailAddr(null);
				customer.setUserPassword(null);
				customer.setMarkedForEdition(true);
				editMode = false;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public String addCustomer() {
		if (isDebugEnabled)
			logger.debug("addCustomer() - Entering...");
		reset();
		this.customer = new CustomerVo();
		customer.setMarkedForEdition(true);
		customer.setUpdtUserId(Constants.DEFAULT_USER_ID);
		editMode = false;
		return TO_EDIT;
	}
	
	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getAnyCustomersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyCustomersMarkedForDeletion() - Entering...");
		if (customers == null) {
			logger.warn("getAnyCustomersMarkedForDeletion() - Customer List is null.");
			return false;
		}
		List<CustomerVo> addrList = getCustomerList();
		for (Iterator<CustomerVo> it=addrList.iterator(); it.hasNext();) {
			CustomerVo vo = it.next();
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
		String custId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - CustId: " + custId);
		CustomerVo vo = getCustomerDao().getByCustId(custId);
		if (editMode == true && vo != null && customer != null
				&& vo.getRowId() != customer.getRowId()) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					//"com.legacytojava.msgui.messages", "customerDoesNotExist", null);
	        		"com.legacytojava.msgui.messages", "customerAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// customer already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "customerAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validateEmailAddress(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailAddress() - addr: " + emailAddr);
		if (!StringUtil.isEmpty(emailAddr)) {
			if (!EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
				// invalid email address
		        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
						"com.legacytojava.msgui.messages", "invalidEmailAddress", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
			else {
				CustomerVo vo = getCustomerDao().getByEmailAddress(emailAddr);
				if (vo != null && customer != null && !vo.getCustId().equals(customer.getCustId())) {
					FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
							"com.legacytojava.msgui.messages", "emailAddressAlreadyUsed", null);
					message.setSeverity(FacesMessage.SEVERITY_WARN);
					throw new ValidatorException(message);
				}
			}
		}
	}
	
	public void validateSsnNumber(FacesContext context, UIComponent component, Object value) {
		String ssn = (String) value;
		if (isDebugEnabled)
			logger.debug("validateSsnNumber() - SSN: " + ssn);
		if (!StringUtil.isEmpty(ssn) && !SsnNumberUtil.isValidSSN(ssn)) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "invalidSsnNumber", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateDate(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateDate() - date = " + value);
		if (value != null && !(value instanceof Date)) {
			FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "invalidDate", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validatePhoneNumber(FacesContext context, UIComponent component, Object value) {
		String phone = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePhoneNumber() - Phone Number: " + phone);
		if (!StringUtil.isEmpty(phone) && !PhoneNumberUtil.isValidPhoneNumber(phone)) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "invalidPhoneNumber", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateMibileCarrier(FacesContext context, UIComponent component, Object value) {
		String carrier = (String) value;
		if (isDebugEnabled)
			logger.debug("validateMibileCarrier() - Phone Number: " + carrier);
		if (!StringUtil.isEmpty(carrier)) {
			try {
				MobileCarrier.getByValue(carrier);
			}
			catch (IllegalArgumentException e) {
		        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
						"com.legacytojava.msgui.messages", "invalidMobileCarrier", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	public void validateZipCode5(FacesContext context, UIComponent component, Object value) {
		String zip5 = (String) value;
		if (isDebugEnabled)
			logger.debug("validateZipCode5() - Zip Code: " + zip5);
		if (!StringUtil.isEmpty(zip5) && !zip5.matches("\\d{5}")) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
	        		"com.legacytojava.msgui.messages", "invalideZipCode", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	public void validateZipCode4(FacesContext context, UIComponent component, Object value) {
		String zip4 = (String) value;
		if (isDebugEnabled)
			logger.debug("validateZipCode4() - Zip Code: " + zip4);
		if (!StringUtil.isEmpty(zip4) && !zip4.matches("\\d{4}")) {
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
	        		"com.legacytojava.msgui.messages", "invalideZipCode", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	@SuppressWarnings({ "unchecked" })
	private List<CustomerVo> getCustomerList() {
		if (customers == null) {
			return new ArrayList<CustomerVo>();
		}
		else {
			return (List<CustomerVo>)customers.getWrappedData();
		}
	}

	public CustomerDao getCustomerDao() {
		if (customerDao == null) {
			customerDao = (CustomerDao) SpringUtil.getWebAppContext().getBean("customerDao");
		}
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}

	public CustomerBo getCustomerBo() {
		if (customerBo == null) {
			customerBo = (CustomerBo) SpringUtil.getWebAppContext().getBean("customerBo");
		}
		return customerBo;
	}

	public CustomerVo getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerVo customer) {
		this.customer = customer;
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

	public UIInput getCustIdInput() {
		return custIdInput;
	}

	public void setCustIdInput(UIInput custIdInput) {
		this.custIdInput = custIdInput;
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

	public CustomerVo getCustMeta() {
		return custMeta;
	}
}
