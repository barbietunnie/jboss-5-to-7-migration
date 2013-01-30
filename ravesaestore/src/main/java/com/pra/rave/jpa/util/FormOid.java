package com.pra.rave.jpa.util;

public enum FormOid {
	SUBJECT("SUBJECT"),
	IPADMIN_1("IPADMIN_1"),
	ADVERSE_1("ADVERSE_1"),
	AESCR_1("AESCR_1"),
	SRF_1("SRF_1");
	
	private final String value;
	private FormOid(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
