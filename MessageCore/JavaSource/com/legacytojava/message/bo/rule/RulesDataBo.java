package com.legacytojava.message.bo.rule;

import java.util.List;

import com.legacytojava.message.vo.rule.RuleVo;

public interface RulesDataBo {
	public List<RuleVo> getCurrentRules();
	
	public RuleVo getRuleByPrimaryKey(String key);
}
