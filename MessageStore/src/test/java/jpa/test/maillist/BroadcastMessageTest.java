package jpa.test.maillist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.StatusId;
import jpa.model.BroadcastMessage;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.BroadcastMessageService;
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
public class BroadcastMessageTest {

	@BeforeClass
	public static void BroadcastMessagePrepare() {
	}

	@Autowired
	BroadcastMessageService service;
	
	@Autowired
	EmailAddressService eaService;

	@Test
	public void testBroadcastMessageService() {
		
		List<BroadcastMessage> bdlist = service.getAll();
		assertFalse(bdlist.isEmpty());
		
		BroadcastMessage bd1 = bdlist.get(0);
		
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		bd1.setUpdtTime(ts);
		bd1.setClickCount(bd1.getClickCount()+1);
		bd1.setOpenCount(bd1.getOpenCount()+1);
		bd1.setSentCount(bd1.getSentCount()+1);
		bd1.setComplaintCount(bd1.getComplaintCount()+1);
		bd1.setUnsubscribeCount(bd1.getUnsubscribeCount()+1);
		bd1.setReferralCount(bd1.getReferralCount()+1);
		bd1.setLastClickTime(ts);
		bd1.setLastOpenTime(ts);
		bd1.setMsgSubject("Test Broadcast message # 1");
		bd1.setMsgBody("Test Broadcast message body here.\n" + bd1.getMsgBody());
		service.update(bd1);
		
		BroadcastMessage bd2 = service.getByRowId(bd1.getRowId());
		assertTrue(ts.equals(bd2.getUpdtTime()));
		System.out.println(StringUtil.prettyPrint(bd2, 2));
		
		List<BroadcastMessage> bdlist2 = service.getByMailingListId(bd1.getMailingList().getListId());
		assertFalse(bdlist2.isEmpty());
		
		List<BroadcastMessage> bdlist3 = service.getByEmailTemplateId(bd1.getEmailTemplate().getTemplateId());
		assertFalse(bdlist3.isEmpty());
		
		BroadcastMessage bd3 = new BroadcastMessage();
		bd3.setMailingList(bd1.getMailingList());
		bd3.setEmailTemplate(bd1.getEmailTemplate());
		bd3.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
		bd3.setStatusId(StatusId.ACTIVE.getValue());
		bd3.setUpdtUserId(Constants.DEFAULT_USER_ID);
		bd3.setStartTime(ts);
		bd3.setUpdtTime(ts);
		service.insert(bd3);
		
		assertTrue(bd3.getRowId()>0  && bd3.getRowId()!=bd1.getRowId());
		service.delete(bd3);
		assert(0==service.deleteByRowId(bd3.getRowId()));
		try {
			service.getByRowId(bd3.getRowId());
			fail();
		}
		catch (NoResultException e) {
			// expected
		}		
	}

	@Test
	public void testBroadcastMessageForWeb() {
		List<BroadcastMessage> bdlist = service.getAll();
		assertFalse(bdlist.isEmpty());
		
		BroadcastMessage bd1 = bdlist.get(0);
		bd1.setSentCount(bd1.getSentCount()+1);
		service.update(bd1);
		
		int count = service.getMessageCountForWeb();
		assertTrue(count>=1);
		
		PagingVo vo = new PagingVo();
		List<BroadcastMessage> pageList = service.getBroadcastsWithPaging(vo);
		assertFalse(pageList.isEmpty());
	}
}
