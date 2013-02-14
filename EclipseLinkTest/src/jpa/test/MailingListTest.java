package jpa.test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.MailingListEnum;
import jpa.model.EmailAddr;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.EmailAddrService;
import jpa.service.MailingListService;
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
public class MailingListTest {

	@BeforeClass
	public static void MailingListPrepare() {
	}

	@Autowired
	MailingListService service;
	
	@Autowired
	EmailAddrService eaService;

	private EmailAddr emailAddr = null;
	private EmailAddr emailAddr2 = null;
	
	@Before
	public void prepare() {
		String testEmailAddr1 = "jpatest1@localhost";
		emailAddr = new EmailAddr();
		emailAddr.setAddress(testEmailAddr1);
		emailAddr.setOrigAddress(testEmailAddr1);
		emailAddr.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr);
		
		String testEmailAddr2 = "jpatest2@localhost";
		emailAddr2 = new EmailAddr();
		emailAddr2.setAddress(testEmailAddr2);
		emailAddr2.setOrigAddress(testEmailAddr2);
		emailAddr2.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr2.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr2);
	}
	
	private String testListId1 = "TestList1";
	private String testListId2 = "TestList2";

	@Test
	public void testMailingListService() {
		List<MailingList> list = service.getAll();
		assertFalse(list.isEmpty());
		
		// test insert
		MailingList rcd1 = new MailingList();
		try {
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			BeanUtils.copyProperties(rcd1, list.get(0));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd1.setListMasterEmailAddr("sitemaster@localhost");
		rcd1.setListId(testListId1);
		List<Subscription> subs = new ArrayList<Subscription>();
		// added next line to prevent this Hibernate error: 
		//	"Found shared references to a collection"
		rcd1.setSubscriptions(subs);
		service.insert(rcd1);
		
		MailingList rcd2 = service.getByListId(testListId1);
		assertNotNull(rcd2);
		
		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		
		MailingList rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue("JpaTest".equals(rcd3.getUpdtUserId()));
		// end of test update
		
		// test insert 2
		MailingList rcd4 = new MailingList();
		try {
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			BeanUtils.copyProperties(rcd4, rcd3);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd4.setListMasterEmailAddr("sitemaster2@localhost");
		rcd4.setListId(testListId2);
		rcd4.setSubscriptions(new ArrayList<Subscription>());
		service.insert(rcd4);
		
		MailingList rcd5 = service.getByListId(testListId2);
		assertTrue(rcd5.getRowId()!=rcd3.getRowId());
		assertFalse(rcd3.getListMasterEmailAddr().equals(rcd5.getListMasterEmailAddr()));
		// end of test insert
		
		Object[] mlst1 = service.getByListIdWithCounts(MailingListEnum.SMPLLST1.name());
		assertTrue(mlst1[0] instanceof MailingList);
		assertTrue(mlst1[1] instanceof Number);
		assertTrue(mlst1[2] instanceof Number);
		assertTrue(mlst1[3] instanceof BigDecimal || mlst1[3] instanceof BigInteger);
		System.out.println(StringUtil.prettyPrint(mlst1[0],1));
		System.out.println("Mailing List Counts: " + mlst1[1] + "," + mlst1[2] + "," + mlst1[3]);
		
		// test delete
		service.delete(rcd3);
		try {
			service.getByRowId(rcd3.getRowId());
			fail();
		}
		catch (NoResultException e) {
		}

		System.out.println(StringUtil.prettyPrint(rcd5,1));
		int rowsDeleted = service.deleteByListId(testListId2);
		assertTrue(1==rowsDeleted);
	}
}
