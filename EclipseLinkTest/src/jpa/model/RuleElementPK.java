package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class RuleElementPK implements Serializable {
	private static final long serialVersionUID = 4082282320803459127L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="RuleLogicRowId",insertable=true,referencedColumnName="Row_Id",nullable=false)
	private RuleLogic ruleLogic;
	
	@Column(name="elementSequence", nullable=false)
	private int elementSequence = -1;

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public int getElementSequence() {
		return elementSequence;
	}

	public void setElementSequence(int elementSequence) {
		this.elementSequence = elementSequence;
	}
}