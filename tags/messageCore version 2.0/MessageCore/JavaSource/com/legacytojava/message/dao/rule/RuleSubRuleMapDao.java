package com.legacytojava.message.dao.rule;

import java.util.List;

import com.legacytojava.message.vo.rule.RuleSubRuleMapVo;

public interface RuleSubRuleMapDao {
	public RuleSubRuleMapVo getByPrimaryKey(String ruleName, String subRuleName);
	public List<RuleSubRuleMapVo> getByRuleName(String ruleName);
	public int deleteByPrimaryKey(String ruleName, String subRuleName);
	public int deleteByRuleName(String ruleName);
	public int update(RuleSubRuleMapVo ruleSubRuleMapVo);
	public int insert(RuleSubRuleMapVo ruleSubRuleMapVo);
}
