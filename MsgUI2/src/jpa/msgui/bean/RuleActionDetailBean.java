package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;

import jpa.constant.Constants;
import jpa.model.rule.RuleActionDetail;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.rule.RuleActionDetailService;
import jpa.service.task.TaskBaseBo;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@ManagedBean(name="ruleAction")
@SessionScoped
public class RuleActionDetailBean implements java.io.Serializable {
	private static final long serialVersionUID = -1479694457663800603L;
	static final Logger logger = Logger.getLogger(RuleActionDetailBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();

	private RuleActionDetailService msgActionDetailDao = null;
	private DataModel<RuleActionDetail> actionDetails = null;
	private RuleActionDetail actionDetail = null;
	private boolean editMode = true;
	
	private UIInput actionIdInput = null;

	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "ruleActionDetailEdit.xhtml";
	private static String TO_SELF = "";
	private static String TO_FAILED = null;
	private static String TO_SAVED = "maintainActionDetails.xhtml";
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public DataModel<RuleActionDetail> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (actionDetails == null) {
			List<RuleActionDetail> MsgActionDetailList = getRuleActionDetailService().getAll();
			actionDetails = new ListDataModel<RuleActionDetail>(MsgActionDetailList);
		}
		return actionDetails;
	}

	public String refresh() {
		actionDetails = null;
		return TO_SELF;
	}
	
	public RuleActionDetailService getRuleActionDetailService() {
		if (msgActionDetailDao == null) {
			msgActionDetailDao = (RuleActionDetailService) SpringUtil.getWebAppContext().getBean(
					"ruleActionDetailService");
		}
		return msgActionDetailDao;
	}

	public void setRuleActionDetailService(RuleActionDetailService msgActionDetailDao) {
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
		this.actionDetail = (RuleActionDetail) actionDetails.getRowData();
		if (isInfoEnabled) {
			logger.info("viewMsgActionDetail() - MsgActionDetail to be edited: "
					+ actionDetail.getActionId());
		}
		actionDetail.setMarkedForEdition(true);
		editMode = true;
		if (isDebugEnabled) {
			logger.debug("viewMsgActionDetail() - RuleActionDetail to be passed to jsp: "
					+ actionDetail);
		}
		return TO_EDIT;
	}
	
	public String saveMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("saveMsgActionDetail() - Entering...");
		if (actionDetail == null) {
			logger.warn("saveMsgActionDetail() - RuleActionDetail is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			actionDetail.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getRuleActionDetailService().update(actionDetail);
			logger.info("saveMsgActionDetail() - Rows Updated: " + 1);
		}
		else {
			getRuleActionDetailService().insert(actionDetail);
			addToList(actionDetail);
			logger.info("saveMsgActionDetail() - Rows Inserted: " + 1);
		}
		return TO_SAVED;
	}

	@SuppressWarnings("unchecked")
	private void addToList(RuleActionDetail vo) {
		List<RuleActionDetail> list = (List<RuleActionDetail>) actionDetails.getWrappedData();
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
		List<RuleActionDetail> list = getMsgActionDetailList();
		for (int i=0; i<list.size(); i++) {
			RuleActionDetail vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getRuleActionDetailService().deleteByActionId(vo.getActionId());
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
		List<RuleActionDetail> mboxList = getMsgActionDetailList();
		for (int i=0; i<mboxList.size(); i++) {
			RuleActionDetail vo = mboxList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.actionDetail = new RuleActionDetail();
				try {
					vo.copyPropertiesTo(this.actionDetail);
					actionDetail.setMarkedForDeletion(false);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				actionDetail.setActionId(null);
				actionDetail.setMarkedForEdition(true);
				editMode = false;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public String addMsgActionDetail() {
		if (isDebugEnabled)
			logger.debug("addMsgActionDetail() - Entering...");
		reset();
		this.actionDetail = new RuleActionDetail();
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
		List<RuleActionDetail> list = getMsgActionDetailList();
		for (Iterator<RuleActionDetail> it=list.iterator(); it.hasNext();) {
			RuleActionDetail vo = it.next();
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
		try {
			RuleActionDetail vo = getRuleActionDetailService().getByActionId(actionDetailId);
			if (!editMode && vo != null) {
				// MsgActionDetail already exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "MsgActionIdAlreadyExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
		catch (NoResultException e) {
			if (editMode) {
				// MsgActionDetail does not exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "MsgActionIdDoesNotExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
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
		String className = actionDetail.getClassName();
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
		String beanId = actionDetail.getServiceName();
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
		return TO_SELF;
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		actionIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<RuleActionDetail> getMsgActionDetailList() {
		if (actionDetails == null) {
			return new ArrayList<RuleActionDetail>();
		}
		else {
			return (List<RuleActionDetail>)actionDetails.getWrappedData();
		}
	}
	
	public RuleActionDetail getActionDetail() {
		return actionDetail;
	}

	public void setActionDetail(RuleActionDetail actionDetail) {
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
