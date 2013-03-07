package jpa.constant;

public enum ClientType {
	Custom("C"),
	System("S");
	
	private final String value;
	private ClientType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
} 
