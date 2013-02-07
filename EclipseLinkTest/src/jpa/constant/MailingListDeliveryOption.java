package jpa.constant;

public enum MailingListDeliveryOption {

	// define mailing list delivery options
	ALL_ON_LIST("ALL"),
	CUSTOMERS_ONLY("CUSTOMERS"),
	PROSPECTS_ONLY("PROSPECTS");

	private final String value;
	private MailingListDeliveryOption(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
