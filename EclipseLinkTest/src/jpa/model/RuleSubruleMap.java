package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="rule_subrule_map", uniqueConstraints=@UniqueConstraint(columnNames = {"RuleLogicRowId", "SubruleLogicRowId"}))
public class RuleSubruleMap extends BaseModel implements Serializable {
	private static final long serialVersionUID = -3976396187608269516L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=RuleLogic.class)
	@JoinColumn(name="RuleLogicRowId",insertable=true,referencedColumnName="Row_Id",nullable=false)
	private RuleLogic ruleLogic;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=RuleLogic.class)
	@JoinColumn(name="SubruleLogicRowId",insertable=true,referencedColumnName="Row_Id",nullable=false)
	private RuleLogic subruleLogic;

	@Column(nullable=false)
	private int subruleSequence = -1;

	public RuleSubruleMap() {
		// must have a no-argument constructor
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

	public int getSubruleSequence() {
		return subruleSequence;
	}

	public void setSubruleSequence(int subruleSequence) {
		this.subruleSequence = subruleSequence;
	}

}