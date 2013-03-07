package jpa.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.model.ClientData;
import jpa.model.ClientVariable;
import jpa.model.CustomerData;
import jpa.model.UserData;
import jpa.service.ClientDataService;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class ClientDataTest {

	@BeforeClass
	public static void ClientsPrepare() {
	}

	@Autowired
	ClientDataService service;

	@Test
	public void clientDataService() {
		List<ClientData> list = service.getAll();
		assertFalse(list.isEmpty());
		
		ClientData tkn0 = service.getByClientId(Constants.DEFAULT_CLIENTID);
		assertNotNull(tkn0);
		
		assertTrue(tkn0.getSystemId().equals(service.getSystemId()));
		assertTrue(tkn0.getSystemKey().equals(service.getSystemKey()));
		assertNotNull(service.getByDomainName(tkn0.getDomainName()));

		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		ClientData tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		ClientData tkn2 = new ClientData();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setClientId(Constants.DEFAULT_CLIENTID + "_2");
		tkn2.setClientVariables(new ArrayList<ClientVariable>());
		tkn2.setCustomers(new ArrayList<CustomerData>());
		tkn2.setUserDatas(new ArrayList<UserData>());
		service.insert(tkn2);
		
		ClientData tkn3 = service.getByClientId(tkn2.getClientId());
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
