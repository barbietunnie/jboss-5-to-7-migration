package com.legacytojava.msgui.bean;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.user.UserDao;
import com.legacytojava.message.vo.UserVo;
import com.legacytojava.msgui.filter.SessionTimeoutFilter;
import com.legacytojava.msgui.util.SpringUtil;

public class ChangePasswordBean {
	static final Logger logger = Logger.getLogger(ChangePasswordBean.class);
	private String currPassword = null;
	private String password = null;
	private String confirm = null;
	private String message = null;
	
	private UserDao userDao = null;
	
	public String changePassword() {
		message = null;
		UserVo vo = getSessionUserVo();
		if (vo == null) {
			message = "User is not logged in!";
			return "changepswd.failed";
		}
		UserVo vo2 = getUserDao().getByPrimaryKey(vo.getUserId());
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
		int rowsUpdated = getUserDao().update(vo2);
		logger.info("changePassword() - rows updated: " + rowsUpdated);
		return "changepswd.saved";
	}
	
	private UserDao getUserDao() {
		if (userDao == null)
			userDao = (UserDao) SpringUtil.getWebAppContext().getBean("userDao");
		return userDao;
	}
	
    // Getters
    public UserVo getSessionUserVo() {
		return (UserVo) getHttpSession().getAttribute(SessionTimeoutFilter.USER_VO_ID);
	}

	public void setSessionUserVo(UserVo userVo) {
		getHttpSession().setAttribute(SessionTimeoutFilter.USER_VO_ID, userVo);
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
