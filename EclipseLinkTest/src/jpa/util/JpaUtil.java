package jpa.util;

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
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
