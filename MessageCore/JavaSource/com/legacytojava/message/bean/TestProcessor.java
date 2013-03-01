package com.legacytojava.message.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Properties;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.HostUtil;
import com.legacytojava.jbatch.Processor;

/**
 * test processor
 */
public class TestProcessor implements Processor {
	static final Logger logger = Logger.getLogger(TestProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private Properties properties;

	// must use constructor without any parameters
	public TestProcessor() {
		logger.info("Entering TestProcessor Constructor...");
	}

	public void process(Object req) throws Exception {
		logger.info("TestProcessor: Entering process() method...");
		logger.info(getProperties());
		Message msg = null;
		Socket skt = null;

		try {
			if (req != null && req instanceof Message) {
				msg = (Message) req;
				logger.info("TestProcessor.process(): JMS Message received.");

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
				else
					logger.error("Message received was not a ObjectMessage/TextMessage.");
			}
			else if (req != null && req instanceof Socket) {
				skt = (Socket) req;
				logger.info("TestProcessor.process(): A Socket received.");
				Properties props = getProperties() == null ? new Properties() : getProperties();
				boolean interactive = false;
				if ("yes".equalsIgnoreCase(props.getProperty("interactive"))) {
					interactive = true;
				}
				String myAddr = HostUtil.getHostIpAddress();
				if (interactive) {
					processSocket(skt, interactive, myAddr);
				}
				else {
					processSocket(skt);
				}
			}
			else if (req != null && req instanceof java.util.TimerTask) {
				logger.info("TestProcessor.process(): A Timertask received.");
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw new Exception(e.toString());
		}
	}

	// implement a simple message consumer
	// to be used by com.legacytojava.test.testQueueSvr
	void processMessage(Message msg) throws Exception {
		final Logger logger = Logger.getLogger("com.legacytojava.queueout");

		MessageBean msgBean = null;
		try {
			msgBean = (MessageBean) ((ObjectMessage) msg).getObject();
			Thread.sleep(500);
		}
		catch (Exception e) {
			logger.error("error: Object message is not a valid MessageBean, discarded", e);
			throw e;
		}

		// BodypartBean aNode =
		String from = null, to = null, subj = null, body = null;
		from = msgBean.getFromAsString();
		to = msgBean.getToAsString();
		subj = msgBean.getSubject();
		body = msgBean.getBody();
		logger.info(from + "^" + to + "^" + subj + "^" + body);
	}

	// implement an interactive echo server
	void processSocket(Socket mySocket, boolean interactive, String myAddr) throws Exception {
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
			logger.error("TestProcessor.process(): Socket Timed out", e);
		}
		catch (IOException e) {
			logger.error("TestProcessor.process(): Failed I/O", e);
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
				logger.error("TestProcessor.process(): Failed I/O", e);
			}
			logger.info("Socket Processor completed...# of Requests: " + reqCount);
		}
	}

	// implement a simple echo server
	// to be used by com.legacytojava.test.testSocketSvr
	void processSocket(Socket mySocket) throws Exception {
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
			logger.error("TestProcessor.process(): Socket Timed out", e);
		}
		catch (Exception e) {
			logger.error("TestProcessor.process(): Failed I/O", e);
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
				logger.error("TestProcessor.process(): Failed I/O", e);
			}
		}
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}