package jpa.constant;

/*
 * define built-in sub-rule names
 */
public enum RuleNameSubrule {
	HardBounce_Subj_Match("HardBounce_Subj_Match", RuleType.ANY, RuleCategory.MAIN_RULE, "Sub rule for hard bounces from postmaster"),
	HardBounce_Body_Match("HardBounce_Body_Match", RuleType.ANY, RuleCategory.MAIN_RULE, "Sub rule for hard bounces from postmaster"),
	MailboxFull_Body_Match("MailboxFull_Body_Match", RuleType.ALL, RuleCategory.MAIN_RULE, "Sub rule for mailbox full"),
	SpamBlock_Body_Match("SpamBlock_Body_Match", RuleType.ANY, RuleCategory.MAIN_RULE, "Sub rule for spam block"),
	VirusBlock_Body_Match("VirusBlock_Body_Match", RuleType.ANY, RuleCategory.MAIN_RULE, "Sub rule for virus block"),
	ChalResp_Body_Match("ChalResp_Body_Match", RuleType.ANY, RuleCategory.MAIN_RULE, "Sub rule for challenge response");
	
	private String value;
	private RuleType ruleType;
	private RuleCategory ruleCategory;
	private String description;
	private RuleNameSubrule(String value, RuleType ruleType, RuleCategory ruleCategory, String description) {
		this.value = value;
		this.ruleType = ruleType;
		this.ruleCategory = ruleCategory;
		this.description = description;
	}
	
	public String getValue() {
		return value;
	}
	
	public RuleType getRuleType() {
		return ruleType;
	}
	
	public RuleCategory getRuleCategory() {
		return ruleCategory;
	}
	
	public String getDescription() {
		return description;
	}
}
