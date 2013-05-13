package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;
import javax.persistence.NoResultException;

import jpa.model.UserData;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.UserDataService;
import jpa.util.EmailAddrUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="userData")
@SessionScoped
public class UserDataBean {
	static final Logger logger = Logger.getLogger(UserDataBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private UserDataService userDao = null;
	private DataModel<UserData> users = null;
	private UserData user = null;
	private boolean editMode = true;
	
	private UIInput userIdInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	public DataModel<UserData> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (users == null) {
			List<UserData> userList = getUserDataService().getAll();
			users = new ListDataModel<UserData>(userList);
		}
		return users;
	}

	public String refresh() {
		users = null;
		return "";
	}
	
	public UserDataService getUserDataService() {
		if (userDao == null) {
			userDao = (UserDataService) SpringUtil.getWebAppContext().getBean("userDataService");
		}
		return userDao;
	}

	public void setUserDataService(UserDataService userDao) {
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
		this.user = (UserData) users.getRowData();
		logger.info("viewUser() - User to be edited: " + user.getUserId());
		user.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled)
			logger.debug("viewUser() - UserData to be passed to jsp: " + user);
		
		return "useraccount.edit";
	}
	
	public String saveUser() {
		if (isDebugEnabled)
			logger.debug("saveUser() - Entering...");
		if (user == null) {
			logger.warn("saveUser() - UserData is null.");
			return "useraccount.failed";
		}
		reset();
		if (user.getEmailAddr() != null && StringUtils.isNotBlank(user.getEmailAddr().getAddress())) {
			if (!EmailAddrUtil.isRemoteEmailAddress(user.getEmailAddr().getAddress())) {
				testResult = "invalidEmailAddress";
				return null;
			}
		}
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			user.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getUserDataService().update(user);
			logger.info("in saveUser() - Rows Updated: " + 1);
		}
		else {
			getUserDataService().insert(user);
			addToList(user);
			logger.info("saveUser() - Rows Inserted: " + 1);
		}
		return "useraccount.saved";
	}

	@SuppressWarnings("unchecked")
	private void addToList(UserData vo) {
		List<UserData> list = (List<UserData>) users.getWrappedData();
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
		List<UserData> smtpList = getUserList();
		for (int i=0; i<smtpList.size(); i++) {
			UserData vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getUserDataService().deleteByUserId(vo.getUserId());
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
		List<UserData> smtpList = getUserList();
		for (int i=0; i<smtpList.size(); i++) {
			UserData vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.user = new UserData();
				try {
					vo.copyPropertiesTo(this.user);
					user.setMarkedForDeletion(false);
					user.setHits(0);
					user.setLastVisitTime(null);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
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
		this.user = new UserData();
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
		List<UserData> smtpList = getUserList();
		for (Iterator<UserData> it=smtpList.iterator(); it.hasNext();) {
			UserData vo = it.next();
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
		try {
			UserData vo = getUserDataService().getByUserId(userId);
			if (editMode == false && vo != null) {
				// user already exist
		        FacesMessage message =jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "userAlreadyExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
		catch (NoResultException e) {
			if (editMode == true) {
				// user does not exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "userDoesNotExist", null);
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
		userIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<UserData> getUserList() {
		if (users == null) {
			return new ArrayList<UserData>();
		}
		else {
			return (List<UserData>)users.getWrappedData();
		}
	}
	
	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
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
