package jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.beanutils.converters.SqlTimeConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;

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

	@Column(name="StatusId", length=1, nullable=false, columnDefinition="char not null default 'A'")
	private String statusId = StatusId.ACTIVE.getValue();
	@Column(name="UpdtTime", nullable=false)
	//@Version // revisit until JPA knows transaction boundary with Optimistic Locking
	protected Timestamp updtTime = new Timestamp(System.currentTimeMillis());
	@Column(name="UpdtUserId", length=10, nullable=false)
	protected String updtUserId = Constants.DEFAULT_USER_ID;
	
	/* Define transient fields for UI application */
	@Transient
	protected boolean markedForDeletion = false;
	@Transient
	protected boolean markedForEdition = false;

	public boolean isMarkedForDeletion() {
		return markedForDeletion;
	}
	public void setMarkedForDeletion(boolean markedForDeletion) {
		this.markedForDeletion = markedForDeletion;
	}
	public boolean isMarkedForEdition() {
		return markedForEdition;
	}
	public void setMarkedForEdition(boolean markedForEdition) {
		this.markedForEdition = markedForEdition;
	}
	
	public void copyPropertiesTo(BaseModel dest) {
		SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
		ConvertUtils.register(converter1, java.sql.Timestamp.class);
		SqlDateConverter converter2 = new SqlDateConverter(null);
		ConvertUtils.register(converter2, java.sql.Date.class);
		SqlTimeConverter converter3 = new SqlTimeConverter(null);
		ConvertUtils.register(converter3, java.sql.Time.class);
		try {
			BeanUtils.copyProperties(dest, this);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/* end of UI */

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
