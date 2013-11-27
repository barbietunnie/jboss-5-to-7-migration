package com.es.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.outbox.DeliveryStatusDao;
import com.es.data.constant.MailingListDeliveryType;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.outbox.DeliveryStatusVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class DeliveryStatusTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private DeliveryStatusDao deliveryStatusDao;
	@Resource
	private EmailAddressDao emailAddrDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	private static long testMsgId = 3L;
	private String testEmailAddr = "demolist1@localhost";

	@BeforeClass
	public static void DeliveryStatusPrepare() {
	}
	
	@Test
	@Rollback(true)
	public void testDeliveryStatus() {
		try {
			if (msgInboxDao.getByPrimaryKey(testMsgId)==null) {
				testMsgId = msgInboxDao.getLastRecord().getMsgId();
			}
			List<DeliveryStatusVo> list = selectByMsgId(testMsgId);
			if (list.size()==0) {
				insert(testMsgId);
			}
			list = selectByMsgId(testMsgId);
			assertTrue(list.size()>0);
			DeliveryStatusVo vo0 = list.get(0);
			DeliveryStatusVo vo = selectByPrimaryKey(vo0.getMsgId(), vo0.getFinalRecipientId());
			assertNotNull(vo);
			DeliveryStatusVo vo2 = insert(vo.getMsgId());
			assertNotNull(vo2);
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2.getMsgId(), vo2.getFinalRecipientId());
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<DeliveryStatusVo> selectByMsgId(long msgId) {
		List<DeliveryStatusVo> actions = deliveryStatusDao.getByMsgId(msgId);
		for (Iterator<DeliveryStatusVo> it = actions.iterator(); it.hasNext();) {
			DeliveryStatusVo deliveryStatusVo = it.next();
			System.out.println("DeliveryStatusDao - selectByMsgId: " + LF + deliveryStatusVo);
		}
		return actions;
	}

	private DeliveryStatusVo selectByPrimaryKey(long msgId, long finalRcptId) {
		DeliveryStatusVo deliveryStatusVo = (DeliveryStatusVo) deliveryStatusDao.getByPrimaryKey(
				msgId, finalRcptId);
		System.out.println("AttachmentsDao - selectByPrimaryKey: " + LF + deliveryStatusVo);
		return deliveryStatusVo;
	}

	private int update(DeliveryStatusVo deliveryStatusVo) {
		deliveryStatusVo.setDeliveryStatus("");
		int rows = deliveryStatusDao.update(deliveryStatusVo);
		System.out.println("AttachmentsDao - update: " + LF + deliveryStatusVo);
		return rows;
	}

	private int deleteByPrimaryKey(long msgId, long finalRcptId) {
		int rowsDeleted = deliveryStatusDao.deleteByPrimaryKey(msgId, finalRcptId);
		System.out.println("DeliveryStatusDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}

	private DeliveryStatusVo insert(long msgId) {
		List<DeliveryStatusVo> list = deliveryStatusDao.getByMsgId(msgId);
		DeliveryStatusVo vo = null;
		if (list.size() > 0) {
			vo = list.get(list.size() - 1);
			vo.setFinalRecipientId(vo.getFinalRecipientId() + 1);
		}
		else {
			vo = new DeliveryStatusVo();
			vo.setDeliveryStatus(MailingListDeliveryType.ALL_ON_LIST.getValue());
			EmailAddressVo addrVo = selectByAddress(testEmailAddr);
			assertNotNull(addrVo);
			vo.setMsgId(msgId);
			vo.setFinalRecipientId(addrVo.getEmailAddrId());
			vo.setFinalRecipient(addrVo.getEmailAddr());
			vo.setMessageId("<24062053.11229376477123.JavaMail.IAPJKW@TSD-97050>");
			vo.setDsnRfc822("RFC822");
			vo.setDsnText("DESn Text");
		}
		int rows = deliveryStatusDao.insert(vo);
		System.out.println("DeliveryStatusDao - insert: rows inserted " + rows + LF + vo);
		return vo;
	}

	private EmailAddressVo selectByAddress(String emailAddr) {
		EmailAddressVo addrVo = emailAddrDao.findSertAddress(emailAddr);
		System.out.println("EmailAddressDao - selectByAddress: "+LF+addrVo);
		return addrVo;
	}
}
