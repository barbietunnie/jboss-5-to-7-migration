package jpa.constant;

/** define rule type constants */
public enum RuleType {
	SIMPLE_RULE("Simple"),
	ALL_RULE("All"),
	ANY_RULE("Any"),
	NONE_RULE("None");
	
	private String value;
	private RuleType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}

