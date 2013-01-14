package com.legacytojava.jbatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import org.apache.log4j.Logger;

import com.legacytojava.message.vo.SocketServerVo;

/**
 * load application processor, and call its process() to perform real tasks.
 */
class SocketThread implements Runnable, java.io.Serializable {
	private static final long serialVersionUID = -8750867597546298468L;
	static final Logger logger = Logger.getLogger(SocketThread.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private final SocketServer sktServer;
	private Processor processor;

	// to be set by SocketServer every time before its run() is called
	private transient Socket mySocket = null;
	// end

	private final JbEventBroker eventBroker;

	// constructor
	SocketThread(SocketServer skt_server) {
		sktServer = skt_server;
		eventBroker = sktServer.eventBroker;
		processor = (Processor) JbMain.getBatchAppContext().getBean(sktServer.processorName);
	}

	/**
	 * set client socket
	 * 
	 * @param socket -
	 *            client socket
	 */
	void setSocket(Socket socket) {
		mySocket = socket;
	}

	/**
	 * process request
	 */
	public void run() {
		if (isDebugEnabled) {
			logger.debug("SocketThread running...");
		}
		Date start_tms = new Date();
		try {
			SocketServerVo socketServerVo = sktServer.socketServerVo;
			int timeout = 0; // default to infinite
			if (socketServerVo != null) {
				timeout = socketServerVo.getSocketTimeout();
				String unit = socketServerVo.getTimeoutUnit();
				if (unit==null) unit = "minute";
				if ("minute".equalsIgnoreCase(unit)) {
					timeout *= 60;
				}
			}
			// set socket time out limit
			mySocket.setSoTimeout(timeout * 60 * 1000); // socket timeout
		}
		catch (java.net.SocketException e) {
			logger.error("run(): Can't set Socket Timeout", e);
		}
		try {
			sktServer.update(MetricsLogger.PROC_INPUT, 1);
			processor.process(mySocket);
		}
		catch (java.io.InterruptedIOException e) {
			boolean AUTOFLASH = true;
			try {
				PrintWriter clientSend = new PrintWriter(mySocket.getOutputStream(), AUTOFLASH);
				clientSend.println("+SOCKET TIMED OUT, BYE");
			}
			catch (IOException ioe) {
				logger.error("run(): Failed to send out TIME OUT BYE.", e);
				/* Exception occurred during process, increase error message count */
				sktServer.update(MetricsLogger.PROC_ERROR, 1);
			}
			logger.error("run(): Socket Timed out", e);
			eventBroker.putException(e);
		}
		catch (InterruptedException e) {
			logger.error("run(): InterruptedException caught", e);
		}
		catch (IOException e) {
			logger.error("run(): Failed I/O", e);
			/* Exception occurred during process, increase error message count */
			sktServer.update(MetricsLogger.PROC_ERROR, 1);
			eventBroker.putException(e);
		}
		catch (Exception e) {
			logger.error("run(): Exception caught", e);
			/* Exception occurred during process, increase error message count */
			sktServer.update(MetricsLogger.PROC_ERROR, 1);
			eventBroker.putException(e);
		}
		finally {
			mySocket = null;
			/* Message processed, update processing time */
			long proc_time = new Date().getTime() - start_tms.getTime();
			sktServer.update(MetricsLogger.PROC_TIME, (int) proc_time);
			logger.info("SocketThread ended. Time spent in milliseconds: " + proc_time);
		}
	} // end of run()
}
