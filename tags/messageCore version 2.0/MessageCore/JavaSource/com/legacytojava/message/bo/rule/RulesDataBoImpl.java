package com.legacytojava.message.bo.rule;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.dao.rule.RuleDao;
import com.legacytojava.message.external.RuleTargetProc;
import com.legacytojava.message.vo.rule.RuleElementVo;
import com.legacytojava.message.vo.rule.RuleVo;

@Component("rulesDataBo")
public final class RulesDataBoImpl implements RulesDataBo {
	static final Logger logger = Logger.getLogger(RulesDataBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private RuleDao ruleDao;

	private RulesDataBoImpl() {
	}
	
	public List<RuleVo> getCurrentRules() {
		List<RuleVo> rules = ruleDao.getActiveRules();
		substituteTargetProc(rules);
		substituteExclListProc(rules);
		return rules;
	}
	
	public RuleVo getRuleByPrimaryKey(String key) {
		RuleVo ruleVo = (RuleVo) ruleDao.getByPrimaryKey(key);
		substituteTargetProc(ruleVo);
		substituteExclListProc(ruleVo);
		return ruleVo;
	}
	
	public RuleDao getRuleDao() {
		return ruleDao;
	}

	private void substituteTargetProc(List<RuleVo> rules) {
		if (rules == null || rules.size() == 0) return;
		for (RuleVo rule : rules) {
			substituteTargetProc(rule);
		}
	}
	
	private void substituteTargetProc(RuleVo rule) {
		List<RuleElementVo> elements = rule.getRuleElementVos();
		if (elements == null) return;
		for (RuleElementVo element : elements) {
			if (element.getTargetProc() == null) continue;
			Object obj = null;
			try { // a TargetProc could be a class name or a bean id
				obj = Class.forName(element.getTargetProc()).newInstance();
				logger.info("Loaded class " + element.getTargetProc() + " for rule "
						+ rule.getRuleName());
			}
			catch (Exception e) { // not a class name, try load it as a Bean
				try {
					obj = SpringUtil.getAppContext().getBean(element.getTargetProc());
					logger.info("Loaded bean " + element.getTargetProc() + " for rule "
							+ rule.getRuleName());
				}
				catch (Exception e2) {
					logger.warn("Failed to load: " + element.getTargetProc() + " for rule "
							+ rule.getRuleName());
				}
				if (obj == null) continue;
			}
			try {
				String text = null;
				if (obj instanceof TaskBaseBo) {
					TaskBaseBo bo = (TaskBaseBo) obj;
					text = (String) bo.process(null);
				}
				else if (obj instanceof RuleTargetProc) {
					RuleTargetProc bo = (RuleTargetProc) obj;
					text = bo.process();
				}
				if (text != null && text.trim().length() > 0) {
					logger.info("Changing Target Text for rule: " + rule.getRuleName());
					logger.info("  From: " + element.getTargetText());
					logger.info("    To: " + text);
					element.setTargetText(text);
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				throw new RuntimeException(e.toString());
			}
		}
	}
	
	private void substituteExclListProc(List<RuleVo> rules) {
		if (rules == null || rules.size() == 0) return;
		for (RuleVo rule : rules) {
			substituteExclListProc(rule);
		}
	}
	
	private void substituteExclListProc(RuleVo rule) {
		List<RuleElementVo> elements = rule.getRuleElementVos();
		if (elements == null) return;
		for (RuleElementVo element : elements) {
			if (element.getExclListProc() == null) continue;
			Object obj = null;
			try {
				obj = SpringUtil.getAppContext().getBean(element.getExclListProc());
			}
			catch (Exception e) {
				logger.error("Failed to load bean: " + element.getExclListProc() + " for rule "
						+ rule.getRuleName());
			}
			if (obj == null || !(obj instanceof TaskBaseBo)) continue;
			TaskBaseBo bo = (TaskBaseBo) obj;
			try {
				String text = (String) bo.process(null);
				if (text != null && text.trim().length() > 0) {
					logger.info("Appending Exclusion list for rule: " + rule.getRuleName());
					logger.info("  Exclusion List: " + text);
					String delimiter = element.getDelimiter();
					if (delimiter == null || delimiter.length() == 0) {
						delimiter = ",";
					}
					String origText = element.getExclusions();
					if (origText != null && origText.length() > 0) {
						origText = origText + delimiter;
					}
					else {
						origText = "";
					}
					element.setExclusions(origText + text);
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				throw new RuntimeException(e.toString());
			}
		}
	}
	
	public static void main(String[] args) {
		RulesDataBo bo = (RulesDataBo) SpringUtil.getAppContext().getBean("rulesDataBo");
		bo.getCurrentRules();
	}
}
