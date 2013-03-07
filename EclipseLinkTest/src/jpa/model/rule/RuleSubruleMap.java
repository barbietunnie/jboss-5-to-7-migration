package jpa.model.rule;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

@Entity
@Table(name="rule_subrule_map", uniqueConstraints=@UniqueConstraint(columnNames = {"RuleLogicRowId", "SubruleLogicRowId"}))
public class RuleSubruleMap extends BaseModel implements Serializable {
	private static final long serialVersionUID = -3976396187608269516L;

	@Embedded
	private RuleSubruleMapPK ruleSubruleMapPK;

	@Column(nullable=false)
	private int subruleSequence = -1;

	public RuleSubruleMap() {
		// must have a no-argument constructor
	}

	public RuleSubruleMapPK getRuleSubruleMapPK() {
		return ruleSubruleMapPK;
	}

	public void setRuleSubruleMapPK(RuleSubruleMapPK ruleSubruleMapPK) {
		this.ruleSubruleMapPK = ruleSubruleMapPK;
	}

	public int getSubruleSequence() {
		return subruleSequence;
	}

	public void setSubruleSequence(int subruleSequence) {
		this.subruleSequence = subruleSequence;
	}

}