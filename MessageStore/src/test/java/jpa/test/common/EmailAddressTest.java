package jpa.test.common;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.SubscriberData;
import jpa.model.EmailAddress;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriberDataService;
import jpa.util.StringUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;
import org.apache.log4j.Logger;
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
@ContextConfiguration(locations={"classpath:/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class EmailAddressTest {
	static final Logger logger = Logger.getLogger(EmailAddressTest.class);

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void EmailAddrPrepare() {
		logger.info("########## EmailAddressTest");
	}

	@Autowired
	EmailAddressService service;
	
	@Autowired
	SubscriberDataService subrService;

	private String testEmailAddr1 = "jpatest1@localhost";
	private String testEmailAddr2 = "jpatest2@localhost";
	
	@Test
	public void testEmailAddressService() {
		// test insert
		EmailAddress rcd1 = new EmailAddress();
		rcd1.setAddress(testEmailAddr1);
		rcd1.setOrigAddress(testEmailAddr1);
		rcd1.setStatusId(StatusId.ACTIVE.getValue());
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);
		
		List<EmailAddress> lst0 = service.getByAddressPattern("@test.com$");
		assertFalse(lst0.isEmpty());
		EmailAddress rcd2 = lst0.get(0);
		assertNotNull(rcd2);
		
		lst0 = service.getByAddressDomain("test.com");
		assertFalse(lst0.isEmpty());
		lst0 = service.getByAddressUser("testuser");
		assertFalse(lst0.isEmpty());
		
		EmailAddress obj = service.getByAddressWithCounts(lst0.get(0).getAddress());
		System.out.println(StringUtil.prettyPrint(obj));
		
		// test paging for UI application
		int rowId = service.getRowIdForPreview();
		assertTrue(rowId>0);
		PagingVo vo = new PagingVo();
		vo.setStatusId(StatusId.ACTIVE.getValue());
		vo.setSearchString("test.com");
		List<EmailAddress> listpg = service.getEmailAddrsWithPaging(vo);
		assertTrue(listpg.size()>0);
		EmailAddress addrObj = listpg.get(0);
		assertNotNull(addrObj.getSentCount());
		assertNotNull(addrObj.getOpenCount());
		assertNotNull(addrObj.getClickCount());
		logger.info("getEmailAddrsWithPaging() : " + StringUtil.prettyPrint(addrObj));
		int count = service.getEmailAddressCount(vo);
		assertTrue(count==listpg.size());
		
		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		EmailAddress rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue("JpaTest".equals(rcd2.getUpdtUserId()));

		List<SubscriberData> lst1 = subrService.getAll();
		assertFalse(lst1.isEmpty());
		
		EmailAddress rcd4 = new EmailAddress();
		try {
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			BeanUtils.copyProperties(rcd4, rcd3);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		rcd4.setAddress(testEmailAddr2);
		rcd4.setOrigAddress(testEmailAddr2);
		rcd4.setSubscriberData(lst1.get(0));
		service.insert(rcd4);
		
		int bounceCount = rcd4.getBounceCount();
		for (int i=0; i<Constants.BOUNCE_SUSPEND_THRESHOLD; i++) {
			service.updateBounceCount(rcd4);
		}
		
		EmailAddress rcd5 = service.getByAddress(testEmailAddr2);
		System.out.println(StringUtil.prettyPrint(rcd5,1));
		assertNotNull(rcd5.getSubscriberData());
		assertTrue(rcd5.getBounceCount()==(bounceCount+Constants.BOUNCE_SUSPEND_THRESHOLD));
		assertTrue(StatusId.SUSPENDED.getValue().equals(rcd5.getStatusId()));
		
		EmailAddress rcd6 = service.findSertAddress("jpatest3@localhost");
		assertNotNull(rcd6);
		assertTrue(rcd6.getRowId()>0);
		System.out.println(StringUtil.prettyPrint(rcd6));
		
		// test delete
		service.delete(rcd4);
		assertTrue(1==service.deleteByAddress(rcd6.getAddress()));
	}
}
