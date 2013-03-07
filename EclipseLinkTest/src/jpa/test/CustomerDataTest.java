package jpa.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import jpa.model.ClientData;
import jpa.model.CustomerData;
import jpa.service.ClientDataService;
import jpa.service.CustomerDataService;
import jpa.service.EmailAddressService;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
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
public class CustomerDataTest {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void CustomerDataPrepare() {
	}

	@Autowired
	CustomerDataService service;
	
	@Autowired
	ClientDataService cdService;
	@Autowired
	EmailAddressService emailService;

	@Test
	public void customerDataService() {
		List<CustomerData> list = service.getAll();
		assertFalse(list.isEmpty());
		
		CustomerData rcd0 = service.getByCustomerId(list.get(0).getCustomerId());
		assertNotNull(rcd0);
		
		// test update
		rcd0.setUpdtUserId("JpaTest");
		service.update(rcd0);
		CustomerData rcd1 = service.getByRowId(rcd0.getRowId());
		assertTrue("JpaTest".equals(rcd1.getUpdtUserId()));
		
		// test insert
		ClientData cd2 = cdService.getByClientId(rcd0.getClientData().getClientId());
		CustomerData rcd2 = new CustomerData();
		try {
			// allow null sql timestamp to be copied
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			// allow null util date to be copied
			DateConverter converter2 = new DateConverter(null);
			ConvertUtils.register(converter2, java.util.Date.class);
			// copy properties from rcd1 to rcd2
			BeanUtils.copyProperties(rcd2, rcd1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd2.setClientData(cd2);
		rcd2.setCustomerId(rcd1.getCustomerId()+"_2");
		rcd2.setEmailAddr(emailService.findSertAddress(rcd2.getCustomerId()+"@localhost"));
		service.insert(rcd2);
		
		CustomerData rcd3 = service.getByCustomerId(rcd1.getCustomerId()+"_2");
		assertNotNull(rcd3);
		assertTrue(rcd1.getRowId()!=rcd3.getRowId());
		
		assertTrue(1==service.deleteByCustomerId(rcd3.getCustomerId()));
	}
}
