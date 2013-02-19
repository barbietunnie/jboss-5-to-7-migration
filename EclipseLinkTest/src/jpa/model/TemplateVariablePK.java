package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class TemplateVariablePK implements Serializable {
	private static final long serialVersionUID = 7193883507541681000L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=TemplateData.class)
	@JoinColumn(name="TemplateDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private TemplateData templateData;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=ClientData.class)
	@JoinColumn(name="ClientDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private ClientData clientData;
	
	@Column(name="VariableName", nullable=false, length=26)
	protected String variableName = "";
	@Column(name="StartTime", nullable=false)
	protected Timestamp startTime = new Timestamp(System.currentTimeMillis());

	public TemplateVariablePK() {}
	
	public TemplateVariablePK(TemplateData templateData, ClientData clientData, String variableName, Timestamp startTime) {
		this.templateData = templateData;
		this.clientData = clientData;
		this.variableName = variableName;
		this.startTime = startTime;
	}

	public TemplateData getTemplateData() {
		return templateData;
	}

	public void setTemplateData(TemplateData templateData) {
		this.templateData = templateData;
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
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