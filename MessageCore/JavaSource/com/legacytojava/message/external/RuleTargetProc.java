package com.legacytojava.message.external;

import com.legacytojava.message.exception.DataValidationException;

public interface RuleTargetProc {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process() throws DataValidationException;
}
