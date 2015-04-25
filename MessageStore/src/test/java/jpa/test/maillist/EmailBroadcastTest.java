package jpa.test.maillist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.BroadcastData;
import jpa.model.EmailBroadcast;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.BroadcastDataService;
import jpa.service.maillist.EmailBroadcastService;
import jpa.util.StringUtil;

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
public class EmailBroadcastTest {

	@BeforeClass
	public static void EmailBroadcastPrepare() {
	}

	@Autowired
	EmailBroadcastService service;
	@Autowired
	BroadcastDataService bcdService;
	@Autowired
	EmailAddressService eaService;

	@Test
	public void testEmailBroadcastService() {
		
		List<BroadcastData> bdlist = bcdService.getAll();
		assertFalse(bdlist.isEmpty());
		
		BroadcastData bd1 = bdlist.get(0);
		
		List<EmailBroadcast> eblist = service.getByBroadcastDataRowId(bd1.getRowId());
		assertFalse(eblist.isEmpty());

		EmailBroadcast eb1 = eblist.get(0);
		
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		eb1.setUpdtTime(ts);
		eb1.setClickCount(eb1.getClickCount()+1);
		eb1.setOpenCount(eb1.getOpenCount()+1);
		eb1.setSentCount(eb1.getSentCount()+1);
		eb1.setLastClickTime(ts);
		eb1.setLastOpenTime(ts);
		service.update(eb1);
		
		EmailBroadcast eb2 = service.getByRowId(eb1.getRowId());
		assertTrue(ts.equals(eb2.getUpdtTime()));
		System.out.println(StringUtil.prettyPrint(eb2, 2));
		
		List<EmailBroadcast> eblist2 = service.getByEmailAddress(eb1.getEmailAddress().getAddress());
		assertFalse(eblist2.isEmpty());
		
		List<EmailBroadcast> eblist3 = service.getByEmailAddrRowId(eb1.getEmailAddress().getRowId());
		assertFalse(eblist3.isEmpty());
		
		EmailBroadcast eb3 = new EmailBroadcast();
		eb3.setBroadcastData(eb1.getBroadcastData());
		eb3.setEmailAddress(eb1.getEmailAddress());
		eb3.setStatusId(StatusId.ACTIVE.getValue());
		eb3.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eb3.setUpdtTime(ts);
		service.insert(eb3);
		
		assertTrue(eb3.getRowId()>0  && eb3.getRowId()!=eb1.getRowId());
		service.delete(eb3);
		assert(0==service.deleteByRowId(eb3.getRowId()));
		try {
			service.getByRowId(eb3.getRowId());
			fail();
		}
		catch (NoResultException e) {
			// expected
		}		
	}

}
