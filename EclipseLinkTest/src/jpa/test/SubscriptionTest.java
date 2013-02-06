package jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.EmailAddr;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.EmailAddrService;
import jpa.service.MailingListService;
import jpa.service.SubscriptionService;
import jpa.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.junit.Before;
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
public class SubscriptionTest {

	@BeforeClass
	public static void SubscriptionPrepare() {
	}

	@Autowired
	SubscriptionService service;
	
	@Autowired
	EmailAddrService eaService;
	@Autowired
	MailingListService mlService;

	private EmailAddr emailAddr1 = null;
	private EmailAddr emailAddr2 = null;
	private EmailAddr emailAddr3 = null;
	
	@Before
	public void prepare() {
		String testEmailAddr1 = "jpatest1@localhost";
		emailAddr1 = new EmailAddr();
		emailAddr1.setEmailAddr(testEmailAddr1);
		emailAddr1.setEmailOrigAddr(testEmailAddr1);
		emailAddr1.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr1);
		
		String testEmailAddr2 = "jpatest2@localhost";
		emailAddr2 = new EmailAddr();
		emailAddr2.setEmailAddr(testEmailAddr2);
		emailAddr2.setEmailOrigAddr(testEmailAddr2);
		emailAddr2.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr2.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr2);

		String testEmailAddr3 = "jpatest3@localhost";
		emailAddr3 = new EmailAddr();
		emailAddr3.setEmailAddr(testEmailAddr3);
		emailAddr3.setEmailOrigAddr(testEmailAddr3);
		emailAddr3.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr3.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr3);
	}
	
	@Test
	public void SubscriptionService() {
		List<MailingList> list = mlService.getAll();
		assertFalse(list.isEmpty());
		
		// test insert
		Subscription rcd1 = new Subscription();
		rcd1.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		rcd1.setMailingList(list.get(0));
		rcd1.setEmailAddr(emailAddr1);
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);
		
		Subscription rcd2 = service.getByPrimaryKey(emailAddr1.getRowId(),list.get(0).getRowId());
		assertNotNull(rcd2);
		rcd2 = service.getByAddressAndListId(emailAddr1.getEmailAddr(), list.get(0).getListId());
		assertNotNull(rcd2);
		
		Subscription rcd6 = new Subscription();
		rcd6.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		rcd6.setMailingList(list.get(0));
		rcd6.setEmailAddr(emailAddr3);
		rcd6.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd6);

		assertFalse(service.getByAddress(emailAddr3.getEmailAddr()).isEmpty());
		//assertTrue(1==service.deleteByPrimaryKey(emailAddr3.getRowId(), list.get(0).getRowId()));
		assertTrue(1==service.deleteByAddress(emailAddr3.getEmailAddr()));

		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		
		Subscription rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue("JpaTest".equals(rcd3.getUpdtUserId()));
		// end of test update

		// test insert 2
		Subscription rcd4 = new Subscription();
		try {
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			BeanUtils.copyProperties(rcd4, rcd3);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd4.setOpenCount(rcd3.getOpenCount()+1);
		rcd4.setEmailAddr(emailAddr2);
		service.insert(rcd4);
		
		Subscription rcd5 = service.getByPrimaryKey(emailAddr2.getRowId(), list.get(0).getRowId());
		assertTrue(rcd5.getRowId()!=rcd3.getRowId());
		assertFalse(rcd3.getOpenCount()==rcd5.getOpenCount());
		assertFalse(rcd3.getEmailAddr().equals(rcd5.getEmailAddr()));
		// end of test insert
		
		// test delete
		service.delete(rcd3);
		try {
			service.getByRowId(rcd3.getRowId());
			fail();
		}
		catch (NoResultException e) {
		}

		System.out.println(StringUtil.prettyPrint(rcd5,1));
		int rowsDeleted = service.deleteByAddressAndListId(emailAddr2.getEmailAddr(), list.get(0).getListId());
		assertTrue(1==rowsDeleted);

		// test subscription
		Subscription sub1 = service.subscribe("jpasubtest1@localhost", list.get(0).getListId());
		System.out.println(StringUtil.prettyPrint(sub1,1));
	
		Subscription sub2 = service.unsubscribe("jpasubtest1@localhost", list.get(0).getListId());
		System.out.println(StringUtil.prettyPrint(sub2,1));
	}
}
