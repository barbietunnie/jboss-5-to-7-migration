package jpa.service.rule;

import java.util.List;

import jpa.model.RuleElement;
import jpa.model.RuleLogic;
import jpa.service.RuleLogicService;
import jpa.service.external.RuleTargetProc;
import jpa.service.task.TaskBaseBo;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataService {
	static final Logger logger = Logger.getLogger(RuleDataService.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private RuleLogicService logicService;

	public RuleDataService() {
	}
	
	public List<RuleLogic> getCurrentRules() {
		List<RuleLogic> rules = logicService.getActiveRules();
		substituteTargetProc(rules);
		substituteExclListProc(rules);
		return rules;
	}
	
	public RuleLogic getRuleByRuleName(String ruleName) {
		RuleLogic ruleVo = (RuleLogic) logicService.getByRuleName(ruleName);
		substituteTargetProc(ruleVo);
		substituteExclListProc(ruleVo);
		return ruleVo;
	}
	
	private void substituteTargetProc(List<RuleLogic> rules) {
		if (rules == null || rules.size() == 0) return;
		for (RuleLogic rule : rules) {
			substituteTargetProc(rule);
		}
	}
	
	private void substituteTargetProc(RuleLogic rule) {
		List<RuleElement> elements = rule.getRuleElements();
		if (elements == null || elements.isEmpty()) return;
		for (RuleElement element : elements) {
			if (element.getTargetProcName() == null) continue;
			Object obj = null;
			try { // a TargetProc could be a class name or a bean id
				obj = Class.forName(element.getTargetProcName()).newInstance();
				logger.info("Loaded class " + element.getTargetProcName() + " for rule "
						+ rule.getRuleName());
			}
			catch (Exception e) { // not a class name, try load it as a Bean
				try {
					obj = SpringUtil.getAppContext().getBean(element.getTargetProcName());
					logger.info("Loaded bean " + element.getTargetProcName() + " for rule "
							+ rule.getRuleName());
				}
				catch (Exception e2) {
					logger.warn("Failed to load: " + element.getTargetProcName() + " for rule "
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
	
	private void substituteExclListProc(List<RuleLogic> rules) {
		if (rules == null || rules.isEmpty()) return;
		for (RuleLogic rule : rules) {
			substituteExclListProc(rule);
		}
	}
	
	private void substituteExclListProc(RuleLogic rule) {
		List<RuleElement> elements = rule.getRuleElements();
		if (elements == null || elements.isEmpty()) return;
		for (RuleElement element : elements) {
			if (element.getExclListProcName() == null) continue;
			Object obj = null;
			try {
				obj = SpringUtil.getAppContext().getBean(element.getExclListProcName());
			}
			catch (Exception e) {
				logger.error("Failed to load bean: " + element.getExclListProcName() + " for rule "
						+ rule.getRuleName());
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
		RuleDataService bo = (RuleDataService) SpringUtil.getAppContext().getBean("ruleDataService");
		bo.getCurrentRules();
	}
}
