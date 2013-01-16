package com.legacytojava.msgui.bean;

import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import com.legacytojava.message.vo.rule.RuleLogicVo;
import com.legacytojava.msgui.util.FacesUtil;

public class MsgBuiltInRulesBean extends MsgRulesBean {
	protected static final Logger logger = Logger.getLogger(MsgBuiltInRulesBean.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	public DataModel getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (ruleLogics == null) {
			List<RuleLogicVo> ruleLogicList = getRuleLogicDao().getAll(true);
			ruleLogics = new ListDataModel(ruleLogicList);
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
		this.ruleLogic = (RuleLogicVo) ruleLogics.getRowData();
		ruleLogic.setMarkedForEdition(true);
		return "msgrule.msgaction.builtin.edit";
	}
}
