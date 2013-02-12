package jpa.constant;

//
// define SMTP Built-in Rule Types
//
public enum RuleNameType {
	/*
	 * From MessageParser, when no rules were matched
	 */
	GENERIC("Generic"), // default rule name for SMTP Email
	/* 
	 * From RFC Scan routine, rule reassignment, or custom routine
	 */
	HARD_BOUNCE("Hard Bounce"), // Hard bounce - suspend,notify,close
	SOFT_BOUNCE("Soft Bounce"), // Soft bounce - bounce++,close
	MAILBOX_FULL("Mailbox Full"), // treated as Soft Bounce
	SIZE_TOO_LARGE("Size Too Large"), // message length exceeded administrative limit, treat as Soft Bounce
	MAIL_BLOCK("Mail Block"), // message content rejected, treat as Soft Bounce
	SPAM_BLOCK("Spam Block"), // blocked by SPAM filter, 
	VIRUS_BLOCK("Virus Block"), // blocked by Virus Scan,
	CHALLENGE_RESPONSE("Challenge Response"), // human response needed
	AUTO_REPLY("Auto Reply"), // automatic response from mail client
	CC_USER("Carbon Copies"), // Mail received from a CC address, drop
	MDN_RECEIPT("MDN Receipt"), // MDN - read receipt, drop
	UNSUBSCRIBE("Unsubscribe"), // remove from mailing list
	SUBSCRIBE("Subscribe"), // add to mailing list
	/*
	 * From rule reassignment or custom routine
	 */
	CSR_REPLY("CSR Reply"), // internal only, reply message from CSR
	RMA_REQUEST("RMA Request"), // internal only
	BROADCAST("Broadcast"), // internal only
	SEND_MAIL("Send Mail"); // internal only
	
	private final String value;
	private RuleNameType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}