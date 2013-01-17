<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ include file="./loadSbsrDaos.jsp" %>

<%@page import="com.legacytojava.message.vo.emailaddr.EmailAddrVo"%>
<%@page import="com.legacytojava.message.vo.inbox.MsgClickCountsVo"%>
<%@page import="com.legacytojava.message.util.StringUtil"%>
<%
	Logger logger = Logger.getLogger("com.legacytojava.jsp");
	ServletContext ctx = application;
	String sbsrId = request.getParameter("sbsrid");
	String listId = request.getParameter("listid");
	int rowsUpdated = 0;
	if (!StringUtil.isEmpty(sbsrId) && !StringUtil.isEmpty(listId)) {
		// update subscriber click count
		try {
			EmailAddrVo addrVo = getEmailAddrDao(ctx).getByAddrId(Long.parseLong(sbsrId));
			if (addrVo != null) {
				rowsUpdated += getSubscriptionDao(ctx).updateClickCount(
						addrVo.getEmailAddrId(), listId);
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
		logger.info("msgclick.jsp - sbsrid or listid is not valued.");
	}

	String msgId = request.getParameter("msgid");
	if (!StringUtil.isEmpty(msgId)) {
		// update newsletter click count
		try {
			MsgClickCountsVo countVo = getMsgClickCountsDao(ctx).getByPrimaryKey(Long.parseLong(msgId));
			if (countVo != null) {
				rowsUpdated += getMsgClickCountsDao(ctx).updateClickCount(
						countVo.getMsgId(), 1);
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
		logger.info("msgclick.jsp - msgid is not valued.");
	}
	
	logger.info("msgclick.jsp - rows updated: " + rowsUpdated);
	%>

<%-- Now serve the space.gif file --%>
<%@ include file="./serveImage.jsp" %>