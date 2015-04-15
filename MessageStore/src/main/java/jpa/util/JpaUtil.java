package jpa.util;

import java.sql.SQLException;

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

	public static void main(String[] args) {
		logger.info(getJpaDialect());
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
