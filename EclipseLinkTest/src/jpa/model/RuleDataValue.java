package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="rule_data_value", uniqueConstraints=@UniqueConstraint(columnNames = {"dataType", "dataValue"}))
//@SecondaryTable(name="msg_data_value")
public class RuleDataValue extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -6383445491730691533L;

	// define a RuleDataValue
	@Transient
	public final static String TEMPLATE_ID = "TEMPLATE_ID";
	@Transient
	public final static String EMAIL_ADDRESS = "EMAIL_ADDRESS";

	@Column(nullable=false, length=26)
	private String dataType = "";
	@Column(nullable=false, length=100)
	private String dataValue = "";
	@Column(nullable=true, length=255)
	private String otherProps = null;

	public RuleDataValue() {
		// must have a no-argument constructor
	}
	
	public RuleDataValue(String dataType, String dataValue, String otherProps) {
		this.dataType = dataType;
		this.dataValue = dataValue;
		this.otherProps = otherProps;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataValue() {
		return dataValue;
	}

	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}

	public String getOtherProps() {
		return otherProps;
	}

	public void setOtherProps(String otherProps) {
		this.otherProps = otherProps;
	}
}
