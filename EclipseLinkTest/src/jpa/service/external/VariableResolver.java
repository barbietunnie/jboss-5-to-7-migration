package jpa.service.external;

import jpa.exception.DataValidationException;

public interface VariableResolver {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process(int addrId) throws DataValidationException;
}
