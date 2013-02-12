package jpa.constant;

public enum TableColumnName {

	// address names associated to client table columns
	CUSTOMER_CARE_ADDR("Customer_Care"), // ContactEmail
	SECURITY_DEPT_ADDR("Security_Dept"),
	RMA_DEPT_ADDR("RMA_Dept"),
	SPAM_CONTROL_ADDR("SPAM_Control"),
	VIRUS_CONTROL_ADDR("Virus_Control"),
	CHALLENGE_HANDLER_ADDR("Challenge_Handler");

	private final String value;
	private TableColumnName(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
