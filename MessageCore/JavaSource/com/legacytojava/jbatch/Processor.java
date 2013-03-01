package com.legacytojava.jbatch;

/**
 * interface to be implemented by application processors
 */
public interface Processor {
	/**
	 * process method, invoked every time a request is received.
	 * 
	 * @param req -
	 *            request
	 * @throws Exception
	 *             if any error
	 */
	void process(Object req) throws Exception;
}