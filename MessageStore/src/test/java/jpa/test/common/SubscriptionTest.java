package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.EntityManagerService;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.MailingListService;
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
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class SubscriptionTest {

	@BeforeClass
	public static void SubscriptionPrepare() {
	}

	@Autowired
	SubscriptionService service;
	
	@Autowired
	EmailAddressService eaService;
	@Autowired
	MailingListService mlService;
	@Autowired
	EntityManagerService emService;

	private EmailAddress emailAddr1 = null;
	private EmailAddress emailAddr2 = null;
	private EmailAddress emailAddr3 = null;
	
	@Before
	public void prepare() {
		String testEmailAddr1 = "jpatest1@localhost";
		emailAddr1 = new EmailAddress();
		emailAddr1.setAddress(testEmailAddr1);
		emailAddr1.setOrigAddress(testEmailAddr1);
		emailAddr1.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr1);
		
		String testEmailAddr2 = "jpatest2@localhost";
		emailAddr2 = new EmailAddress();
		emailAddr2.setAddress(testEmailAddr2);
		emailAddr2.setOrigAddress(testEmailAddr2);
		emailAddr2.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr2.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr2);

		String testEmailAddr3 = "jpatest3@localhost";
		emailAddr3 = new EmailAddress();
		emailAddr3.setAddress(testEmailAddr3);
		emailAddr3.setOrigAddress(testEmailAddr3);
		emailAddr3.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr3.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr3);
	}
	
	@Test
	public void SubscriptionService() {
		List<MailingList> list = mlService.getAll(false);
		assertFalse(list.isEmpty());

		List<Subscription> subs = service.getByListId(list.get(0).getListId());
		if (!subs.isEmpty()) {
			Subscription rcd7 = subs.get(0);
			java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
			rcd7.setOpenCount(rcd7.getOpenCount()+1);
			rcd7.setSentCount(rcd7.getSentCount()+1);
			rcd7.setClickCount(rcd7.getClickCount()+1);
			rcd7.setLastClickTime(updtTime);
			rcd7.setLastOpenTime(updtTime);
			rcd7.setLastSentTime(updtTime);
			service.update(rcd7);
			String address = rcd7.getEmailAddr().getAddress();
			String listId = rcd7.getMailingList().getListId();
			Subscription rcd8 = service.getByAddressAndListId(address, listId);
			System.out.println("RCD8: " + StringUtil.prettyPrint(rcd8,1));
			
			service.getByUniqueKey(rcd7.getEmailAddr().getRowId(), rcd7.getMailingList().getListId());
		}

		// test insert
		Subscription rcd1 = new Subscription();
		rcd1.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		rcd1.setMailingList(list.get(0));
		rcd1.setEmailAddr(emailAddr1);
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);
		
		Subscription rcd2 = service.getByAddressAndListId(emailAddr1.getAddress(), list.get(0).getListId());
		assertNotNull(rcd2);
		
		assert(1<=service.updateClickCount(emailAddr1.getRowId(), list.get(0).getListId()));
		assert(1<=service.updateOpenCount(emailAddr1.getRowId(), list.get(0).getListId()));
		
		// test paging for UI application
		PagingVo vo = new PagingVo();
		vo.setStatusId(StatusId.ACTIVE.getValue());
		vo.setSearchString("test.com");
		List<Subscription> listpg = service.getSubscriptionsWithPaging(list.get(0).getListId(), vo);
		assertTrue(listpg.size()>0);
		int count = service.getSubscriptionCount(list.get(0).getListId(), vo);
		assertTrue(count==listpg.size());
		
		Subscription rcd6 = new Subscription();
		rcd6.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		rcd6.setMailingList(list.get(0));
		rcd6.setEmailAddr(emailAddr3);
		rcd6.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd6);

		assertFalse(service.getByAddress(emailAddr3.getAddress()).isEmpty());

		assertTrue(1<=service.getByListIdSubscribersOnly(list.get(0).getListId()).size());
		assertTrue(1<=service.getByListIdProsperctsOnly(list.get(0).getListId()).size());
		
		//assertTrue(1==service.deleteByPrimaryKey(emailAddr3.getRowId(), list.get(0).getRowId()));
		assertTrue(1==service.deleteByAddress(emailAddr3.getAddress()));

		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		
		emService.detach(rcd2); /* added to work around following Derby error:
		 			"cannot be updated because it has changed or been deleted since it was last read." */
		Subscription rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue("JpaTest".equals(rcd3.getUpdtUserId()));
		// end of test update
		assertTrue(1==service.updateSentCount(rcd2.getRowId(), 1));

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
		
		Subscription rcd5 = service.getByAddressAndListId(emailAddr2.getAddress(), list.get(0).getListId());
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
		int rowsDeleted = service.deleteByAddressAndListId(emailAddr2.getAddress(), list.get(0).getListId());
		assertTrue(1==rowsDeleted);

		// test subscription
		Subscription sub1 = service.subscribe("jpasubtest1@localhost", list.get(0).getListId());
		System.out.println(StringUtil.prettyPrint(sub1,1));
	
		Subscription sub2 = service.unsubscribe("jpasubtest1@localhost", list.get(0).getListId());
		System.out.println(StringUtil.prettyPrint(sub2,1));
	}
}
