package com.es.bo.rule;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.es.bo.external.AbstractTargetProc;
import com.es.core.util.SpringUtil;
import com.es.dao.rule.RuleDao;
import com.es.vo.rule.RuleElementVo;
import com.es.vo.rule.RuleVo;

@Component("rulesDataBo")
public class RulesDataBo {
	static final Logger logger = Logger.getLogger(RulesDataBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private RuleDao ruleDao;

	public RulesDataBo() {
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
	
	private void substituteTargetProc(List<RuleVo> rules) {
		if (rules == null) {
			return;
		}
		for (RuleVo rule : rules) {
			substituteTargetProc(rule);
		}
	}
	
	private void substituteTargetProc(RuleVo rule) {
		List<RuleElementVo> elements = rule.getRuleElementVos();
		if (elements == null) {
			return;
		}
		for (RuleElementVo element : elements) {
			if (StringUtils.isBlank(element.getTargetProc())) {
				continue;
			}
			Object proc = null;
			try { // a TargetProc could be a class name or a bean id
				proc = Class.forName(element.getTargetProc()).newInstance();
				logger.info("Loaded class " + element.getTargetProc() + " for rule "
						+ rule.getRuleName());
			}
			catch (Exception e) { // not a class name, try load it as a Bean
				try {
					proc = SpringUtil.getAppContext().getBean(element.getTargetProc());
					logger.info("Loaded bean " + element.getTargetProc() + " for rule "
							+ rule.getRuleName());
				}
				catch (Exception e2) {
					logger.warn("Failed to load: " + element.getTargetProc() + " for rule "
							+ rule.getRuleName());
				}
				if (proc == null) {
					continue;
				}
			}
			try {
				String text = null;
				if (proc instanceof AbstractTargetProc) {
					text = ((AbstractTargetProc)proc).process();
				}
				if (StringUtils.isNotBlank(text)) {
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
		if (rules == null) {
			return;
		}
		for (RuleVo rule : rules) {
			substituteExclListProc(rule);
		}
	}
	
	private void substituteExclListProc(RuleVo rule) {
		List<RuleElementVo> elements = rule.getRuleElementVos();
		if (elements == null) {
			return;
		}
		for (RuleElementVo element : elements) {
			if (StringUtils.isBlank(element.getExclListProc())) {
				continue;
			}
			Object proc = null;
			try {
				proc = SpringUtil.getAppContext().getBean(element.getExclListProc());
			}
			catch (Exception e) {
				logger.error("Failed to load bean: " + element.getExclListProc() + " for rule "
						+ rule.getRuleName());
				continue;
			}
			String text = null;
			try {
				if (proc instanceof AbstractTargetProc) {
					text = ((AbstractTargetProc)proc).process();
				}
				if (StringUtils.isNotBlank(text)) {
					logger.info("Appending Exclusion list for rule: " + rule.getRuleName());
					logger.info("  Exclusion List: " + text);
					String delimiter = element.getDelimiter();
					if (delimiter == null || delimiter.length() == 0) {
						delimiter = ",";
					}
					String origText = element.getExclusions();
					if (StringUtils.isNotBlank(origText)) {
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
		RulesDataBo bo = SpringUtil.getAppContext().getBean(RulesDataBo.class);
		bo.getCurrentRules();
	}
}
