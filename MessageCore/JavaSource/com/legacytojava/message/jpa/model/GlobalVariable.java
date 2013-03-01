package com.legacytojava.message.jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="GlobalVariable", uniqueConstraints=@UniqueConstraint(columnNames = {"variableName", "startTime"}))
public class GlobalVariable extends BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = 7381275253094081485L;
	
	@Column(name="VariableValue", length=255)
	private String variableValue = null;

	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}
