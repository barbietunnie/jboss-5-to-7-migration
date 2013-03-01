package com.legacytojava.message.jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.NoResultException;

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

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.jpa.model.Clients;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class ClientsTest {

	@BeforeClass
	public static void ClientsPrepare() {
	}

	@Autowired
	com.legacytojava.message.jpa.service.ClientsService service;

	@Test
	public void ClientsService() {
		List<Clients> list = service.getAll();
		assertFalse(list.isEmpty());
		
		Clients tkn0 = service.getByClientId(Constants.DEFAULT_CLIENTID);
		assertNotNull(tkn0);
		
		assertTrue(tkn0.getSystemId().equals(service.getSystemId()));
		assertTrue(tkn0.getSystemKey().equals(service.getSystemKey()));
		assertNotNull(service.getByDomainName(tkn0.getDomainName()));

		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		Clients tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		Clients tkn2 = new Clients();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setClientId(Constants.DEFAULT_CLIENTID + "_2");
		service.insert(tkn2);
		
		Clients tkn3 = service.getByClientId(tkn2.getClientId());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select with NoResultException
		service.delete(tkn3);
		try {
			service.getByClientId(tkn2.getClientId());
			fail();
		}
		catch (NoResultException e) {
		}
		
		assertTrue(0==service.deleteByClientId(tkn3.getClientId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
