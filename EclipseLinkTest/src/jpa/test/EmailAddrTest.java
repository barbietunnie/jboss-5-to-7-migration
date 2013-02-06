package jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.CustomerData;
import jpa.model.EmailAddr;
import jpa.service.CustomerDataService;
import jpa.service.EmailAddrService;
import jpa.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
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
public class EmailAddrTest {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void EmailAddrPrepare() {
	}

	@Autowired
	EmailAddrService service;
	
	@Autowired
	CustomerDataService cdService;

	private String testEmailAddr1 = "jpatest1@localhost";
	private String testEmailAddr2 = "jpatest2@localhost";
	
	@Test
	public void testEmailAddrService() {
		// test insert
		EmailAddr rcd1 = new EmailAddr();
		rcd1.setEmailAddr(testEmailAddr1);
		rcd1.setEmailOrigAddr(testEmailAddr1);
		rcd1.setStatusId(StatusId.ACTIVE.getValue());
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);
		
		List<EmailAddr> list = service.getAll();
		assertFalse(list.isEmpty());
		
		EmailAddr rcd2 = service.getByEmailAddr(list.get(0).getEmailAddr());
		assertNotNull(rcd2);
		
		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		EmailAddr rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue("JpaTest".equals(rcd2.getUpdtUserId()));

		List<CustomerData> lst1 = cdService.getAll();
		assertFalse(lst1.isEmpty());
		
		EmailAddr rcd4 = new EmailAddr();
		try {
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			BeanUtils.copyProperties(rcd4, rcd3);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		rcd4.setEmailAddr(testEmailAddr2);
		rcd4.setEmailOrigAddr(testEmailAddr2);
		rcd4.setCustomerData(lst1.get(0));
		service.insert(rcd4);
		
		EmailAddr rcd5 = service.getByEmailAddr(testEmailAddr2);
		System.out.println(StringUtil.prettyPrint(rcd5));
		assertNotNull(rcd5.getCustomerData());
		
		EmailAddr rcd6 = service.findSertEmailAddr("jpatest3@localhost");
		assertNotNull(rcd6);
		assertTrue(rcd6.getRowId()>0);
		System.out.println(StringUtil.prettyPrint(rcd6));
		
		// test delete
		assertTrue(1==service.deleteByEmailAddr(rcd3.getEmailAddr()));
	}
}
