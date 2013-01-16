<%@page import="com.legacytojava.message.dao.emailaddr.MailingListDao"%>
<%@page import="com.legacytojava.message.dao.emailaddr.EmailAddrDao"%>
<%@page import="com.legacytojava.message.dao.emailaddr.SubscriptionDao"%>
<%@page import="com.legacytojava.message.bo.mailinglist.MailingListBo"%>
<%@page import="com.legacytojava.msgui.util.SpringUtil"%>
<%@page import="com.legacytojava.message.vo.emailaddr.MailingListVo"%>
<%@page import="com.legacytojava.message.vo.emailaddr.EmailVariableVo"%>
<%@page import="com.legacytojava.message.dao.emailaddr.EmailVariableDao"%>
<%@page import="com.legacytojava.message.dao.inbox.MsgClickCountsDao"%>
<%@page import="com.legacytojava.message.bo.customer.CustomerBo"%>
<%@page import="com.legacytojava.message.constant.Constants"%>
<%@page import="com.legacytojava.message.constant.VariableType"%>
<%@page import="com.legacytojava.message.bo.template.*"%>
<%@page import="org.apache.log4j.Logger" %>
<%@page import="java.util.*" %>
<%!
	MailingListDao mailingListDao = null;
	MailingListDao getMailingListDao(ServletContext ctx) {
		if (mailingListDao == null) {
			mailingListDao = (MailingListDao) SpringUtil.getWebAppContext(ctx).getBean("mailingListDao");
		}
		return mailingListDao;
	}

	MailingListBo mailingListBo = null;
	MailingListBo getMailingListBo(ServletContext ctx) {
		if (mailingListBo == null) {
			mailingListBo = (MailingListBo) SpringUtil.getWebAppContext(ctx).getBean("mailingListBo");
		}
		return mailingListBo;
	}

	EmailAddrDao emailAddrDao = null;
	EmailAddrDao getEmailAddrDao(ServletContext ctx) {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddrDao) SpringUtil.getWebAppContext(ctx).getBean("emailAddrDao");
		}
		return emailAddrDao;
	}

	SubscriptionDao subscriptionDao = null;
	SubscriptionDao getSubscriptionDao(ServletContext ctx) {
		if (subscriptionDao == null) {
			subscriptionDao = (SubscriptionDao) SpringUtil.getWebAppContext(ctx).getBean("subscriptionDao");
		}
		return subscriptionDao;
	}

	EmailVariableDao emailVariableDao = null;
	EmailVariableDao getEmailVariableDao(ServletContext ctx) {
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableDao) SpringUtil.getWebAppContext(ctx).getBean("emailVariableDao");
		}
		return emailVariableDao;
	}

	MsgClickCountsDao msgClickCountsDao = null;
	MsgClickCountsDao getMsgClickCountsDao(ServletContext ctx) {
		if (msgClickCountsDao == null) {
			msgClickCountsDao = (MsgClickCountsDao) SpringUtil.getWebAppContext(ctx).getBean("msgClickCountsDao");
		}
		return msgClickCountsDao;
	}

	private CustomerBo customerBo = null;
	CustomerBo getCustomerBo(ServletContext ctx) {
		if (customerBo == null) {
			customerBo = (CustomerBo) SpringUtil.getWebAppContext(ctx).getBean("customerBo");
		}
		return customerBo;
	}
	
	List<MailingListVo> getSbsrMailingLists(ServletContext ctx, long emailAddrId) {
		List<MailingListVo> subedList = getMailingListDao(ctx).getSubscribedLists(emailAddrId);
		HashMap<String, MailingListVo> map = new HashMap<String, MailingListVo>();
		for (Iterator<MailingListVo> it = subedList.iterator(); it.hasNext();) {
			MailingListVo vo = (MailingListVo) it.next();
			map.put(vo.getListId(), vo);
		}
		List<MailingListVo> allList = getMailingListDao(ctx).getAll(true);
		for (int i = 0; i < allList.size(); i++) {
			MailingListVo vo = (MailingListVo) allList.get(i);
			if (map.containsKey(vo.getListId())) {
				allList.set(i, map.get(vo.getListId()));
			}
		}
		return allList;
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName) {
		return renderURLVariable(ctx, emailVariableName, null);
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName, Long sbsrId) {
		return renderURLVariable(ctx, emailVariableName, sbsrId, null, null);
	}
	
	String renderURLVariable(ServletContext ctx, String emailVariableName, Long sbsrId, String listId, Long msgId) {
		Logger logger = Logger.getLogger("com.legacytojava.jsp");
		String renderedValue = "";
		EmailVariableVo vo = getEmailVariableDao(ctx).getByName(emailVariableName);
		HashMap<String, RenderVariable> vars = new HashMap<String, RenderVariable>();
		if (sbsrId != null) {
			RenderVariable var = new RenderVariable(
					"SubscriberAddressId",
					sbsrId.toString(),
					null,
					VariableType.TEXT,
					Constants.YES_CODE,
					Constants.NO_CODE,
					null);
			vars.put(var.getVariableName(), var);
		}
		if (listId != null && listId.trim().length() > 0) {
			RenderVariable var = new RenderVariable(
					"MailingListId",
					listId,
					null,
					VariableType.TEXT,
					Constants.YES_CODE,
					Constants.NO_CODE,
					null);
			vars.put(var.getVariableName(), var);
		}
		if (msgId != null) {
			RenderVariable var = new RenderVariable(
					"BroadcastMsgId",
					msgId.toString(),
					null,
					VariableType.TEXT,
					Constants.YES_CODE,
					Constants.NO_CODE,
					null);
			vars.put(var.getVariableName(), var);
		}
		if (vo != null) {
			try {
				renderedValue = RenderUtil.renderTemplateText(vo.getDefaultValue(), null, vars);
			}
			catch (Exception e) {
				logger.error("loadSbsrDaos.jsp - renderURLVariable: ", e);
			}
		}
		return renderedValue;
	}
	
	String blankToNull(String str) {
		if (str == null || str.trim().length() ==0)
			return null;
		else
			return str;
	}

	String nullToBlank(String str) {
		if (str == null)
			return "";
		else
			return str;
	}
	%>