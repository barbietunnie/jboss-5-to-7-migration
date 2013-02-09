package jpa.constant;

/** define data type constants for Internet mail */
public enum RuleDataName {
	FROM_ADDR(EmailAddrType.FROM_ADDR.getValue()),
	TO_ADDR(EmailAddrType.TO_ADDR.getValue()),
	REPLYTO_ADDR(EmailAddrType.REPLYTO_ADDR.getValue()),
	CC_ADDR(EmailAddrType.CC_ADDR.getValue()),
	BCC_ADDR(EmailAddrType.BCC_ADDR.getValue()),
	
	SUBJECT(VariableName.SUBJECT),
	BODY(VariableName.BODY),
	MSG_REF_ID(VariableName.MSG_REF_ID),
	RULE_NAME(VariableName.RULE_NAME),
	X_HEADER(VariableName.XHEADER_DATA_NAME),
	RETURN_PATH("ReturnPath"),
	// mailbox properties
	MAILBOX_USER(VariableName.MAILBOX_USER),
	MAILBOX_HOST(VariableName.MAILBOX_HOST),
	// the next two items are not implemented yet
	RFC822(VariableName.RFC822),
	DELIVERY_STATUS(VariableName.DELIVERY_STATUS),
	// define data type constants for Internet email attachments
	MIME_TYPE("MimeType"),
	FILE_NAME("FileName");

//	public static final String[] DATATYPES = 
//		{ FROM_ADDR, TO_ADDR, REPLYTO_ADDR, CC_ADDR, BCC_ADDR,
//			SUBJECT, BODY, MSG_REF_ID, RULE_NAME, MAILBOX_USER, MAILBOX_HOST };

	private String value;
	private RuleDataName(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
