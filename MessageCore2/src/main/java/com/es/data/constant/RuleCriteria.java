package com.es.data.constant;

/** define criteria for simple rule */
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

	private String value;
	private RuleCriteria(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	public static RuleCriteria getByValue(String value) {
		for (RuleCriteria type : RuleCriteria.values()) {
			if (type.getValue().equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("No enum const value constant.RuleCriteria." + value);
	}
}

