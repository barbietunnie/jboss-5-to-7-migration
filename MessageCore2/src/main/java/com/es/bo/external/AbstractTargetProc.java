package com.es.bo.external;

import com.es.exception.DataValidationException;

public interface AbstractTargetProc {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process() throws DataValidationException;
}
