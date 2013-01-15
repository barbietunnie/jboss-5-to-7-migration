package com.legacytojava.timerejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bo.mailreader.DuplicateCheckDao;
import com.legacytojava.message.bo.mailreader.DuplicateCheckJdbcDao;
import com.legacytojava.message.bo.mailreader.MailReaderBoImpl;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.mailbox.MailBoxDao;
import com.legacytojava.message.vo.MailBoxVo;

/**
 * Session Bean implementation class MailReader
 */
/*
 * Use Startup annotation to ensures that bean is loaded when server is started
 * so that lifecycle callback methods (@PostConstruct) will be invoked.
 */
@Startup
@Singleton(name="MailReader",mappedName="ejb/MailReader")
//@Stateless(name="MailReader",mappedName="ejb/MailReader")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(MailReaderRemote.class)
@Local(MailReaderLocal.class)
public class MailReader implements MailReaderRemote, MailReaderLocal {
	protected static final Logger logger = Logger.getLogger(MailReader.class);
	@Resource
	private SessionContext context;
	
	private AbstractApplicationContext factory = null;
	private AbstractApplicationContext ruleEngineFactory = null;
	private MailBoxDao mailBoxDao;
	private ExecutorService pool = null;
	private DuplicateCheckDao duplicateCheck = null;
	final int MAX_POOL_SIZE = 20;
	private int MINIMUM_WAIT = 5; // seconds
	private int INTERVAL = 10;
	
	private final List<MailReaderBoImpl> readers = new ArrayList<MailReaderBoImpl>();

    /**
     * Default constructor. 
     */
    public MailReader() {
		factory = SpringUtil.getAppContext();
		ruleEngineFactory = new ClassPathXmlApplicationContext("spring-ruleengine-jee.xml");
		
		mailBoxDao = (MailBoxDao) factory.getBean("mailBoxDao");
		duplicateCheck = (DuplicateCheckJdbcDao) factory.getBean("duplicateCheck");
		pool = Executors.newFixedThreadPool(MAX_POOL_SIZE);
		//pool = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
		//pool = Executors.newCachedThreadPool();
    }

    @PostConstruct
	public void starttUp() {
		logger.info("Entering startUp() method, starting Mail Readers...");
		startMailReader(60);
	}

	public void startMailReader(int interval) {
 		stopMailReader(); // stop pending timers
		readers.clear();
		readers.addAll(getMailReaders());
		INTERVAL = interval < MINIMUM_WAIT ? MINIMUM_WAIT : interval;
		 // at least 5 seconds
		context.getTimerService().createTimer(INTERVAL * 1000, "MailReader");
		logger.info("startMailReader(): MailReader Timer created to expire after " + INTERVAL
				+ " seconds.");
		// create timer to purge aged records from MSGIDDUP table.
		context.getTimerService().createTimer(60 * 60 * 1000, "DuplicateCheck");
		logger.info("startMailReader(): DuplicateCheck Timer created to expire after 1 hour.");
	}

	private List<MailReaderBoImpl> getMailReaders() {
		List<MailBoxVo> mailBoxVos = null;
		if (ClientUtil.isTrialPeriodEnded() && !ClientUtil.isProductKeyValid()) {
			mailBoxVos = mailBoxDao.getAllForTrial(true);
		}
		else {
			mailBoxVos = mailBoxDao.getAll(true); // get all mailboxes
		}
		List<MailReaderBoImpl> readerList = new ArrayList<MailReaderBoImpl>();
		if (mailBoxVos == null || mailBoxVos.size() == 0) {
			logger.warn("getMailReaders(): no mailbox found, exit.");
			return readerList;
		}
		for (int i = 0; i < mailBoxVos.size(); i++) {
			MailBoxVo mailBoxVo = (MailBoxVo) mailBoxVos.get(i);
			mailBoxVo.setFromTimer(true);
			MailReaderBoImpl reader = new MailReaderBoImpl(mailBoxVo, factory, ruleEngineFactory);
			readerList.add(reader);
			logger.info("getMailReaders(): loaded MailReaderBo: " + mailBoxVo.getUserId() + "/"
					+ mailBoxVo.getHostName());
		}
		return readerList;
	}

	public void stopMailReader() {
		TimerService timerService = context.getTimerService();
		Collection<?> timers = timerService.getTimers();
		if (timers != null) {
			for (Iterator<?> it=timers.iterator(); it.hasNext(); ) {
				Timer timer = (Timer)it.next();
				stopTimer(timer);
			}
		}
	}

	private void stopTimer(Timer timer) {
		if (timer != null) {
			try {
				timer.cancel();
				logger.info("stopTimer(): timer stopped.");
			}
			catch (NoSuchObjectLocalException e) {
				logger.error("NoSuchObjectLocalException caught", e);
			}
			catch (IllegalStateException e) {
				logger.error("IllegalStateException caught", e);
			}
		}
	}

	private java.util.Date lastUpdtTime = new java.util.Date();
	
	@Timeout
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void programmaticTimeout(Timer timer) {
		if (readers.isEmpty()) {
			logger.warn("########## Mail Reader list is empty ##########");
			return;
		}
		logger.info("programmaticTimeout() - " + timer.getInfo());
		if ("DuplicateCheck".equals(timer.getInfo())) {
			duplicateCheck.purge(24); // purge records older than 24 hours
			context.getTimerService().createTimer(60 * 60 * 1000, "DuplicateCheck");
		}
		else { // MailReader
			Future<?>[] futures = new Future[readers.size()];
			for (int i = 0; i < futures.length; i++) {
				MailReaderBoImpl reader = (MailReaderBoImpl) readers.get(i);
				try {
					futures[i] = pool.submit(reader);
				}
				catch (Exception e) {
					logger.fatal("IOException caught", e);
					throw new EJBException(e.getMessage());
				}
				try { // give each thread some time to make initial connection
					Thread.sleep(500);
				}
				catch (InterruptedException e) {}
			}
			// wait for all threads to complete
			for (int i = 0; i < futures.length; i++) {
				Future<?> future = futures[i];
				try {
					future.get();
				}
				catch (InterruptedException e) {}
				catch (ExecutionException e) {
					logger.error("ExecutionException caught", e);
				}
			}
			java.util.Date currTime = new java.util.Date();
			if ((currTime.getTime() - lastUpdtTime.getTime()) > (15*60*1000)) {
				// reload mailboxes every 15 minutes
				readers.clear();
				readers.addAll(getMailReaders());
				lastUpdtTime = currTime;
			}
			context.getTimerService().createTimer(INTERVAL * 1000, "MailReader");
		}
	}
	
	/*
	 * XXX NOT working with JBoss 7.1.
	 */
	@Schedule(second="0/30", info="Single Timer") // expire once on the next 30th second.
	public void automaticTimeout(Timer timer) {
		logger.info("Automatic timeout occured : "  + timer.getInfo());
		startMailReader(60);
	}

	@PreDestroy
	public void shutdownThreadPool() {
		if (pool != null) {
			pool.shutdown();
		}
		try {
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				pool.shutdownNow();
			}
		}
		catch (InterruptedException e) {}
		readers.clear();
	}

}
