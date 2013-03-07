package jpa.constant;

public enum MailingListDeliveryType {

	// define mailing list delivery options
	ALL_ON_LIST("ALL"),
	CUSTOMERS_ONLY("CUSTOMERS"),
	PROSPECTS_ONLY("PROSPECTS");

	private final String value;
	private MailingListDeliveryType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
