package com.legacytojava.msgui.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.MailingListVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class MailingListsBean {
	static final Logger logger = Logger.getLogger(MailingListsBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private MailingListDao mailingListDao = null;
	private DataModel mailingLists = null;
	private MailingListVo mailingList = null;
	private boolean editMode = true;
	
	private UIInput listIdInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (mailingLists == null) {
			List<MailingListVo> mailingListList = null;
			if (!ClientUtil.isProductKeyValid() && ClientUtil.isTrialPeriodEnded()) {
				mailingListList = getMailingListDao().getAllForTrial(false);
			}
			else {
				mailingListList = getMailingListDao().getAll(false);
			}
			mailingLists = new ListDataModel(mailingListList);
		}
		return mailingLists;
	}

	public String refresh() {
		mailingLists = null;
		return "";
	}
	
	public MailingListDao getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListDao) SpringUtil.getWebAppContext().getBean("mailingListDao");
		}
		return mailingListDao;
	}

	public void setMailingListDao(MailingListDao mailingListDao) {
		this.mailingListDao = mailingListDao;
	}
	
	public String viewMailingList() {
		if (isDebugEnabled)
			logger.debug("viewMailingList() - Entering...");
		if (mailingLists == null) {
			logger.warn("viewMailingList() - MailingList List is null.");
			return "mailinglist.failed";
		}
		if (!mailingLists.isRowAvailable()) {
			logger.warn("viewMailingList() - MailingList Row not available.");
			return "mailinglist.failed";
		}
		reset();
		this.mailingList = (MailingListVo) mailingLists.getRowData();
		logger.info("viewMailingList() - MailingList to be edited: " + mailingList.getListId());
		mailingList.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewMailingList() - MailingListVo to be passed to jsp: " + mailingList);
		
		return "mailinglist.edit";
	}
	
	public String saveMailingList() {
		if (isDebugEnabled)
			logger.debug("saveMailingList() - Entering...");
		if (mailingList == null) {
			logger.warn("saveMailingList() - MailingListVo is null.");
			return "mailinglist.failed";
		}
		reset();
		if (!EmailAddrUtil.isRemoteEmailAddress(mailingList.getEmailAddr())) {
			testResult = "invalidEmailAddress";
			return null;
		}
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			mailingList.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getMailingListDao().update(mailingList);
			logger.info("saveMailingList() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getMailingListDao().insert(mailingList);
			if (rowsInserted > 0)
				addToList(mailingList);
			logger.info("saveMailingList() - Rows Inserted: " + rowsInserted);
		}
		return "mailinglist.saved";
	}

	@SuppressWarnings("unchecked")
	private void addToList(MailingListVo vo) {
		List<MailingListVo> list = (List<MailingListVo>) mailingLists.getWrappedData();
		list.add(vo);
	}
	
	public String deleteMailingLists() {
		if (isDebugEnabled)
			logger.debug("deleteMailingLists() - Entering...");
		if (mailingLists == null) {
			logger.warn("deleteMailingLists() - MailingList List is null.");
			return "mailinglist.failed";
		}
		reset();
		List<MailingListVo> mailList = getMailingListList();
		for (int i=0; i<mailList.size(); i++) {
			MailingListVo vo = mailList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMailingListDao().deleteByListId(vo.getListId());
				if (rowsDeleted > 0) {
					logger.info("deleteMailingLists() - MailingList deleted: " + vo.getListId());
				}
				mailList.remove(vo);
			}
		}
		return "mailinglist.deleted";
	}
	
	public String copyMailingList() {
		if (isDebugEnabled)
			logger.debug("copyMailingList() - Entering...");
		if (mailingLists == null) {
			logger.warn("copyMailingList() - MailingList List is null.");
			return "mailinglist.failed";
		}
		reset();
		List<MailingListVo> mailList = getMailingListList();
		for (int i=0; i<mailList.size(); i++) {
			MailingListVo vo = mailList.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.mailingList = (MailingListVo) vo.getClone();
					mailingList.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					this.mailingList = new MailingListVo();
				}
				mailingList.setListId(null);
				mailingList.setMarkedForEdition(true);
				editMode = false;
				return "mailinglist.edit";
			}
		}
		return null;
	}
	
	public String addMailingList() {
		if (isDebugEnabled)
			logger.debug("addMailingList() - Entering...");
		reset();
		this.mailingList = new MailingListVo();
		mailingList.setMarkedForEdition(true);
		editMode = false;
		return "mailinglist.edit";
	}
	
	public String cancelEdit() {
		refresh();
		return "mailinglist.canceled";
	}
	
	public String uploadFiles() {
		String listId = mailingList.getListId();
		//String pageUrl = "/upload/msgInboxAttachFiles.jsp?frompage=uploademails&listid="+listId;
		String pageUrl = "/upload/emailAddrAttachFile.jsp?frompage=uploademails&listid="+listId;
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext ectx = context.getExternalContext();
		try {
			ectx.redirect(ectx.encodeResourceURL(ectx.getRequestContextPath() + pageUrl));
		}
		catch (IOException e) {
			logger.error("uploadFiles() - IOException caught", e);
			throw new FacesException("Cannot redirect to " + pageUrl + " due to IO exception.", e);
		}
		return null;
	}
	
	public boolean getAnyListsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyListsMarkedForDeletion() - Entering...");
		if (mailingLists == null) {
			logger.warn("getAnyListsMarkedForDeletion() - MailingList List is null.");
			return false;
		}
		List<MailingListVo> mailList = getMailingListList();
		for (Iterator<MailingListVo> it=mailList.iterator(); it.hasNext();) {
			MailingListVo vo = it.next();
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
		String listId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - listId: " + listId);
		MailingListVo vo = getMailingListDao().getByListId(listId);
		if (editMode == true && vo != null && mailingList != null
				&& vo.getRowId() != mailingList.getRowId()) {
			// mailingList does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
	        		"com.legacytojava.msgui.messages", "mailingListAlreadyExist", null);
					//"com.legacytojava.msgui.messages", "mailingListDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// mailingList already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "mailingListAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validateEmailAddress(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailAddress() - addr: " + emailAddr);
		if (!StringUtil.isEmpty(emailAddr)) {
			if (!EmailAddrUtil.isRemoteOrLocalEmailAddress(emailAddr)) {
				// invalid email address
		        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
						"com.legacytojava.msgui.messages", "invalidEmailAddress", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	public void validateAccountUserName(FacesContext context, UIComponent component, Object value) {
		String acctUserName = (String) value;
		if (isDebugEnabled)
			logger.debug("validateAccountUserName() - account user name: " + acctUserName);
		if (!StringUtil.isEmpty(acctUserName)) {
			if (!acctUserName.matches("^(?i)([a-z0-9\\.\\_\\%\\+\\-])+$")) {
				// invalid email address
		        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
						"com.legacytojava.msgui.messages", "invalidAccountUserName", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	/**
	 * actionListener
	 * @param e
	 */
	public void actionFired(ActionEvent e) {
		logger.info("actionFired(ActionEvent) - " + e.getComponent().getId());
	}
	
	/**
	 * valueChangeEventListener
	 * @param e
	 */
	public void fieldValueChanged(ValueChangeEvent e) {
		if (isDebugEnabled)
			logger.debug("fieldValueChanged(ValueChangeEvent) - " + e.getComponent().getId() + ": "
					+ e.getOldValue() + " -> " + e.getNewValue());
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		listIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<MailingListVo> getMailingListList() {
		if (mailingLists == null) {
			return new ArrayList<MailingListVo>();
		}
		else {
			return (List<MailingListVo>)mailingLists.getWrappedData();
		}
	}
	
	public MailingListVo getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingListVo mailingList) {
		this.mailingList = mailingList;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public UIInput getListIdInput() {
		return listIdInput;
	}

	public void setListIdInput(UIInput listIdInput) {
		this.listIdInput = listIdInput;
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
}
