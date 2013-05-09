package jpa.msgui.bean;

import static com.legacytojava.message.constant.Constants.DASHES_OF_33;
import static com.legacytojava.message.constant.Constants.MSG_DELIMITER_BEGIN;
import static com.legacytojava.message.constant.Constants.MSG_DELIMITER_END;
import static com.legacytojava.message.constant.Constants.NO_CODE;
import static com.legacytojava.message.constant.Constants.YES_CODE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;
import javax.mail.Address;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.model.SessionUpload;
import jpa.model.message.MessageAttachment;
import jpa.model.message.MessageInbox;
import jpa.model.message.MessageRfcField;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.msgui.vo.SearchFieldsVo.PageAction;
import jpa.service.SessionUploadService;
import jpa.service.message.MessageInboxService;
import jpa.service.msgin.MessageInboxBo;

import org.apache.log4j.Logger;

public class MsgInboxBean {
	static final Logger logger = Logger.getLogger(MsgInboxBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	final static boolean DisplaySearchVo = false;
	
	private MessageInboxService msgInboxDao = null;
	private MessageInboxBo msgInboxBo = null;
	private SessionUploadService sessionUploadDao = null;
	private DataModel folder = null;
	private MessageInbox message = null;
	private boolean editMode = true;
	private boolean isHtml = false;
	private boolean checkAll = false;

	private HtmlDataTable dataTable;
	private MessageInbox replyMessageVo = null;
	private List<MsgInboxWebVo> messageThreads = null;
	private List<SessionUpload> uploads = null;
	private UIInput fromAddrInput = null;
	private UIInput toAddrInput = null;

	private MessageRfcField rfcFields = null;
	
	private final SearchFieldsVo searchVo = new SearchFieldsVo();
	private boolean pagingButtonPushed = false;
	
	private static String TO_EDIT = "message.edit";
	private static String TO_FAILED = "message.failed";
	private static String TO_DELETED = "message.deleted";
	private static String TO_CANCELED = "message.canceled";
	private static String TO_SENT = "message.sent";
	private static String TO_FORWARD = "message.forward";
	private static String TO_REPLY = "message.reply";
	private static String TO_CLOSED = "message.closed";
	private static String TO_PAGING = "message.paging";
	private static String TO_SELF = "message.toself";
	
	public MessageInboxService getMessageInboxService() {
		if (msgInboxDao == null) {
			msgInboxDao = (MessageInboxService) SpringUtil.getWebAppContext().getBean("messageInboxService");
		}
		return msgInboxDao;
	}

	public MessageInboxBo getMessageInboxBo() {
		if (msgInboxBo == null) {
			msgInboxBo = (MessageInboxBo) SpringUtil.getWebAppContext().getBean("messageInboxBo");
		}
		return msgInboxBo;
	}

	public SessionUploadService getSessionUploadService() {
		if (sessionUploadDao == null) {
			sessionUploadDao = (SessionUploadService) SpringUtil.getWebAppContext().getBean(
					"sessionUploadService");
		}
		return sessionUploadDao;
	}

	public String pageFirst() {
		dataTable.setFirst(0);
		searchVo.setPageAction(PageAction.FIRST);
		return TO_PAGING;
	}

	public String pagePrevious() {
		dataTable.setFirst(dataTable.getFirst() - dataTable.getRows());
		searchVo.setPageAction(PageAction.PREVIOUS);
		return TO_PAGING;
	}

	public String pageNext() {
		dataTable.setFirst(dataTable.getFirst() + dataTable.getRows());
		searchVo.setPageAction(PageAction.NEXT);
		return TO_PAGING;
	}

	public String pageLast() {
		int count = dataTable.getRowCount();
		int rows = dataTable.getRows();
		dataTable.setFirst(count - ((count % rows != 0) ? count % rows : rows));
		searchVo.setPageAction(PageAction.LAST);
		return TO_PAGING;
	}

	public int getLastPageRow() {
		int lastRow = dataTable.getFirst() + dataTable.getRows();
		if (lastRow > dataTable.getRowCount())
			return dataTable.getRowCount();
		else
			return lastRow;
	}
	
	/**
	 * actionListener
	 * @param e
	 */
	public void pagingActionFired(ActionEvent e) {
		logger.info("pagingActionFired() - " + e.getComponent().getId());
		pagingButtonPushed = true;
	}
	
	public SearchFieldsVo getSearchFieldVo() {
		return searchVo;
	}
	
	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			resetSearchVo();
		}
		SimpleMailTrackingMenu menu = (SimpleMailTrackingMenu) FacesUtil
				.getSessionMapValue("mailtracking");
		if (menu != null) {
			SearchFieldsVo menuSearchVo = menu.getSearchFieldVo();
			//logger.info("Menu SearchFieldVo: " + menuSearchVo);
			//logger.info("Inbox SearchFieldVo: " + searchVo);
			if (!menuSearchVo.equalsLevel1(searchVo)) {
				if (menuSearchVo.getLogList().size() > 0) {
					logger.info("getAll() - " + menuSearchVo.listChanges());
				}
				menuSearchVo.copyLevel1To(searchVo);
				resetSearchVo();
			}
		}
		// retrieve total number of rows
		if (searchVo.getRowCount() < 0) {
			int rowCount = getMessageInboxService().getRowCountForWeb(searchVo);
			searchVo.setRowCount(rowCount);
		}
		/* This block DOES NOT resolve browser "refresh" issue as the "refresh" button
		   still triggers the ActionListner which executes pagingActionFired() method.
		   The SOLUTION found so far is to use <redirect/> in JSF navigation. */
		if (pagingButtonPushed) {
			pagingButtonPushed = false;
		}
		else {
			searchVo.setPageAction(PageAction.CURRENT);
		}
		/* end of browser "refresh" */
		if (folder == null || !searchVo.getPageAction().equals(PageAction.CURRENT)) {
			//logger.info("SearchVo Before: " + searchVo);
			// retrieve rows based on page action
			List<MsgInboxWebVo> msgInboxList = getMessageInboxService().getListForWeb(searchVo);
			/* set search keys for paging */
			if (!msgInboxList.isEmpty()) {
				MsgInboxWebVo firstRow = (MsgInboxWebVo) msgInboxList.get(0);
				searchVo.setReceivedTimeFirst(firstRow.getReceivedTime());
				searchVo.setMsgIdFirst(firstRow.getMsgId());
				MsgInboxWebVo lastRow = (MsgInboxWebVo) msgInboxList.get(msgInboxList.size() - 1);
				searchVo.setReceivedTimeLast(lastRow.getReceivedTime());
				searchVo.setMsgIdLast(lastRow.getMsgId());
			}
			else {
				searchVo.setReceivedTimeFirst(null);
				searchVo.setReceivedTimeLast(null);
				searchVo.setMsgIdFirst(-1);
				searchVo.setMsgIdLast(-1);
			}
			if (DisplaySearchVo) {
				logger.info("SearchVo After: " + searchVo);
			}
			// reset page action
			searchVo.setPageAction(PageAction.CURRENT);
			// wrap the list into PagedListDataModel
			folder = new PagedListDataModel(msgInboxList, searchVo.getRowCount(), searchVo.getPageSize());
		}
		return folder;
	}

	private void reset() {
		fromAddrInput = null;
		toAddrInput = null;
	}
	
	private void refresh() {
		folder = null;
		replyMessageVo = null;
		messageThreads = null;
		isHtml = false;
		checkAll = false;
		if (dataTable.getFirst() <= 0) {
			// to display messages newly arrived
			pageFirst();
		}
	}
	
	private void resetSearchVo() {
		searchVo.resetPageContext();
		if (dataTable != null) dataTable.setFirst(0);
		refresh();
	}
	
	public String refreshClicked() {
		refresh();
		searchVo.resetPageContext();
		// go back to the first page in order to display newly arrived messages
		pageFirst();
		return TO_SELF;
	}
	
	public String viewAll() {
		searchVo.resetFlags();
		refresh();
		searchVo.resetPageContext();
		pageFirst();
		return TO_SELF;
	}
	
	public String viewUnread() {
		searchVo.resetFlags();
		searchVo.setRead(Boolean.valueOf(false));
		refresh();
		searchVo.resetPageContext();
		pageFirst();
		return TO_SELF;
	}
	
	public String viewRead() {
		searchVo.resetFlags();
		searchVo.setRead(Boolean.valueOf(true));
		refresh();
		searchVo.resetPageContext();
		pageFirst();
		return TO_SELF;
	}
	
	public String viewFlagged() {
		searchVo.resetFlags();
		searchVo.setFlagged(Boolean.valueOf(true));
		refresh();
		searchVo.resetPageContext();
		pageFirst();
		return TO_SELF;
	}
	
	private void clearUploads() {
		String sessionId = FacesUtil.getSessionId();
		if (uploads != null) {
			uploads.clear();
		}
		int rowsDeleted = getSessionUploadService().deleteBySessionId(sessionId);
		logger.info("clearUploads() - SessionId: " + sessionId + ", rows deleted: " + rowsDeleted);
		uploads = null;
	}
	
	public String viewMessage() {
		if (isDebugEnabled)
			logger.debug("viewMessage() - Entering...");
		if (folder == null) {
			logger.warn("viewMessage() - Inbox fodler is null.");
			return TO_FAILED;
		}
		if (!folder.isRowAvailable()) {
			logger.warn("viewMessage() - Inbox folder Row not available.");
			return TO_FAILED;
		}
		clearUploads(); // clear session upload records
		MsgInboxWebVo webVo = (MsgInboxWebVo) folder.getRowData();
		// retrieve other message properties including attachments
		message = getMessageInboxBo().getMessageByPK(webVo.getMsgId());
		
		return viewMessage(message);
	}
	
	private String viewMessage(MessageInbox message) {
		String contentType = message.getBodyContentType();
		if (contentType != null && contentType.toLowerCase().startsWith("text/html")) {
			// set default value for HTML check box
			setHtml(true);
		}
		if (message.getMessageAttachmentList() != null) {
			// empty attachment bodies to reduce HTTP session size
			for (int i = 0; i < message.getMessageAttachmentList().size(); i++) {
				MessageAttachment vo = message.getMessageAttachmentList().get(i);
				if (vo.getAttachmentValue() != null) {
					vo.setAttachmentSize(vo.getAttachmentValue().length);
					vo.setAttachmentValue(null);
				}
			}
		}
		if (isInfoEnabled) {
			logger.info("viewMessage() - Message to be viewed: " + message.getMsgSubject() + ","
					+ message.getRowId());
		}
		
		if (!message.getMessageRfcFieldList().isEmpty()) {
			rfcFields = message.getMessageRfcFieldList().get(0);
		}
		else {
			rfcFields = null;
		}
		
		message.setMarkedForEdition(true);
		editMode = true;
		message.setReadCount(message.getReadCount() + 1);
		// update ReadCount
		int rowsUpdated = getMessageInboxService().updateCounts(message);
		if (rowsUpdated > 0) {
			logger.info("viewMessage() - Message updated: " + message.getRowId());
		}
		// fetch message threads
		List<MessageInbox> threads = getMessageInboxService().getByLeadMsgId(message.getLeadMessageRowId());
		if (threads != null && threads.size() > 1) {
			messageThreads = MessageThreadsBuilder.buildThreads(threads);
		}
		else {
			messageThreads = null;
		}
		if (isDebugEnabled) {
			//logger.debug("viewMessage() - MessageInbox to be passed to jsp: " + message);
			logger.debug("viewMessage() - MessageInbox to be passed to jsp: " + LF + "Msg RowId: "
					+ message.getRowId() + LF + "Number of Attachments: "
					+ message.getAttachmentCount() + LF + "Subject: " + message.getMsgSubject()
					+ LF + "Message Body: " + LF + message.getMsgBody());
		}
		return TO_EDIT;
	}
	
	public String viewThread() {
		String msgId = FacesUtil.getRequestParameter("msgThreadId");
		logger.info("viewThread() - msgId: " + msgId);
		if (msgId == null) return null;
		message = getMessageInboxService().getByPrimaryKey(Integer.parseInt(msgId));
		
		return viewMessage(message);
	}
	
	public String deleteMessages() {
		if (isDebugEnabled)
			logger.debug("deleteMessages() - Entering...");
		if (folder == null) {
			logger.warn("deleteMessages() - MsgInbox is null.");
			return TO_FAILED;
		}
		List<MsgInboxWebVo> list = getMessageList();
		for (int i=0; i<list.size(); i++) {
			MsgInboxWebVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMessageInboxService().deleteByRowId(vo.getMsgId());
				if (rowsDeleted > 0) {
					logger.info("deleteMessages() - Mailbox message deleted: " + vo.getMsgId());
					searchVo.setRowCount(searchVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_SELF;
	}
	
	public String deleteMessage() {
		if (message == null) {
			logger.error("deleteMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		int rowsDeleted = getMessageInboxService().deleteByRowId(message.getRowId());
		if (rowsDeleted > 0) {
			logger.info("deleteMessage() - Mailbox message deleted: " + message.getRowId());
			searchVo.setRowCount(searchVo.getRowCount() - rowsDeleted);
		}
		getMessageList().remove(message);
		refresh();
		return TO_DELETED;
	}
	
	public String attachFiles() {
		String pageUrl = "/upload/msgInboxAttachFiles.jsp?frompage=msgreply";
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext ectx = context.getExternalContext();
		try {
			ectx.redirect(ectx.encodeResourceURL(ectx.getRequestContextPath() + pageUrl));
		}
		catch (IOException e) {
			logger.error("attachFiles() - IOException caught", e);
			throw new FacesException("Cannot redirect to " + pageUrl + " due to IO exception.", e);
		}
		return null;
	}
	
	public String markAsRead() {
		if (isDebugEnabled)
			logger.debug("markAsRead() - Entering...");
		if (folder == null) {
			logger.warn("markAsRead() - Inbox folder is null.");
			return TO_FAILED;
		}
		List<MsgInboxWebVo> list = getMessageList();
		// update Read Count
		for (Iterator<MsgInboxWebVo> it=list.iterator(); it.hasNext();) {
			MsgInboxWebVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				vo.setMarkedForDeletion(false);
				if (vo.getReadCount() <= 0) {
					vo.setReadCount(1);
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
					int rowsUpdated = getMessageInboxService().updateCounts(vo);
					if (rowsUpdated > 0) {
						logger.info("markAsRead() - Message updated: " + vo.getMsgId());
					}
				}
			}
		}
		return TO_SELF;
	}
	
	public String markAsUnread() {
		if (isDebugEnabled)
			logger.debug("markAsUnread() - Entering...");
		if (folder == null) {
			logger.warn("markAsUnread() - MsgInbox is null.");
			return TO_FAILED;
		}
		List<MsgInboxWebVo> list = getMessageList();
		// update Read Count
		for (Iterator<MsgInboxWebVo> it=list.iterator(); it.hasNext();) {
			MsgInboxWebVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				vo.setMarkedForDeletion(false);
				if (vo.getReadCount() > 0) {
					vo.setReadCount(0);
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
					int rowsUpdated = getMessageInboxService().updateCounts(vo);
					if (rowsUpdated > 0) {
						logger.info("markAsUnread() - Message updated: " + vo.getMsgId());
					}
				}
			}
		}
		return TO_SELF;
	}
	
	public String markAsFlagged() {
		if (isDebugEnabled)
			logger.debug("markAsFlagged() - Entering...");
		if (folder == null) {
			logger.warn("markAsFlagged() - MsgInbox is null.");
			return TO_FAILED;
		}
		List<MsgInboxWebVo> list = getMessageList();
		// update Flagged
		for (Iterator<MsgInboxWebVo> it=list.iterator(); it.hasNext();) {
			MsgInboxWebVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				vo.setMarkedForDeletion(false);
				if (!YES_CODE.equalsIgnoreCase(vo.getFlagged())) {
					vo.setFlagged(YES_CODE);
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
					int rowsUpdated = getMessageInboxService().updateCounts(vo);
					if (rowsUpdated > 0) {
						logger.info("markAsFlagged() - Message updated: " + vo.getMsgId());
					}
				}
			}
		}
		return TO_SELF;
	}
	
	public String markAsUnflagged() {
		if (isDebugEnabled)
			logger.debug("markAsUnflagged() - Entering...");
		if (folder == null) {
			logger.warn("markAsUnflagged() - MsgInbox is null.");
			return TO_FAILED;
		}
		List<MsgInboxWebVo> list = getMessageList();
		// update Flagged
		for (Iterator<MsgInboxWebVo> it=list.iterator(); it.hasNext();) {
			MsgInboxWebVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				vo.setMarkedForDeletion(false);
				if (!NO_CODE.equalsIgnoreCase(vo.getFlagged())) {
					vo.setFlagged(NO_CODE);
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
					int rowsUpdated = getMessageInboxService().updateCounts(vo);
					if (rowsUpdated > 0) {
						logger.info("markAsUnflagged() - Message updated: " + vo.getMsgId());
					}
				}
			}
		}
		return TO_SELF;
	}
	
	public String replyMessage() {
		if (message == null) {
			logger.error("replyMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		try {
			replyMessageVo = (MessageInbox) message.getClone();
		}
		catch (CloneNotSupportedException e) {
			logger.error("CloneNotSupportedException caught", e);
			return TO_FAILED;
		}
		replyMessageVo.setIsReply(true);
		replyMessageVo.setComposeFromAddress(message.getToAddress());
		replyMessageVo.setComposeToAddress(message.getFromAddress());
		replyMessageVo.setMsgSubject("Re:"+message.getMsgSubject());
		replyMessageVo.setMsgBody(getReplyEnvelope() + message.getMsgBody());
		reset(); // avoid carrying over the current bound value
		
		// retrieve uploaded files
		retrieveUploadFiles();
		return TO_REPLY;
	}
	
	public String closeMessage() {
		if (message == null) {
			logger.error("closeMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		message.setStatusId(MsgStatusCode.CLOSED);
		message.setUpdtUserId(FacesUtil.getLoginUserId());
		int rowsUpdated = getMessageInboxService().updateStatusId(message);
		if (rowsUpdated > 0) {
			logger.info("closeMessage() - Mailbox message closed: " + message.getMsgId());
			searchVo.setRowCount(searchVo.getRowCount() - rowsUpdated);
		}
		refresh();
		return TO_CLOSED;
	}
	
	public String closeThread() {
		if (message == null) {
			logger.error("closeThread() - MessageInbox is null");
			return TO_FAILED;
		}
		message.setStatusId(MsgStatusCode.CLOSED);
		message.setUpdtUserId(FacesUtil.getLoginUserId());
		int rowsUpdated = getMessageInboxService().updateStatusIdByLeadMsgId(message);
		if (rowsUpdated > 0) {
			logger.info("closeThread() - messages closed (LeadMsgId): " + message.getLeadMsgId());
			searchVo.setRowCount(searchVo.getRowCount() - rowsUpdated);
		}
		refresh();
		return TO_CLOSED;
	}
	
	public String openMessage() {
		if (message == null) {
			logger.error("closeMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		message.setStatusId(MsgStatusCode.OPENED);
		message.setUpdtUserId(FacesUtil.getLoginUserId());
		int rowsUpdated = getMessageInboxService().updateStatusId(message);
		if (rowsUpdated > 0) {
			logger.info("openMessage() - Mailbox message opened: " + message.getMsgId());
			searchVo.setRowCount(searchVo.getRowCount() + rowsUpdated);
		}
		refresh();
		return TO_CLOSED;
	}
	
	public String reassignRule() {
		if (message == null) {
			logger.error("reassignRule() - MessageInbox is null");
			return TO_FAILED;
		}
		// retrieve the original message
		MessageInbox msgData = getMessageInboxBo().getMessageByPK(message.getMsgId());
		if (msgData == null) {
			logger.error("reassignRule() - Original message has been deleted, msgId: "
					+ message.getMsgId());
			return TO_FAILED;
		}
		if (message.getRuleName().equals(msgData.getRuleName())) {
			return null;
		}
		// 1) send the message to rule-engine queue with new rule name
		try {
			MessageBean msgBean  = MessageBeanBuilder.createMessageBean(msgData);
			TaskBaseBo assignRuleBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean(
					"assignRuleNameBo");
			assignRuleBo.setTaskArguments(message.getRuleName());
			msgBean.setSendDate(new java.util.Date());
			String jmsMsgId = (String) assignRuleBo.process(msgBean);
			logger.info("reassignRule() - assign rule to: " + message.getRuleName()
					+ ", jmsMsgId: " + jmsMsgId);
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			return TO_FAILED;
		}
		// 2) close the current message
		return closeMessage();
	}
	
	public List<SessionUpload> retrieveUploadFiles() {
		String sessionId = FacesUtil.getSessionId();
		boolean valid = FacesUtil.isSessionIdValid();
		logger.info("retrieveUploadFiles() - SessionId: " + sessionId + ", Valid? " + valid);
		uploads = getSessionUploadService().getBySessionId4Web(sessionId);
		if (isDebugEnabled && uploads != null)
			logger.debug("retrieveUploadFiles() - files retrieved: " + uploads.size());
		return uploads;
	}
	
	private String getReplyEnvelope() {
		StringBuffer sb = new StringBuffer();
		sb.append(LF + LF);
		sb.append(MSG_DELIMITER_BEGIN + message.getFromAddress()
				+ MSG_DELIMITER_END);
		sb.append(LF + LF);
		sb.append(DASHES_OF_33 + LF);
		return sb.toString();
	}
	
	public String forwardMessage() {
		if (message == null) {
			logger.error("forwardMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		try {
			replyMessageVo = (MessageInbox) message.getClone();
		}
		catch (CloneNotSupportedException e) {
			logger.error("CloneNotSupportedException caught", e);
			return TO_FAILED;
		}
		replyMessageVo.setIsForward(true);
		replyMessageVo.setComposeFromAddress(message.getToAddress());
		replyMessageVo.setComposeToAddress("");
		replyMessageVo.setMsgSubject("Fwd:" + message.getMsgSubject());
		replyMessageVo.setMsgBody(getForwardEnvelope() + message.getMsgBody());
		reset(); // avoid carrying over the current bound value
		return TO_FORWARD;
	}
	
	private String getForwardEnvelope() {
		StringBuffer sb = new StringBuffer();
		sb.append(LF + LF);
		sb.append(MSG_DELIMITER_BEGIN + message.getFromAddress()
				+ MSG_DELIMITER_END + LF);
		sb.append(LF);
		sb.append("> From: " + message.getFromAddress() + LF);
		sb.append("> To: " + message.getToAddress() + LF);
		sb.append("> Date: " + message.getReceivedTime() + LF);
		sb.append("> Subject: " + message.getMsgSubject() + LF);
		sb.append(">" + LF + LF);
		sb.append(DASHES_OF_33 + LF);
		return sb.toString();
	}
	
	public String removeUploadFile() {
		String seq = FacesUtil.getRequestParameter("seq");
		String name = FacesUtil.getRequestParameter("name");
		String id = FacesUtil.getSessionId();
		logger.info("removeUploadFile() - id/seq/name: " + id + "/" + seq + "/" + name);
		try {
			int sessionSeq = Integer.parseInt(seq);
			for (int i = 0; uploads != null && i < uploads.size(); i++) {
				SessionUpload vo = uploads.get(i);
				if (sessionSeq == vo.getSessionSeq()) {
					uploads.remove(i);
					break;
				}
			}
			int rowsDeleted = getSessionUploadService().deleteByPrimaryKey(id, sessionSeq);
			logger.info("removeUploadFile() - rows deleted: " + rowsDeleted + ", file name: "
					+ name);
		}
		catch (RuntimeException e) {
			logger.error("RuntimeException caught", e);
		}
		return TO_SELF;
	}
	
	public String sendMessage() {
		if (message == null) {
			logger.error("sendMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		if (replyMessageVo == null) {
			logger.error("sendMessage() - replyMessageVo is null");
			return TO_FAILED;
		}
		// make sure we have all the data to rebuild a message bean
		// retrieve original message
		MessageInbox msgData = getMessageInboxBo().getMessageByPK(message.getMsgId());
		if (msgData == null) {
			logger.error("sendMessage() - Original message has been deleted, msgId: "
					+ message.getMsgId());
			return TO_FAILED;
		}
		Long msgsSent = null;
		try {
			// retrieve original message
			MessageBean messageBean  = MessageBeanBuilder.createMessageBean(msgData);
			// retrieve new addresses
			Address[] from = InternetAddress.parse(replyMessageVo.getComposeFromAddress());
			Address[] to = InternetAddress.parse(replyMessageVo.getComposeToAddress());
			// retrieve new message body
			String wholeMsgText = replyMessageVo.getMsgBody();
			wholeMsgText = wholeMsgText == null ? "" : wholeMsgText; // just for safety
			String origContentType = messageBean.getBodyContentType();
			if (origContentType == null) { // should never happen
				origContentType = "text/plain";
			}
			String replyMsg = null;
			// remove original message from message body
			int pos1 = wholeMsgText.indexOf(MSG_DELIMITER_BEGIN);
			int pos2 = wholeMsgText.indexOf(MSG_DELIMITER_END, pos1 + 1);
			if (pos1 >= 0 && pos2 > pos1) {
				replyMsg = wholeMsgText.substring(0, pos1);
				int pos3 = wholeMsgText.indexOf(DASHES_OF_33, pos2 + 1);
				if (pos3 > pos2) {
					String origMsg = wholeMsgText.substring(pos3 + DASHES_OF_33.length());
					logger.info("Orig Msg: " + origMsg);
				}
				else {
					String origMsg = wholeMsgText.substring(pos2 + MSG_DELIMITER_END.length());
					logger.info("Orig Msg: " + origMsg);
				}
			}
			else {
				replyMsg = wholeMsgText;
			}
			// construct messageBean for new message
			if (replyMessageVo.getIsForward()) { // forward
				// leave body content type unchanged
				byte[] bytes = messageBean.getBodyNode().getValue();
				if (replyMsg.trim().length() > 0) {
					// append original message's headers to new message body
					replyMsg += MessageBodyBuilder.constructOriginalHeader(messageBean, 
							origContentType.indexOf("html") >= 0);
				}
				// append original message
				messageBean.getBodyNode().setValue(replyMsg + new String(bytes));
				// set addresses and subject
				messageBean.setFrom(from);
				messageBean.setTo(to);
				// use new subject
				messageBean.setSubject(replyMessageVo.getMsgSubject());
				if (StringUtil.isEmpty(messageBean.getSenderId())) {
					messageBean.setSenderId(FacesUtil.getLoginUserSenderId());
				}
				// process the message
				TaskBaseBo forwardBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean(
						"forwardBo");
				forwardBo.setTaskArguments("$" + EmailAddressType.TO_ADDR);
				msgsSent = (Long) forwardBo.process(messageBean);
				logger.info("sendMessage() - Message to send:\n" + messageBean);
			}
			else { // reply
				MessageBean mBean = new MessageBean();
				mBean.setOriginalMail(messageBean);
				//mBean.setBody(msgBody); // new message body
				String contentType = origContentType;
				if (origContentType.startsWith("text/plain") && isHtml) {
					contentType = "text/html";
				}
				// retrieve upload files
				String sessionId = FacesUtil.getSessionId();
				List<SessionUpload> list = getSessionUploadService().getBySessionId(sessionId);
				if (list != null && list.size() > 0) {
					// construct multipart
					mBean.setContentType("multipart/mixed");
					// message body part
					BodypartBean aNode = new BodypartBean();
					aNode.setContentType(contentType);
					aNode.setValue(replyMsg);
					aNode.setSize(replyMsg.length());
					mBean.put(aNode);
					// message attachments
					for (int i = 0; i < list.size(); i++) {
						SessionUpload vo = list.get(i);
						BodypartBean subNode = new BodypartBean();
						subNode.setContentType(vo.getContentType());
						subNode.setDisposition(Part.ATTACHMENT);
						subNode.setDescription(vo.getFileName());
						byte[] bytes = vo.getSessionValue();
						subNode.setValue(bytes);
						if (bytes != null) {
							subNode.setSize(bytes.length);
						}
						else {
							subNode.setSize(0);
						}
						mBean.put(subNode);
						mBean.updateAttachCount(1);
						mBean.getComponentsSize().add(Integer.valueOf(subNode.getSize()));
					}
					// remove uploaded files from session table
					clearUploads();
				}
				else {
					mBean.setContentType(contentType);
					mBean.setBody(replyMsg);
				}
				// set addresses and subject
				mBean.setFrom(from);
				mBean.setTo(to);
				mBean.setSubject(replyMessageVo.getMsgSubject());
				mBean.setSenderId(FacesUtil.getLoginUserSenderId());
				// process the message
				TaskBaseBo csrReplyBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean(
						"csrReplyBo");
				msgsSent = (Long) csrReplyBo.process(mBean);
				logger.info("sendMessage() - Message to send:" + LF + mBean);
			}
		}
		catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
			return TO_FAILED;
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			return TO_FAILED;
		}
		// update replyCount or forwardCount
		if (msgsSent != null && msgsSent.intValue() > 0) {
			if (replyMessageVo.getIsReply())
				message.setReplyCount(message.getReplyCount() + 1);
			if (replyMessageVo.getIsForward())
				message.setForwardCount(message.getForwardCount() + 1);
			int rowsUpdated = getMessageInboxService().updateCounts(message);
			if (rowsUpdated > 0) {
				logger.info("sendMessage() - Message updated: " + message.getMsgId());
			}
		}
		return TO_SENT;
	}
	
	public String cancelSend() {
		replyMessageVo = null;
		return TO_CANCELED;
	}
	
	public boolean getAnyMessagesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMessagesMarkedForDeletion() - Entering...");
		if (folder == null) {
			logger.warn("getAnyMessagesMarkedForDeletion() - MsgInbox is null.");
			return false;
		}
		List<MsgInboxWebVo> list = getMessageList();
		for (Iterator<MsgInboxWebVo> it=list.iterator(); it.hasNext();) {
			MsgInboxWebVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate FROM email address
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateFromAddress(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateFromAddress() - From Address: " + value);
		String fromAddr = (String) value;
		if (!isValidEmailAddress(fromAddr)) {
			// invalid email address
	        FacesMessage message = com.legacytojava.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "invalidEmailAddress", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	/**
	 * Validate TO email address
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateToAddress(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateToAddress() - To Address: " + value);
		String toAddr = (String) value;
		if (!isValidEmailAddress(toAddr)) {
			// invalid email address
	        FacesMessage message = com.legacytojava.msgui.util.MessageUtil.getMessage(
					"com.legacytojava.msgui.messages", "invalidEmailAddress", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	private boolean isValidEmailAddress(String addrs) {
		List<String> list = getAddressList(addrs);
		for (int i = 0; i < list.size(); i++) {
			if (!EmailAddrUtil.isRemoteOrLocalEmailAddress(list.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	private List<String> getAddressList(String addrs) {
		List<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(addrs, ",");
		while (st.hasMoreTokens()) {
			String addr = st.nextToken();
			list.add(EmailAddrUtil.removeDisplayName(addr, true));
		}
		return list;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<MsgInboxWebVo> getMessageList() {
		if (folder == null) {
			return new ArrayList<MsgInboxWebVo>();
		}
		else {
			return (List<MsgInboxWebVo>)folder.getWrappedData();
		}
	}
	
	public MessageInbox getMessage() {
		return message;
	}

	public void setMessage(MessageInbox message) {
		this.message = message;
	}

	public MessageInbox getReplyMessageVo() {
		return replyMessageVo;
	}

	public void setReplyMessageVo(MessageInbox replyMessageVo) {
		this.replyMessageVo = replyMessageVo;
	}

	public List<MsgInboxWebVo> getMessageThreads() {
		return messageThreads;
	}

	public void setMessageThreads(List<MsgInboxWebVo> messageThreads) {
		this.messageThreads = messageThreads;
	}
	
	public List<SessionUpload> getUploads() {
		return uploads;
	}

	public void setUploads(List<SessionUpload> uploads) {
		this.uploads = uploads;
	}
	
	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public boolean isHtml() {
		return isHtml;
	}

	public void setHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

	public boolean isCheckAll() {
		return checkAll;
	}

	public void setCheckAll(boolean checkAll) {
		this.checkAll = checkAll;
	}

	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
	}

	public UIInput getFromAddrInput() {
		return fromAddrInput;
	}

	public void setFromAddrInput(UIInput fromAddrInput) {
		this.fromAddrInput = fromAddrInput;
	}

	public UIInput getToAddrInput() {
		return toAddrInput;
	}

	public void setToAddrInput(UIInput toAddrInput) {
		this.toAddrInput = toAddrInput;
	}

	public MessageRfcField getRfcFields() {
		return rfcFields;
	}

	public void setRfcFields(MessageRfcField rfcFields) {
		this.rfcFields = rfcFields;
	}
}
