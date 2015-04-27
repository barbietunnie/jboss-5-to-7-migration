<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="jpa.model.EmailAddress"%>
<%@page import="jpa.model.BroadcastMessage"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="javax.persistence.NoResultException"%>
<%
	Logger logger = Logger.getLogger("com.legacytojava.jsp");
	ServletContext ctx = application;
	String sbsrId = request.getParameter("sbsrid");
	String listId = request.getParameter("listid");
	int rowsUpdated = 0;
	if (StringUtils.isNotBlank(sbsrId) && StringUtils.isNotBlank(listId)) {
		// update subscriber click count
		try {
			EmailAddress addrVo = getEmailAddressService(ctx).getByRowId(Integer.parseInt(sbsrId));
			if (addrVo != null) {
				getSubscriptionService(ctx).updateOpenCount(addrVo.getRowId(), listId);
				rowsUpdated += 1;
			}
		}
		catch (NumberFormatException e) {
			logger.error("NumberFormatException caught: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
	else {
		logger.info("msgopen.jsp - sbsrid or listid is not valued.");
	}

	String msgId = request.getParameter("msgid");
	if (StringUtils.isNotBlank(msgId)) {
		// update newsletter click count
		try {
			BroadcastMessage bm = getBroadcastMessageService(ctx).getByRowId(Integer.parseInt(msgId));
			rowsUpdated += getBroadcastMessageService(ctx).updateOpenCount(bm.getRowId());
		}
		catch (NoResultException e) {
			logger.error("msgunsub.jsp - Failed to find broadcase message by id: " + msgId);
		}
		catch (NumberFormatException e) {
			logger.error("NumberFormatException caught: " + e.getMessage());
		}
	}
	else {
		logger.info("msgopen.jsp - msgid is not valued.");
	}
	
	logger.info("msgopen.jsp - rows updated: " + rowsUpdated);

	boolean isInfoEnabled = false; //logger.isInfoEnabled();
	if (isInfoEnabled) {
		logger.info("ServletContext RealPath: " + application.getRealPath("./"));
		logger.info("Request Servlet path: " + request.getServletPath());
		logger.info("Request Context path: " + request.getContextPath());
	}
	%>

<%-- Now serve the space.gif file --%>
<%@ include file="./serveImage.jsp" %>