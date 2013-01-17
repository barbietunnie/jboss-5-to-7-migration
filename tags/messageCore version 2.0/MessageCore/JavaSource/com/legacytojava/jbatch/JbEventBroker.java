package com.legacytojava.jbatch;

import java.io.Serializable;
import java.util.Vector;

/**
 * JbEventBroker Class - an event generator
 */
public class JbEventBroker implements Serializable {
	private static final long serialVersionUID = -7493698985735364186L;
	private Vector<JbEventListener> vListeners = new Vector<JbEventListener>();

	/**
	 * put an Exception to trigger an event
	 * 
	 * @param excep
	 */
	public void putException(Exception excep) {
		exceptionAdded(excep);
	}

	/**
	 * put an Error to trigger an event
	 * 
	 * @param error
	 */
	public void putError(Exception error) {
		errorAdded(error);
	}

	/**
	 * add an event listener
	 * 
	 * @param lsnr -
	 *            event listener
	 */
	public synchronized void addAgentEventListener(JbEventListener lsnr) {
		if (vListeners.contains(lsnr))
			return;

		vListeners.addElement(lsnr);
	}

	/**
	 * remove an event listener
	 * 
	 * @param lsnr -
	 *            event listener
	 */
	public synchronized void removeAgentEventListener(JbEventListener lsnr) {
		vListeners.removeElement(lsnr);
	}

	/*
	 * add an Exception listener to list 
	 * @param excep an Exception
	 */
	private void exceptionAdded(Exception excep) {
		Vector<?> vLsnrs;
		synchronized (this) {
			vLsnrs = (Vector<?>) vListeners.clone();
		}

		if (vLsnrs.size() == 0)
			return;

		JbEvent event = new JbEvent(this);
		event.setException(excep);

		for (int i = 0; i < vLsnrs.size(); ++i) {
			JbEventListener listener = (JbEventListener) vLsnrs.elementAt(i);
			listener.exceptionCaught(event);
		}
	}

	/*
	 * add a fatal Exception listener to list 
	 * @param error an Exception
	 */
	private void errorAdded(Exception error) {
		Vector<?> vLsnrs;
		synchronized (this) {
			vLsnrs = (Vector<?>) vListeners.clone();
		}

		if (vLsnrs.size() == 0)
			return;

		JbEvent event = new JbEvent(this);
		event.setException(error);

		for (int i = 0; i < vLsnrs.size(); ++i) {
			JbEventListener listener = (JbEventListener) vLsnrs.elementAt(i);
			listener.errorOccured(event);
		}
	}
}