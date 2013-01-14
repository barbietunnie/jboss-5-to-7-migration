package com.legacytojava.message.ejb.rule;
import java.util.List;

import javax.ejb.Local;

import com.legacytojava.message.vo.rule.RuleVo;

@Local
public interface RulesDataLocal {
	public List<RuleVo> getCurrentRules();
	public RuleVo getRuleByPrimaryKey(String key);
}
