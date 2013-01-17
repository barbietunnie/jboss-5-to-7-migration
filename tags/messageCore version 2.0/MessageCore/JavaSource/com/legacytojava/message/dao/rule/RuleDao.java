package com.legacytojava.message.dao.rule;

import java.util.List;

import com.legacytojava.message.vo.rule.RuleVo;

public interface RuleDao {
	public RuleVo getByPrimaryKey(String ruleName);
	public List<RuleVo> getActiveRules();
}
