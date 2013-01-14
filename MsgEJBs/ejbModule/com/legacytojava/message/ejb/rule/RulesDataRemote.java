package com.legacytojava.message.ejb.rule;
import java.util.List;

import javax.ejb.Remote;

import com.legacytojava.message.vo.rule.RuleVo;

@Remote
public interface RulesDataRemote {
	public List<RuleVo> getCurrentRules();
	public RuleVo getRuleByPrimaryKey(String key);
}
