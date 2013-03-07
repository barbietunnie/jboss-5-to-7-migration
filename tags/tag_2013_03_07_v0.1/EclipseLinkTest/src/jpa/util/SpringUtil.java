package jpa.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringUtil {
	static final Logger logger = Logger.getLogger(SpringUtil.class);
	
	private static AbstractApplicationContext applContext = null;
	
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
	

	private static String[] getSpringConfigXmlFiles() {
		List<String> cfgFileNames = new ArrayList<String>();
		cfgFileNames.add("classpath*:spring-jpa-config.xml");
		return cfgFileNames.toArray(new String[]{});
	}

	private static final ThreadLocal<PlatformTransactionManager> txmgrThreadLocal = new ThreadLocal<PlatformTransactionManager>();
	private static final ThreadLocal<TransactionStatus> statusThreadLocal = new ThreadLocal<TransactionStatus>();
	
	public static void startTransaction() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("service_"+ TX_COUNTER.get().incrementAndGet());
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		txmgrThreadLocal.set(txmgr);
		statusThreadLocal.set(status);
	}

	public static void commitTransaction() {
		PlatformTransactionManager txmgr = txmgrThreadLocal.get();
		TransactionStatus status = statusThreadLocal.get();
		if (txmgr==null || status==null) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		txmgr.commit(status);
		txmgrThreadLocal.remove();
		statusThreadLocal.remove();
	}

	public static void rollbackTransaction() {
		PlatformTransactionManager txmgr = txmgrThreadLocal.get();
		TransactionStatus status = statusThreadLocal.get();
		if (txmgr==null || status==null) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		txmgr.rollback(status);
		txmgrThreadLocal.remove();
		statusThreadLocal.remove();
	}

	public static void clearTransaction() {
		PlatformTransactionManager txmgr = txmgrThreadLocal.get();
		TransactionStatus status = statusThreadLocal.get();
		if (txmgr!=null && status!=null) {
			rollbackTransaction();
		}
	}

	private static final ThreadLocal<AtomicInteger> TX_COUNTER = new ThreadLocal<AtomicInteger>() {
		public AtomicInteger initialValue() {
			return new AtomicInteger(1);
		}
	};

	public static void main(String[] args) {
		getAppContext();
	}
}
