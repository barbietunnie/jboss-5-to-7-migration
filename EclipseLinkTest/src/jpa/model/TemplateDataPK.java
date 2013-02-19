package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class TemplateDataPK implements Serializable {
	private static final long serialVersionUID = 3942772930671670809L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=ClientData.class)
	@JoinColumn(name="ClientDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private ClientData clientData;

	@Column(name="TemplateId", nullable=false, length=26)
	private String templateId = "";
	@Column(name="StartTime", nullable=false)
	private Timestamp startTime = new Timestamp(System.currentTimeMillis());

	public TemplateDataPK() {}
	
	public TemplateDataPK(ClientData clientData, String templateId, Timestamp startTime) {
		this.clientData = clientData;
		this.templateId = templateId;
		this.startTime = startTime;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public ClientData getClientData() {
		return clientData;
	}

	public void setClientData(ClientData clientData) {
		this.clientData = clientData;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

}