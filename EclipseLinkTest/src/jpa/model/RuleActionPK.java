package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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

	@ManyToOne(targetEntity=ClientData.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="ClientDataRowId", insertable=true, updatable=true, referencedColumnName="Row_Id", nullable=true)
	private ClientData clientData;

	public RuleActionPK() {}
	
	public RuleActionPK(RuleLogic ruleLogic, int actionSequence, Timestamp startTime, ClientData clientData) {
		this.ruleLogic = ruleLogic;
		this.actionSequence = actionSequence;
		this.startTime = startTime;
		this.clientData = clientData;
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

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}
}