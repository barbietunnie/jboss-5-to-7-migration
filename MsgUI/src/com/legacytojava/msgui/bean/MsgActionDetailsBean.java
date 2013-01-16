package com.legacytojava.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.dao.action.MsgActionDetailDao;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.action.MsgActionDetailVo;
import com.legacytojava.msgui.util.FacesUtil;
import com.legacytojava.msgui.util.SpringUtil;

public class MsgActionDetailsBean {
	static final Logger logger = Logger.getLogger(MsgActionDetailsBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();

	private MsgActionDetailDao msgActionDetailDao = null;
	private DataModel actionDetails = null;
	private MsgActionDetailVo actionDetail = null;
	private boolean editMode = true;
	
	private UIInput actionIdInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "actiondetail.edit";
	private static String TO_FAILED = "actiondetail.failed";
	private static String TO_SAVED = "actiondetail.saved";
	private static String TO_DELETED = "actiondetail.deleted";
	private static String TO_CANCELED = "actiondetail.canceled";

	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (actionDetails == null) {
			List<MsgActionDetailVo> MsgActionDetailList = getMsgActionDetailDao().getAll();
			actionDetails = new ListDataModel(MsgActionDetailList);
		}
		return actionDetails;
	}

	public String refresh() {
		actionDetails = null;
		return "";
	}
	
	public MsgActionDetailDao getMsgActionDetailDao() {
		if (msgActionDetailDao == null) {
			msgActionDetailDao = (MsgActionDetailDao) SpringUtil.getWebAppContext().getBean(
					"msgActionDetailDao");
		}
		return msgActionDetailDao;
	}

	public void setMsgActionDetailDao(MsgActionDetailDao msgActionDetailDao) {
		this.msgActionDetailDao = msgActionDetailDao;
	}
	
