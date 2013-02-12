package jpa.constant;

/*
 * define custom rule names
 */
public enum RuleNameOther {
	UNATTENDED_MAILBOX("Unattended_Mailbox",false),
	OUF_OF_OFFICE_AUTO_REPLY("OutOfOffice_AutoReply",false),
	CONTACT_US("Contact_Us",false),
	XHEADER_SPAM_SCORE("XHeader_SpamScore",false),
	EXECUTABLE_ATTACHMENT("Executable_Attachment",false),
	HARD_BOUNCE_WATCHED_MAILBOX("HardBouce_WatchedMailbox",false),
	HARD_BPUNCE_NO_FINAL_RCPT("HardBounce_NoFinalRcpt",false),

	HardBounce_Subj_Match("HardBounce_Subj_Match",true),
	HardBounce_Body_Match("HardBounce_Body_Match",true),
	MailboxFull_Body_Match("MailboxFull_Body_Match",true),
	SpamBlock_Body_Match("SpamBlock_Body_Match",true),
	VirusBlock_Body_Match("VirusBlock_Body_Match",true),
	ChalResp_Body_Match("ChalResp_Body_Match",true);
	
	private String value;
	private boolean isSubrule;
	private RuleNameOther(String value, boolean isSubrule) {
		this.value = value;
		this.isSubrule = isSubrule;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isSubrule() {
		return isSubrule;
	}
}
