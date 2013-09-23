package jpa.model.rule;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import jpa.model.SenderData;

@Embeddable
public class RuleActionPK implements Serializable {
	private static final long serialVersionUID = 5992598892836372267L;

	@ManyToOne(targetEntity=RuleLogic.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="RuleLogicRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private RuleLogic ruleLogic;
	
	@Column(nullable=false)
	private int actionSequence = 0;
	@Column(nullable=false)
	private Timestamp startTime;

	@ManyToOne(targetEntity=SenderData.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="SenderDataRowId", insertable=true, updatable=true, referencedColumnName="Row_Id", nullable=true)
	private SenderData senderData;

	public RuleActionPK() {}
	
	public RuleActionPK(RuleLogic ruleLogic, int actionSequence, Timestamp startTime, SenderData senderData) {
		this.ruleLogic = ruleLogic;
		this.actionSequence = actionSequence;
		this.startTime = startTime;
		this.senderData = senderData;
	}

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public int getActionSequence() {
		return actionSequence;
	}

	public void setActionSequence(int actionSequence) {
		this.actionSequence = actionSequence;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}
}