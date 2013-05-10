package jpa.msgui.bean;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import jpa.model.UserData;
import jpa.msgui.filter.SessionTimeoutFilter;
import jpa.msgui.util.SpringUtil;
import jpa.service.UserDataService;

import org.apache.log4j.Logger;

public class ChangePasswordBean {
	static final Logger logger = Logger.getLogger(ChangePasswordBean.class);
	private String currPassword = null;
	private String password = null;
	private String confirm = null;
	private String message = null;
	
	private UserDataService userDao = null;
	
	public String changePassword() {
		message = null;
		UserData vo = getSessionUserData();
		if (vo == null) {
			message = "User is not logged in!";
			return "changepswd.failed";
		}
		UserData vo2 = getUserDataService().getByUserId(vo.getUserId());
		if (vo2 == null) {
			message = "Internal error, contact programming!";
			return "changepswd.failed";
		}
		logger.info("changePassword() - UserId: " +  vo.getUserId());
		if (!vo2.getPassword().equals(currPassword)) {
			message = "Current password is invalied.";
			return "changepswd.failed";
		}
		vo2.setPassword(password);
		getUserDataService().update(vo2);
		logger.info("changePassword() - rows updated: " + 1);
		return "changepswd.saved";
	}
	
	private UserDataService getUserDataService() {
		if (userDao == null)
			userDao = (UserDataService) SpringUtil.getWebAppContext().getBean("userDataService");
		return userDao;
	}
	
    // Getters
    public UserData getSessionUserData() {
		return (UserData) getHttpSession().getAttribute(SessionTimeoutFilter.USER_DATA_ID);
	}

	public void setSessionUserData(UserData userVo) {
		getHttpSession().setAttribute(SessionTimeoutFilter.USER_DATA_ID, userVo);
	}

	public HttpSession getHttpSession() {
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		return ((HttpSession) ctx.getSession(true));
	}
    
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getConfirm() {
		return confirm;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	public String getCurrPassword() {
		return currPassword;
	}

	public void setCurrPassword(String currPassword) {
		this.currPassword = currPassword;
	}
}