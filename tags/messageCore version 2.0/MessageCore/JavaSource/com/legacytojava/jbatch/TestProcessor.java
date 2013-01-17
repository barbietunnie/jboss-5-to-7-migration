package com.legacytojava.jbatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.legacytojava.message.vo.ServerBaseVo;
import com.legacytojava.message.vo.SocketServerVo;

/**
 * test processor
 */
public class TestProcessor extends RunnableProcessor {
	static final Logger logger = Logger.getLogger(TestProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private ServerBaseVo serverBaseVo;
	private Properties properties;

	// must use constructor without any parameters
	public TestProcessor() {
		logger.info("Entering Constructor...");
	}

	public void process(Object req) throws JMSException, IOException {
		logger.info("Entering process() method...");
		if (req == null) {
			logger.warn("request object is null.");
			return;
		}
		logger.info(serverBaseVo);
		Message msg = null;
		Socket skt = null;
		if (req instanceof Message) {
			msg = (Message) req;
			logger.info("process(): JMS Message received.");
			if (msg instanceof ObjectMessage) {
				processMessage(msg);
			}
			else if (msg instanceof TextMessage) {
				logger.info(msg);
			}
			else if (msg instanceof Message) {
				logger.info(msg);
				Enumeration<?> enu = msg.getPropertyNames();
				while (enu.hasMoreElements()) {
					logger.info("property name:" + enu.nextElement());
				}
			}
			else {
				logger.error("Message received was not a ObjectMessage/TextMessage.");
			}
		}
		else if (req instanceof Socket) {
			skt = (Socket) req;
			logger.info("process(): A Socket received.");
			boolean interactive = false;
			if (serverBaseVo instanceof SocketServerVo) {
				SocketServerVo socketVo = (SocketServerVo) serverBaseVo;
				if ("yes".equalsIgnoreCase(socketVo.getInteractive())) {
					interactive = true;
				}
			}
			String myAddr = HostUtil.getHostIpAddress();
			if (interactive) {
				processSocket(skt, interactive, myAddr);
			}
			else {
				try {
					processSocket(skt);
				}
				catch (InterruptedException e) {
					throw new IOException("InterruptedException caught, " + e);
				}
			}
		}
		else if (req instanceof java.util.TimerTask) {
			logger.info("process(): A Timertask received.");
		}
		else if (req instanceof javax.mail.Message) {
			logger.info("process(): A javax.mail.Message received.");
		}
		else if (req instanceof javax.mail.Message[]) {
			logger.info("process(): A javax.mail.Message[] received.");
		}
		else {
			logger.info("process(): received  a " + req.getClass().getName());
		}
	}

	// implement a simple message consumer
	// to be used by com.legacytojava.test.testQueueSvr
	void processMessage(Message msg) {
		final Logger logger = Logger.getLogger("com.legacytojava.queueout");
		logger.info("JMS Message Received: " + msg);
	}

	// implement an interactive echo server
	void processSocket(Socket mySocket, boolean interactive, String myAddr) {
		final boolean AUTOFLASH = true;
		String nextLine;
		int reqCount = 0;
		PrintWriter clientSend = null;
		BufferedReader clientReceive = null;
		try {
			clientSend = new PrintWriter(mySocket.getOutputStream(), AUTOFLASH);

			clientReceive = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

			if (interactive)
				clientSend.println("+HELO " + myAddr);

			while ((nextLine = clientReceive.readLine()) != null) {
				try {
					if (nextLine.equalsIgnoreCase("QUIT"))
						break;
					else {
						clientSend.println(" - YOUR REQUEST: " + nextLine);
					}
				}
				catch (Exception e) {
					clientSend.println(" - ERROR EXCEPTION CAUGHT: " + e);
				}
				reqCount++;
				clientSend.flush();
			} // end of while

			if (interactive)
				clientSend.println("+BYE");
		}
		catch (java.io.InterruptedIOException e) {
			if (clientSend != null)
				clientSend.println("+SOCKET TIMED OUT, BYE");
			logger.error("processSocket(): Socket Timed out", e);
		}
		catch (IOException e) {
			logger.error("processSocket(): Failed I/O", e);
		}
		finally {
			try {
				if (clientSend != null)
					clientSend.close();
				if (clientReceive != null)
					clientReceive.close();
				if (mySocket != null)
					mySocket.close();
			}
			catch (IOException e) {
				logger.error("processSocket(): Failed I/O", e);
			}
			logger.info("Socket Processor completed...# of Requests: " + reqCount);
		}
	}

	// implement a simple echo server
	// to be used by com.legacytojava.test.testSocketSvr
	void processSocket(Socket mySocket) throws IOException, InterruptedException {
		final boolean AUTOFLASH = true;
		String nextLine;
		PrintWriter clientSend = null;
		BufferedReader clientReceive = null;
		try {
			clientSend = new PrintWriter(mySocket.getOutputStream(), AUTOFLASH);

			clientReceive = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

			if ((nextLine = clientReceive.readLine()) != null) {
				Thread.sleep(100);
				try {
					clientSend.println(nextLine);
				}
				catch (Exception e) {
					clientSend.println(" - ERROR EXCEPTION CAUGHT: " + e);
				}
				clientSend.flush();
			}
		}
		catch (java.io.InterruptedIOException e) {
			if (clientSend != null)
				clientSend.println("+SOCKET TIMED OUT, BYE");
			logger.error("processSocket(): Socket Timed out", e);
		}
		finally {
			try {
				if (clientSend != null)
					clientSend.close();
				if (clientReceive != null)
					clientReceive.close();
				if (mySocket != null)
					mySocket.close();
			}
			catch (IOException e) {
				logger.error("processSocket(): Failed I/O", e);
			}
		}
	}

	public ServerBaseVo getServerBaseVo() {
		return serverBaseVo;
	}

	public void setServerBaseVo(ServerBaseVo serverBaseVo) {
		this.serverBaseVo = serverBaseVo;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}