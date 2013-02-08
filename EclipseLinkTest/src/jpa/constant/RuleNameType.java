package jpa.constant;

//
// define SMTP Built-in Rule Types
//
public enum RuleNameType {
	/*
	 * From MessageParser, when no rules were matched
	 */
	GENERIC("generic"), // default rule name for SMTP Email
	/* 
	 * From RFC Scan routine, rule reassignment, or custom routine
	 */
	HARD_BOUNCE("hard bounce"), // Hard bounce - suspend,notify,close
	SOFT_BOUNCE("soft bounce"), // Soft bounce - bounce++,close
	MAILBOX_FULL("mailbox full"), // treated as Soft Bounce
	SIZE_TOO_LARGE("size too large"), // message length exceeded administrative limit, treat as Soft Bounce
	MAIL_BLOCK("mail block"), // message content rejected, treat as Soft Bounce
	SPAM_BLOCK("spam block"), // blocked by SPAM filter, 
	VIRUS_BLOCK("virus block"), // blocked by Virus Scan,
	CHALLENGE_RESPONSE("challenge response"), // human response needed
	AUTO_REPLY("auto reply"), // automatic response from mail client
	CC_USER("carbon copies"), // Mail received from a CC address, drop
	MDN_RECEIPT("mdn receipt"), // MDN - read receipt, drop
	UNSUBSCRIBE("unsubscribe"), // remove from mailing list
	SUBSCRIBE("subscribe"), // add to mailing list
	/*
	 * From rule reassignment or custom routine
	 */
	CSR_REPLY("csr reply"), // internal only, reply message from CSR
	RMA_REQUEST("rma request"), // internal only
	BROADCAST("broadcast"), // internal only
	SEND_MAIL("send mail"); // internal only
	
	private final String value;
	private RuleNameType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}