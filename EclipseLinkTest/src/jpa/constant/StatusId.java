package jpa.constant;

public enum StatusId {
	// define general statusId	
	ACTIVE("A"),
	INACTIVE("I"),
	SUSPENDED("S"); // for email address

	private final String code;
	
	private StatusId(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}
