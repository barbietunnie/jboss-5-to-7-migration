package jpa.constant;

public enum CarrierCode {
	//
	// define carrier code
	//
	SMTPMAIL("S"),
	WEBMAIL("W"),
	READONLY("R");
	
	private String value;
	private CarrierCode(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
