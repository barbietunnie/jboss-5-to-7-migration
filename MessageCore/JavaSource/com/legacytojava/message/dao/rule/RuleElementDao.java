package com.legacytojava.message.dao.rule;

import java.util.List;

import com.legacytojava.message.vo.rule.RuleElementVo;

public interface RuleElementDao {
	public RuleElementVo getByPrimaryKey(String ruleName, int elementSeq);
	public List<RuleElementVo> getAll();
	public List<RuleElementVo> getByRuleName(String ruleName);
	public int update(RuleElementVo ruleElementVo);
	public int deleteByPrimaryKey(String ruleName, int elementSeq);
	public int deleteByRuleName(String ruleName);
	public int insert(RuleElementVo ruleElementVo);
}
