package com.legacytojava.message.init;

import java.io.Serializable;

public class VariableDto implements Serializable {
	private static final long serialVersionUID = -6728465846945527769L;
	private String name;
	private String value;
	
	public VariableDto() {
		name = null;
		value = null;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
