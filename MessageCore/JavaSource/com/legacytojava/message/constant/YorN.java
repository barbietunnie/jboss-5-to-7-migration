package com.legacytojava.message.constant;

public enum YorN {
	Y("Y"),N("N");

	private final String value;
	private YorN(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}