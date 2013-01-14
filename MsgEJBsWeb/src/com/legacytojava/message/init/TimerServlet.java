package com.legacytojava.message.init;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.legacytojava.message.ejb.mailinglist.CheckSchedulesTimerRemote;
import com.legacytojava.message.ejb.mailinglist.MailingListTimerRemote;

/**
 * Servlet implementation class for TimerServlet. It is used to start Mailing
 * List Schedulers, and update schedulers if any schedule has changed.
 */
public class TimerServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	private static final long serialVersionUID = 6199184553095981470L;
	protected static final Logger logger = Logger.getLogger(TimerServlet.class);

	//private ServletContext servletContext;
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public TimerServlet() {
		super();
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.warn("doGet() - " + request.getQueryString());
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.warn("doPost() - " + request.getQueryString());
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
		super.init();
		//servletContext = getServletContext();
		initLog4J();
		try {
			startTimerEjb();
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw new ServletException(e.getMessage());
		}
		//timer = new CheckScheduleChangesTimer(getServletContext(), this);
		//timer.schedule();
	}

	private void initLog4J() {
		Properties props = new Properties();
		Properties sysProps = System.getProperties();
		URL propsUrl = getAsUrl("log4j.extra.properties");
		if (propsUrl != null) {
			try {
				// load the properties using the URL (from the CLASSPATH)
				props.load(propsUrl.openStream());
				props.list(System.out);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Add the key/properties from the application-specific properties to
		// the System Properties.
		sysProps.putAll(props);
		
		URL log4jUrl = getAsUrl("log4j-timer.xml");
		boolean jbossLog4j = true;
		if (jbossLog4j) return; // use jboss's log4j.xml
		// following codes are ignored.
		if (log4jUrl != null) {
			System.out.println("log4j-timer.xml provided, configure log4j...");
			// An URL (from the CLASSPATH) that points to the Log4J XML
			// configuration file was provided, so use Log4J's
			// DOMConfigurator with the URL to initialize Log4J.
			DOMConfigurator.configure(log4jUrl);
		}
		else {
			System.out.println("log4j-timer.xml not provided, use default...");
			// An URL that points to the Log4J XML configuration file wasn't
			// provided, so use Log4J's BasicConfigurator to initialize Log4J.
			BasicConfigurator.configure();
		}
	}
	
	private URL getAsUrl(String name) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(name);
		return url;
	}

	//@EJB(mappedName="ejb/MailingListTimer", beanInterface=MailingListTimerLocal.class)
	@EJB(lookup="java:app/MsgEJBs/MailingListTimer!com.legacytojava.message.ejb.mailinglist.MailingListTimerRemote")
	private MailingListTimerRemote mailingListTimer;
	//@EJB(mappedName="ejb/CheckSchedulesTimer", beanInterface=CheckSchedulesTimerLocal.class)
	@EJB(lookup="java:app/MsgEJBs/CheckSchedulesTimer!com.legacytojava.message.ejb.mailinglist.CheckSchedulesTimerRemote")
	private CheckSchedulesTimerRemote checkScehdulesTimer;

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	void startTimerEjb() {
		logger.info("startTimerEjb() - calling MailingListTimer");
		try {
			mailingListTimer.scheduleTimerTasks();
			logger.info("startTimerEjb() - MailingListTimer started");
			checkScehdulesTimer.startTimers();
			logger.info("startTimerEjb() - CheckSchedulesTimer started");
		}
		catch (EJBException e) {
			logger.error("EJBException caught", e);
			throw e;
		}
	}
}