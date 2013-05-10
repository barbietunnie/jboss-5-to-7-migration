package jpa.msgui.bean;

import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import jpa.model.rule.RuleLogic;
import jpa.msgui.util.FacesUtil;

import org.apache.log4j.Logger;

public class MsgBuiltInRulesBean extends MsgRulesBean {
	protected static final Logger logger = Logger.getLogger(MsgBuiltInRulesBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

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
	
	public String viewMsgActions() {
		if (isDebugEnabled)
			logger.debug("viewMsgActions() - Entering...");
		if (ruleLogics == null) {
			logger.warn("viewMsgActions() - RuleLogic List is null.");
			return "msgrule.failed";
		}
		if (!ruleLogics.isRowAvailable()) {
			logger.warn("viewMsgActions() - RuleLogic Row not available.");
			return "msgrule.failed";
		}
		reset();
		msgActions = null;
		this.ruleLogic = (RuleLogic) ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		return "msgrule.msgaction.builtin.edit";
	}
}
