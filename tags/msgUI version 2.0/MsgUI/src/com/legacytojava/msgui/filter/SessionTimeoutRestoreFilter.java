package com.legacytojava.msgui.filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.user.UserDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.UserVo;
import com.legacytojava.msgui.util.HttpServletUtil;
import com.legacytojava.msgui.util.SpringUtil;

/**
 * The UserVo filter. A specific user id is saved in a long-living cookie so
 * that when a user session is timed out, user session data can be retrieved
 * from database using the user id, and the user session can be restored without
 * requiring user to login again every time user closes browser or user session
 * times out.
 */
public class SessionTimeoutRestoreFilter implements Filter {
	static final Logger logger = Logger.getLogger(SessionTimeoutRestoreFilter.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	private String timeoutPage = "/timeout.faces";
	private String loginPage = "/login.faces";
	/** The unique ID to set and get the SessionId from the HttpSession. */
    private static final String COOKIE_ID = "UserVoFilterCookieId";
    private static final int COOKIE_MAX_AGE = 31536000; // 60*60*24*365 seconds; 1 year.
	/** The unique ID to set and get the UserVo from the HttpSession. */
	public static final String USER_VO_ID = "SessionTimeoutFilter.UserVo";

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) {
		// Nothing to do here.
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 * TODO: not completed yet
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		// Check PathInfo.
		if (isPageSessioned(httpRequest) == false) {
			chain.doFilter(request, response);
			return;
		}
		// Get UserVo from HttpSession.
		HttpSession httpSession = httpRequest.getSession();
		UserVo userVo = (UserVo) httpSession.getAttribute(USER_VO_ID);
		if (userVo == null) {
			// No UserVo found in HttpSession; lookup SessionId in cookie.
			String sessionId = HttpServletUtil.getCookieValue(httpRequest, COOKIE_ID);
			if (sessionId != null) {
				// SessionId found in cookie. Lookup UserVo by SessionId in
				// database.
				userVo = getUserDao(httpSession).getByPrimaryKey(sessionId);
			}
			if (userVo == null) {
				// No SessionId found in cookie, or no UserVo found in database.
				// Create a new UserVo.
				sessionId = UUID.randomUUID().toString();
				userVo = new UserVo();
				userVo.setSessionId(sessionId);
				// TODO: populate userVo before insert
				int rowsInserted = getUserDao(httpSession).insert(userVo);
				if (rowsInserted > 0 && isDebugEnabled)
					logger.debug("doFilter() - userVo inserted");
				// Put SessionId in cookie.
				HttpServletUtil.setCookieValue(httpResponse, COOKIE_ID, sessionId, COOKIE_MAX_AGE);
			}
			// Set UserVo in current HttpSession.
			httpSession.setAttribute(USER_VO_ID, userVo);
		}
		// Add hit count and update UserVo.
		userVo.addHit();
		// Continue filtering.
		chain.doFilter(request, response);
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// Apparently there's nothing to destroy?
	}
	
	/**
	 * session shouldn't be checked for some pages. For example: for timeout
	 * page.. Since we're redirecting to timeout page from this filter, if we
	 * don't disable session control for it, filter will again redirect to it
	 * and this will be result with an infinite loop...
	 */
	private boolean isPageSessioned(HttpServletRequest httpRequest) {
		String pathInfo = StringUtil
				.trim(httpRequest.getRequestURI(), httpRequest.getContextPath());
		boolean pageSessioned = false;
		if (pathInfo.startsWith("/includes") || pathInfo.startsWith("/images")
				|| pathInfo.startsWith("/htmls")) {
			// This is not necessary, but it might be useful if you want to skip
			// some include files. If those include files are loaded, continue 
			// the filter chain and abort this filter, because it is usually not
			// necessary to lookup for any UserVo then. Or, if the url-pattern 
			// in the web.xml is specific enough, this if-block can be removed.
			pageSessioned = false;
		}
		if (pathInfo.matches("^/[a-zA-Z]\\w*\\.(faces|jsp).*")) {
			// file name matches "/*.faces" or "/*.jsp"
			if (!pathInfo.startsWith(loginPage) && !pathInfo.startsWith(timeoutPage)) {
				// not login page and timeout page
				pageSessioned = true;
			}
		}
		if (isDebugEnabled) {
			logger.debug("isPageSessioned() - pathInfo: " + pathInfo + ", Sessioned: "
					+ pageSessioned);
		}
		return pageSessioned;
	}
	
	private static UserDao getUserDao(HttpSession httpSession) {
		ServletContext ctx = httpSession.getServletContext();
		return (UserDao) SpringUtil.getWebAppContext(ctx).getBean("userDao");
	}
}
