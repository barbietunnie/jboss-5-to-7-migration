package jpa.constant;

public class XHeaderName {

	//
	// define X-Header names, used to communicate between MailSender and its
	// calling programs when a raw message stream is passed to MailSender.
	// Calling programs should set their values accordingly.
	//
	public static final String XHEADER_MSG_ID = "X-Msg_Id";
	public static final String XHEADER_MSG_REF_ID = "X-MsgRef_Id";
	public static final String XHEADER_USE_SECURE_SMTP = "X-Use_Secure_Smtp"; // Yes/No
	public static final String XHEADER_SAVE_RAW_STREAM = "X-Save_Raw_Stream"; // Yes/No
	public static final String XHEADER_OVERRIDE_TEST_ADDR = "X-Override_Test_Addr"; // Yes/No
	public static final String XHEADER_EMBED_EMAILID = "X-Embed_Email_Id"; // Yes/No
	public static final String XHEADER_RENDER_ID = "X-Render_Id";
	public static final String XHEADER_RULE_NAME = "X-Rule_Name";
	public static final String XHEADER_ORIG_RULE_NAME = "X-Orig_Rule_Name";
	public static final String XHEADER_CLIENT_ID = "X-Client_Id";
	public static final String XHEADER_CUSTOMER_ID = "X-Customer_Id";
	public static final String XHEADER_PRIORITY = "X-Priority";
	public static final String XHEADER_MAILER = "X-Mailer";
	public static final String RETURN_PATH = "Return-Path";

}
