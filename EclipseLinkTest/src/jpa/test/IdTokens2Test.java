package jpa.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jpa.util.SpringUtil;
import jpa.model.IdTokens;
import jpa.service.IdTokensService;

public class IdTokens2Test {

	private static final String PERSISTENCE_UNIT_NAME = "MessageDB";

	private static PlatformTransactionManager txmgr;
	private static TransactionStatus status;
	
	@BeforeClass
	public static void IdTokensPrepare() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("idtokens_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		status = txmgr.getTransaction(def);
	}

	@AfterClass
	public static void IdTokenTeardown() {
		txmgr.rollback(status);
	}

	/*
	 * load entity manager factory by spring as a spring bean
	 */
	@Test(expected=RollbackException.class)
	public void springEntityManager() {
		//EntityManagerFactory emf = (EntityManagerFactory) JbMain.getBatchAppContext().getBean("entityManagerFactory");
		EntityManagerFactory emf = SpringUtil.getAppContext().getBean(LocalContainerEntityManagerFactoryBean.class).getObject();
		EntityManager entityManager = emf.createEntityManager();
		// Read the existing entries and write to console
		Query q = entityManager.createQuery("select t from IdTokens t");
		@SuppressWarnings("unchecked")
		List<IdTokens> tokens = q.getResultList();
		try {
			for (IdTokens token : tokens) {
				System.out.println(token);
				// update record
				entityManager.getTransaction().begin();
				if ("SysAdmin".equalsIgnoreCase(token.getUpdtUserId())) {
					token.setUpdtUserId("admin");
				}
				else {
					token.setUpdtUserId("SysAdmin");
				}
				/*
				 * UpdtTime field is used for Optimistic Locking
				 */
				token.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
				entityManager.persist(token);
				entityManager.getTransaction().commit();
			}
			System.out.println("Size: " + tokens.size());
		}
		finally {
			entityManager.close();
		}
	}

	@Test
	public void idTokensService1() {
		IdTokensService service = (IdTokensService) SpringUtil.getAppContext().getBean("idTokensService");

		List<IdTokens> list = service.getAll();
		assertFalse(list.isEmpty());
		
		IdTokens tkn0 = service.getByClientId("System");
		assertNotNull(tkn0);
		
		// test update - it should not create a new record
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		IdTokens tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert - a new record should be created
		IdTokens tkn2 = new IdTokens();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setClientId("JBatchCorp");
		service.insert(tkn2);
		
		IdTokens tkn3 = service.getByClientId("JBatchCorp");
		assertNotNull(tkn3);
		assertTrue(tkn1.getRowId()!=tkn3.getRowId());
		// end of test insert
		
		service.delete(tkn3);
		try {
			service.getByRowId(tkn3.getRowId());
			fail();
		}
		catch (NoResultException e) {
			// expected
		}
		
		assertTrue(0==service.deleteByClientId(tkn3.getClientId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
	
	@Test(expected=javax.persistence.NoResultException.class)
	public void idTokensService2() {
		IdTokensService service = (IdTokensService) SpringUtil.getAppContext().getBean("idTokensService");

		IdTokens tkn0 = service.getByClientId("System");
		assertNotNull(tkn0);
		
		IdTokens tkn1 = new IdTokens();
		try {
			BeanUtils.copyProperties(tkn1, tkn0);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		tkn1.setClientId("JBatchCorp");
		service.insert(tkn1);
		
		IdTokens tkn2 = service.getByClientId(tkn1.getClientId());
		assertNotNull(tkn2);
		
		service.delete(tkn2);
		service.getByClientId(tkn2.getClientId());
	}

	/* 
	 * !!! load entity manager factory by EclipseLink from persistence.xml
	 */
	@Ignore
	public void persistenceXmlfile() {
		HashMap<Object,Object> properties = new HashMap<Object,Object>();
		properties.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, "META-INF/jpa-persistence.xml");
		//properties.put(PersistenceUnitProperties.CLASSLOADER, this.getClass().getClassLoader());
		
		EntityManagerFactory emf = new PersistenceProvider().createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
		EntityManager entityManager = emf.createEntityManager();
		// Read the existing entries and write to console
		Query q = entityManager.createQuery("select t from IdTokens t");
		@SuppressWarnings("unchecked")
		List<IdTokens> tokens = q.getResultList();
		for (IdTokens token : tokens) {
			System.out.println(token);
			// update record
			entityManager.getTransaction().begin();
			if ("SysAdmin".equalsIgnoreCase(token.getUpdtUserId())) {
				token.setUpdtUserId("admin");
			}
			else {
				token.setUpdtUserId("SysAdmin");
			}
			token.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
			entityManager.persist(token);
			entityManager.getTransaction().commit();
		}
		System.out.println("Size: " + tokens.size());

		entityManager.close();
	}

}