	public String viewMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("viewMsgActionDetail() - Entering...");
		if (actionDetails == null) {
			logger.warn("viewMsgActionDetail() - MsgActionDetail List is null.");
			return TO_FAILED;
		}
		if (!actionDetails.isRowAvailable()) {
			logger.warn("viewMsgActionDetail() - MsgActionDetail Row not available.");
			return TO_FAILED;
		}
		reset();
		this.actionDetail = (MsgActionDetailVo) actionDetails.getRowData();
		if (isInfoEnabled) {
			logger.info("viewMsgActionDetail() - MsgActionDetail to be edited: "
					+ actionDetail.getActionId());
		}
		actionDetail.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewMsgActionDetail() - MsgActionDetailVo to be passed to jsp: "
					+ actionDetail);
		}
		return TO_EDIT;
	}
	
	public String saveMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("saveMsgActionDetail() - Entering...");
		if (actionDetail == null) {
			logger.warn("saveMsgActionDetail() - MsgActionDetailVo is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (!StringUtil.isEmpty(FacesUtil.getLoginUserId())) {
			actionDetail.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			int rowsUpdated = getMsgActionDetailDao().update(actionDetail);
			logger.info("saveMsgActionDetail() - Rows Updated: " + rowsUpdated);
		}
		else {
			int rowsInserted = getMsgActionDetailDao().insert(actionDetail);
			if (rowsInserted > 0)
				addToList(actionDetail);
			logger.info("saveMsgActionDetail() - Rows Inserted: " + rowsInserted);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(MsgActionDetailVo vo) {
		List<MsgActionDetailVo> list = (List<MsgActionDetailVo>) actionDetails.getWrappedData();
		list.add(vo);
	}
	
	public String deleteMsgActionDetails() {
		if (isDebugEnabled)
			logger.debug("deleteMsgActionDetails() - Entering...");
		if (actionDetails == null) {
			logger.warn("deleteMsgActionDetails() - MsgActionDetail List is null.");
			return TO_FAILED;
		}
		reset();
		List<MsgActionDetailVo> list = getMsgActionDetailList();
		for (int i=0; i<list.size(); i++) {
			MsgActionDetailVo vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMsgActionDetailDao().deleteByActionId(vo.getActionId());
				if (rowsDeleted > 0) {
					logger.info("deleteMsgActionDetails() - MsgActionDetail deleted: "
							+ vo.getActionId());
				}
				list.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public String copyMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("copyMsgActionDetail() - Entering...");
		if (actionDetails == null) {
			logger.warn("copyMsgActionDetail() - MsgActionDetail List is null.");
			return TO_FAILED;
		}
		reset();
		List<MsgActionDetailVo> mboxList = getMsgActionDetailList();
		for (int i=0; i<mboxList.size(); i++) {
			MsgActionDetailVo vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				try {
					this.actionDetail = (MsgActionDetailVo) vo.getClone();
					actionDetail.setMarkedForDeletion(false);
				}
				catch (CloneNotSupportedException e) {
					this.actionDetail = new MsgActionDetailVo();
				}
				actionDetail.setActionId(null);
				actionDetail.setMarkedForEdition(true);
				editMode = false;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public String addMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("addMsgActionDetail() - Entering...");
		reset();
		this.actionDetail = new MsgActionDetailVo();
		actionDetail.setMarkedForEdition(true);
		actionDetail.setUpdtUserId(Constants.DEFAULT_USER_ID);
		editMode = false;
		return TO_EDIT;
	}
	
	public String cancelEdit() {
		refresh();
		return TO_CANCELED;
	}
	
	public boolean getAnyActionDetailsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyActionDetailsMarkedForDeletion() - Entering...");
		if (actionDetails == null) {
			logger.warn("getAnyActionDetailsMarkedForDeletion() - MsgActionDetail List is null.");
			return false;
		}
		List<MsgActionDetailVo> list = getMsgActionDetailList();
		for (Iterator<MsgActionDetailVo> it=list.iterator(); it.hasNext();) {
			MsgActionDetailVo vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate primary key
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String actionDetailId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - MsgActionDetailKey: " + actionDetailId);
		MsgActionDetailVo vo = getMsgActionDetailDao().getByActionId(actionDetailId);
		if (editMode && vo == null) {
			// MsgActionDetail does not exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "MsgActionIdDoesNotExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		if (!editMode && vo != null) {
			// MsgActionDetail already exist
	        FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "MsgActionIdAlreadyExist", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public String testActionDetail() {
		if (isDebugEnabled)
			logger.debug("testActionDetail() - Entering...");
		if (actionDetail == null) {
			logger.warn("testActionDetail() - ActionDetailVo is null.");
			return TO_FAILED;
		}
		testResult = null;
		String className = actionDetail.getProcessClassName();
		if (className != null && className.trim().length() > 0) {
			try {
				Object bo = Class.forName(className).newInstance();
				if (bo instanceof TaskBaseBo) {
					testResult = "actionDetailClassNameTestSuccess";
				}
				else {
					testResult = "actionDetailClassNameTestFailure";
				}
			}
			catch (Exception e) {
				logger.error("Exception caught: " + e.toString());
				testResult = "actionDetailClassNameTestFailure";
			}
		}
		String beanId = actionDetail.getProcessBeanId();
		if (beanId != null && testResult == null) {
			FacesContext facesCtx = FacesContext.getCurrentInstance();
			ServletContext sctx = (ServletContext) facesCtx.getExternalContext().getContext();
			WebApplicationContext ctx = WebApplicationContextUtils
					.getRequiredWebApplicationContext(sctx);
			try {
				Object bo = ctx.getBean(beanId);
				if (bo instanceof TaskBaseBo) {
					testResult = "actionDetailBeanIdTestSuccess";
				}
				else {
					testResult = "actionDetailBeanIdTestFailure";
				}
			}
			catch (Exception e) {
				logger.error("Exception caught: " + e.toString());
				testResult = "actionDetailBeanIdTestFailure";
			}
		}
		return null;
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		actionIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<MsgActionDetailVo> getMsgActionDetailList() {
		if (actionDetails == null) {
			return new ArrayList<MsgActionDetailVo>();
		}
		else {
			return (List<MsgActionDetailVo>)actionDetails.getWrappedData();
		}
	}
	
	public MsgActionDetailVo getActionDetail() {
		return actionDetail;
	}

	public void setActionDetail(MsgActionDetailVo actionDetail) {
		this.actionDetail = actionDetail;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public UIInput getActionIdInput() {
		return actionIdInput;
	}

	public void setActionIdInput(UIInput actionDetailIdInput) {
		this.actionIdInput = actionDetailIdInput;
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
}
