package jpa.constant;

/** define rule type constants */
public enum RuleType {
	SIMPLE("Simple"),
	ALL("All"),
	ANY("Any"),
	NONE("None");
	
	private String value;
	private RuleType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}

