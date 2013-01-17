package com.legacytojava.msgui.corejsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.legacytojava.message.dao.customer.CustomerDao;
import com.legacytojava.message.vo.CustomerVo;

public class CustomersBean {
	static final Logger logger = Logger.getLogger(CustomersBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();

	private CustomerDao customerDao = null;
	private DataModel customers = null;
	private CustomerVo customer = null;
	private boolean editMode = true;

	public DataModel getAll() {
		if (customers == null) {
			List<CustomerVo> customerList = getCustomerDao().getAll();
			customers = new ListDataModel(customerList);
		}
		return customers;
	}

	public void refresh() {
		customers = null;
	}
	
	public CustomerDao getCustomerDao() {
		if (customerDao == null) {
			FacesContext facesCtx = FacesContext.getCurrentInstance();
			ServletContext sctx = (ServletContext) facesCtx.getExternalContext().getContext();
			WebApplicationContext ctx = WebApplicationContextUtils
					.getRequiredWebApplicationContext(sctx);
			customerDao = (CustomerDao) ctx.getBean("customerDao");
		}
		return customerDao;
	}

	public void setCustomerDao(CustomerDao customerDao) {
		this.customerDao = customerDao;
	}
	
	public String viewCustomer() {
		if (isDebugEnabled)
			logger.debug("viewCustomer() - Entering...");
		if (customers == null) {
			logger.warn("Customer List is null.");
			return "";
		}
		if (!customers.isRowAvailable()) {
			logger.warn("Customer Row not available.");
			return "";			
		}
		
		this.customer = (CustomerVo) customers.getRowData();
		if (isInfoEnabled)
			logger.info("CustId to be viewed: " + customer.getCustId());
		customer.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("CustomerVo to be passed to jsp: " + customer);
		
		return "edit";
	}
	
	public String viewCustomerV0() {
		if (isDebugEnabled)
			logger.debug("viewCustomer() - Entering...");
		if (customers == null) {
			logger.warn("Customer List is null.");
			return "";
		}
		FacesContext facesCtx = FacesContext.getCurrentInstance();
		Map<String, String> params = facesCtx.getExternalContext().getRequestParameterMap();
		// retrieve request parameter value
		String custId = params.get("custId");
		List<CustomerVo> custList = getCustomerList();
		for (Iterator<CustomerVo> it=custList.iterator(); it.hasNext();) {
			CustomerVo vo = it.next();
			if (vo.getCustId().equals(custId)) {
				Map<String, Object> session = facesCtx.getExternalContext().getSessionMap();
				// set session object so it is viewable from jsp
				session.put("cust_vo", vo);
				vo.setMarkedForEdition(true);
				if (isDebugEnabled)
					logger.debug("CustomerVo to be passed to jsp: " + vo);
				return "edit";
			}
		}
		return "";
	}

	public String saveCustomer() {
		if (isDebugEnabled)
			logger.debug("saveCustomer() - Entering...");
		if (customer == null) {
			logger.warn("CustomerVo is null.");
			return "";
		}
		// update database
		int rowsUpdated = getCustomerDao().update(customer);
		logger.info("in saveCustomer() - Rows Updated: " + rowsUpdated);
		return "success";
	}

	public String saveCustomerV0() {
		if (isDebugEnabled)
			logger.debug("saveCustomer() - Entering...");
		
		FacesContext facesCtx = FacesContext.getCurrentInstance();
		Map<String, Object> session = facesCtx.getExternalContext().getSessionMap();
		CustomerVo cust_vo = (CustomerVo) session.get("cust_vo");
		if (cust_vo == null) {
			logger.warn("CustomerVo is null.");
			return "";
		}
		// update database
		int rowsUpdated = getCustomerDao().update(cust_vo);
		logger.info("in saveCustomer() - Rows Updated: " + rowsUpdated);
		return "success";
	}

	public String deleteCustomers() {
		if (isDebugEnabled)
			logger.debug("deleteCustomers() - Entering...");
		if (customers == null) {
			logger.warn("Customer List is null.");
			return "";
		}
		List<CustomerVo> custList = getCustomerList();
		for (int i=0; i<custList.size(); i++) {
			CustomerVo vo = custList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getCustomerDao().delete(vo.getCustId());
				if (rowsDeleted > 0) {
					logger.info("deleteCustomers() - customer deleted: " + vo.getCustId());
					custList.remove(vo);
				}
			}
		}
		return "";
	}
	
	public boolean getAnyCustomersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyCustomersMarkedForDeletion() - Entering...");
		if (customers == null) {
			logger.warn("Customer List is null.");
			return false;
		}
		List<CustomerVo> custList = getCustomerList();
		for (Iterator<CustomerVo> it=custList.iterator(); it.hasNext();) {
			CustomerVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
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
}
