package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="action_detail")
public class ActionDetail extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -7004743275045358426L;

	@ManyToOne(targetEntity=MsgDataType.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="ActionPropertyRowId", insertable=true, updatable=true, referencedColumnName="Row_Id", nullable=true)
	private MsgDataType actionProperty;

	@Column(nullable=false, length=26, unique=true)
	private String actionId = "";
	@Column(nullable=true, length=100)
	private String description = null;
	@Column(nullable=false, length=50)
	private String serviceName = "";
	@Column(nullable=true, length=255)
	private String className = null;

	public ActionDetail() {
		// must have a no-argument constructor
	}

	public MsgDataType getActionProperty() {
		return actionProperty;
	}

	public void setActionProperty(MsgDataType actionProperty) {
		this.actionProperty = actionProperty;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
