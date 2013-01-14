package com.legacytojava.message.constant;

//
// define SMTP Built-in Rule Types
//
public enum RuleNameType {
	/*
	 * From MessageParser, when no rules were matched
	 */
	GENERIC, // default rule name for SMTP Email
	/* 
	 * From RFC Scan routine, rule reassignment, or custom routine
	 */
	HARD_BOUNCE, // Hard bounce - suspend,notify,close
	SOFT_BOUNCE, // Soft bounce - bounce++,close
	MAILBOX_FULL, // treated as Soft Bounce
	MSGSIZE_TOO_BIG, // message length exceeded administrative limit, treat as Soft Bounce
	MAIL_BLOCK, // message content rejected, treat as Soft Bounce
	SPAM_BLOCK, // blocked by SPAM filter, 
	VIRUS_BLOCK, // blocked by Virus Scan,
	CHALLENGE_RESPONSE, // human response needed
	AUTO_REPLY, // automatic response from mail client
	CC_USER, // Mail received from a CC address, drop
	MDN_RECEIPT, // MDN - read receipt, drop
	UNSUBSCRIBE, // remove from mailing list
	SUBSCRIBE, // add to mailing list
	/*
	 * From rule reassignment or custom routine
	 */
	CSR_REPLY, // internal only, reply message from CSR
	RMA_REQUEST, // internal only
	BROADCAST, // internal only
	SEND_MAIL; // internal only
}