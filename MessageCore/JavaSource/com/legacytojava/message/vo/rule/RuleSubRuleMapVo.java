package com.legacytojava.message.vo.rule;

import java.io.Serializable;
import com.legacytojava.message.vo.BaseVo;

public class RuleSubRuleMapVo extends BaseVo implements Serializable {

	private static final long serialVersionUID = 5397520412552955484L;
	private String ruleName = "";
	private String subRuleName = "";
	private int subRuleSeq = -1;

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getSubRuleName() {
		return subRuleName;
	}

	public void setSubRuleName(String subRuleName) {
		this.subRuleName = subRuleName;
	}

	public int getSubRuleSeq() {
		return subRuleSeq;
	}

	public void setSubRuleSeq(int subRuleSeq) {
		this.subRuleSeq = subRuleSeq;
	}

}