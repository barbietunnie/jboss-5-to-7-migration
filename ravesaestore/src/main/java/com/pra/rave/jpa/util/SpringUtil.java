package com.pra.rave.jpa.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.pra.util.logger.LoggerHelper;

public class SpringUtil {
	static final Logger logger = LoggerHelper.getLogger();
	
	private static AbstractApplicationContext applContext = null;
	
	/**
	 * If it's running in JBoss server, it loads a set of xmls using JNDI's,
	 * otherwise loads a set of xmls using mysql data source.
	 * @return ApplicationContext
	 */
	public static AbstractApplicationContext getAppContext() {
		if (applContext == null) {
			String[] fileNames = getSpringConfigXmlFiles();
			applContext = new ClassPathXmlApplicationContext(fileNames);
		} 
		return applContext;
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
	

	public static String[] getSpringConfigXmlFiles() {
		List<String> cfgFileNames = new ArrayList<String>();
		cfgFileNames.add("classpath:META-INF/spring-rave-config.xml");
		return cfgFileNames.toArray(new String[]{});
	}

	public static void main(String[] args) {
		getAppContext();
	}
}
