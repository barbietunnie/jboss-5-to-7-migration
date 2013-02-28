package jpa.model.message;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import jpa.model.ClientData;

@Embeddable
public class TemplateVariablePK implements Serializable {
	private static final long serialVersionUID = 7193883507541681000L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=ClientData.class)
	@JoinColumn(name="ClientDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private ClientData clientData;
	
	@Column(name="VariableId", nullable=false, length=26)
	private String variableId = "";
	@Column(name="VariableName", nullable=false, length=26)
	protected String variableName = "";
	@Column(name="StartTime", nullable=false)
	protected Timestamp startTime = new Timestamp(System.currentTimeMillis());

	public TemplateVariablePK() {}
	
	public TemplateVariablePK(ClientData clientData, String variableId, String variableName, Timestamp startTime) {
		this.clientData = clientData;
		this.variableId = variableId;
		this.variableName = variableName;
		this.startTime = startTime;
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}
	public String getVariableId() {
		return variableId;
	}

	public void setVariableId(String variableId) {
		this.variableId = variableId;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
}