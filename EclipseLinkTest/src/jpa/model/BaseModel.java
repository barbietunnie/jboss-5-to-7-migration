package jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import jpa.constant.Constants;
import jpa.constant.StatusId;

@MappedSuperclass
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -3737571995910644181L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="Row_Id", updatable=false)
	protected int rowId = 0;

	@Column(name="StatusId", length=1, nullable=false, columnDefinition="char not null")
	private String statusId = StatusId.ACTIVE.getValue();
	@Column(name="UpdtTime", nullable=false)
	@Version
	protected Timestamp updtTime = null;
	@Column(name="UpdtUserId", length=10, nullable=false)
	protected String updtUserId = Constants.DEFAULT_USER_ID;

	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public Timestamp getUpdtTime() {
		return updtTime;
	}
	public void setUpdtTime(Timestamp updtTime) {
		this.updtTime = updtTime;
	}
	public String getUpdtUserId() {
		return updtUserId;
	}
	public void setUpdtUserId(String updtUserId) {
		this.updtUserId = updtUserId;
	}
	public int getRowId() {
		return rowId;
	}
}
