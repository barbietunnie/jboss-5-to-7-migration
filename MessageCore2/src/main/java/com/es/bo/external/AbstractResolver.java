package com.es.bo.external;

import com.es.exception.DataValidationException;

public interface AbstractResolver {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public String process(long addrId) throws DataValidationException;
}
