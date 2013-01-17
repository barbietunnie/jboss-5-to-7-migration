package com.legacytojava.jbatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.AbstractApplicationContext;

import com.legacytojava.jbatch.common.ProductKey;
import com.legacytojava.message.bo.mailreader.MailReaderBoImpl;
import com.legacytojava.message.dao.mailbox.MailBoxDao;
import com.legacytojava.message.dao.timer.TimerServerDao;
import com.legacytojava.message.vo.MailBoxVo;
import com.legacytojava.message.vo.ServerBaseVo;
import com.legacytojava.message.vo.SocketServerVo;
import com.legacytojava.message.vo.TimerServerVo;

/**
 * JbMain class that loads resources, and starts and monitors servers.
 * <p>
 * JBatch is a java batch job container.
 */
public final class JbMain implements Runnable, JbMainMBean {
	static final Logger logger = Logger.getLogger(JbMain.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private static JbMain theMonitor = null;

	static final String QUEUE_LISTENERS = "queueListeners";
	static final String MAIL_READERS = "mailReaders";
	static final String SOCKET_SERVERS = "socketServers";
	static final String TIMER_SERVERS = "timerServers";

	public static final int QUEUE_SVR = 1, SOCKET_SVR = 2, TIMER_SVR = 3,
		MAIL_SVR = 4, SMTP_ALERT = 5, CLEAR_ALERT = 6;

	final Properties queueListeners = new Properties();
	final Properties socketServers = new Properties();
	final List<MailBoxVo> mailBoxVos = new ArrayList<MailBoxVo>();
	final List<TimerServerVo> timerServers = new ArrayList<TimerServerVo>();
	
	private static EventAlert eventAlert = null;

	static int MAX_THREADS = 100;
	static final Properties appConf = new Properties();
	static Resource resource = null;

	private static int numberofThreadAtStart = -1;
	private static String APP_ROOT = null;
	private static String APP_NAME = null;

	public static final String QUEUE_SVR_TYPE = "QueueServer";
	public static final String MAIL_SVR_TYPE = "MailReader";
	public static final String SOCKET_SVR_TYPE = "SocketServer";
	public static final String TIMER_SVR_TYPE = "TimerServer";

	private Thread thisThread = null;

	/* variables initialized in constructor */
	final HashMap<JbThread, Future<?>> queueThreadStarted = new HashMap<JbThread, Future<?>>();
	final HashMap<JbThread, Future<?>> socketThreadStarted = new HashMap<JbThread, Future<?>>();
	final HashMap<TimerServer, Future<?>> timerThreadStarted = new HashMap<TimerServer, Future<?>>();
	final HashMap<MailReader, Future<?>> mailboxThreadStarted = new HashMap<MailReader, Future<?>>();

	final Properties queueThreadFailed = new Properties();
	final Properties socketThreadFailed = new Properties();
	final List<TimerServerVo> timerThreadFailed = new ArrayList<TimerServerVo>();
	final List<MailBoxVo> mailboxThreadFailed = new ArrayList<MailBoxVo>();

	private final List<java.util.Timer> systemTaskPool = new ArrayList<java.util.Timer>();

	volatile boolean keepRunning = true;
	static final List<MetricsLogger> metricsLoggers = new ArrayList<MetricsLogger>();
	private final ExecutorService threadPool;
	/* end of variables */
	
	// used by wrap up method
	private Object locker = new Object();
	private boolean firstTime = true;

	private static final int DEFAULT_SHUTDOWN_DELAY = 4; // in seconds
	static int shutDownDelay = DEFAULT_SHUTDOWN_DELAY;
	final String LF = System.getProperty("line.separator", "\n");

	/**
	 * constructor, create a JbMain instance
	 */
	private JbMain() {
		queueThreadStarted.clear();
		socketThreadStarted.clear();
		timerThreadStarted.clear();
		mailboxThreadStarted.clear();
		queueThreadFailed.clear();
		socketThreadFailed.clear();
		timerThreadFailed.clear();
		mailboxThreadFailed.clear();
		systemTaskPool.clear();

		keepRunning = true;
		metricsLoggers.clear();
		threadPool = Executors.newCachedThreadPool();
		numberofThreadAtStart = Thread.activeCount();
	}

	/**
	 * program main entry
	 * 
	 * <pre>
	 * -- load configuration properties
	 * -- for each server/job defined in configuration
	 * -- start the server task
	 * -- start the monitoring thread
	 * -- start the server watch timer task 
	 * </pre>
	 * 
	 * @param argv - not used
	 */
	public static void main(String argv[]) {
		try {
			theMonitor = getInstance();
			theMonitor.loadProperties();
			theMonitor.init();
			theMonitor.start();
		}
		catch (Throwable e) {
			logger.error("Exception caught", e);
			System.exit(1);
		}
	}

	/**
	 * @return a JbMain instance
	 */
	public static JbMain getInstance() {
		if (theMonitor == null)
			theMonitor = new JbMain();
		return theMonitor;
	}

	public static int getNumberOfThreadAtStart() {
		return numberofThreadAtStart;
	}

	/**
	 * The factory is initialized by loading all "spring-jbatch-*" files from
	 * the root class folder. It should only be used by batch programs.
	 * 
	 * @return Spring Application Context factory.
	 */
	public static AbstractApplicationContext getBatchAppContext() {
		return SpringUtil.getAppContext();
	}

	private static Boolean isKeyValid = null;
	
	public static boolean isProductKeyValid() {
		if (isKeyValid == null) {
			boolean isValid = ProductKey.validateKey(getProductKeyFromFile());
			isKeyValid = Boolean.valueOf(isValid);
		}
		return isKeyValid;
	}
	
	public static String getProductKeyFromFile() {
		// ThreadContextClassLoader works with both command line JVM and JBoss
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("productkey.txt");
		if (url == null) {
			logger.warn("productkey.txt file not found.");
			return null;
		}
		BufferedReader br = null;
		try {
			InputStream is = url.openStream();
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			Pattern pattern = Pattern.compile("\\b((.{5})(-.{5}){4})\\b");
			while ((line=br.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find() && matcher.groupCount() >= 1) {
					return matcher.group(1);
				}
			}
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (IOException e) {}
			} 
		}
		return null;
	}
	
