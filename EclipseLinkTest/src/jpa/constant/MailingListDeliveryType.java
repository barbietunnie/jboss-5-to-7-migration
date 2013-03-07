package jpa.constant;

public enum MailingListDeliveryType {

	// define mailing list delivery options
	ALL_ON_LIST("ALL"),
	SUBSCRIBERS_ONLY("SUBSCRIBERS"),
	PROSPECTS_ONLY("PROSPECTS");

	private final String value;
	private MailingListDeliveryType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
