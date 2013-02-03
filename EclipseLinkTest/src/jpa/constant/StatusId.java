package jpa.constant;

public enum StatusId {
	// define general statusId	
	ACTIVE("A"),
	INACTIVE("I"),
	SUSPENDED("S"); // for email address

	private final String value;
	
	private StatusId(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
