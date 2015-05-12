package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.model.DataModel;

import jpa.model.BroadcastMessage;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.BroadcastMessageService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="broadcastMsg")
@SessionScoped
public class BroadcastMsgBean implements java.io.Serializable {
	private static final long serialVersionUID = -5557435572452796392L;
	static final Logger logger = Logger.getLogger(BroadcastMsgBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private BroadcastMessageService broadcastMsgDao = null;
	private EmailAddressService emailAddrDao = null;
	private transient DataModel<BroadcastMessage> broadcasts = null;
	private BroadcastMessage broadcastMsg = null;
	private boolean editMode = true;

	private transient HtmlDataTable dataTable;
	private final PagingVo pagingVo =  new PagingVo();
	
	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_VIEW = "broadcastMsgView.xhtml";
	static final String TO_SELF = null; // null -> remains in the same view
	static final String TO_PAGING = TO_SELF;
	static final String TO_FAILED = null;
	static final String TO_DELETED = TO_SELF;
	static final String TO_SAVED = "broadcastsList.xhtml";
	static final String TO_CANCELED = "main.xhtml";
	static final String TO_CANCELED_FROM_VIEW = TO_SAVED;

	@SuppressWarnings("unchecked")
	public DataModel<BroadcastMessage> getBroadcasts() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		// retrieve total number of rows
		if (pagingVo.getRowCount() < 0) {
			int rowCount = getBroadcastMessageService().getMessageCountForWeb();
			pagingVo.setRowCount(rowCount);
		}
		if (broadcasts == null || !pagingVo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<BroadcastMessage> brdList = getBroadcastMessageService().getBroadcastsWithPaging(
					pagingVo);
			/* set keys for paging */
			if (!brdList.isEmpty()) {
				BroadcastMessage firstRow = brdList.get(0);
				pagingVo.setIdFirst(firstRow.getRowId());
				BroadcastMessage lastRow = brdList.get(brdList.size() - 1);
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

	public BroadcastMessageService getBroadcastMessageService() {
		if (broadcastMsgDao == null) {
			broadcastMsgDao = SpringUtil.getWebAppContext().getBean(BroadcastMessageService.class);
		}
		return broadcastMsgDao;
	}

	public EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressService.class);
		}
		return emailAddrDao;
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
		this.broadcastMsg = broadcasts.getRowData();
		logger.info("viewBroadcastMsg() - Broadcast to be viewed: " + broadcastMsg.getRowId());
		broadcastMsg.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewBroadcastMsg() - BroadcastMessage to be passed to jsp: " + broadcastMsg);
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
		List<BroadcastMessage> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			BroadcastMessage vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getBroadcastMessageService().deleteByRowId(vo.getRowId());
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
		List<BroadcastMessage> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			BroadcastMessage vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				getBroadcastMessageService().update(vo);
				logger.info("saveBroadcasts() - Broadcast updated: " + vo.getRowId());
			}
		}
		refresh();
		return TO_SAVED;
	}

	public String cancelEdit() {
		refresh();
		String viewId = FacesUtil.getCurrentViewId();
		if (StringUtils.contains(viewId, "broadcastMsgView")) {
			return TO_CANCELED_FROM_VIEW;
		}
		else {
			return TO_CANCELED;
		}
	}

	public boolean getAnyBroadcastsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyBroadcastsMarkedForDeletion() - Entering...");
		if (broadcasts == null) {
			logger.warn("getAnyBroadcastsMarkedForDeletion() - Broadcast List is null.");
			return false;
		}
		List<BroadcastMessage> subrList = getBroadcastList();
		for (Iterator<BroadcastMessage> it=subrList.iterator(); it.hasNext();) {
			BroadcastMessage vo = it.next();
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
	private List<BroadcastMessage> getBroadcastList() {
		if (broadcasts == null) {
			return new ArrayList<BroadcastMessage>();
		}
		else {
			return (List<BroadcastMessage>)broadcasts.getWrappedData();
		}
	}

	public BroadcastMessage getBroadcastMsg() {
		return broadcastMsg;
	}

	public void setBroadcastMsg(BroadcastMessage subscriber) {
		this.broadcastMsg = subscriber;
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
}
