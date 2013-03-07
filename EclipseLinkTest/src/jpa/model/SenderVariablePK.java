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
public class SenderVariablePK implements Serializable {
	private static final long serialVersionUID = 1422523897905980641L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=SenderData.class)
	@JoinColumn(name="SenderDataRowId", insertable=true, referencedColumnName="Row_Id", nullable=false)
	private SenderData senderData;
	
	@Column(name="VariableName", nullable=false, length=26)
	protected String variableName = "";
	@Column(name="StartTime", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	protected Date startTime = new Date(System.currentTimeMillis());

	public SenderVariablePK() {}
	
	public SenderVariablePK(SenderData senderData, String variableName, java.util.Date startTime) {
		this.senderData = senderData;
		this.variableName = variableName;
		this.startTime = startTime;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
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