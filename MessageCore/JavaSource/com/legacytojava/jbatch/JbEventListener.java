package com.legacytojava.jbatch;

/**
 * JbEvent Listener Interface
 */
public interface JbEventListener extends java.util.EventListener {
	/**
	 * invoked when an Exception occurs
	 * 
	 * @param e
	 *            an JbEvent object
	 */
	void exceptionCaught(JbEvent e);

	/**
	 * invoked when a fatal Exception occurs
	 * 
	 * @param e
	 *            an JbEvent object
	 */
	void errorOccured(JbEvent e);
}