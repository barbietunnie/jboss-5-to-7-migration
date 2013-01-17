package com.legacytojava.jbatch;

import junit.framework.TestCase;

import com.legacytojava.jbatch.pool.NamedPools;
import com.legacytojava.jbatch.pool.ObjectPool;
import com.legacytojava.jbatch.smtp.SmtpConnection;
import com.legacytojava.message.bo.mailsender.SmtpWrapperUtil;

public class TestJBatch extends TestCase
{
	static int init_count = 0;
	JbMain theMonitor = null;
	final static String LF = System.getProperty("line.separator","\n");

	/**
	 * Constructor for TemplateTest.
	 * @param arg0
	 */
	public TestJBatch(String arg0) throws Exception
	{
	  super(arg0);
	  
	  try
	  {
		  theMonitor = JbMain.getInstance();
		  theMonitor.loadProperties();
		  theMonitor.init();
	  }
	  catch (Exception e)
	  {
		  e.printStackTrace();
		  throw e;
	  }
	  System.out.println("Constructor executed, count="+(++init_count));
	}
	
	public void testJbMain() throws Exception
	{
		System.out.println();
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println(LF+"********** Starting testJbMain ********** ");
		assertTrue(JbMain.metricsLoggers.isEmpty());
		assertNotNull(theMonitor);
		assertTrue(theMonitor.socketThreadFailed.isEmpty());
		assertTrue(theMonitor.timerThreadFailed.isEmpty());
		assertTrue(theMonitor.mailboxThreadFailed.isEmpty());
		
		assertNotNull(JbMain.appConf);
		assertNotNull(JbMain.resource);
		assertNotNull(JbMain.getEventAlert());
		assertNotNull(JbMain.getBatchAppContext());
		
		assertTrue(!theMonitor.isAnotherInstanceRunning());
	}
	
	public void testQueueServers() throws Exception
	{
		System.out.println(LF+"********** Starting testQueueServers **********");
		
		assertTrue(!theMonitor.queueListeners.isEmpty());
		assertTrue(theMonitor.queueThreadStarted.isEmpty());
		theMonitor.startQueueJobs(theMonitor.queueListeners);
		assertTrue(theMonitor.queueThreadFailed.isEmpty());
		assertTrue(!theMonitor.queueThreadStarted.isEmpty());
		Thread.sleep(1000);
		theMonitor.displayMetrics(JbMain.QUEUE_SVR_TYPE);
		theMonitor.stopQueueJobs();
		assertTrue(theMonitor.queueThreadStarted.isEmpty());
	}
	
	public void testMailReaders() throws Exception
	{
		System.out.println(LF+"********** Starting testMailReaders **********");
		
		assertTrue(!theMonitor.mailBoxVos.isEmpty());
		assertTrue(theMonitor.mailboxThreadStarted.isEmpty());
		theMonitor.startMailReaders(theMonitor.mailBoxVos);
		assertTrue(theMonitor.mailboxThreadFailed.isEmpty());
		assertTrue(!theMonitor.mailboxThreadStarted.isEmpty());
		Thread.sleep(2000);
		theMonitor.displayMetrics(JbMain.MAIL_SVR_TYPE);
		theMonitor.stopMailReaders();
		assertTrue(theMonitor.mailboxThreadStarted.isEmpty());
	}
	
	public void testSocketServers() throws Exception
	{
		System.out.println(LF+"********** Starting testSocketServers **********");
		
		assertTrue(!theMonitor.socketServers.isEmpty());
		assertTrue(theMonitor.socketThreadStarted.isEmpty());
		theMonitor.startSocketServers(theMonitor.socketServers);
		assertTrue(theMonitor.socketThreadFailed.isEmpty());
		assertTrue(!theMonitor.socketThreadStarted.isEmpty());
		Thread.sleep(1000);
		theMonitor.displayMetrics(JbMain.SOCKET_SVR_TYPE);
		theMonitor.stopSocketServers();
		assertTrue(theMonitor.socketThreadStarted.isEmpty());
	}
	
	public void restTimerServers() throws Exception
	{
		System.out.println(LF+"********** Starting testTimerServers **********");
		
		assertTrue(!theMonitor.timerServers.isEmpty());
		assertTrue(theMonitor.timerThreadStarted.isEmpty());
		theMonitor.startTimerTasks(theMonitor.timerServers);
		assertTrue(theMonitor.timerThreadFailed.isEmpty());
		assertTrue(!theMonitor.timerThreadStarted.isEmpty());
		Thread.sleep(1000);
		theMonitor.displayMetrics(JbMain.TIMER_SVR_TYPE);
		theMonitor.stopTimerJobs();
		assertTrue(theMonitor.timerThreadStarted.isEmpty());
	}
	
	public void testSmtpConnection() throws Exception
	{
		System.out.println(LF+"********** Starting testSmtpConnection **********");
		{
			NamedPools smtps = SmtpWrapperUtil.getSmtpNamedPools();
			assertNotNull(smtps);
			if (smtps.size() > 0)
			{
				ObjectPool smtp = smtps.getPools().get(0);
				int size = smtp.getSize();
				assertEquals(size, 4);
				SmtpConnection[] conn = new SmtpConnection[size];
				for (int i=0; i<size; i++)
				{
					conn[i] = (SmtpConnection) smtp.getItem();
					assertNotNull(conn[i]);
					conn[i].testConnection(true);
				}
				assertEquals(smtp.getNumberOfFreeItems(), 0);
				for (int i=0; i<size; i++)
				{
					smtp.returnItem(conn[i]);
				}
				assertEquals(smtp.getNumberOfFreeItems(), size);
			}
		}
		
		{
			NamedPools secus = SmtpWrapperUtil.getSecuNamedPools();
			assertNotNull(secus);
			if (secus.size()>0)
			{
				ObjectPool secu = secus.getPools().get(0);
				int size=secu.getSize();
				assertEquals(size,1);
				SmtpConnection[] conn = new SmtpConnection[size];
				for (int i=0; i<size; i++)
				{
					conn[i] = (SmtpConnection) secu.getItem();
					assertNotNull(conn[i]);
					conn[i].testConnection(true);
				}
				assertEquals(secu.getNumberOfFreeItems(), 0);
				for (int i=0; i<size; i++)
				{
					secu.returnItem(conn[i]);
				}
				assertEquals(secu.getNumberOfFreeItems(), size);
			}
		}
	}
		
	public void testMetricsReport() throws Exception
	{
		System.out.println(LF+"********** Starting TestMetricsReport **********");
		System.out.println(JbMain.getMetricsReport(null, "30"));
	}
	
}
