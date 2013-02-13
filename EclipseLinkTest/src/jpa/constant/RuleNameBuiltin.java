package jpa.constant;

//
// define SMTP Built-in Rule Types
//
public enum RuleNameBuiltin {
	/*
	 * From MessageParser, when no rules were matched
	 */
	GENERIC("Generic", RuleType.SIMPLE, "Non bounce or system could not recognize it"), // default rule name for SMTP Email
	/* 
	 * From RFC Scan routine, rule reassignment, or custom routine
	 */
	HARD_BOUNCE("Hard Bounce", RuleType.ANY, "From RFC Scan Routine, or from postmaster with sub-rules"), // Hard bounce - suspend,notify,close
	SOFT_BOUNCE("Soft Bounce", RuleType.SIMPLE, "Soft bounce, from RFC scan routine"), // Soft bounce - bounce++,close
	MAILBOX_FULL("Mailbox Full", RuleType.ANY, "Mailbox full from postmaster with sub-rules"), // treated as Soft Bounce
	SIZE_TOO_LARGE("Size Too Large", RuleType.SIMPLE, "Message size too large"), // message length exceeded administrative limit, treat as Soft Bounce
	MAIL_BLOCK("Mail Block", RuleType.ALL, "Bounced from Bulk Email Filter"), // message content rejected, treat as Soft Bounce
	SPAM_BLOCK("Spam Block", RuleType.ANY, "Bounced from Spam blocker"), // blocked by SPAM filter, 
	VIRUS_BLOCK("Virus Block", RuleType.ANY, "Bounced from Virus blocker"), // blocked by Virus Scan,
	CHALLENGE_RESPONSE("Challenge Response", RuleType.ANY, "Bounced from Challenge Response"), // human response needed
	AUTO_REPLY("Auto Reply", RuleType.ANY, "Auto reply from email client software"), // automatic response from mail client
	CC_USER("Carbon Copies", RuleType.SIMPLE, "From scan routine, message received as recipient of CC or BCC"), // Mail received from a CC address, drop
	MDN_RECEIPT("MDN Receipt", RuleType.SIMPLE, "From RFC scan, Message Delivery Notification, a positive receipt"), // MDN - read receipt, drop
	UNSUBSCRIBE("Unsubscribe", RuleType.ALL, "Remove from a mailing list"), // remove from mailing list
	SUBSCRIBE("Subscribe", RuleType.ALL, "Subscribe to a mailing list"), // add to mailing list
	/*
	 * From rule reassignment or custom routine
	 */
	CSR_REPLY("CSR Reply", RuleType.SIMPLE, "Called from internal program"), // internal only, reply message from CSR
	RMA_REQUEST("RMA Request", RuleType.SIMPLE, "RMA request, internal only"), // internal only
	BROADCAST("Broadcast", RuleType.SIMPLE, "Called from internal program"), // internal only
	SEND_MAIL("Send Mail", RuleType.SIMPLE, "Called from internal program"); // internal only
	
	private final String value;
	private final RuleType ruleType;
	private final String description;
	private RuleNameBuiltin(String value, RuleType ruleType, String description) {
		this.value = value;
		this.ruleType = ruleType;
		this.description = description;
	}
	
	public String getValue() {
		return value;
	}
	
	public RuleType getRuleType() {
		return ruleType;
	}
	
	public String getDescription() {
		return description;
	}
}