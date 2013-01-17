package com.legacytojava.message.dao.rule;

import java.util.List;

import com.legacytojava.message.vo.rule.RuleLogicVo;

public interface RuleLogicDao {
	public RuleLogicVo getByPrimaryKey(String ruleName, int ruleSeq);
	public List<RuleLogicVo> getByRuleName(String ruleName);
	public int getNextRuleSequence();
	public List<RuleLogicVo> getActiveRules();
	public List<RuleLogicVo> getAll(boolean builtInRule);
	public List<RuleLogicVo> getAllSubRules(boolean excludeBuiltIn);
	public List<String> getBuiltinRuleNames4Web();
	public List<String> getCustomRuleNames4Web();
	public int update(RuleLogicVo ruleLogicVo);
	public int deleteByPrimaryKey(String ruleName, int ruleSeq);
	public int insert(RuleLogicVo ruleLogicVo);
}
