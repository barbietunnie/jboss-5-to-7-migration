package com.legacytojava.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.user.UserDao;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.UserVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class UserAccountsBean {
	static final Logger logger = Logger.getLogger(UserAccountsBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private UserDao userDao = null;
	private DataModel users = null;
	private UserVo user = null;
	private boolean editMode = true;
	
	private UIInput userIdInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (users == null) {
			List<UserVo> userList = getUserDao().getAll(false);
			users = new ListDataModel(userList);
		}
		return users;
	}

	public String refresh() {
		users = null;
		return "";
	}
	
	public UserDao getUserDao() {
		if (userDao == null) {
			userDao = (UserDao) SpringUtil.getWebAppContext().getBean("userDao");
		}
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public String viewUser() {
		if (isDebugEnabled)
			logger.debug("viewUser() - Entering...");
		if (users == null) {
			logger.warn("viewUser() - User List is null.");
			return "useraccount.failed";
		}
		if (!users.isRowAvailable()) {
			logger.warn("viewUser() - User Row not available.");
			return "useraccount.failed";
		}
		reset();
		this.user = (UserVo) users.getRowData();
		logger.info("viewUser() - User to be edited: " + user.getUserId());
		user.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewUser() - UserVo to be passed to jsp: " + user);
		
		return "useraccount.edit";
	}
	
	public String saveUser() {
		if (isDebugEnabled)
			logger.debug("saveUser() - Entering...");
		if (user == null) {
			logger.warn("saveUser() - UserVo is null.");
			return "useraccount.failed";
		}
		reset();
		if (user.getEmailAddr() != null && user.getEmailAddr().trim().length() > 0) {
			if (!EmailAddrUtil.isRemoteEmailAddress(user.getEmailAddr())) {
				testResult = "invalidEmailAddress";
				return null;
			}
		}
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			user.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getUserDao().update(user);
			logger.info("in saveUser() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getUserDao().insert(user);
			if (rowsInserted > 0)
				addToList(user);
			logger.info("saveUser() - Rows Inserted: " + rowsInserted);
		}
		return "useraccount.saved";
	}

	@SuppressWarnings("unchecked")
	private void addToList(UserVo vo) {
		List<UserVo> list = (List<UserVo>) users.getWrappedData();
		list.add(vo);
	}
	
	public String deleteUsers() {
		if (isDebugEnabled)
			logger.debug("deleteUsers() - Entering...");
		if (users == null) {
			logger.warn("deleteUsers() - User List is null.");
			return "useraccount.failed";
		}
		reset();
		List<UserVo> smtpList = getUserList();
		for (int i=0; i<smtpList.size(); i++) {
			UserVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getUserDao().deleteByPrimaryKey(vo.getUserId());
				if (rowsDeleted > 0) {
					logger.info("deleteUsers() - User deleted: " + vo.getUserId());
				}
				smtpList.remove(vo);
			}
		}
		return "useraccount.deleted";
	}
	
	public String copyUser() {
		if (isDebugEnabled)
			logger.debug("copyUser() - Entering...");
		if (users == null) {
			logger.warn("copyUser() - User List is null.");
			return "useraccount.failed";
		}
		reset();
		List<UserVo> smtpList = getUserList();
		for (int i=0; i<smtpList.size(); i++) {
			UserVo vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.user = (UserVo) vo.getClone();
					user.setMarkedForDeletion(false);
					user.setHits(0);
					user.setLastVisitTime(null);
				}
				catch (CloneNotSupportedException e) {
					this.user = new UserVo();
				}
				user.setUserId(null);
				user.setMarkedForEdition(true);
				editMode = false;
				return "useraccount.edit";
			}
		}
		return null;
	}
	
	public String addUser() {
		if (isDebugEnabled)
			logger.debug("addUser() - Entering...");
		reset();
		this.user = new UserVo();
		user.setMarkedForEdition(true);
		editMode = false;
		return "useraccount.edit";
	}
	
	public String cancelEdit() {
		refresh();
		return "useraccount.canceled";
	}
	
	public boolean getAnyUsersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyUsersMarkedForDeletion() - Entering...");
		if (users == null) {
			logger.warn("getAnyUsersMarkedForDeletion() - User List is null.");
			return false;
		}
		List<UserVo> smtpList = getUserList();
		for (Iterator<UserVo> it=smtpList.iterator(); it.hasNext();) {
			UserVo vo = it.next();
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
		String userId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - userId: " + userId);
		UserVo vo = getUserDao().getByPrimaryKey(userId);
		if (editMode == true && vo == null) {
			// user does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "userDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// user already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "userAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
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
		userIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<UserVo> getUserList() {
		if (users == null) {
			return new ArrayList<UserVo>();
		}
		else {
			return (List<UserVo>)users.getWrappedData();
		}
	}
	
	public UserVo getUser() {
		return user;
	}

	public void setUser(UserVo user) {
		this.user = user;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public UIInput getUserIdInput() {
		return userIdInput;
	}

	public void setUserIdInput(UIInput userIdInput) {
		this.userIdInput = userIdInput;
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
