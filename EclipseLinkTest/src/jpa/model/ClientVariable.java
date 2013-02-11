package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="client_variable", uniqueConstraints=@UniqueConstraint(columnNames = {"clientDataRowId", "variableName", "startTime"}))
public class ClientVariable extends BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = -5873779791693771806L;

	@Embedded
	private ClientVariablePK clientVariablePK;

	@Column(name="VariableValue", length=2046)
	private String variableValue = null;

	public ClientVariable() {}
	
	public ClientVariablePK getClientVariablePK() {
		return clientVariablePK;
	}
	public void setClientVariablePK(ClientVariablePK clientVariablePK) {
		this.clientVariablePK = clientVariablePK;
	}
	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}
