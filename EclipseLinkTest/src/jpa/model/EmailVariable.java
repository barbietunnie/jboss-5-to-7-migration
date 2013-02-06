package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="email_variable")
public class EmailVariable extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 3479703348151037887L;

	@Column(nullable=false, length=26, unique=true)
	private String variableName = "";
	@Column(nullable=false, length=1, columnDefinition="char(1) not null")
	private String variableType = "";
	@Column(nullable=true, length=50)
	private String tableName = null;
	@Column(nullable=true, length=50)
	private String columnName = null;
	@Column(nullable=false, length=1, columnDefinition="boolean not null")
	private boolean isBuiltIn = false;
	@Column(nullable=true, length=255)
	private String defaultValue = null;
	@Column(nullable=true, length=255)
	private String variableQuery = null;
	@Column(nullable=true, length=100)
	private String variableProc = null;

	@Transient
	public static final String SYSTEM_VARIABLE = "S";
	@Transient
	public static final String CUSTOMER_VARIABLE = "C";

	public EmailVariable() {
		// must have a no-argument constructor
	}

	/**
	 * define components for UI
	 */
	public String getVariableQueryShort() {
		if (variableQuery == null || variableQuery.length() <= 40)
			return variableQuery;
		else
			return variableQuery.substring(0,40);
	}
	
	public String getClassNameShort() {
		if (variableProc == null || variableProc.length() <= 20) {
			return variableProc;
		}
		else {
			int lastDot = variableProc.lastIndexOf(".");
			if (lastDot > 0) {
				return variableProc.substring(lastDot);
			}
			else {
				return variableProc.substring(0,20);
			}
		}
	}
	
	public boolean getIsSystemVariable() {
		return SYSTEM_VARIABLE.equals(variableType);
	}
	/** end of UI components */

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String getVariableType() {
		return variableType;
	}

	public void setVariableType(String variableType) {
		this.variableType = variableType;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isBuiltIn() {
		return isBuiltIn;
	}

	public void setBuiltIn(boolean isBuiltIn) {
		this.isBuiltIn = isBuiltIn;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getVariableQuery() {
		return variableQuery;
	}

	public void setVariableQuery(String variableQuery) {
		this.variableQuery = variableQuery;
	}

	public String getVariableProc() {
		return variableProc;
	}

	public void setVariableProc(String variableProc) {
		this.variableProc = variableProc;
	}
}
