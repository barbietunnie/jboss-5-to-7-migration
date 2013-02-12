package jpa.constant;

public enum TableColumnName {

	// address names associated to client_data table columns
	CUSTOMER_CARE_ADDR("custcareEmail"),
	SECURITY_DEPT_ADDR("securityEmail"),
	RMA_DEPT_ADDR("rmaDeptEmail"),
	SPAM_CONTROL_ADDR("spamCntrlEmail"),
	VIRUS_CONTROL_ADDR("virusCntrlEmail"),
	CHALLENGE_HANDLER_ADDR("chaRspHndlrEmail");

	private final String value;
	private TableColumnName(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
