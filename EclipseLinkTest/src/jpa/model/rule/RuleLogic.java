package jpa.model.rule;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.eclipse.persistence.annotations.CascadeOnDelete;

import jpa.constant.RuleCategory;
import jpa.model.BaseModel;

@Entity
@Table(name="rule_logic", uniqueConstraints=@UniqueConstraint(columnNames = {"ruleName"}))
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="RuleLogictWithCount",
		entities={
		 @EntityResult(entityClass=RuleLogic.class),
	  	},
	  	columns={
		 @ColumnResult(name="subruleCount"),
	  	}),
	})
public class RuleLogic extends BaseModel implements Serializable {
	private static final long serialVersionUID = -2269909582844476550L;

	@Transient
	public static final String MAPPING_RULE_LOGIC_WITH_COUNT = "RuleLogictWithCount";

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="ruleElementPK.ruleLogic", orphanRemoval=true)
	@OrderBy
	@CascadeOnDelete
	private List<RuleElement> ruleElements;
	
	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="ruleSubruleMapPK.ruleLogic", orphanRemoval=true)
	@CascadeOnDelete
	private List<RuleSubruleMap> ruleSubruleMaps;
	
	@Column(nullable=false, length=26)
	private String ruleName = "";
	@Column(nullable=false)
	private int evalSequence = -1;

	@Column(length=8, nullable=false)
	private String ruleType = "";
	@Column(nullable=false)
	private Timestamp startTime;
	@Column(length=8, nullable=false)
	private String mailType = "";
	@Column(length=1, nullable=false, columnDefinition="char(1)")
	private String ruleCategory = RuleCategory.MAIN_RULE.getValue();
	@Column(nullable=false, columnDefinition="boolean not null")
	private boolean isSubrule = false;
	@Column(nullable=false, columnDefinition="boolean not null")
	private boolean isBuiltinRule = false;
	@Column(length=255, nullable=true)
	private String description = null;
	
	@Transient
	private String origRuleName = null;
	@Transient
	private int origRuleSeq = -1;

	/** Define properties for UI components */
	public String getIsSubRuleDesc() {
		if (isSubrule()) {
			return "SubRule";
		}
		else if (ruleSubruleMaps != null && ruleSubruleMaps.size() > 0) {
			return "Edit";
		}
		else {
			return "Add";
		}
	}
	public String getRuleCategoryDesc() {
		if (RuleCategory.PRE_RULE.getValue().equalsIgnoreCase(getRuleCategory())) {
			return "Pre Scan";
		}
		else if (RuleCategory.POST_RULE.getValue().equalsIgnoreCase(getRuleCategory())) {
			return "Post Scan";
		}
		else {
			return "Main";
		}
	}
	/** End of UI properties */

	public RuleLogic() {
		// must have a no-argument constructor
	}

	public List<RuleElement> getRuleElements() {
		return ruleElements;
	}

	public void setRuleElements(List<RuleElement> ruleElements) {
		this.ruleElements = ruleElements;
	}

	public List<RuleSubruleMap> getRuleSubruleMaps() {
		return ruleSubruleMaps;
	}

	public void setRuleSubruleMaps(List<RuleSubruleMap> ruleSubruleMaps) {
		this.ruleSubruleMaps = ruleSubruleMaps;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public int getEvalSequence() {
		return evalSequence;
	}

	public void setEvalSequence(int evalSequence) {
		this.evalSequence = evalSequence;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public String getMailType() {
		return mailType;
	}

	public void setMailType(String mailType) {
		this.mailType = mailType;
	}

	public String getRuleCategory() {
		return ruleCategory;
	}

	public void setRuleCategory(String ruleCategory) {
		this.ruleCategory = ruleCategory;
	}

	public boolean isSubrule() {
		return isSubrule;
	}

	public void setSubrule(boolean isSubrule) {
		this.isSubrule = isSubrule;
	}

	public boolean isBuiltinRule() {
		return isBuiltinRule;
	}

	public void setBuiltinRule(boolean isBuiltinRule) {
		this.isBuiltinRule = isBuiltinRule;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setOrigRuleName(String origRuleName) {
		this.origRuleName = origRuleName;
	}

	public int getOrigRuleSeq() {
		return origRuleSeq;
	}

	public void setOrigRuleSeq(int origRuleSeq) {
		this.origRuleSeq = origRuleSeq;
	}

}