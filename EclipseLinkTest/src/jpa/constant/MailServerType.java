package jpa.constant;

public enum MailServerType {

	// define mail server type
	SMTP("smtp"),
	SMTPS("smtps"),
	EXCHANGE("exch");

	private final String value;
	private MailServerType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static MailServerType getByValue(String value) {
		for (MailServerType t : MailServerType.values()) {
			if (t.getValue().equalsIgnoreCase(value)) {
				return t;
			}
		}
		return MailServerType.SMTP; // default to SMTP
	}
 }
