package jpa.constant;

/*
 * define custom rule names
 */
public enum RuleNameCustom {
	UNATTENDED_MAILBOX("Unattended_Mailbox", RuleType.ALL, RuleCategory.PRE_RULE, "Simply get rid of the messages from the mailbox."),
	OUF_OF_OFFICE_AUTO_REPLY("OutOfOffice_AutoReply", RuleType.ALL, RuleCategory.MAIN_RULE, "Ouf of the office auto reply"),
	CONTACT_US("Contact_Us", RuleType.ALL, RuleCategory.MAIN_RULE, "Contact Us Form submitted from web site"),
	XHEADER_SPAM_SCORE("XHeader_SpamScore", RuleType.SIMPLE, RuleCategory.MAIN_RULE, "Examine x-headers for SPAM score."),
	EXECUTABLE_ATTACHMENT("Executable_Attachment", RuleType.ALL, RuleCategory.MAIN_RULE, "Emails with executable attachment file(s)"),
	HARD_BOUNCE_WATCHED_MAILBOX("HardBouce_WatchedMailbox", RuleType.ALL, RuleCategory.POST_RULE, "Post rule for hard bounced emails."),
	HARD_BPUNCE_NO_FINAL_RCPT("HardBounce_NoFinalRcpt", RuleType.ALL, RuleCategory.POST_RULE, "Post rule for hard bounces without final recipient.");
	
	private String value;
	private RuleType ruleType;
	private RuleCategory ruleCategory;
	private String description;
	private RuleNameCustom(String value, RuleType ruleType, RuleCategory ruleCategory, String description) {
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
