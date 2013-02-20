package com.legacytojava.message.dao.outbox;

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

import com.legacytojava.message.constant.MailingListDeliveryOption;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.outbox.DeliveryStatusVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class DeliveryStatusTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private DeliveryStatusDao deliveryStatusDao;
	@Resource
	private EmailAddrDao emailAddrDao;
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
		if (list.size() > 0) {
			DeliveryStatusVo vo = list.get(list.size() - 1);
			vo.setFinalRecipientId(vo.getFinalRecipientId() + 1);
			int rows = deliveryStatusDao.insert(vo);
			System.out.println("DeliveryStatusDao - insert: rows inserted " + rows + LF + vo);
			return vo;
		}
		else {
			DeliveryStatusVo vo = new DeliveryStatusVo();
			vo.setDeliveryStatus(MailingListDeliveryOption.ALL_ON_LIST);
			EmailAddrVo addrVo = selectByAddress(testEmailAddr);
			assertNotNull(addrVo);
			vo.setMsgId(msgId);
			vo.setFinalRecipientId(addrVo.getEmailAddrId());
			vo.setFinalRecipient(addrVo.getEmailAddr());
			vo.setMessageId("<24062053.11229376477123.JavaMail.IAPJKW@TSD-97050>");
			vo.setDsnRfc822("RFC822");
			vo.setDsnText("DESn Text");
			int rows = deliveryStatusDao.insert(vo);
			System.out.println("DeliveryStatusDao - insert: rows inserted " + rows + LF + vo);
			return vo;
		}
	}

	private EmailAddrVo selectByAddress(String emailAddr) {
		EmailAddrVo addrVo = emailAddrDao.findByAddress(emailAddr);
		System.out.println("EmailAddrDao - selectByAddress: "+LF+addrVo);
		return addrVo;
	}
}
