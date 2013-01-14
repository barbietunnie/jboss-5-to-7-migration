package com.legacytojava.jbatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bo.mailreader.DuplicateCheckDao;
import com.legacytojava.message.bo.mailreader.MailProcessor;
import com.legacytojava.message.constant.MailProtocol;
import com.legacytojava.message.dao.mailbox.MailBoxDao;
import com.legacytojava.message.vo.MailBoxVo;

/**
 * <pre>
 * Monitors the given mailbox for new e-mails
 * - initialize required objects.
 * - open the mail session, store and folder object.
 * - for each email in the opened folder
 * 	. parse email header and body into serialized components
 * 	. create a MessageBean with its components
 * 	. extract attachments if there are any, and save to the MessageBean
 * 	. write the MessageBean to a output queue
 * 	. set DELETE flag for the message and issue folder.close()
 * </pre>
 */
public class MailReader extends JbThread implements Serializable, JbEventListener,
		ConnectionListener {
	private static final long serialVersionUID = 5835053994481156179L;
	private static final Logger logger = Logger.getLogger(MailReader.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();

	private final MailBoxVo mailBoxVo;
	private final JbEventBroker eventBroker;
	protected final String processorName;
	private final boolean debugSession = false;
	final Session session;

	private static final int MAX_READ_PER_PASS = 100;
	private int RETRY_MAX = 10; // , default to 10, -1 -> infinite retry
	private final int readPerPass;
	private int freq;

	private Folder folder = null;
	private final int[] retry_freq = { 5, 10, 10, 20, 20, 20, 30, 30, 30, 30, 60, 60, 60, 60, 60 };
		// in seconds
	private static final int RETRY_FREQ = 120; // in seconds
	private static final int MAX_NUM_THREADS = 20; // limit to 20 threads
	private int sleep_for = 0;

	/**
	 * create a MailReader instance
	 * 
	 * @param props -
	 *            mailbox properties
	 */
	MailReader(MailBoxVo mailBoxVo) {
		super(mailBoxVo, JbMain.MAIL_SVR_TYPE);
		this.mailBoxVo = mailBoxVo; // for convenience only
		logger.info("in Constructor - MailBox Properties:" + LF + mailBoxVo);

		eventBroker = new JbEventBroker();
		eventBroker.addAgentEventListener(this);

		processorName = mailBoxVo.getProcessorName();
		if (processorName == null) {
			throw new IllegalArgumentException(
					"processor_name must be defined in mailbox Properties");
		}
		MSG2PROC = mailBoxVo.getMessageCount();

		MAX_CLIENTS = mailBoxVo.getThreads();
		MAX_CLIENTS = MAX_CLIENTS > MAX_NUM_THREADS ? MAX_NUM_THREADS : MAX_CLIENTS;
		MAX_CLIENTS = MAX_CLIENTS <= 0 ? 1 : MAX_CLIENTS; // sanity check
		
		int MIN_WAIT = 2 * 1000; // default = 2 seconds
		int MAX_WAIT = 60 * 1000; // up to a minute

		Integer temp;
		if ((temp = mailBoxVo.getMinimumWait()) != null) {
			MIN_WAIT = Math.abs(temp.intValue() * 1000);
		}
		if ((temp = mailBoxVo.getRetryMax()) != null) {
			RETRY_MAX = temp.intValue();
		}
		logger.info("Minimum wait in seconds = " + MIN_WAIT / 1000);
		logger.info("Maximum number of retries = " + RETRY_MAX);

		// number of e-mails (="readPerPass") to process per read
		int read_per_pass = mailBoxVo.getReadPerPass();
		if (read_per_pass <= 0) {
			read_per_pass = 5; // default to 5
		}
		readPerPass = read_per_pass;
		// final - accessible by inner class

		// issue a read to the mailbox in every "freq" MILLIseconds
		freq = MIN_WAIT + readPerPass * 100;
		if (freq > MAX_WAIT) {
			// wait for up to MAX_WAIT seconds between reads.
			freq = MAX_WAIT;
		}
		
		// enable RFC2231 support in parameter lists, since javamail 1.4
		// Since very few existing programs support RFC2231, disable it for now
		/*
		System.setProperty("mail.mime.encodeparameters", "true");
		System.setProperty("mail.mime.decodeparameters", "true");
		System.setProperty("mail.mime.encodefilename", "true");
		System.setProperty("mail.mime.decodefilename", "true");
		*/
		
		// to make the reader more tolerable
		System.setProperty("mail.mime.multipart.ignoremissingendboundary", "true");
		System.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true");
		
		Properties m_props = (Properties) System.getProperties().clone();
		m_props.setProperty("mail.debug", "true");
		m_props.setProperty("mail.debug.quote", "true");

		/*
		 * POP3 - properties of com.sun.mail.pop3 
		 * mailbox can be accessed via URL: pop3://user:password@host:port/INBOX
		 */
		// set timeouts in milliseconds. default for both is infinite
		// Socket connection timeout
		m_props.setProperty("mail.pop3.connectiontimeout", "900000");
		// Socket I/O timeout
		m_props.setProperty("mail.pop3.timeout", "750000");
		// m_props.setProperty("mail.pop3.rsetbeforequit","true");
		/* issue RSET before QUIT, default: false */

		/* IMAP - properties of com.sun.mail.imap */
		// set timeouts in milliseconds. default for both is infinite
		// Socket connection timeout
		m_props.setProperty("mail.imap.connectiontimeout", "900000");
		// Socket I/O timeout
		m_props.setProperty("mail.imap.timeout", "750000");
		
		// Certain IMAP servers do not implement the IMAP Partial FETCH
		// functionality properly
		// set Partial fetch to false to workaround exchange server 5.5 bug
		m_props.setProperty("mail.imap.partialfetch","false");
		
		// Get a Session object
		if ("yes".equalsIgnoreCase(mailBoxVo.getUseSsl())) {
			m_props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			m_props.setProperty("mail.pop3.socketFactory.fallback", "false");
			m_props.setProperty("mail.pop3.port", mailBoxVo.getPortNumber()+"");
			m_props.setProperty("mail.pop3.socketFactory.port", mailBoxVo.getPortNumber()+"");
			session = Session.getInstance(m_props);
		}
		else {
			session = Session.getInstance(m_props, null);
		}
		
		jbPool = Executors.newFixedThreadPool(MAX_CLIENTS);
	}

	public static void main(String[] args) {
		MailBoxDao mailBoxDao = (MailBoxDao) JbMain.getBatchAppContext().getBean("mailBoxDao");
		List<MailBoxVo> mboxList = mailBoxDao.getAll(true);
		if (mboxList == null) return;
		
		for (Iterator<MailBoxVo> it=mboxList.iterator(); it.hasNext(); ) {
			MailBoxVo vo = it.next();
			MailReader reader = new MailReader(vo);
			try {
				reader.start();
				reader.join();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	/**
	 * return MailReader metrics, used by MBean
	 * 
	 * @return a List containing metrics data
	 */
	public List<String> getStatus() {
		List<String> v = new ArrayList<String>();
		if (mailBoxVo != null) {
			v.add("MailReader: user=" + mailBoxVo.getUserId() + ", host="
					+ mailBoxVo.getHostName() + ", #Threads=" + MAX_CLIENTS);
			v.add(mailBoxVo.toString());
		}
		return v;
	}

	/**
	 * run the MailReader, invoke Application plug-in to process mails
	 * @throws MessagingException 
	 */
	public void run() {
		logger.info("thread " + getName() + " running");

		final String _folder = mailBoxVo.getFolderName();
		boolean normal_exit = false;
		session.setDebug(true); // DON'T CHANGE THIS

		String protocol = mailBoxVo.getProtocol();
		if (!MailProtocol.IMAP.equalsIgnoreCase(protocol)
				&& !MailProtocol.POP3.equalsIgnoreCase(protocol)) {
			eventBroker.putException(new Exception("Invalid protocol " + protocol));
			return;
		}

		Store store = null;
		try {
			// Get a Store object
			store = session.getStore(protocol);
			store.addConnectionListener(this);
		}
		catch (NoSuchProviderException pe) {
			logger.fatal("NoSuchProviderException caught during session.getStore()", pe);
			eventBroker.putException(pe);
			return;
		}

		try {
			connect(store, 0, RETRY_MAX); // could fail due to authentication error
			folder = getFolder(store, 0, 1); // retry once on folder

			// reset debug mode
			session.setDebug(debugSession);

			final Thread thisThread = this;
			// only imap support MessageCountListener
			if (MailProtocol.IMAP.equalsIgnoreCase(protocol)) {
				// Add messageCountListener to listen to new messages for IMAP
				addMsgCountListener(folder, thisThread, _folder);
			}

			if (MailProtocol.POP3.equalsIgnoreCase(protocol)) {
				pop3();
			}
			else { // imap protocol
				imap();
			}
		}
		catch (InterruptedException e) {
			normal_exit = true;
			logger.info("InterruptedException caught. Process exiting...", e);
		}
		catch (MessagingException e) {
			logger.fatal("MessagingException caught, exiting...", e);
			update(MetricsLogger.PROC_ERROR, 1);
			eventBroker.putException(e);
		}
		catch (ExecutionException e) {
			logger.fatal("ExecutionException caught, exiting...", e);
			update(MetricsLogger.PROC_ERROR, 1);
			eventBroker.putException(e);
		}
		finally {
			try {
				if (folder != null && folder.isOpen()) {
					folder.close(false);
				}
				store.close();
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}
		}

		if (normal_exit) { // normal exit
			JbMain.getInstance().serverExited(this);
		}
		else { // recycle the server
			JbMain.getInstance().serverAborted(this, mailBoxVo);
		}
		logger.info("MailReader thread " + getName() + " ended");
	} // end of run()

	private void pop3() throws MessagingException, InterruptedException, ExecutionException {
		final String _user = mailBoxVo.getUserId();
		final String _host = mailBoxVo.getHostName();
		final String _folder = mailBoxVo.getFolderName();
		int retries = 0;
		while (keepRunning) {
			try {
				if (folder.isOpen()) {
					folder.close(false);
				}
			}
			catch (MessagingException em) {
				logger.error("MessagingException caught during folder.close", em);
			}
			Thread.yield();
			try {
				Thread.sleep(freq); // terminate the thread if interrupted
				// reopen the folder in order to pick up the new
				// messages
				folder.open(Folder.READ_WRITE);
			}
			catch (MessagingException e) {
				logger.error("Failed to open folder " + _user + "@" + _host + ":" + _folder);
				logger.error("MessagingException caught", e);
				update(MetricsLogger.PROC_ERROR, 1);
				if (retries++ < RETRY_MAX || RETRY_MAX < 0) {
					// wait for a while and try to reopen the folder
					if (retries < retry_freq.length) {
						sleep_for = retry_freq[retries];
					}
					else {
						sleep_for = RETRY_FREQ;
					}
					logger.error("Exception caught during folder.open(), retry(=" + retries
							+ ") in " + sleep_for + " seconds");
					Thread.sleep(sleep_for * 1000);
						// terminate the thread if interrupted
					continue;
				}
				else {
					logger.fatal("All retries failed for " + _user + "@" + _host + ":" + _folder);
					throw e;
				}
			}
			if (retries > 0) {
				logger.error("Folder " + _user + "@" + _host + ":" + _folder + " opened after  "
						+ retries + " retries");
				retries = 0; // reset retry counter
			}
			Date start_tms = new Date();
			int msgCount = 0;
			if (keepRunning && (msgCount = folder.getMessageCount()) > 0) {
				logger.info(_user + "'s " + _host + " has " + msgCount + " messages.");
				// "readPerPass" is used so the flagged messages will be
				// purged more often
				int msg2proc = Math.min(msgCount, readPerPass);

				// if we can't keep up, process more messages in each
				// cycle
				if (msgCount > msg2proc * 10) {
					msg2proc *= 10;
				}
				else if (msgCount > msg2proc * 5) {
					msg2proc *= 5;
				}
				msg2proc = msg2proc > MAX_READ_PER_PASS ? MAX_READ_PER_PASS : msg2proc;
				logger.info("number of messages to be processed in this cycle: " + msg2proc);
				Message[] msgs = null;
				try {
					msgs = folder.getMessages(1, msg2proc);
				}
				catch (IndexOutOfBoundsException ie) {
					logger.error("IndexOutOfBoundsException caught, retry with getMessage()", ie);
					update(MetricsLogger.PROC_ERROR, 1);
					msgs = folder.getMessages();
					logger.info("Retry with folder.getMessage() is successful.");
				}
				executeThreads(msgs); // process the messages
				if (keepRunning) { // eventListener could set it to false
					folder.close(true);
						// "true" to delete the flagged messages
					logger.info(msgs.length + " messages have been purged from pop3 mailbox.");
				}
				long proc_time = new Date().getTime() - start_tms.getTime();
				update(MetricsLogger.PROC_TIME, (int) proc_time, msgs.length);
				if (MSG2PROC > 0 && getMetricsLogger().getMetricsData().getTotalInput() > MSG2PROC) {
					keepRunning = false;
					this.interrupt();
				}
			}
		} // end of while
	}
	
	private void imap() throws MessagingException, InterruptedException, ExecutionException {
		folder.open(Folder.READ_WRITE);
		/*
		 * fix for iPlanet: iPlanet wouldn't pick up the existing
		 * messages, the MessageCountListener may not be implemented
		 * correctly for iPlanet.
		 */
		if (keepRunning && folder.getMessageCount() > 0) {
			logger.info(mailBoxVo.getUserId() + "'s " + mailBoxVo.getFolderName() + " has "
					+ folder.getMessageCount() + " messages.");
			Date start_tms = new Date();
			Message msgs[] = folder.getMessages();
			executeThreads(msgs);
			if (keepRunning) { // eventListener could set it to false
				folder.expunge(); // remove messages marked as DELETED
				logger.info(msgs.length + " messages have been expunged from imap mailbox.");
			}
			long proc_time = new Date().getTime() - start_tms.getTime();
			update(MetricsLogger.PROC_TIME, (int) proc_time, msgs.length);
		}
		/* end of fix for iPlanet */

		while (keepRunning) {
			Thread.sleep(freq); // sleep for freq milliseconds
			// This is to force the IMAP server to send us
			// EXISTS notifications.
			folder.getMessageCount();
		}		
	}
	
	/**
	 * Add messageCountListener to listen to new messages for IMAP
	 * 
	 * @param folder
	 * @param thisThread
	 * @param _folder
	 */
	private void addMsgCountListener(final Folder folder, final Thread thisThread,
			final String _folder) {
		folder.addMessageCountListener(new MessageCountAdapter() {
			private final Logger logger = Logger.getLogger(MessageCountAdapter.class);
			public void messagesAdded(MessageCountEvent ev) {
				Message[] msgs = ev.getMessages();
				logger.info("Got " + msgs.length + " new messages from " + _folder);
				Date start_tms = new Date();
				try {
					executeThreads(msgs);
					// remove messages marked DELETED
					if (keepRunning) { // eventListener could set it to false
						folder.expunge();
						logger.info(msgs.length
										+ " messages have been expunged from imap mailbox.");
					}
				}
				catch (InterruptedException e) {
					logger.info("InterruptedException caught. Process exiting...", e);
					thisThread.interrupt();
				}
				catch (MessagingException ex) {
					logger.fatal("MessagingException caught", ex);
					update(MetricsLogger.PROC_ERROR, 1);
					eventBroker.putException(ex);
				}
				catch (ExecutionException e) {
					logger.fatal("ExecutionException caught", e);
					update(MetricsLogger.PROC_ERROR, 1);
					eventBroker.putException(e);
				}
				finally {
					long proc_time = new Date().getTime() - start_tms.getTime();
					update(MetricsLogger.PROC_TIME, (int) proc_time, msgs.length);
				}
				if (MSG2PROC > 0 && getMetricsLogger().getMetricsData().getTotalInput() > MSG2PROC) {
					keepRunning = false;
					thisThread.interrupt();
				}
			}
		}); // end of imap folder.addMessageCountListener
	}
	
	/**
	 * process e-mails using Executor. Each thread process an email at a time.
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	private void executeThreads(Message[] msgs) throws InterruptedException, ExecutionException {
		if (msgs == null || msgs.length == 0) return;
		Future<?>[] futures = new Future<?>[msgs.length];
		// update input count
		update(MetricsLogger.PROC_INPUT, msgs.length);
		ThreadPoolExecutor tPool = (ThreadPoolExecutor) jbPool;
		for (int i = 0; i < msgs.length; i++) {
			JmsProcessor jmsProcessor = (JmsProcessor) JbMain.getBatchAppContext().getBean(
					"jmsProcessor");
			DuplicateCheckDao duplicateCheck = (DuplicateCheckDao) JbMain.getBatchAppContext()
					.getBean("duplicateCheck");
			// RunnableProcessor processor = (RunnableProcessor)
			// JbMain.getCtxFactory().getBean(
			// processorName);
			MailProcessor processor = new MailProcessor(jmsProcessor, mailBoxVo, duplicateCheck);
			Message[] msg = { msgs[i] };
			processor.prepare(msg, eventBroker);
			Future<?> future = jbPool.submit(processor);
			// number worker threads
			update(MetricsLogger.PROC_WORKER, tPool.getPoolSize());
			futures[i] = future;
		}
		// wait for all threads to complete
		for (int i = 0; i < futures.length; i++) {
			Future<?> future = futures[i];
			future.get();
		}
		// assume the processor produces one output per input
		update(MetricsLogger.PROC_OUTPUT, msgs.length);
	}

	/**
	 * implement ConnectionListener interface
	 * 
	 * @param e -
	 *            Connection event
	 */
	public void opened(ConnectionEvent e) {
		logger.info(">>> ConnectionListener: connection opened()");
	}

	/**
	 * implement ConnectionListener interface
	 * 
	 * @param e -
	 *            Connection event
	 */
	public void disconnected(ConnectionEvent e) {
		logger.info(">>> ConnectionListener: connection disconnected()");
	}

	/**
	 * implement ConnectionListener interface
	 * 
	 * @param e -
	 *            Connection event
	 */
	public void closed(ConnectionEvent e) {
		logger.info(">>> ConnectionListener: connection closed()");
	}

	/* end of the implementation */

	/**
	 * connect to Store, with retry logic
	 * 
	 * @param store -
	 *            Store object
	 * @param retries -
	 *            number of retries performed
	 * @param RETRY_MAX -
	 *            number of retries to be performed before throwing Exception
	 * @throws MessagingException 
	 *             when retries reached the RETRY_MAX
	 */
	void connect(Store store, int retries, int RETRY_MAX) throws MessagingException {
		// -1 to use the default port
		logger.info("Port used: " + mailBoxVo.getPortNumber());

		if (retries > 0) { // retrying, close store first
			try {
				store.close();
			}
			catch (MessagingException e) {
				logger.error("Exception caught during store.close on retry", e);
			}
		}

		try {
			// connect
			store.connect(mailBoxVo.getHostName(), mailBoxVo.getPortNumber(),
					mailBoxVo.getUserId(), mailBoxVo.getUserPswd());
		}
		catch (MessagingException me) {
			if (retries < RETRY_MAX || RETRY_MAX < 0) {
				if (retries < retry_freq.length) {
					sleep_for = retry_freq[retries];
				}
				else {
					sleep_for = RETRY_FREQ;
				}
				logger.error("MessagingException caught during store.connect, retry(=" + retries
						+ ") in " + sleep_for + " seconds");
				try {
					Thread.sleep(sleep_for * 1000);
				}
				catch (InterruptedException e) {
					logger.warn("InterruptedException caught", e);
				}
				connect(store, ++retries, RETRY_MAX);
			}
			else {
				logger.fatal("Exception caught during store.connect, all retries failed...");
				throw me;
			}
		}
	}

	/**
	 * retrieve Folder with retry logic
	 * 
	 * @param store -
	 *            Store object
	 * @param retries -
	 *            number of retries performed
	 * @param RETRY_MAX -
	 *            number of retries to be performed before throwing Exception
	 * @return Folder instance
	 * @throws MessagingException 
	 *             when retries reached RETRY_MAX
	 */
	Folder getFolder(Store store, int retries, int RETRY_MAX) throws MessagingException {
		try {
			// Open a Folder
			folder = store.getFolder(mailBoxVo.getFolderName());

			if (folder == null || !folder.exists()) {
				throw new MessagingException("Invalid folder " + mailBoxVo.getFolderName());
			}
		}
		catch (MessagingException me) {
			if (retries < RETRY_MAX || RETRY_MAX < 0) {
				if (retries < retry_freq.length) {
					sleep_for = retry_freq[retries];
				}
				else {
					sleep_for = RETRY_FREQ;
				}
				logger.error("MessagingException caught during store.getFolder, retry(=" + retries
						+ ") in " + sleep_for + " seconds");
				try {
					Thread.sleep(sleep_for * 1000);
				}
				catch (InterruptedException e) {
					logger.warn("InterruptedException caught", e);
				}				
				return getFolder(store, ++retries, RETRY_MAX);
			}
			else {
				logger.fatal("Exception caught during store.getFolder, all retries failed");
				throw me;
			}
		}
		return folder;
	}

	/**
	 * implement JbEventListener interface
	 * 
	 * @param e -
	 *            JbEvent event
	 */
	public void exceptionCaught(JbEvent e) {
		Exception excep = e.getException();
		logger.error("Exception caught by MailReader", excep);
		if (excep instanceof javax.jms.JMSException) {
			logger.error("Stopping the mail reader due to JMSException");
			keepRunning = false;
			JbMain.getEventAlert().issueExcepAlert(JbMain.MAIL_SVR,
					"MailReader caught JMSException, stopping reader " + mailBoxVo.getUserId(),
					excep);
		}
		else if (excep instanceof javax.mail.MessagingException) {
			logger.error("Stopping the mail reader due to MessagingException");
			keepRunning = false;
			JbMain.getEventAlert().issueExcepAlert(
					JbMain.MAIL_SVR,
					"MailReader caught MessagingException, stopping reader "
							+ mailBoxVo.getUserId(), excep);
		}
		else if (excep instanceof java.sql.SQLException) {
			// TODO: refresh db connection
			JbMain.getEventAlert().issueExcepAlert(JbMain.MAIL_SVR,
					"MailReader caught SQLException, reader " + mailBoxVo.getUserId(), excep);
		}
		else if (excep instanceof Exception) {
			// unchecked Exception, stop the mail reader
			logger.fatal("Stopping the mail reader due to unchecked Exception");
			keepRunning = false;
			JbMain.getEventAlert().issueExcepAlert(
					JbMain.MAIL_SVR,
					"MailReader caught Unchecked Exception, stopping reader "
							+ mailBoxVo.getUserId(), excep);
		}
	}

	/**
	 * implement JbEventListener interface
	 * 
	 * @param e -
	 *            JbEvent event
	 */
	public void errorOccured(JbEvent e) {
		Exception excep = e.getException();
		logger.error("Error caught by MailThread", excep);
	}
	// end of the implementation

	public MailBoxVo getMailBoxVo() {
		return mailBoxVo;
	}
}