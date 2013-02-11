package jpa.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class RuleSubruleMapPK implements Serializable {
	private static final long serialVersionUID = -2513677830331526726L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=RuleLogic.class)
	@JoinColumn(name="RuleLogicRowId",insertable=true,referencedColumnName="Row_Id",nullable=false)
	private RuleLogic ruleLogic;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=RuleLogic.class)
	@JoinColumn(name="SubruleLogicRowId",insertable=true,referencedColumnName="Row_Id",nullable=false)
	private RuleLogic subruleLogic;

	public RuleSubruleMapPK() {}
	
	public RuleSubruleMapPK(RuleLogic ruleLogic, RuleLogic subRuleLogic) {
		this.ruleLogic = ruleLogic;
		this.subruleLogic = subRuleLogic;
	}

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public RuleLogic getSubruleLogic() {
		return subruleLogic;
	}

	public void setSubruleLogic(RuleLogic subruleLogic) {
		this.subruleLogic = subruleLogic;
	}

}