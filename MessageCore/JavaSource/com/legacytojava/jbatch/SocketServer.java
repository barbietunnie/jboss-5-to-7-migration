package com.legacytojava.jbatch;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.legacytojava.message.vo.SocketServerVo;

/**
 * Socket server is responsible of accepting socket requests and dispatch the
 * requests to SocketThread instance for processing.
 * 
 * <pre>
 * 1) create a Socket server based on socket server properties
 * 2) create a thread pool
 * 3) call run() method to perform following:
 * 3.1) listen to the Server Socket
 * 3.2) create a worker
 * 3.3) pass client socket to the worker
 * 3.4) the worker execute the thread
 * 4) repeat step 3.1 to 3.4 until shut down request or a Exception is received
 * 4.1) shut down all threads and clean up the thread pool
 * 4.2) inform JbMain that the server is stopped
 * </pre>
 */
class SocketServer extends JbThread implements java.io.Serializable, JbEventListener {
	private static final long serialVersionUID = 1049584640041154018L;
	protected static final Logger logger = Logger.getLogger(SocketServer.class);

	final SocketServerVo socketServerVo;
	final JbEventBroker eventBroker;
	protected final String processorName;
	private final int SERVER_PORT;
	transient ServerSocket serverSocket = null;
	private int socketTimeout = 0;

	public SocketServerVo getSocketServerVo() {
		return socketServerVo;
	}

	/**
	 * create a SocketServer instance
	 * 
	 * @param props -
	 *            properties
	 * @throws IOException 
	 */
	SocketServer(SocketServerVo socketServerVo) throws IOException {
		super(socketServerVo, JbMain.SOCKET_SVR_TYPE);
		this.socketServerVo = socketServerVo;
		eventBroker = new JbEventBroker();
		eventBroker.addAgentEventListener(this);

		processorName = socketServerVo.getProcessorName();
		if (processorName == null) {
			throw new IllegalArgumentException("processor_name must be defined in socketProperties");
		}
		SERVER_PORT = socketServerVo.getSocketPort();
		MAX_CLIENTS = socketServerVo.getConnections();

		socketTimeout = socketServerVo.getServerTimeout();
		String unit = socketServerVo.getTimeoutUnit();
		if (unit==null) unit = "minute";
		if ("minute".equalsIgnoreCase(unit)) {
			socketTimeout *= 60;
		}
		MSG2PROC = socketServerVo.getMessageCount();
		init();
		logger.info("Created Socket Server for port " + SERVER_PORT + ", number of connections "
				+ MAX_CLIENTS);
	}

	public static void main(String[] args) {
		SocketServer server = (SocketServer) JbMain.getBatchAppContext().getBean("socketServer");
		try {
			server.start();
			server.join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * initialize ServerSocket
	 * @throws IOException 
	 */
	final void init() throws IOException {
		// create a listening socket
		serverSocket = new ServerSocket(SERVER_PORT, MAX_CLIENTS);

		logger.info("Creating Socket Threads, # of Threads: " + MAX_CLIENTS);
		// create SocketThread pool
		// Have the socketThread run at a slightly lower priority so that new
		// requests are handled with higher priority than in-progress requests.
		jbPool = Executors.newFixedThreadPool(MAX_CLIENTS);
	}

	/**
	 * perform tasks
	 */
	public void run() {
		logger.info("Entering SocketServer.run() method...");
		try {
			serverSocket.setSoTimeout(socketTimeout * 1000);
			// server socket timeout
		}
		catch (SocketException e) {
			logger.error("Failed to set Socket Timeout value.");
		}
		boolean normal_exit = false;
		ThreadPoolExecutor tPool = (ThreadPoolExecutor) jbPool;
		while (keepRunning) {
			Thread.yield(); // give other threads chance to run
			// for test only
			if (MSG2PROC > 0 && getMetricsLogger().getMetricsData().getInputCount() > MSG2PROC) {
				normal_exit = true;
				break;
			}
			// end
			try {
				SocketThread sktThread = new SocketThread(this);
				// accept a new client request
				Socket clientSocket = serverSocket.accept();

				InetAddress clientAddr = clientSocket.getInetAddress();
				String clientFrom = clientAddr.toString();

				if (isDebugEnabled) {
					logger.debug("Client address: " + clientFrom);
				}
				/* only process local requests */
				// if (clientFrom.indexOf("127.0.0.1")>=0 ||
				// clientFrom.indexOf("localhost")>=0)
				{
					// create a new handler
					sktThread.setSocket(clientSocket);
					jbPool.execute(sktThread);
					update(MetricsLogger.PROC_WORKER, tPool.getPoolSize());
				}
			}
			catch (InterruptedIOException e) { // timeout
				logger.error("ServerSocket.accept() timed out, communication abandoned.");
			}
			catch (IOException e) {
				logger.error("IOException caught during serving socket", e);
				update(MetricsLogger.PROC_ERROR, 1);
			}
			catch (Exception e) {
				logger.error("Throwable caught during serving socket", e);
				update(MetricsLogger.PROC_ERROR, 1);
			}
			catch (Throwable e) {
				logger.error("Throwable caught during serving socket", e);
				update(MetricsLogger.PROC_ERROR, 1);
				keepRunning = false; // really bad thing happened
			}
		}
		// shutdown thread pool
		shutDownPool();
		try {
			// make sure to close the server socket
			serverSocket.close();
		}
		catch (IOException e) {
			logger.error("Failed Socket I/O: ", e);
		}

		if (normal_exit) {
			JbMain.getInstance().serverExited(this);
		}
		else {
			// recycle the server
			JbMain.getInstance().serverAborted(this, socketServerVo); 
		}
		logger.info("SocketServer.run() ended.");
	}

	/**
	 * implement JbEventListener methods
	 * 
	 * @param e -
	 *            event
	 */
	public void exceptionCaught(JbEvent e) {
		Exception excep = e.getException();
		logger.error("EventListener: Exception caught by SocketThread. " + excep);
		if (excep instanceof InterruptedIOException) {
			// ignore, recycle the thread
		}
		else if (excep instanceof IOException) {
			// TODO: determine severity and perform actions, recycle the thread
			// for now
			JbMain.getEventAlert().issueExcepAlert(JbMain.SOCKET_SVR,
					"SocketServer: caught IOException, server " + socketServerVo.getSocketPort(), excep);
		}
		else {
			keepRunning = false;
			this.interrupt();
			JbMain.getEventAlert().issueExcepAlert(
					JbMain.SOCKET_SVR,
					"SocketServer: caught Unchecked Exception, stopping server "
							+ socketServerVo.getSocketPort(), excep);
		}
	}

	/**
	 * implement JbEventListener methods
	 * 
	 * @param e -
	 *            event
	 */
	public void errorOccured(JbEvent e) {
		Exception excep = e.getException();
		logger.error("Error caught by SocketThread. " + excep);
	}
}
