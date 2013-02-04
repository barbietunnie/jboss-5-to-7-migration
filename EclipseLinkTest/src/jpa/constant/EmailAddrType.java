package jpa.constant;

public enum EmailAddrType {
	FROM_ADDR("From"),
	REPLYTO_ADDR("Replyto"),
	TO_ADDR("To"),
	CC_ADDR("Cc"),
	BCC_ADDR("Bcc"),
	
	FORWARD_ADDR("Forward"),
	FINAL_RCPT_ADDR("FinalRcpt"),
	ORIG_RCPT_ADDR("OrigRcpt");
	
	private String value;
	EmailAddrType(String value) {
		this.value=value;
	}
	public String value() {
		return value;
	}
	@Override public String toString() {
		return value();
	}
	public EmailAddrType fromValue(String value) {
		for (EmailAddrType v : EmailAddrType.values()) {
			if (v.value().equalsIgnoreCase(value)) {
				return v;
			}
		}
		return null;
	}
}
