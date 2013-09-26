package jpa.msgui.bean;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import jpa.model.rule.RuleLogic;
import jpa.msgui.util.FacesUtil;

import org.apache.log4j.Logger;

@ManagedBean(name="builtinRule")
@SessionScoped
public class RuleLogicBuiltinRuleBean extends RuleLogicBean {
	private static final long serialVersionUID = -498930141487046944L;
	protected static final Logger logger = Logger.getLogger(RuleLogicBuiltinRuleBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	private static String TO_EDIT = "ruleActionBuiltinEdit";
	private static String TO_FAILED = null;
	
	@Override
	public DataModel<RuleLogic> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (ruleLogics == null) {
			List<RuleLogic> ruleLogicList = getRuleLogicService().getAll(true);
			ruleLogics = new ListDataModel<RuleLogic>(ruleLogicList);
		}
		return ruleLogics;
	}
	
	@Override
	public String viewMsgActions() {
		if (isDebugEnabled)
			logger.debug("viewMsgActions() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewMsgActions() - RuleLogic List is null.");
			return TO_FAILED;
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewMsgActions() - RuleLogic Row not available.");
			return TO_FAILED;
		}
		reset();
		ruleActions = null;
		this.ruleLogic = (RuleLogic) ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		return TO_EDIT;
	}
}
