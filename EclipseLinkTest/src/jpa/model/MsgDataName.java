package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="msg_data_name")
public class MsgDataName extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -8077139332207748775L;

	@Column(nullable=false, length=26, unique=true)
	private String dataType = "";
	@Column(nullable=true, length=100)
	private String description = null;

	public MsgDataName() {
		// must have a no-argument constructor
	}
	
	public MsgDataName(String dataType) {
		this.dataType = dataType;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
