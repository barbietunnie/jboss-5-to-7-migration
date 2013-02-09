package jpa.constant;

/** define criteria constants for simple rule */
public enum RuleCriteria {
	STARTS_WITH("starts_with"),
	ENDS_WITH("ends_with"),
	CONTAINS("contains"),
	EQUALS("equals"),
	GREATER_THAN("greater_than"),
	LESS_THAN("less_than"),
	VALUED("valued"),
	NOT_VALUED("not_valued"),
	REG_EX("reg_ex");
//	public static final String[] CRITERIAS = 
//	{ STARTS_WITH, ENDS_WITH, CONTAINS, EQUALS, GREATER_THAN, LESS_THAN, VALUED, NOT_VALUED, REG_EX };

	private String value;
	private RuleCriteria(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}

