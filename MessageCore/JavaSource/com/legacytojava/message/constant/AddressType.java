package com.legacytojava.message.constant;

/*
 * Can't implement until Spring support enum in JdbcTemplate
 */
public enum AddressType {
	FROM_ADDR("From"),
	REPLYTO_ADDR("Replyto"),
	TO_ADDR("To"),
	CC_ADDR("Cc"),
	BCC_ADDR("Bcc"),
	
	FORWARD_ADDR("Forward"),
	FINAL_RCPT_ADDR("FinalRcpt"),
	ORIG_RCPT_ADDR("OrigRcpt");
	
	private String value;
	AddressType(String value) {
		this.value=value;
	}
	public String value() {
		return value;
	}
	@Override public String toString() {
		return value();
	}
	public AddressType fromValue(String value) {
		for (AddressType v : AddressType.values()) {
			if (v.value().equalsIgnoreCase(value)) {
				return v;
			}
		}
		return null;
	}
}
