package jpa.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class GlobalVariablePK implements Serializable {
	private static final long serialVersionUID = -4075342089197999829L;

	@Column(name="VariableName", nullable=false, length=26)
	protected String variableName = "";
	@Column(name="StartTime", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	protected Date startTime = new Date(System.currentTimeMillis());

	public GlobalVariablePK() {}
	
	public GlobalVariablePK(String variableName, java.util.Date startTime) {
		this.variableName = variableName;
		this.startTime = startTime;
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