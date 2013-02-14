package jpa.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.EmailAddr;
import jpa.model.MailingList;
import jpa.model.UnsubComment;
import jpa.service.EmailAddrService;
import jpa.service.MailingListService;
import jpa.service.UnsubCommentService;

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
public class UnsubCommentTest {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void UnsubCommentPrepare() {
	}

	@Autowired
	UnsubCommentService service;
	
	@Autowired
	EmailAddrService emailService;
	@Autowired
	MailingListService mlistService;

	private String testUnsubComment1 = "jpa test comment 1";
	private String testUnsubComment2 = "jpa test comment 2";
	
	EmailAddr emailAddr = null;
	MailingList mlist = null;
	@Before
	public void prepare() {
		emailAddr = emailService.findSertAddress("jpatest1@localhost");
		List<MailingList> list = mlistService.getAll();
		if (!list.isEmpty()) {
			mlist = list.get(0);
		}
	}
	
	@Test
	public void testUnsubCommentService() {
		// test insert
		UnsubComment rcd1 = new UnsubComment();
		rcd1.setEmailAddr(emailAddr);
		rcd1.setMailingList(mlist);
		rcd1.setComments(testUnsubComment1);
		rcd1.setStatusId(StatusId.ACTIVE.getValue());
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);
		
		List<UnsubComment> lst0 = service.getByAddress(emailAddr.getAddress());
		assertFalse(lst0.isEmpty());
		UnsubComment rcd2 = lst0.get(0);
		assertNotNull(rcd2);
		
		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		UnsubComment rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue("JpaTest".equals(rcd2.getUpdtUserId()));
		
		UnsubComment rcd4 = new UnsubComment();
		try {
			SqlTimestampConverter converter1 = new SqlTimestampConverter(null);
			ConvertUtils.register(converter1, java.sql.Timestamp.class);
			BeanUtils.copyProperties(rcd4, rcd3);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		rcd4.setComments(testUnsubComment2);
		service.insert(rcd4);
		
		List<UnsubComment> lst2 = service.getByAddress(rcd4.getEmailAddr().getAddress());
		assertTrue(lst2.size()==2);
		
		// test delete
		service.delete(rcd4);
		assertTrue(1<=service.deleteByAddress(rcd2.getEmailAddr().getAddress()));
	}
}
