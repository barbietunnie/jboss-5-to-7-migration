package com.es.data.constant;

public enum MailingListDeliveryType {

	// define mailing list delivery options
	ALL_ON_LIST("All"),
	SUBSCRIBERS_ONLY("Subr"),
	PROSPECTS_ONLY("Prsp");

	private final String value;
	private MailingListDeliveryType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
