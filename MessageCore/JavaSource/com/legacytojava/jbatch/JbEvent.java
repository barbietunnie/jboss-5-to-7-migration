package com.legacytojava.jbatch;

/**
 * JbEvent class extends EventObject.
 */
public class JbEvent extends java.util.EventObject {
	private static final long serialVersionUID = 2184860285700833160L;
	Exception excep = null;

	/**
	 * create a JbEvent
	 * 
	 * @param source -
	 *            event source
	 */
	public JbEvent(Object source) {
		super(source);
	}

	/**
	 * save an Exception instance
	 * 
	 * @param _excep
	 *            an Exception instance
	 */
	void setException(Exception _excep) {
		excep = _excep;
	}

	/**
	 * get the Exception that triggered the event
	 * 
	 * @return the Exception
	 */
	public Exception getException() {
		return excep;
	}
}