	/**
	 * @return a EventAlert instance
	 */
	public static EventAlert getEventAlert() {
		if (eventAlert==null)
			eventAlert = (EventAlert) getBatchAppContext().getBean("eventAlert");
		return eventAlert;
	}

	/**
	 * @deprecated - replaced by getBatchConfigXmlFiles()
	 */
	static String[] getConfigXmlFilesV0() {
		ClassLoader loader = JbMain.class.getClassLoader(); //ClassLoader.getSystemClassLoader();
		String[] cfgFiles = null;
		try {
			List<String> cfgFileNames = new ArrayList<String>();
			Enumeration<URL> _enum = loader.getResources("./");
				// not working when running from a jar file
			if (_enum.hasMoreElements()) {
				URL url = (URL)_enum.nextElement();
				if (isDebugEnabled)
					logger.debug("App Root URL: "+url);
				File file = new File(url.getPath());
				if (file!=null && file.isDirectory()) {
					String[] fileNames = file.list();
					for (int i=0; i<fileNames.length; i++) {
						if (isDebugEnabled)
							logger.debug(url+" - "+fileNames[i]);
						if (fileNames[i].startsWith("spring-jbatch-")
								&& fileNames[i].endsWith(".xml")) {
							cfgFileNames.add(fileNames[i]);
						}
					}
				}
			}
			cfgFiles = new String[cfgFileNames.size()];
			System.arraycopy(cfgFileNames.toArray(), 0, cfgFiles, 0, cfgFiles.length);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			cfgFiles = new String[0];
		}
		return cfgFiles;
	}

	void loadProperties() {
		// we will use this later to see if all our Threads have died.
		// we also use it to check if it is running inside JbMain.
		numberofThreadAtStart = Thread.activeCount();
		logger.info("Number of threads at start " + numberofThreadAtStart);
		
		HashMap<?,?> app_conf = (HashMap<?,?>) getBatchAppContext().getBean("appProperties");
		appConf.putAll(app_conf);
		logger.info("AppProperties: " + appConf);
		
		try {
			HashMap<?,?> readers = (HashMap<?,?>) getBatchAppContext().getBean(MAIL_READERS);
			logger.info("readers" + readers);
			if (readers != null && "yes".equals(readers.get("startServer"))) {
				MailBoxDao mailBoxDao = (MailBoxDao) getBatchAppContext().getBean("mailBoxDao");
				List<MailBoxVo> mboxList = mailBoxDao.getAll(true);
				mailBoxVos.addAll(mboxList);
			}
		}
		catch (NoSuchBeanDefinitionException e) {
			logger.warn("No MailReaders are defined in Config file");
		}
		
		try {
			HashMap<?,?> timers = (HashMap<?,?>) getBatchAppContext().getBean(TIMER_SERVERS);
			if (timers != null && "yes".equals(timers.get("startServer"))) {
				TimerServerDao timerDao = (TimerServerDao) getBatchAppContext()
						.getBean("timerServerDao");
				List<TimerServerVo> timerList = timerDao.getAll(true);
				timerServers.addAll(timerList);
			}
		}
		catch (NoSuchBeanDefinitionException e) {
			logger.warn("No TimerServer are defined in Config file");
		}
		
		try {
			HashMap<?,?> listeners = (HashMap<?,?>) getBatchAppContext().getBean(QUEUE_LISTENERS);
			queueListeners.putAll(listeners);
			logger.info("QueueListeners: " + queueListeners);
		}
		catch (NoSuchBeanDefinitionException e) {
			logger.warn("No QueueListeters are defined in Config file");
		}

		try {
			HashMap<?,?> sockets = (HashMap<?,?>) getBatchAppContext().getBean(SOCKET_SERVERS);
			socketServers.putAll(sockets);
			logger.info("SocketServers: " + socketServers);
		}
		catch (NoSuchBeanDefinitionException e) {
			logger.warn("No SocketServers are defined in Config file");
		}

		APP_ROOT = System.getProperty("user.dir");
		APP_NAME = appConf.getProperty("app_name", "jbatch");

		String _delay = appConf.getProperty("shutDownDelay");
		try {
			shutDownDelay = Integer.parseInt(_delay);
		}
		catch (NumberFormatException e) {
			// ignore
		}
		catch (Exception e) {
			// ignore
		}
		
		// shutdown delay, at least 4 seconds
		shutDownDelay = shutDownDelay < DEFAULT_SHUTDOWN_DELAY ? DEFAULT_SHUTDOWN_DELAY
				: shutDownDelay;
	}
	
