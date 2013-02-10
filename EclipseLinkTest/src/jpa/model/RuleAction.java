package jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="rule_action", 
	uniqueConstraints=@UniqueConstraint(columnNames = {"RuleLogicRowId", "actionSequence", "startTime", "ClientDataRowId"}))
public class RuleAction extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 6097614369008930898L;

	@ManyToOne(targetEntity=RuleLogic.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="RuleLogicRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private RuleLogic ruleLogic;

	@ManyToOne(targetEntity=ActionDetail.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="ActionDetailRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private ActionDetail actionDetail;

	@ManyToOne(targetEntity=ClientData.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="ClientDataRowId", insertable=true, updatable=true, referencedColumnName="Row_Id", nullable=true)
	private ClientData clientData;

	@Column(nullable=false)
	private int actionSequence = 0;
	@Column(nullable=false)
	private Timestamp startTime;
	@Column(nullable=true, length=4054)
	private String fieldValues = null;

	public RuleAction() {
		// must have a no-argument constructor
	}

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public ActionDetail getActionDetail() {
		return actionDetail;
	}

	public void setActionDetail(ActionDetail actionDetail) {
		this.actionDetail = actionDetail;
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
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

	public String getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(String fieldValues) {
		this.fieldValues = fieldValues;
	}
}
