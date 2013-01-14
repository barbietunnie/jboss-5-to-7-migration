package com.legacytojava.jbatch;

/**
 * define standard MBean interface for JBatch
 */
public interface JbMainMBean {
	String[] getQueueJobs();
	String[] getSocketServers();
	String[] getTimerJobs();
	String[] getMailReaders();
	boolean restartQueueJobs();
	boolean stopQueueJobs();
	boolean restartSocketServers();
	boolean stopSocketServers();
	boolean restartMailReaders();
	boolean stopMailReaders();
	boolean restartTimerJobs();
	boolean stopTimerJobs();
}
