package jpa.constant;

public enum RuleNameCustom {
	UNATTENDED_MAILBOX("Unattended_Mailbox"),
	OUF_OF_OFFICE_AUTO_REPLY("OutOfOffice_AutoReply"),
	CONTACT_US("Contact_Us"),
	XHEADER_SPAM_SCORE("XHeader_SpamScore"),
	EXECUTABLE_ATTACHMENT("Executable_Attachment"),
	HARD_BOUNCE_WATCHED_MAILBOX("HardBouce_WatchedMailbox"),
	HARD_BPUNCE_NO_FINAL_RCPT("HardBounce_NoFinalRcpt");

	private String value;
	private RuleNameCustom(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
