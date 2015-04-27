package jpa.test.maillist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.service.maillist.BroadcastTrackingService;
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
public class BroadcastTrackingTest {

	@BeforeClass
	public static void BroadcastTrackingPrepare() {
	}

	@Autowired
	BroadcastTrackingService service;
	@Autowired
	BroadcastMessageService bcdService;
	@Autowired
	EmailAddressService eaService;

	@Test
	public void testBroadcastTrackingService() {
		
		List<BroadcastMessage> bdlist = bcdService.getAll();
		assertFalse(bdlist.isEmpty());
		
		BroadcastMessage bd1 = bdlist.get(0);
		
		List<BroadcastTracking> eblist = service.getByBroadcastDataRowId(bd1.getRowId());
		assertFalse(eblist.isEmpty());
		
		BroadcastTracking eb1 = eblist.get(0);
		
		try {
			service.getByPrimaryKey(eb1.getEmailAddress().getRowId(), eb1.getBroadcastMessage().getRowId());
		}
		catch (NoResultException e) {
			fail();
		}
		
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		eb1.setUpdtTime(ts);
		eb1.setClickCount(eb1.getClickCount()+1);
		eb1.setOpenCount(eb1.getOpenCount()+1);
		eb1.setSentCount(eb1.getSentCount()+1);
		eb1.setLastClickTime(ts);
		eb1.setLastOpenTime(ts);
		service.update(eb1);
		
		BroadcastTracking eb2 = service.getByRowId(eb1.getRowId());
		assertTrue(ts.equals(eb2.getUpdtTime()));
		System.out.println(StringUtil.prettyPrint(eb2, 2));
		
		List<BroadcastTracking> eblist2 = service.getByEmailAddress(eb1.getEmailAddress().getAddress());
		assertFalse(eblist2.isEmpty());
		
		List<BroadcastTracking> eblist3 = service.getByEmailAddrRowId(eb1.getEmailAddress().getRowId());
		assertFalse(eblist3.isEmpty());
		
		BroadcastTracking eb3 = new BroadcastTracking();
		eb3.setBroadcastMessage(eb1.getBroadcastMessage());
		String random_no = "1"; //String.valueOf(13 + new java.util.Random().nextInt(100));
		eb3.setEmailAddress(eaService.findSertAddress("tracking_" + random_no + "@test.com"));
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
