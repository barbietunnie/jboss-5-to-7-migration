package jpa.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class ClientVariablePK implements Serializable {
	private static final long serialVersionUID = 1422523897905980641L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=ClientData.class)
	@JoinColumn(name="ClientDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private ClientData clientData;
	
	@Column(name="VariableName", nullable=false, length=26)
	protected String variableName = "";
	@Column(name="StartTime", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	protected Date startTime = new Date(System.currentTimeMillis());

	public ClientVariablePK() {}
	
	public ClientVariablePK(ClientData clientData, String variableName, java.util.Date startTime) {
		this.clientData = clientData;
		this.variableName = variableName;
		this.startTime = startTime;
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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
}