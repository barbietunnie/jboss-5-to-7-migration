package com.es.vo.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.es.vo.comm.BaseVo;


public class RuleVo extends BaseVo implements Serializable {

	private static final long serialVersionUID = -2931244363657786275L;

	private String ruleName = "";

	private RuleLogicVo ruleLogicVo = null;
	private List<RuleElementVo> ruleElementVos = null; // list of RuleElementVo
	private List<RuleSubRuleMapVo> ruleSubRuleVos = null; // list of RuleSubRuleVo
	
	public List<RuleElementVo> getRuleElementVos() {
		if (ruleElementVos==null) {
			ruleElementVos = new ArrayList<RuleElementVo>();
		}
		return ruleElementVos;
	}
	public void setRuleElementVos(List<RuleElementVo> ruleElementVos) {
		this.ruleElementVos = ruleElementVos;
	}
	public RuleLogicVo getRuleLogicVo() {
		return ruleLogicVo;
	}
	public void setRuleLogicVo(RuleLogicVo ruleLogicVo) {
		this.ruleLogicVo = ruleLogicVo;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public List<RuleSubRuleMapVo> getRuleSubRuleVos() {
		if (ruleSubRuleVos==null) {
			ruleSubRuleVos = new ArrayList<RuleSubRuleMapVo>();
		}
		return ruleSubRuleVos;
	}
	public void setRuleSubRuleVos(List<RuleSubRuleMapVo> ruleSubRuleVos) {
		this.ruleSubRuleVos = ruleSubRuleVos;
	}
}