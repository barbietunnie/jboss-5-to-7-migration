package com.legacytojava.msgui.listener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.user.SessionUploadDao;
import com.legacytojava.message.dao.user.UserDao;
import com.legacytojava.message.vo.UserVo;
import com.legacytojava.msgui.filter.SessionTimeoutFilter;
import com.legacytojava.msgui.util.SpringUtil;

/**
 * When a user session times out, the sessionDestroyed() method will be invoked.
 * This method will make necessary cleanups (logging out user, updating database
 * and audit logs, etc...). After this method, we will be in a clean and stable
 * state.
 */
public class SessionExpiredListener implements HttpSessionListener {
	static final Logger logger = Logger.getLogger(SessionExpiredListener.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	public SessionExpiredListener() {
	}

	public void sessionCreated(HttpSessionEvent event) {
		if (isDebugEnabled)
			logger.debug("sessionCreated() - " + event.getSession().getId());
	}

	public void sessionDestroyed(HttpSessionEvent event) {
		// get the session to be destroyed...
		HttpSession session = event.getSession();
		if (isDebugEnabled)
			logger.debug("sessionDestroyed() - " + session.getId() + " Logging out user...");
		/*
		 * nobody can reach user data after this point because session is
		 * invalidated already. So, get the user data from session and save
		 * its logout information before losing it. User's redirection to the
		 * timeout page will be handled by the SessionTimeoutFilter.
		 */
		try {
			sessionExpired(session);
		}
		catch (Exception e) {
			logger.error("error while logging out at session destroyed", e);
		}
	}

	/**
	 * Gets the logged in user data from userVo and makes necessary logout
	 * operations.
	 */
	public static void sessionExpired(HttpSession httpSession) {
		// update users table with "hits" and "last access time"
		UserVo userVo = (UserVo) httpSession.getAttribute(SessionTimeoutFilter.USER_VO_ID);
		if (userVo != null) {
			int rowsUpdated = getUserDao(httpSession).update4Web(userVo);
			logger.info("sessionExpired() - Users table - rows updated: " + rowsUpdated);
		}
		else {
			logger.warn("sessionExpired() - UserVo is null, user info not updated.");
		}
		
		//jsessionid=1A706557C46AB7A909464548A0622EEF
		String sessionId = httpSession.getId(); // get SessionId
		logger.info("sessionExpired() - sessionId: " + sessionId);
		// clean up user session tables
		if (sessionId != null) {
			int rowsDeleted = getSessionUploadDao(httpSession).deleteBySessionId(sessionId);
			logger.info("sessionExpired() - rows deleted from SessionUploads: " + rowsDeleted);
		}
		else {
			logger.warn("sessionExpired() - SessionId is null, SessionUploads not cleaned.");
		}
	}

	private static UserDao getUserDao(HttpSession httpSession) {
		ServletContext ctx = httpSession.getServletContext();
		return (UserDao) SpringUtil.getWebAppContext(ctx).getBean("userDao");
	}

	private static SessionUploadDao getSessionUploadDao(HttpSession httpSession) {
		ServletContext ctx = httpSession.getServletContext();
		return (SessionUploadDao) SpringUtil.getWebAppContext(ctx).getBean("sessionUploadDao");
	}
}
