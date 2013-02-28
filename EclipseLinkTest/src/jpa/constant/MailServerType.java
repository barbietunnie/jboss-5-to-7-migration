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
}
