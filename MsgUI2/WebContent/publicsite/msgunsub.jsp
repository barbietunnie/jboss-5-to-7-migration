<%@page import="javax.persistence.NoResultException"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="jpa.model.EmailAddress"%>
<%@page import="jpa.model.BroadcastMessage"%>
<%@page import="jpa.util.StringUtil"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%
	Logger logger = Logger.getLogger("jpa.msgui.publicsite.jsp");
	ServletContext ctx = application;
	String sbsrId = request.getParameter("sbsrid"); // email address id
	String listId = request.getParameter("listid");
	int rowsUpdated = 0;
	if (StringUtils.isNotBlank(sbsrId) && StringUtils.isNotBlank(listId)) {
		// update subscriber click count
		try {
			EmailAddress addrVo = getEmailAddressService(ctx).getByRowId(Integer.parseInt(sbsrId));
			rowsUpdated += getSubscriptionService(ctx).updateClickCount(addrVo.getRowId(), listId);
		}
		catch (NoResultException e) {
			logger.error("msgopen.jsp - Failed to find email address by id: " + sbsrId);
		}
	}
	else {
		logger.info("msgunsub.jsp - sbsrid or listid is not valued.");
	}

	String msgId = request.getParameter("msgid"); // broadcast message id
	if (StringUtils.isNotBlank(msgId)) {
		// update newsletter click count
		try {
			BroadcastMessage bm = getBroadcastMessageService(ctx).getByRowId(Integer.parseInt(msgId));
			rowsUpdated += getBroadcastMessageService(ctx).updateUnsubscribeCount(bm.getRowId());
		}
		catch (NoResultException e) {
			logger.error("msgunsub.jsp - Failed to find broadcase message by id: " + msgId);
		}
		catch (NumberFormatException e) {
			logger.error("NumberFormatException caught: " + e.getMessage());
		}
	}
	else {
		logger.info("msgunsub.jsp - msgid is not valued.");
	}
	
	logger.info("msgunsub.jsp - rows updated: " + rowsUpdated);
	
	out.println("<H2>Records Updated: " + rowsUpdated + "</H2>");
	response.setStatus(200);
	response.setContentType("text/html");
	response.flushBuffer();
	%>

<%-- Now serve the space.gif file
<%@ include file="./serveImage.jsp" %>
 --%>