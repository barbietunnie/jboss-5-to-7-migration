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
@Table(name="rule_element", uniqueConstraints=@UniqueConstraint(columnNames = {"RuleLogicRowId", "elementSequence"}))
public class RuleElement extends BaseModel implements Serializable {
	private static final long serialVersionUID = -4142842697269887792L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="RuleLogicRowId",insertable=true,referencedColumnName="Row_Id",nullable=false)
	private RuleLogic ruleLogic;
	
	@Column(nullable=false)
	private int elementSequence = -1;

	@Column(length=26, nullable=false)
	private String dataName = "";
	@Column(length=50, nullable=true)
	private String headerName = null;
	@Column(length=16, nullable=false)
	private String criteria = "";
	@Column(length=1, nullable=false, columnDefinition="boolean not null")
	private boolean isCaseSensitive = false;
	@Column(length=2000, nullable=true)
	private String targetText = null;
	@Column(length=100, nullable=true)
	private String targetProcName = null;
	@Column(length=8100, nullable=true)
	private String exclusions = null;
	@Column(length=100, nullable=true)
	private String exclListProcName = null;
	@Column(length=5, nullable=false, columnDefinition="char(5)")
	private String delimiter = null;

	public RuleElement() {
		// must have a no-argument constructor
	}

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

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	public void setCaseSensitive(boolean isCaseSensitive) {
		this.isCaseSensitive = isCaseSensitive;
	}

	public String getTargetText() {
		return targetText;
	}

	public void setTargetText(String targetText) {
		this.targetText = targetText;
	}

	public String getTargetProcName() {
		return targetProcName;
	}

	public void setTargetProcName(String targetProcName) {
		this.targetProcName = targetProcName;
	}

	public String getExclusions() {
		return exclusions;
	}

	public void setExclusions(String exclusions) {
		this.exclusions = exclusions;
	}

	public String getExclListProcName() {
		return exclListProcName;
	}

	public void setExclListProcName(String exclListProcName) {
		this.exclListProcName = exclListProcName;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

}