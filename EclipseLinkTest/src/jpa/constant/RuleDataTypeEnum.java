package jpa.constant;

public enum RuleDataTypeEnum {
	EMAIL_ADDRESS("Email Address"),
	TEMPLATE_ID("Template Id"),
	QUEUE_NAME("Queue Name"),
	RULE_NAME("Rule Name");

	private String description;
	private RuleDataTypeEnum(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
}
