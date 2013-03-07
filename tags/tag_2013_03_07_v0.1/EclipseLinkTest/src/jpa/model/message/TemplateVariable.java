package jpa.model.message;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseVariableModel;

@Entity
@Table(name="template_variable", uniqueConstraints=@UniqueConstraint(columnNames = {"ClientDataRowId", "variableId", "variableName", "startTime"}))
public class TemplateVariable extends BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = -5646384767553614998L;

	@Embedded
	private TemplateVariablePK templateVariablePK;
	
	@Lob
	@Column(name="VariableValue", length=65530, nullable=true)
	private String variableValue = null;

	public TemplateVariable() {}
	
	public TemplateVariablePK getTemplateVariablePK() {
		return templateVariablePK;
	}

	public void setTemplateVariablePK(TemplateVariablePK templateVariablePK) {
		this.templateVariablePK = templateVariablePK;
	}

	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}
