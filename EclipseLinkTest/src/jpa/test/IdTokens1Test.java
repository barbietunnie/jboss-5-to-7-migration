package jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.model.IdTokens;
import jpa.service.SenderDataService;
import jpa.service.IdTokensService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class IdTokens1Test {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void IdTokensPrepare() {
	}

	@Test
	public void selectIdTokens() {
		Query q = entityManager.createQuery("select t from IdTokens t");
		@SuppressWarnings("unchecked")
		List<IdTokens> tokens = q.getResultList();
		for (IdTokens token : tokens) {
			System.out.println(token);
		}
		System.out.println("Size: " + tokens.size());
		entityManager.close();
		assertFalse(tokens.isEmpty());
	}

	@Autowired
	IdTokensService service;
	
	@Autowired
	SenderDataService cdService;

	@Test
	public void idTokensService1() {
		List<IdTokens> list = service.getAll();
		assertFalse(list.isEmpty());
		
		IdTokens tkn0 = service.getBySenderId(Constants.DEFAULT_SENDER_ID);
		assertNotNull(tkn0);
		
		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		IdTokens tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		
		// test insert
		SenderData cd2 = cdService.getBySenderId("JBatchCorp");
		IdTokens tkn2 = new IdTokens();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setSenderData(cd2);
		service.insert(tkn2);
		
		IdTokens tkn3 = service.getBySenderId("JBatchCorp");
		assertNotNull(tkn3);
		assertTrue(tkn1.getRowId()!=tkn3.getRowId());
		
		assertTrue(1==service.deleteBySenderId(tkn3.getSenderData().getSenderId()));
	}
}
