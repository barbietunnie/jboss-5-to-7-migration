package com.legacytojava.jbatch;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * define MBean server for JBatch
 */
public class JbMBeanSvr {
	static final Logger logger = Logger.getLogger(JbMBeanSvr.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	MBeanServer server;

	/**
	 * Creates a new instance of JbMBeanSvr
	 */
	public JbMBeanSvr() {
	}

	/**
	 * program entry to start MBean server
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] argv) {

		JbMBeanSvr agent = new JbMBeanSvr();
		try {
			agent.start();
		}
		catch (Throwable e) {
			logger.error("Throwable caught", e);
			System.exit(1);
		}
	}

	/**
	 * start MBean server
	 * 
	 * @throws Exception
	 *             if any error
	 */
	void start() throws Exception {
		JbMain theMonitor = JbMain.getInstance();
		theMonitor.loadProperties();
		theMonitor.init();
		initMBeanServer();
		theMonitor.start();
		startAgent();
	}

	/**
	 * initialize MBean server
	 */
	private void initMBeanServer() {
		server = MBeanServerFactory.createMBeanServer("AgentServer");
		// Resource resource = JbMain.getResource();
	}

	/**
	 * start MBean agent
	 * 
	 * @throws Exception
	 *             if any error
	 */
	void startAgent() throws Exception {
		HtmlAdaptorServer adaptorServer = new HtmlAdaptorServer();
		try {
			registerMyMBean("JbMain", "main", JbMain.getInstance());
			ObjectName AdaptorObjName = new ObjectName("AgentServer:type=htmladaptor,port=8082");
			adaptorServer.setPort(8082);
			server.registerMBean(adaptorServer, AdaptorObjName);
			adaptorServer.start();
		}
		catch (MalformedObjectNameException e) {
			logger.error("Bad object name", e);
		}
		catch (InstanceAlreadyExistsException e) {
			logger.error("Already exists", e);
		}
		catch (MBeanRegistrationException e) {
			logger.error("Registration problems", e);
		}
		catch (NotCompliantMBeanException e) {
			logger.error("MBean not compliant", e);
		}
	}

	/**
	 * register MBean server
	 * 
	 * @param type -
	 *            server type
	 * @param name -
	 *            server name
	 * @param mbean -
	 *            MBean instance
	 * @throws NullPointerException
	 * @throws MalformedObjectNameException
	 * @throws NotCompliantMBeanException
	 * @throws MBeanRegistrationException
	 * @throws InstanceAlreadyExistsException
	 */
	void registerMyMBean(String type, String name, Object mbean)
			throws MalformedObjectNameException, NullPointerException,
			InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName objName = new ObjectName("AgentServer:type=" + type + ",name=" + name);
		try {
			server.unregisterMBean(objName);
		}
		catch (Exception e) {
			// ignore
		}
		server.registerMBean(mbean, objName);
	}
}
