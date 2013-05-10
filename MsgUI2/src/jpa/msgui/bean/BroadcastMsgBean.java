package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.DataModel;

import jpa.model.message.MessageClickCount;
import jpa.model.message.MessageInbox;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageClickCountService;
import jpa.service.message.MessageInboxService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class BroadcastMsgBean {
	static final Logger logger = Logger.getLogger(BroadcastMsgBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private MessageClickCountService msgClickCountsDao = null;
	private EmailAddressService emailAddrDao = null;
	private MessageInboxService msgInboxDao = null;
	private DataModel<MessageClickCount> broadcasts = null;
	private MessageClickCount broadcast = null;
	private boolean editMode = true;
	private MessageInbox broadcastMsg = null;

	private HtmlDataTable dataTable;
	private final PagingVo pagingVo =  new PagingVo();;
	
	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_VIEW = "broadcastlist.view";
	static final String TO_PAGING = "broadcastlist.paging";
	static final String TO_FAILED = "broadcastlist.failed";
	static final String TO_DELETED = "broadcastlist.deleted";
	static final String TO_SAVED = "broadcastlist.saved";
	static final String TO_CANCELED = "broadcastlist.canceled";

	public DataModel<MessageClickCount> getBroadcasts() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getMessageClickCountService().getMessageCountForWeb();
			pagingVo.setRowCount(rowCount);
		}
		if (broadcasts == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<MessageClickCount> brdList = getMessageClickCountService().getBroadcastsWithPaging(
					pagingVo);
			/* set keys for paging */
			if (!brdList.isEmpty()) {
				MessageClickCount firstRow = (MessageClickCount) brdList.get(0);
				pagingVo.setIdFirst(firstRow.getRowId());
				MessageClickCount lastRow = (MessageClickCount) brdList.get(brdList.size() - 1);
				pagingVo.setIdLast(lastRow.getRowId());
			}
			else {
				pagingVo.setIdFirst(-1);
				pagingVo.setIdLast(-1);
			}
			//logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			broadcasts = new PagedListDataModel(brdList, pagingVo.getRowCount(), pagingVo.getPageSize());
		}
		return broadcasts;
	}
	
	public String pageFirst() {
		dataTable.setFirst(0);
		pagingVo.setPageAction(PagingVo.PageAction.FIRST);
		return TO_PAGING;
	}

	public String pagePrevious() {
		dataTable.setFirst(dataTable.getFirst() - dataTable.getRows());
		pagingVo.setPageAction(PagingVo.PageAction.PREVIOUS);
		return TO_PAGING;
	}

	public String pageNext() {
		dataTable.setFirst(dataTable.getFirst() + dataTable.getRows());
		pagingVo.setPageAction(PagingVo.PageAction.NEXT);
		return TO_PAGING;
	}

	public String pageLast() {
		int count = dataTable.getRowCount();
		int rows = dataTable.getRows();
		dataTable.setFirst(count - ((count % rows != 0) ? count % rows : rows));
		pagingVo.setPageAction(PagingVo.PageAction.LAST);
		return TO_PAGING;
	}
    
	public int getLastPageRow() {
		int lastRow = dataTable.getFirst() + dataTable.getRows();
		if (lastRow > dataTable.getRowCount())
			return dataTable.getRowCount();
		else
			return lastRow;
	}
	
	public PagingVo getPagingVo() {
		return pagingVo;
	}
	
	public void refresh() {
		broadcasts = null;
		if (dataTable.getFirst() <= 0) {
			// to display messages newly arrived
			pageFirst();
		}
	}

	public String refreshPage() {
		refresh();
		pagingVo.setRowCount(-1);
		return "";
	}
	
	public void resetPagingVo() {
		pagingVo.resetPageContext();
		if (dataTable != null) dataTable.setFirst(0);
		refresh();
	}

	public MessageClickCountService getMessageClickCountService() {
		if (msgClickCountsDao == null) {
			msgClickCountsDao = (MessageClickCountService) SpringUtil.getWebAppContext().getBean(
					"msgClickCountsDao");
		}
		return msgClickCountsDao;
	}

	public EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddressService) SpringUtil.getWebAppContext().getBean("emailAddrDao");
		}
		return emailAddrDao;
	}

	public MessageInboxService getMessageInboxService() {
		if (msgInboxDao == null) {
			msgInboxDao = (MessageInboxService) SpringUtil.getWebAppContext().getBean("msgInboxDao");
		}
		return msgInboxDao;
	}

	public String viewBroadcastMsg() {
		if (isDebugEnabled)
			logger.debug("viewBroadcastMsg() - Entering...");
		if (broadcasts == null) {
			logger.warn("viewBroadcastMsg() - Broadcast List is null.");
			return TO_FAILED;
		}
		if (!broadcasts.isRowAvailable()) {
			logger.warn("viewBroadcastMsg() - Broadcast Row not available.");
			return TO_FAILED;
		}
		reset();
		this.broadcast = (MessageClickCount) broadcasts.getRowData();
		logger.info("viewBroadcastMsg() - Broadcast to be viewed: " + broadcast.getMessageInbox().getRowId());
		broadcast.setMarkedForEdition(true);
		editMode = true;
		broadcastMsg = broadcast.getMessageInbox();
		if (isDebugEnabled) {
			logger.debug("viewBroadcastMsg() - MessageClickCount to be passed to jsp: " + broadcast);
		}
		return TO_VIEW;
	}

	public String deleteBroadcasts() {
		if (isDebugEnabled)
			logger.debug("deleteBroadcasts() - Entering...");
		if (broadcasts == null) {
			logger.warn("deleteBroadcasts() - Broadcast List is null.");
			return TO_FAILED;
		}
		reset();
		List<MessageClickCount> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			MessageClickCount vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMessageClickCountService().deleteByRowId(vo.getRowId());
				if (rowsDeleted > 0) {
					logger.info("deleteBroadcasts() - Broadcast deleted: " + vo.getRowId());
					pagingVo.setRowCount(pagingVo.getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public String saveBroadcasts() {
		if (isDebugEnabled)
			logger.debug("saveBroadcasts() - Entering...");
		if (broadcasts == null) {
			logger.warn("saveBroadcasts() - Broadcast List is null.");
			return TO_FAILED;
		}
		reset();
		List<MessageClickCount> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			MessageClickCount vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				getMessageClickCountService().update(vo);
				logger.info("saveBroadcasts() - Broadcast updated: " + vo.getRowId());
			}
		}
		refresh();
		return TO_SAVED;
	}

	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}

	public boolean getAnyBroadcastsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyBroadcastsMarkedForDeletion() - Entering...");
		if (broadcasts == null) {
			logger.warn("getAnyBroadcastsMarkedForDeletion() - Broadcast List is null.");
			return false;
		}
		List<MessageClickCount> subrList = getBroadcastList();
		for (Iterator<MessageClickCount> it=subrList.iterator(); it.hasNext();) {
			MessageClickCount vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	void reset() {
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings("unchecked")
	private List<MessageClickCount> getBroadcastList() {
		if (broadcasts == null) {
			return new ArrayList<MessageClickCount>();
		}
		else {
			return (List<MessageClickCount>)broadcasts.getWrappedData();
		}
	}

	public MessageClickCount getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(MessageClickCount subscriber) {
		this.broadcast = subscriber;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
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

	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
	}

	public MessageInbox getBroadcastMsg() {
		return broadcastMsg;
	}

	public void setBroadcastMsg(MessageInbox broadcastMsg) {
		this.broadcastMsg = broadcastMsg;
	}
}
