package com.legacytojava.message.constant;

/*
 * Can't implement until Spring support enum in JdbcTemplate
 */
public enum CarrierCodeType {
	SMTPMAIL_CODE("S"),
	WEBMAIL_CODE("W"),
	READONLY_CODE("R");

	private String value;
	CarrierCodeType(String value) {
		this.value=value;
	}
	public String value() {
		return value;
	}
	@Override public String toString() {
		return value();
	}
	public CarrierCodeType fromValue(String value) {
		for (CarrierCodeType v : CarrierCodeType.values()) {
			if (v.value().equalsIgnoreCase(value)) {
				return v;
			}
		}
		return null;
	}
}
