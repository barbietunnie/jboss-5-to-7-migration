package jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embedded;
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

	@Embedded
	private RuleActionPK ruleActionPK;
	
	@ManyToOne(targetEntity=RuleActionDetail.class, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="RuleActionDetailRowId", insertable=true, updatable=true, referencedColumnName="Row_Id", nullable=false)
	private RuleActionDetail ruleActionDetail;

	@Column(nullable=true, length=4054)
	private String fieldValues = null;

	public RuleAction() {
		// must have a no-argument constructor
	}

	public RuleAction(RuleLogic ruleLogic, int actionSequence,
			Timestamp startTime, ClientData clientData,
			RuleActionDetail ruleActionDetail, String fieldValues) {
		ruleActionPK = new RuleActionPK();
		ruleActionPK.setRuleLogic(ruleLogic);
		ruleActionPK.setActionSequence(actionSequence);
		ruleActionPK.setStartTime(startTime);
		ruleActionPK.setClientData(clientData);
		this.ruleActionDetail = ruleActionDetail;
		this.fieldValues = fieldValues;
	}

	public RuleActionPK getRuleActionPK() {
		return ruleActionPK;
	}

	public void setRuleActionPK(RuleActionPK ruleActionPK) {
		this.ruleActionPK = ruleActionPK;
	}

	public RuleActionDetail getRuleActionDetail() {
		return ruleActionDetail;
	}

	public void setRuleActionDetail(RuleActionDetail ruleActionDetail) {
		this.ruleActionDetail = ruleActionDetail;
	}

	public String getFieldValues() {
		return fieldValues;
	}

	public void setFieldValues(String fieldValues) {
		this.fieldValues = fieldValues;
	}
}
