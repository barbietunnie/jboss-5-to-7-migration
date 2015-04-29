package jpa.test.maillist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Random;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.model.Subscription;
import jpa.model.UnsubComment;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriptionService;
import jpa.service.common.UnsubCommentService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.service.maillist.BroadcastTrackingBo;
import jpa.service.maillist.BroadcastTrackingService;

import org.apache.commons.lang3.StringUtils;
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
public class BroadcastTrackingBoTest {

	@BeforeClass
	public static void BroadcastTrackingBoPrepare() {
	}

	@Autowired
	BroadcastTrackingService service;
	@Autowired
	BroadcastMessageService bcdService;
	@Autowired
	EmailAddressService eaService;
	@Autowired
	SubscriptionService subService;
	@Autowired
	BroadcastTrackingBo bcstTrkBo;
	@Autowired
	UnsubCommentService unsubCmtService;

	@Test
	public void testBroadcastTrackingBo() {
		
		List<BroadcastMessage> bmlist = bcdService.getAll();
		assertFalse(bmlist.isEmpty());
		
		BroadcastMessage bd1 = bmlist.get(0);
		
		List<BroadcastTracking> btlist = service.getByBroadcastDataRowId(bd1.getRowId());
		assertFalse(btlist.isEmpty());
		
		BroadcastTracking bt1 = btlist.get(0);
		
		String testComment = "My test comment " + (7 + new Random().nextInt(100));
		Subscription sub1 = bcstTrkBo.removeFromList(bt1.getRowId(), testComment);
		assertNotNull(sub1);
		List<UnsubComment> cmts = unsubCmtService.getByAddressAndListId(sub1.getEmailAddr().getAddress(), sub1.getMailingList().getListId());
		assertFalse(cmts.isEmpty());
		String comments = "";
		for (UnsubComment cmt : cmts) {
			comments += cmt.getComments();
		}
		assert(StringUtils.contains(comments, testComment));
		
		List<Subscription> sublist = subService.getByListId("SMPLLST2");
		assertFalse(sublist.isEmpty());
		Subscription sub2 = sublist.get(0);
		
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		
		BroadcastTracking bt2 = new BroadcastTracking();
		bt2.setBroadcastMessage(bt1.getBroadcastMessage());
		bt2.setEmailAddress(sub2.getEmailAddr());
		bt2.setStatusId(StatusId.ACTIVE.getValue());
		bt2.setUpdtUserId(Constants.DEFAULT_USER_ID);
		bt2.setUpdtTime(ts);
		service.insert(bt2);
		
		Subscription sub3 = bcstTrkBo.removeFromList(bt1.getBroadcastMessage().getRowId(), sub2.getEmailAddr().getRowId());
		assertNotNull(sub3);
	}

}
