package jpa.util;

import java.sql.SQLException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class JpaUtil {
	static final Logger logger = Logger.getLogger(JpaUtil.class);
	static boolean isDebugEnabled = logger.isDebugEnabled();

	private static EntityManagerFactory emf;
	
	public static String getDBProductName() {
		DataSource ds = (DataSource) SpringUtil.getAppContext().getBean("msgDataSource");
		try {
			String prodName = ds.getConnection().getMetaData().getDatabaseProductName();
			logger.info("Database product name: " + prodName);
			return prodName;
		}
		catch (SQLException e) {}
		// Failed to get Database name from data source, now inspecting JPA Vendor Adapter
		JpaVendorAdapter adapter = (JpaVendorAdapter) SpringUtil.getAppContext().getBean("jpaVendorAdapter");
		Set<String> keys = adapter.getJpaPropertyMap().keySet();
		for (java.util.Iterator<String> it=keys.iterator(); it.hasNext();) {
			String key = it.next();
			if (StringUtils.containsIgnoreCase(key, "database")) {
				Object obj = adapter.getJpaPropertyMap().get(key);
				Pattern p = Pattern.compile("database\\.(\\w{1,20})Platform$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher m = p.matcher(obj.toString());
				if (m.find() && m.groupCount() >= 1) {
					for (int i=0; i<=m.groupCount(); i++) {
						//logger.info(i + " : " + m.group(i));
					}
					logger.info("Database platform name: " + m.group(1));
					return m.group(1);
				}
			}
		}
		return "UnKnown";
	}

	public static String getJpaDialect() {
		 Object dialect = SpringUtil.getAppContext().getBean("jpaDialect");
		 if (dialect != null) {
			 return dialect.getClass().getSimpleName();
		 }
		 else {
			 return "Unknown";
		 }
	}
	
	public static void setQueryHints(Query query) {
		if (StringUtils.containsIgnoreCase(JpaUtil.getJpaDialect(), "EclipseLink_DoNotUse")) {
			// setting to read-only mode caused transaction anomaly (Duplicate Key on insert).
			query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
		}
		else if (StringUtils.containsIgnoreCase(JpaUtil.getJpaDialect(), "Hibernate")) {
			// TODO - complete unit test
			query.setHint("org.hibernate.readOnly", Boolean.TRUE);
		}
	}

	public static boolean isDeadlockException(Exception e) {
		Exception ex = ExceptionUtil.findException(e, java.sql.SQLException.class);
		if (ex != null && ex.getMessage().contains("Lock wait timeout exceeded")) {
			logger.error("Caught SQL Deadlock error: " + ex.getMessage());
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		getDBProductName();
		logger.info("Jpa Dialect: " + getJpaDialect());
	}

	/**
	 * @deprecated - use injection instead
	 */
	public static EntityManagerFactory getEntityManagerFactory() {
		if (emf == null) {
			emf = SpringUtil.getAppContext().getBean(LocalContainerEntityManagerFactoryBean.class).getObject();
		}
		return emf;
 	}
	
	/**
	 * @deprecated - use injection instead
	 */
	public static EntityManager getEntityManager() {
		EntityManagerFactory factory = getEntityManagerFactory();
		// Check for EM associated with any currency transaction
		EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(factory);
		if (em != null) {
			if (isDebugEnabled) {
				logger.debug("Found Transactional EntityManager [" + em + "] for JPA Transaction");
			}
			return em;
		}

		// Check for EM bound to Thread
		EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(factory);
		if (emHolder != null && emHolder.getEntityManager() != null) {
			if (isDebugEnabled) {
				logger.debug("Found thread-bound EntityManager [" + emHolder.getEntityManager()
						+ "] for JPA transaction");
			}
			return emHolder.getEntityManager();
		}

		// return a new session, if none above
		return factory.createEntityManager();
	}

	/**
	 * @deprecated - use injection instead
	 */
	public static void releaseEntityManager(EntityManager em) {
		EntityManagerFactory factory = getEntityManagerFactory();
		try {
			EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(factory);
			// If bound to thread do not close, let the unbind/transaction
			// boundary close
			if (emHolder != null && (emHolder.getEntityManager() == em)) {
				if (isDebugEnabled) {
					logger.debug("Found thread-bound EntityManager [" + emHolder.getEntityManager()
							+ "] for JPA transaction");
				}
				return;
			}

			// if not bouund to thread then close;
			EntityManagerFactoryUtils.closeEntityManager(em);
		} catch (RuntimeException e) {
			// LOG the error and re throw the exception
			logger.error("ATTN: Unable to execute release hibernate session", e);
			throw e;
		}
	}

}
