package com.legacytojava.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.DataModel;

import org.apache.log4j.Logger;

import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.inbox.MsgClickCountsDao;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.inbox.MsgClickCountsVo;
import com.legacytojava.message.vo.inbox.MsgInboxVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class BroadcastMsgListBean {
	static final Logger logger = Logger.getLogger(BroadcastMsgListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private MsgClickCountsDao msgClickCountsDao = null;
	private EmailAddrDao emailAddrDao = null;
	private MsgInboxDao msgInboxDao = null;
	private DataModel broadcasts = null;
	private MsgClickCountsVo broadcast = null;
	private boolean editMode = true;
	private MsgInboxVo broadcastMsg = null;

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

	public DataModel getBroadcasts() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getMsgClickCountsDao().getMsgCountForWeb();
			pagingVo.setRowCount(rowCount);
		}
		if (broadcasts == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<MsgClickCountsVo> brdList = getMsgClickCountsDao().getBroadcastsWithPaging(
					pagingVo);
			/* set keys for paging */
			if (!brdList.isEmpty()) {
				MsgClickCountsVo firstRow = (MsgClickCountsVo) brdList.get(0);
				pagingVo.setIdFirst(firstRow.getMsgId());
				MsgClickCountsVo lastRow = (MsgClickCountsVo) brdList.get(brdList.size() - 1);
				pagingVo.setIdLast(lastRow.getMsgId());
			}
			else {
				pagingVo.setIdFirst(-1);
				pagingVo.setIdLast(-1);
			}
			//logger.info("PagingVo After: " + pagingVo);
			pagingVo.setPageAction(PagingVo.PageAction.CURRENT);
			broadcasts = new PagedListDataModel(brdList, pagingVo.getRowCount(), pagingVo
					.getPageSize());
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

	public MsgClickCountsDao getMsgClickCountsDao() {
		if (msgClickCountsDao == null) {
			msgClickCountsDao = (MsgClickCountsDao) SpringUtil.getWebAppContext().getBean(
					"msgClickCountsDao");
		}
		return msgClickCountsDao;
	}

	public EmailAddrDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = (EmailAddrDao) SpringUtil.getWebAppContext().getBean("emailAddrDao");
		}
		return emailAddrDao;
	}

	public MsgInboxDao getMsgInboxDao() {
		if (msgInboxDao == null) {
			msgInboxDao = (MsgInboxDao) SpringUtil.getWebAppContext().getBean("msgInboxDao");
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
		this.broadcast = (MsgClickCountsVo) broadcasts.getRowData();
		logger.info("viewBroadcastMsg() - Broadcast to be viewed: " + broadcast.getMsgId());
		broadcast.setMarkedForEdition(true);
		editMode = true;
		broadcastMsg = getMsgInboxDao().getByPrimaryKey(broadcast.getMsgId());
		if (isDebugEnabled) {
			logger.debug("viewBroadcastMsg() - MsgClickCountsVo to be passed to jsp: " + broadcast);
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
		List<MsgClickCountsVo> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			MsgClickCountsVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMsgClickCountsDao().deleteByPrimaryKey(vo.getMsgId());
				if (rowsDeleted > 0) {
					logger.info("deleteBroadcasts() - Broadcast deleted: " + vo.getMsgId());
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
		List<MsgClickCountsVo> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			MsgClickCountsVo vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				int rowsUpdated = getMsgClickCountsDao().update(vo);
				if (rowsUpdated > 0) {
					logger.info("saveBroadcasts() - Broadcast updated: " + vo.getMsgId());
				}
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
		List<MsgClickCountsVo> subrList = getBroadcastList();
		for (Iterator<MsgClickCountsVo> it=subrList.iterator(); it.hasNext();) {
			MsgClickCountsVo vo = it.next();
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
	private List<MsgClickCountsVo> getBroadcastList() {
		if (broadcasts == null) {
			return new ArrayList<MsgClickCountsVo>();
		}
		else {
			return (List<MsgClickCountsVo>)broadcasts.getWrappedData();
		}
	}

	public MsgClickCountsVo getBroadcast() {
		return broadcast;
	}

	public void setBroadcast(MsgClickCountsVo subscriber) {
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

	public MsgInboxVo getBroadcastMsg() {
		return broadcastMsg;
	}

	public void setBroadcastMsg(MsgInboxVo broadcastMsg) {
		this.broadcastMsg = broadcastMsg;
	}
}
