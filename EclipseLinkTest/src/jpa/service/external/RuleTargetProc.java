package jpa.service.external;

import jpa.exception.DataValidationException;

public interface RuleTargetProc {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process() throws DataValidationException;
}
