package com.legacytojava.jbatch;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.legacytojava.message.util.ServiceLocator;

public class SpringUtil {
	static final Logger logger = Logger.getLogger(SpringUtil.class);
	
	private static AbstractApplicationContext applContext = null;
	private static AbstractApplicationContext daoAppCtx = null;
	
	/**
	 * If it's running in JBoss server, it loads a set of xmls using JNDI's,
	 * otherwise loads a set of xmls using mysql data source.
	 * @return ApplicationContext
	 */
	public static AbstractApplicationContext getAppContext() {
		if (applContext == null) {
			String[] fileNames = null;
			if (isRunningInJBoss()) {
				logger.info("getAppContext() - Running under JBoss, load server xmls");
				fileNames = getServerConfigXmlFiles();
			}
			else {
				if (JbMain.getNumberOfThreadAtStart() < 0) {
					logger.info("getAppContext() - running standalone, load standalone xmls");
					fileNames = getStandaloneConfigXmlFiles();
				}
				else {
					logger.info("getAppContext() - running batch standalone, load batch xmls");
					fileNames = getBatchConfigXmlFiles();
				}
			}
			applContext = new ClassPathXmlApplicationContext(fileNames);
		} 
		return applContext;
	}

	private static boolean ListJBossJndiNames = false;
	
	public static boolean isRunningInJBoss() {
		try {
			// see if it's running under JBoss by doing a JNDI lookup
			ServiceLocator.getDataSource("java:jboss/MessageDS");
			logger.info("isRunningInJBoss() - Running in JBoss.");
			if (ListJBossJndiNames) {
				ServiceLocator.listContext("java:comp");
				ServiceLocator.listContext("java:/queue");
				ServiceLocator.listContext("java:comp/env/jms");
				ServiceLocator.listContext("java:comp/env/jdbc");
				ServiceLocator.listContext("java:jboss");
			}
			return true;
		}
		catch (javax.naming.NamingException e) {
			logger.info("isRunningInJBoss() - Running standalone.");
		}
		return false;
	}

	/**
	 * This method is intended to be used by table creation classes.
	 * @return ApplicationContext
	 */
	public static AbstractApplicationContext getDaoAppContext() {
		if (applContext != null) {
			return applContext;
		}
		else if (daoAppCtx == null) {
			List<String> fnames = new ArrayList<String>();
			fnames.add("classpath*:spring-common-config.xml");
			if (isRunningInJBoss()) {
				logger.info("getDaoAppContext() - Running under JBoss, load jndi_ds xmls");
				fnames.add("classpath*:spring-jmsqueue_jee-config.xml");
			}
			else {
				logger.info("getDaoAppContext() - running standalone, load mysql_ds xmls");
				fnames.add("classpath*:spring-mysql-config.xml");
			}
			daoAppCtx = new ClassPathXmlApplicationContext(fnames.toArray(new String[]{}));
		}
		return daoAppCtx;
	}

	public static Object getBean(AbstractApplicationContext factory, String name) {
		try {
			return factory.getBean(name);
		}
		catch (IllegalStateException e) {
			logger.error("IllegalStateException caught, call 'refresh'", e);
			//String err = e.toString();
			//String regex = ".*BeanFactory.*refresh.*ApplicationContext.*";
			factory.refresh();
			return factory.getBean(name);
		}
	}
	
	private static String[] getBatchConfigXmlFiles() {
		ClassLoader loader = JbMain.class.getClassLoader();
		List<String> cfgFileNames = new ArrayList<String>();
		cfgFileNames.add("classpath:spring-common-config.xml");
		cfgFileNames.add("classpath:spring-mysql-config.xml");
		cfgFileNames.add("classpath:spring-jmsqueue_rmt-config.xml");
		cfgFileNames.add("classpath:spring-jbatch-config.xml");
		URL mreader = loader.getResource("spring-jbatch-mailreader.xml");
		URL msender = loader.getResource("spring-jbatch-mailsender.xml");
		URL testsvrs = loader.getResource("spring-jbatch-testservers.xml");
		if (mreader != null) {
			cfgFileNames.add("classpath:spring-jbatch-mailreader.xml");
		}
		else if (msender != null) {
			cfgFileNames.add("classpath:spring-jbatch-mailsender.xml");
			URL smtp = loader.getResource("spring-jbatch-smtp.xml");
			if (smtp != null) {
				cfgFileNames.add("classpath:spring-jbatch-smtp.xml");
			}
		}
		else if (testsvrs != null) {
			cfgFileNames.add("classpath:spring-jbatch-testservers.xml");
		}
		String[] cfgFiles = cfgFileNames.toArray(new String[]{});
		return cfgFiles;
	}

	public static String[] getServerConfigXmlFiles() {
		List<String> cfgFileNames = new ArrayList<String>();
		cfgFileNames.add("classpath*:spring-common-config.xml");
		cfgFileNames.add("classpath*:spring-jmsqueue_jee-config.xml");
		return cfgFileNames.toArray(new String[]{});
	}

	public static String[] getStandaloneConfigXmlFiles() {
		List<String> cfgFileNames = new ArrayList<String>();
		cfgFileNames.add("classpath:spring-common-config.xml");
		cfgFileNames.add("classpath:spring-jmsqueue_rmt-config.xml");
		cfgFileNames.add("classpath:spring-mysql-config.xml");
		return cfgFileNames.toArray(new String[]{});
	}

}
