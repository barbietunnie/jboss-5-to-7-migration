package jpa.constant;

public enum XHeaderName {
	//
	// define X-Header names, used to communicate between MailSender and its
	// calling programs when a raw message stream is passed to MailSender.
	// Calling programs should set their values accordingly.
	//
	MSG_ID("X-Msg_Id"),
	MSG_REF_ID("X-MsgRef_Id"),
	USE_SECURE_SMTP("X-Use_Secure_Smtp"), // Yes/No
	SAVE_RAW_STREAM("X-Save_Raw_Stream"), // Yes/No
	OVERRIDE_TEST_ADDR("X-Override_Test_Addr"), // Yes/No
	EMBED_EMAILID("X-Embed_Email_Id"), // Yes/No
	RENDER_ID("X-Render_Id"),
	RULE_NAME("X-Rule_Name"),
	ORIG_RULE_NAME("X-Orig_Rule_Name"),
	CLIENT_ID("X-Client_Id"),
	CUSTOMER_ID("X-Customer_Id"),
	PRIORITY("X-Priority"),
	MAILER("X-Mailer"),
	RETURN_PATH("Return-Path");

	private final String value;
	private XHeaderName(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
