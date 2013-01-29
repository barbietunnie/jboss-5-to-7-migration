package com.pra.rave.jpa.util;

public enum RaveTransactionType {
	UPDATE("Update"),
	INSERT("Insert"),
	REMOVE("Remove"),
	UPSERT("Upsert"),
	CONTEXT("Context");
	
	private final String value;
	private RaveTransactionType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public RaveTransactionType getByValue(String value) {
		for (RaveTransactionType type : values()) {
			if (type.getValue().equalsIgnoreCase(value)) {
				return type;
			}
		}
		return null;
	}
}
