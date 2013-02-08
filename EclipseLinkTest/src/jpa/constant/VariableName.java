package jpa.constant;

public class VariableName {
	//
	// define variable names
	//
	
	public static enum LIST_VARIABLE_NAME {
		MailingListAddress, MailingListId, MailingListName, SubscriberAddress, SubscriberAddressId, BroadcastMsgId
	}
	// content names
	public final static String SUBJECT = "Subject";
	public final static String BODY = "Body";
	// X-Header data type
	public static final String XHEADER_DATA_NAME = "X-Header";
	// template names
	public final static String SUBJECT_TEMPLATE = "SubjTemplate";
	public final static String BODY_TEMPLATE = "BodyTemplate";
	// field names for String fields
	public final static String RULE_NAME = "RuleName";
	public final static String CARRIER_CODE = "CarrierCode";
	public final static String MAILBOX_HOST = "MailboxHost";
	public final static String MAILBOX_USER = "MailboxUser";
	public final static String MAILBOX_NAME = "MailboxName";
	public final static String FOLDER_NAME = "FolderName";
	public final static String CLIENT_ID = "ClientId";
	public final static String CUSTOMER_ID = "CustId";
	public final static String TO_PLAIN_TEXT = "ToPlainText"; // "yes" or "no"
	// field names for numeric fields (Long)
	public final static String MSG_ID = "MsgId";
	public final static String MSG_REF_ID = "MsgRefId";
	// email property names
	public final static String PRIORITY = "Priority";
	public final static String SEND_DATE = "SendDate";
	public final static String RFC822 = "Rfc822";
	public final static String DELIVERY_REPORT = "Report";
	public static final String DELIVERY_STATUS = "DeliveryStatus";

}
