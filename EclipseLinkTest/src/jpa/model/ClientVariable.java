package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="client_variable", uniqueConstraints=@UniqueConstraint(columnNames = {"clientRowId", "variableName", "startTime"}))
public class ClientVariable extends BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = -5873779791693771806L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="ClientRowId", referencedColumnName="Row_Id", columnDefinition="int")
	private ClientData clientData;

	@Column(name="VariableValue", columnDefinition="text")
	private String variableValue = null;

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}
	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}