	void init() throws SQLException {
		HostUtil.getHostIpAddress();
		HostUtil.getHostName();
		resource = (Resource) getBatchAppContext().getBean("resource");
		resource.init(); // throws SQLException
		// initialize the static EventAlert instance
		eventAlert = getEventAlert();
	}

	/**
	 * start all servers and jobs
	 */
	void start() {
		// startup all servers
		if (queueListeners.size() > 0) {
			startQueueJobs(queueListeners);
		}
		if (socketServers.size() > 0) {
			startSocketServers(socketServers);
		}
		if (timerServers != null && timerServers.size() > 0) {
			startTimerTasks(timerServers);
		}
		if (mailBoxVos!=null && mailBoxVos.size() > 0) {
			startMailReaders(mailBoxVos);
		}
		// start system timer tasks
		startSystemTimerTasks();
		// start main thread - control transfered to run() method
		thisThread = new Thread(this);
		thisThread.setPriority(Thread.MIN_PRIORITY + 1);
		thisThread.setName("TheMonitor");
		thisThread.start();
		// register shutdown hook thread
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				this.setName("ShutdownHook");
				logger.info("shutdownHook: shutting down the Agent...");
				wrapup();
			}
		});
		// issue clear alert
		getEventAlert().issueInfoAlert(CLEAR_ALERT,
				APP_NAME + " on " + HostUtil.getHostIpAddress() + ":" + APP_ROOT + " started");
	}

	/******************************** 
	 * Implements JbMainMBean
	 * ******************************/
	
	public String[] getQueueJobs() {
		List<String> v = new ArrayList<String>();
		Set<JbThread> keys = queueThreadStarted.keySet();
		for (Iterator<JbThread> it = keys.iterator(); it.hasNext();) {
			JbThread server = it.next();
			v.addAll(server.getStatus());
		}
		return List2Strings(v);
	}

	public String[] getSocketServers() {
		List<String> v = new ArrayList<String>();
		Set<JbThread> keys = socketThreadStarted.keySet();
		for (Iterator<JbThread> it = keys.iterator(); it.hasNext();) {
			JbThread server = it.next();
			v.addAll(server.getStatus());
		}
		return List2Strings(v);
	}

	public String[] getTimerJobs() {
		List<String> v = new ArrayList<String>();
		Set<TimerServer> keys = timerThreadStarted.keySet();
		for (Iterator<TimerServer> it = keys.iterator(); it.hasNext();) {
			TimerServer server = it.next();
			v.addAll(server.getStatus());
		}
		return List2Strings(v);
	}

	public String[] getMailReaders() {
		List<String> v = new ArrayList<String>();
		Set<MailReader> keys = mailboxThreadStarted.keySet();
		for (Iterator<MailReader> it = keys.iterator(); it.hasNext();) {
			MailReader server = it.next();
			v.addAll(server.getStatus());
		}
		return List2Strings(v);
	}

	public String[] getAllStatus() {
		String[] queueJobs = getQueueJobs();
		String[] socketServers = getSocketServers();
		String[] timerServers = getTimerJobs();
		String[] mailReaders = getMailReaders();
		int totalLen = queueJobs.length + socketServers.length + timerServers.length + mailReaders.length;
		String[] allServers = new String[totalLen];
		System.arraycopy(queueJobs, 0, allServers, 0, queueJobs.length);
		System.arraycopy(socketServers, 0, allServers, queueJobs.length, socketServers.length);
		System.arraycopy(timerServers, 0, allServers, queueJobs.length + socketServers.length,
				timerServers.length);
		System.arraycopy(mailReaders, 0, allServers, queueJobs.length + socketServers.length
				+ timerServers.length, mailReaders.length);
		return allServers;
	}

	/*
	 * convert list to string array
	 */
	private String[] List2Strings(List<String> v) {
		String[] strs = new String[v.size()];
		for (int i = 0; i < strs.length; i++) {
			strs[i] = v.get(i);
		}
		return strs;
	}

	public boolean restartQueueJobs() {
		stopQueueJobInstances();
		try {
			startQueueJobs(queueListeners);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			return false;
		}
		return true;
	}

	public boolean stopQueueJobs() {
		stopQueueJobInstances();
		return true;
	}

	public boolean restartSocketServers() {
		stopSocketServerInstances();
		try {
			startSocketServers(socketServers);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			return false;
		}
		return true;
	}

	public boolean stopSocketServers() {
		stopSocketServerInstances();
		return true;
	}

	public boolean restartMailReaders() {
		stopMailReaderInstances();
		try {
			startMailReaders(mailBoxVos);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			return false;
		}
		return true;
	}

	public boolean stopMailReaders() {
		stopMailReaderInstances();
		return true;
	}

	public boolean restartTimerJobs() {
		stopTimerInstances();
		try {
			startTimerTasks(timerServers);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			return false;
		}
		return true;
	}

	public boolean stopTimerJobs() {
		stopTimerInstances();
		return true;
	}

	/********************************  
	 * End of MBean Implementations
	 * ******************************/

	/**
	 * start all queue jobs, failed queue jobs will be put into a pool.
	 * 
	 * @param queListeners
	 *            list of queue listener names and their bean ID's
	 */
	void startQueueJobs(Properties queListeners) {
		if (queListeners == null) {
			return;
		}
		queueThreadFailed.clear();
		
		java.util.Set<?> keys = queListeners.keySet();
		for (Iterator<?> it = keys.iterator(); it.hasNext();) {
			String listenerName = (String)it.next();
			String listenerBeanId = (String)queListeners.get(listenerName);
			logger.info("Creating QueueListener " + listenerName + "/" + listenerBeanId);

			// instantiate a sub thread with its queue properties
			QueueListener mt = null;
			try {
				mt = (QueueListener)JbMain.getBatchAppContext().getBean(listenerBeanId);
			}
			catch (Exception e) {
				logger.error("Failed to initialize an QueueListener", e);
				logger.info("QueueListener Name/Id: " + listenerName+"/"+listenerBeanId);
				queueThreadFailed.setProperty(listenerName, listenerBeanId);
				eventAlert.issueFatalAlert(QUEUE_SVR, "QueueListener " + listenerName
						+ " failed to start, ", e);
				continue;
			}
			ServerBaseVo queueVo = mt.getServerBaseVo(); 
			int PRIORITY = Thread.NORM_PRIORITY;
			String Priority = queueVo.getPriority()==null?"medium":queueVo.getPriority();
			if (Priority.equalsIgnoreCase("high"))
				PRIORITY = Thread.MAX_PRIORITY - 1;
			else if (Priority.equalsIgnoreCase("low"))
				PRIORITY = Thread.MIN_PRIORITY + 1;

			mt.setPriority(PRIORITY);
			mt.setName(listenerName+"."+listenerBeanId);
			
			mt.setServerName(listenerName);
			mt.setServerId(listenerBeanId);

			Future<?> future = threadPool.submit(mt);
			queueThreadStarted.put(mt,future);
			try { // give each thread some time to make initial connection
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
		}
		if (queueThreadFailed.isEmpty())
			eventAlert.issueClearAlert(QUEUE_SVR);
	}

	/**
	 * start failed queue jobs
	 */
	void startFailedQueueServers() {
		Properties props = new Properties();
		synchronized (queueThreadFailed) {
			props.putAll(queueThreadFailed);
			startQueueJobs(props);
		}
	}

	/**
	 * stop queue jobs
	 */
	void stopQueueJobInstances() {
		stopThreads(queueThreadStarted);
	}

	/**
	 * start all socket servers, failed server will be put into a pool.
	 * 
	 * @param sktServers
	 *            list of socket server names and their bean ID's
	 */
	void startSocketServers(Properties sktServers) {
		if (sktServers == null) {
			return;
		}
		socketThreadFailed.clear();
		
		java.util.Set<?> keys = sktServers.keySet();
		for (Iterator<?> it=keys.iterator(); it.hasNext(); ) {
			String serverName = (String)it.next();
			String serverBeanId = sktServers.getProperty(serverName);
			logger.info("Creating SocketServer " + serverName + "/" + serverBeanId);
			
			// instantiate a sub thread with its socket properties
			SocketServer mt = null;
			try {
				mt = (SocketServer) JbMain.getBatchAppContext().getBean(serverBeanId);
			}
			catch (Exception e) {
				logger.error("Failed to initialize a SocketServer", e);
				socketThreadFailed.setProperty(serverName, serverBeanId);
				eventAlert.issueFatalAlert(SOCKET_SVR, "SocketServer " + serverName
						+ " failed to start, ", e);
				continue;
			}

			SocketServerVo socketVo = mt.getSocketServerVo();
			int port = socketVo.getSocketPort();
			if (port <= 0) {
				throw new IllegalArgumentException(
						"port number must be greater than 0");
			}

			int PRIORITY = Thread.NORM_PRIORITY;
			String Priority = socketVo.getPriority()==null?"medium":socketVo.getPriority();
			if (Priority.equalsIgnoreCase("high"))
				PRIORITY = Thread.MAX_PRIORITY - 1;
			else if (Priority.equalsIgnoreCase("low"))
				PRIORITY = Thread.MIN_PRIORITY + 1;

			mt.setPriority(PRIORITY);
			mt.setName("SocketServer." + port);
			mt.setServerName(serverName);
			mt.setServerId(serverBeanId);

			Future<?> future = threadPool.submit(mt);
			socketThreadStarted.put(mt,future);
			try { // give each thread some time to make initial connection
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
		}
		if (socketThreadFailed.isEmpty())
			eventAlert.issueClearAlert(SOCKET_SVR);
	}

	/**
	 * start failed socket servers.
	 */
	void startFailedSocketServers() {
		Properties props = new Properties();
		synchronized (socketThreadFailed) {
			props.putAll(socketThreadFailed);
			startSocketServers(props);
		}
	}

	/**
	 * stop socket servers
	 */
	void stopSocketServerInstances() {
		stopThreads(socketThreadStarted);
	}

	/**
	 * start all timer tasks, failed tasks will be put into a pool.
	 * 
	 * @param tmrServers
	 *            list of task server names and their bean ID's
	 */
	void startTimerTasks(List<TimerServerVo> tmrServers) {
		if (tmrServers == null) {
			return;
		}
		timerThreadFailed.clear();
		
		for (Iterator<TimerServerVo> it=tmrServers.iterator(); it.hasNext(); ) {
			TimerServerVo serverVo = it.next();
			String serverName = serverVo.getServerName();
			logger.info("Creating TimerServer " + serverName);
			
			// instantiate a sub thread with its properties
			TimerServer mt = new TimerServer(serverVo);
			
			logger.info("Creating Timer Server for processor " + serverVo.getProcessorName()
					+ ", number of threads "
					+ serverVo.getThreads());
			
			mt.setServerName(serverName);
			mt.setServerId(serverVo.getProcessorName());
			
			timerThreadStarted.put(mt, null);
			mt.schedule();
			try { // give each thread some time to make initial connection
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
		}
		if (timerThreadFailed.isEmpty())
			eventAlert.issueClearAlert(TIMER_SVR);
	}

	/**
	 * start failed timer tasks
	 */
	void startFailedTimerTasks() {
		List<TimerServerVo> props = new ArrayList<TimerServerVo>();
		synchronized (timerThreadFailed) {
			props.addAll(timerThreadFailed);
			startTimerTasks(props);
		}
	}

	/**
	 * stop timer tasks
	 */
	void stopTimerInstances() {
		// a list was used to store all the timer servers
		if (timerThreadStarted.size() > 0) {
			logger.info("Stopping Timer Servers...");
			Set<?> keys = timerThreadStarted.keySet();
			for (Iterator<?> it = keys.iterator(); it.hasNext(); ) {
				Object obj = it.next();
				if (obj instanceof TimerServer) {
					TimerServer mt = (TimerServer) obj;
					mt.cancel(); // stop the timer
					logger.info("Timer Task " + mt.getServerName() + "/" + mt.getServerId()
							+ " cancelled.");
				}
			}
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
			timerThreadStarted.clear();
			logger.info("all timer servers are stopped.");
		}
	}

	/**
	 * start all mail readers, failed readers will be put into a pool.
	 * 
	 * @param mReaders
	 *            list of mail reader names and their bean ID's
	 */
	void startMailReaders(List<MailBoxVo> mReaders) {
		if (mReaders == null) {
			return;
		}
		mailboxThreadFailed.clear();

		if (mReaders.size() > 0 && isAnotherInstanceRunning()) {
			logger.fatal("Another MailReader instance is running, exiting...");
			eventAlert.issueFatalAlert(MAIL_SVR,
					"Another MailReader instance is running, exiting...");
			throw new IllegalMonitorStateException(
					"Another MailReader instance is running, exiting...");
		}

		for (Iterator<MailBoxVo> it=mReaders.iterator(); it.hasNext() ;) {
			MailBoxVo mboxVo = it.next();
			String readerName = mboxVo.getServerName();
			logger.info("Creating MailReader for " + readerName);

			// instantiate a sub thread with mailbox properties
			MailReader mt = new MailReader(mboxVo);
			
			int PRIORITY = Thread.NORM_PRIORITY;
			String Priority = mboxVo.getPriority()==null?"medium":mboxVo.getPriority();
			if (Priority.equalsIgnoreCase("high"))
				PRIORITY = Thread.MAX_PRIORITY - 1;
			else if (Priority.equalsIgnoreCase("low"))
				PRIORITY = Thread.MIN_PRIORITY + 1;

			mt.setPriority(PRIORITY);
			// keep the thread name as short as possible
			mt.setName(readerName+"."+mboxVo.getProcessorName());
			mt.setServerName(readerName);
			mt.setServerId(mboxVo.getProcessorName());

			Future<?> future = threadPool.submit(mt);
			mailboxThreadStarted.put(mt,future);
			try { // give each thread some time to make initial connection
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
		}
		if (mailboxThreadFailed.isEmpty())
			eventAlert.issueClearAlert(MAIL_SVR);
	}

	void startMailReadersV2(List<MailBoxVo> mReaders) {
		if (mReaders == null) {
			return;
		}
		mailboxThreadFailed.clear();

		if (mReaders.size() > 0 && isAnotherInstanceRunning()) {
			logger.fatal("Another MailReader instance is running, exiting...");
			eventAlert.issueFatalAlert(MAIL_SVR,
					"Another MailReader instance is running, exiting...");
			throw new IllegalMonitorStateException(
					"Another MailReader instance is running, exiting...");
		}

		for (Iterator<MailBoxVo> it=mReaders.iterator(); it.hasNext() ;) {
			MailBoxVo mboxVo = it.next();
			mboxVo.setFromTimer(false);
			String readerName = mboxVo.getServerName();
			logger.info("Creating MailReader for " + readerName);
			MailReaderBoImpl mt = new MailReaderBoImpl(mboxVo, getBatchAppContext(),
					getBatchAppContext());
			//Future<?> future = 
				threadPool.submit(mt);
			//mailboxThreadStarted.put(mt,future);
			logger.info("MailReader started: " + mt.getMailBoxVo().getServerName());
			try { // give each thread some time to make initial connection
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
		}
		if (mailboxThreadFailed.isEmpty())
			eventAlert.issueClearAlert(MAIL_SVR);
	}

	/**
	 * start failed mail readers
	 */
	void startFailedMailReaders() {
		List<MailBoxVo> props = new ArrayList<MailBoxVo>();
		synchronized (mailboxThreadFailed) {
			props.addAll(mailboxThreadFailed);
			startMailReaders(props);
		}
	}

	/**
	 * stop mail readers
	 */
	void stopMailReaderInstances() {
		stopThreads(mailboxThreadStarted);
	}

	/**
	 * check if another mail reader instance is running. works under Solaris and
	 * Linux. 
	 * @return true if another instance is running
	 */
	boolean isAnotherInstanceRunning() {
		String osname = System.getProperty("os.name");
		String osarch = System.getProperty("os.arch");
		logger.info("OS name/arch:" + osname + "/" + osarch);
		// only Unix OS is supported for now.
		if (osname.toLowerCase().indexOf("windows") >= 0)
			return false;

		try {
			logger.info("Now sleep for a moment for the old instance to die...");
			Thread.sleep(6000); // give some time for the old instance to die
		}
		catch (InterruptedException e) {
		}

		BufferedReader bin = null;
		try {
			Process proc = null;
			String command = null;
			// ps with Solaris flavor
			String command_solaris = "/usr/bin/ps -aef | grep \"java\" | grep -v \"grep\" | grep \""
					+ APP_ROOT + "\" | wc -l";
			String command_linux = "/bin/ps -awx | grep \"java\" | grep -v \"grep\" | grep \""
					+ APP_ROOT + "\" | wc -l";
			if (osname.toLowerCase().indexOf("sunos") >= 0) {
				command = command_solaris;
				logger.info("command to execute: " + command_solaris);
				proc = Runtime.getRuntime().exec(
						new String[] { "/usr/bin/ksh", "-e", command_solaris });
			}
			else {
				command = command_linux;
				logger.info("command to execute: " + command_solaris);
				proc = Runtime.getRuntime().exec(
						new String[] { "/bin/bash", "-e", command_linux });
			}
			InputStream in = proc.getInputStream();
			bin = new BufferedReader(new InputStreamReader(in));
			String line = null, result = "";
			while ((line = bin.readLine()) != null) {
				result += line;
			}
			logger.info("Number of instances running = " + result.trim());
			try {
				proc.waitFor(); // wait for the process to complete
			}
			catch (InterruptedException ei) {
			}
			if (proc.exitValue() == 0) {
				// "result" maybe blank
				if (Integer.parseInt(result.trim()) > 1)
					return true; // >1 to exclude itself
			}
			else {
				logger.error("Error: command " + command + " failed, code = "
						+ proc.exitValue());
			}
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
		}
		finally {
			if (bin != null) {
				try {
					bin.close();
				}
				catch (IOException e) {}
			}
		}
		return false;
	}

	/**
	 * start system timer tasks 
	 */
	private void startSystemTimerTasks() {
		logger.info("Creating TimerTasks for ServerWatch and MetricsTask");
		/* start ServerWatch TimerTask */
		java.util.Timer timer = new java.util.Timer();
		ServerWatch sw = new ServerWatch(this);
		// schedule to run in every 5 minutes, delay 1 minute
		timer.schedule(sw, 60 * 1000, 5 * 60 * 1000);

		/* start MetricsTask to purge aged records from metrics_logger */
		MetricsTask metricsTask = new MetricsTask();
		// schedule to run in every hour, start at the beginning of next hour
		Calendar calendar = Calendar.getInstance();
		//calendar.roll(Calendar.HOUR_OF_DAY, true); // roll up a hour
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		// if it's too close to the end of hour, roll up another one
		if (calendar.get(Calendar.MINUTE) > 50)
			calendar.add(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		timer.schedule(metricsTask, calendar.getTime(), 60 * 60 * 1000);
		
		systemTaskPool.add(timer);
	}

	/**
	 * stop system timer tasks
	 */
	private void stopSystemTimerTasks() {
		if (systemTaskPool.size() > 0) {
			logger.info("Stopping System Timer Tasks...");
			Iterator<?> it = systemTaskPool.iterator();
			while (it.hasNext()) { // more tasks?
				Object obj = it.next();
				if (obj instanceof java.util.Timer) {
					java.util.Timer tmr = (java.util.Timer) obj;
					tmr.cancel(); // stop the timer
				}
			}
			systemTaskPool.clear();
			logger.info("all system timer tasks are stopped.");
		}
	}

	public void serverAborted(Object server, ServerBaseVo serverBaseVo) {
		if (!keepRunning) {
			logger.warn("serverAborted() - keepRunning is false, do nothing.");
			return; // system shutting down, do nothing
		}
		if (server instanceof QueueListener) {
			JbThread svr = (JbThread) server;
			synchronized (queueThreadFailed) {
				if (keepRunning) {
					queueThreadFailed.setProperty(svr.getServerName(), svr.getServerId());
					logger.info("QueueServer Added to failedQueue for " + svr.getServerName() + "/"
							+ svr.getServerId());
				}
				queueThreadStarted.remove(server);
			}
		}
		else if (server instanceof SocketServer) {
			JbThread svr = (JbThread) server;
			synchronized (socketThreadFailed) {
				if (keepRunning) {
					socketThreadFailed.setProperty(svr.getServerName(), svr.getServerId());
					logger.info("SocketServer Added to failedQueue for " + svr.getServerName() + "/"
							+ svr.getServerId());
				}
				socketThreadStarted.remove(server);
			}
		}
		else if (server instanceof TimerServer) {
			TimerServer svr = (TimerServer) server;
			synchronized (timerThreadFailed) {
				if (keepRunning) {
					timerThreadFailed.add(svr.getTimerServerVo());
					logger.info("TimerServer Added to failedQueue for " + svr.getServerName() + "/"
							+ svr.getServerId());
				}
				timerThreadStarted.remove(server);
			}
		}
		else if (server instanceof MailReader) {
			MailReader svr = (MailReader) server;
			synchronized (mailboxThreadFailed) {
				if (keepRunning) {
					mailboxThreadFailed.add(svr.getMailBoxVo());
					logger.info("MailReader Added to failedQueue for " + svr.getServerName() + "/"
							+ svr.getServerId());
				}
				mailboxThreadStarted.remove(server);
			}
		}
		else { // should never happen
			logger.error("Unknown Server aborted. Properties below:");
			logger.info("serverBaseVo: " + LF + serverBaseVo);
		}
	}

	public void serverExited(Object server) {
		if (server instanceof QueueListener) {
			queueThreadStarted.remove(server);
		}
		else if (server instanceof SocketServer) {
			socketThreadStarted.remove(server);
		}
		else if (server instanceof TimerServer) {
			timerThreadStarted.remove(server);
		}
		else if (server instanceof MailReader) {
			mailboxThreadStarted.remove(server);
		}
		else { // should never happen
			String serverClassName = server==null?"null":server.getClass().getName();
			logger.error("Unknown Server exited: " + serverClassName);
		}
	}

	/**
	 * stop all sub-threads (servers, jobs and tasks)
	 */
	void stopAllSubThreads() {
		HashMap<Object, Future<?>> pool = new HashMap<Object, Future<?>>();
		pool.putAll(queueThreadStarted);
		pool.putAll(socketThreadStarted);
		pool.putAll(mailboxThreadStarted);
		stopThreads(pool);
		
		if (threadPool != null) { // clean up pool
			logger.info("Shutdown Agent ThreadPool...");
			threadPool.shutdown();
			try {
				if (!threadPool.awaitTermination(shutDownDelay, TimeUnit.SECONDS)) {
					threadPool.shutdownNow();
				}
			}
			catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				threadPool.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
			}
		}
		logger.info("Shutdown Agent ThreadPool... Completed");
	}

	/**
	 * stop threads stored in the pool
	 * 
	 * @param pool
	 *            list of threads to be stopped
	 */
	void stopThreads(HashMap<?, ?> pool) {
		// a Hash map was used to store all the sub threads
		logger.info("Stopping AgentThreads...Entering phase 1...");
		Set<?> keys = pool.keySet();
		for (Iterator<?> it = keys.iterator(); it.hasNext(); ) {
			Object obj = it.next();
			if (obj instanceof JbThread) {
				JbThread mt = (JbThread) obj;
				if (mt.isAlive()) {
					mt.keepRunning = false; // stop the thread
				}
				Future<?> future = (Future<?>) pool.get(obj);
				if (future != null && !future.isDone()) {
					future.cancel(false);
				}
			}
		}
		// wait for sub threads to die
		int waitcount = 0;
		boolean allDead;
		do {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}
			logger.info("Stopping AgentThreads...Entering phase 2, count " + waitcount + " ...");
			allDead = true;
			for (Iterator<?> it = keys.iterator(); it.hasNext(); ) {
				JbThread mt = (JbThread) it.next();
				if (mt.isAlive()) {
					allDead = false;
					if (mt instanceof SocketServer && waitcount > 0) {
						mt.interrupt();
					}
					if (mt instanceof MailReader && waitcount > 1) {
						mt.interrupt();
					}
					// break;
				}
			}
		} while (!allDead && waitcount++ <= shutDownDelay); 
			// wait for up to 4 seconds
		// check if all threads are dead
		if (!allDead) {
			logger.info("Stopping AgentThreads...Entering phase 3...");
			for (Iterator<?> it = keys.iterator(); it.hasNext(); ) {
				Object obj = it.next();
				if (obj instanceof JbThread) {
					JbThread mt = (JbThread) obj;
					if (mt.isAlive()) {
						mt.interrupt(); // force the sub thread to quit
					}
				}
			}
		}
		// clear the list
		pool.clear();
		logger.info("all sub threads are stopped.");
	}

	/**
	 * start the monitoring thread
	 */
	public void run() {
		logger.info("JbMain thread started at " + new Date());
		long counter = 0;
		int loops = 720; // display status every hour
		// while (Thread.activeCount() > numberofThreadAtStart+2)
		while (keepRunning) {
			try {
				Thread.yield();
				if ("yes".equalsIgnoreCase(appConf.getProperty("request_gc"))) {
					requestGC(0.2);
				}
				Thread.sleep(5000); // 5 seconds
				counter++;
				if (counter%loops==0) displayStatus();
			}
			catch (InterruptedException e) {
				logger.info("Main thread was interrupted. Process exiting...");
				break;
			}
			catch (Exception e) {
				logger.error("Main thread caught Exception. Process exiting...", e);
				break;
			}
		}
		logger.info(APP_NAME + " main thread ended.");
		System.exit(0); // will start the shutdown hook thread
	}

	/**
	 * to release all resources obtained by servers and tasks
	 */
	void wrapup() {
		logger.info("JbMain.java - entering wrapup method...");
		synchronized (locker) {
			if (firstTime) {
				firstTime = !firstTime;
			}
			else {
				logger.info("wrapup() is already in progress, return.");
				return;
			}
		}
		keepRunning = false;
		// disable SNMP alert during shutdown process
		if ("yes".equalsIgnoreCase(appConf.getProperty("silent_shutdown"))) {
			EventAlert.silent = true; // turn off alert during shutdown
		}
		try {
			stopAllSubThreads();
		}
		catch (Exception e) {
			logger.error("Exception caught during stopSubThreads()", e);
		}
		try {
			stopTimerInstances();
		}
		catch (Exception e) {
			logger.error("Exception caught during stopAgentTimers()", e);
		}
		try {
			stopSystemTimerTasks();
		}
		catch (Exception e) {
			logger.error("Exception caught during stopSystemTimerTasks()", e);
		}
		try {
			updateMetrics();
		}
		catch (Exception e) {
			logger.error("Exception caught during stopAgentStatus()", e);
		}
		resource.wrapup();
		getEventAlert().issueInfoAlert(CLEAR_ALERT,
				APP_NAME + " on " + HostUtil.getHostIpAddress() + ":" + APP_ROOT + " stopped");
	}

	/**
	 * to update metrics, to be scheduled to run hourly by SystemTimerTask
	 */
	static void updateMetrics() {
		for (int i = 0; i < metricsLoggers.size(); i++) {
			MetricsLogger metrics = (MetricsLogger) metricsLoggers.get(i);
			metrics.updateTable();
			// purge aged records from metrics_logger table
			Calendar calendar = new GregorianCalendar();
			// perform purge daily at midnight 1:00AM 
			if (calendar.get(Calendar.HOUR_OF_DAY) == 1) {
				metrics.purge(100);
			}
		}
	}

	void displayStatus() {
		logger.info("Display Servers Status:");
		String[] status = getAllStatus();
		for (int i = 0; i < status.length; i++) {
			String nbr = StringUtils.leftPad((i + 1) + "", 3, "0");
			logger.info("line " + nbr + " - " + status[i]);
		}
	}

	/**
	 * display metrics status for all servers
	 */
	void displayMetrics() {
		displayMetrics(null);
	}

	/**
	 * display metrics status of specified server
	 * 
	 * @param name -
	 *            server name
	 */
	void displayMetrics(String name) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < metricsLoggers.size(); i++) {
			MetricsLogger status = (MetricsLogger) metricsLoggers.get(i);
			//logger.info("Server Type: " + status.getMetricsData().getServerType());
			if (name != null && !status.getMetricsData().getServerType().equals(name)) {
				continue;
			}
			sb.append(status.getMetrics());
		}
		logger.info("Display Servers Metrics:" + LF + sb.toString());
	}

	/**
	 * returns processing status for all servers
	 * 
	 * @return metrics report
	 */
	public static String getMetricsReport() {
		return getMetricsReport(null, null);
	}

	/**
	 * returns processing status for specified server
	 * 
	 * @param name -
	 *            server name
	 * @param days -
	 *            report status of past number of days
	 * @return metrics report
	 */
	public static String getMetricsReport(String name, String days) {
		if (name != null && name.trim().length() == 0) {
			name = null;
		}
		if (days == null) {
			days = "1";
		}
		int goback_days;
		try {
			goback_days = Integer.parseInt(days);
		}
		catch (Exception e) {
			goback_days = 1;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<pre>");
		for (int i = 0; i < metricsLoggers.size(); i++) {
			MetricsLogger status = (MetricsLogger) metricsLoggers.get(i);
			if (name != null && !status.getMetricsData().getServerType().equals(name)) {
				continue;
			}
			sb.append(status.getMetrics());
		}
		sb.append("</pre>");
		for (int i = 0; i < metricsLoggers.size(); i++) {
			MetricsLogger status = (MetricsLogger) metricsLoggers.get(i);
			if (name != null && !status.getMetricsData().getServerType().equals(name)) {
				continue;
			}
			sb.append(status.getAllMetrics(goback_days));
		}
		return sb.toString();
	}

	/**
	 * This is not recommended since gc() is not a real time function.
	 * 
	 * @param usedMemLimit
	 */
	void requestGC(double usedMemLimit) {
		try {
			Runtime runTime = Runtime.getRuntime();

			double freeMem = (double)runTime.freeMemory();
			double totalMem = (double)runTime.totalMemory();
			// check free memory size, call garbage collect when needed.
			if (freeMem / totalMem < usedMemLimit) {
				logger.info("Memory low, call gc. Memory available: " + runTime.freeMemory()
						+ ", Total Memory: " + runTime.totalMemory());
				// runTime.runFinalization();
				runTime.gc();
				logger.info("After gc. Memory available: " + runTime.freeMemory()
						+ ", Total Memory: " + runTime.totalMemory());
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during requestGC()", e);
		}
	}

}
