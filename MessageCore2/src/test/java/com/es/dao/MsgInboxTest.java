package com.es.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.es.dao.inbox.MsgClickCountDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.inbox.MsgUnreadCountDao;
import com.es.dao.outbox.MsgSequenceDao;
import com.es.data.constant.CarrierCode;
import com.es.data.preload.RuleNameEnum;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.inbox.MsgClickCountVo;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.inbox.MsgInboxWebVo;
import com.es.vo.inbox.SearchFieldsVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class MsgInboxTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgUnreadCountDao unreadCountDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	@Resource
	private MsgClickCountDao msgClickCountsDao;
	@Resource
	private MsgSequenceDao msgSequenceDao;
	@Resource
	private EmailAddressDao emailAddressDao;
	static long testMsgId = 2L;
	static long testFromMsgId = 1L;
	
	@BeforeClass
	public static void MsgInboxPrepare() {
	}
	
	@Test
	@Rollback(true)
	public void testMessageInbox() {
		try {
			MsgInboxVo msgInboxVo = selectByMsgId(testMsgId);
			if (msgInboxVo == null) {
				msgInboxVo = msgInboxDao.getFirstRecord();
				testMsgId = msgInboxVo.getMsgId();
			}
			assertNotNull(msgInboxVo);
			EmailAddressVo emailAddressVo = emailAddressDao.getByAddrId(testFromMsgId);
			if (emailAddressVo == null) {
				emailAddressVo = emailAddressDao.findSertAddress(msgInboxVo.getFromAddress());
				testFromMsgId = emailAddressVo.getEmailAddrId();
			}
			List<MsgInboxVo> list = selectByFromAddrId(testFromMsgId);
			assertTrue(list.size()>0);
			List<MsgInboxVo> list2 = selectByToAddrId(msgInboxVo.getToAddrId());
			assertTrue(list2.size()>0);
			MsgInboxWebVo webvo = selectInboundGenericMsg();
			int unreadCountBefore = unreadCountDao.selectInboxUnreadCount();
			MsgInboxVo msgvo = insert(webvo.getMsgId());
			int unreadCountAfter = unreadCountDao.selectInboxUnreadCount();
			assertNotNull(msgvo);
			if (msgvo.getReadCount() == 0) {
				assertTrue(unreadCountAfter == (unreadCountBefore + 1));
			}
			else {
				assertTrue(unreadCountAfter == unreadCountBefore);
			}
			int rowsUpdated = update(msgvo.getMsgId());
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(msgvo.getMsgId());
			assertEquals(rowsDeleted, 1);
			int unreadCountAfterDelete = unreadCountDao.selectInboxUnreadCount();
			assertTrue(unreadCountAfterDelete == unreadCountBefore);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	@Rollback(true)
	public void testBroadCastAndClickCounts() {
		try {
			// get "Closed" Broadcast messages
			MsgInboxWebVo webvo = selectBroadcastMsg(SearchFieldsVo.MsgType.Closed);
			if (webvo == null) {
				// did not find any, get any "Broadcast" messages
				webvo = selectBroadcastMsg(null);
			}
			assertNotNull(webvo);
			int unreadCountBefore = unreadCountDao.selectInboxUnreadCount();
			MsgInboxVo msgvo = insert(webvo.getMsgId());
			int unreadCountAfter = unreadCountDao.selectInboxUnreadCount();
			assertTrue(unreadCountAfter == unreadCountBefore);
			assertNotNull(msgvo);
			MsgClickCountVo ccvo = insertClickCount(msgvo, testMsgId);
			assertNotNull(ccvo);
			MsgClickCountVo ccvo2 = selectClickCounts(ccvo.getMsgId());
			assertNotNull(ccvo2);
			ccvo2.setComplaintCount(ccvo.getComplaintCount());
			ccvo2.setUnsubscribeCount(ccvo.getUnsubscribeCount());
			assertTrue(ccvo.equalsTo(ccvo2));
			int rowsCCUpdated = updateClickCounts(ccvo2);
			assertTrue(rowsCCUpdated>0);
			int rowsCCDeleted = deleteClickCounts(ccvo2.getMsgId());
			assertEquals(rowsCCDeleted, 1);
			int rowsDeleted = deleteByPrimaryKey(msgvo.getMsgId());
			assertEquals(rowsDeleted, 1);
			int unreadCountAfterDelete = unreadCountDao.selectInboxUnreadCount();
			assertTrue(unreadCountAfterDelete == unreadCountBefore);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private MsgInboxVo selectByMsgId(long msgId) {
		MsgInboxVo msgInboxVo = (MsgInboxVo)msgInboxDao.getByPrimaryKey(msgId);
		System.out.println("MsgInboxDao - selectByPrimaryKey: "+LF+msgInboxVo);
		return msgInboxVo;
	}
	
	private List<MsgInboxVo> selectByFromAddrId(long msgId) {
		List<MsgInboxVo> actions = msgInboxDao.getByFromAddrId(msgId);
		for (Iterator<MsgInboxVo> it = actions.iterator(); it.hasNext();) {
			MsgInboxVo vo = it.next();
			System.out.println("MsgInboxDao - selectByFromAddrId: " + vo.getMsgId() + " - "
					+ vo.getFromAddress());
		}
		return actions;
	}

	private List<MsgInboxVo> selectByToAddrId(long msgId) {
		List<MsgInboxVo> actions = msgInboxDao.getByToAddrId(msgId);
		for (Iterator<MsgInboxVo> it = actions.iterator(); it.hasNext();) {
			MsgInboxVo vo = it.next();
			System.out.println("MsgInboxDao - selectByToAddrId: " + vo.getMsgId() + " - "
					+ vo.getToAddress());
		}
		return actions;
	}
	
	private MsgInboxWebVo selectBroadcastMsg(SearchFieldsVo.MsgType msgType) {
		SearchFieldsVo vo = new SearchFieldsVo();
		vo.setRuleName(RuleNameEnum.BROADCAST.getValue());
		vo.setMsgType(msgType);
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(vo);
		for (MsgInboxWebVo webVo : list) {
			System.out.println("MsgInboxWebVo - selectBroadcastMsg: " + LF + webVo);
			return webVo;
		}
		return null;
	}

	private MsgInboxWebVo selectInboundGenericMsg() {
		SearchFieldsVo vo = new SearchFieldsVo();
		vo.setRuleName(RuleNameEnum.GENERIC.getValue());
		vo.setMsgType(SearchFieldsVo.MsgType.Received);
		List<MsgInboxWebVo> list = msgInboxDao.getListForWeb(vo);
		for (MsgInboxWebVo webVo : list) {
			System.out.println("MsgInboxWebVo - selectInboundGenericMsg: " + LF + webVo);
			return webVo;
		}
		throw new IllegalStateException("Failed to fetch any Generic inbound messages.");
	}

	private int update(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		int rows = 0;
		if (msgInboxVo!=null) {
			msgInboxVo.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
			msgInboxVo.setPurgeDate(new java.sql.Date(new java.util.Date().getTime()));
			rows = msgInboxDao.update(msgInboxVo);
			System.out.println("MsgInboxDao - update: rows updated:  "+ rows);
			System.out.println("InboxUnreadCount: "+ unreadCountDao.selectInboxUnreadCount());
		}
		return rows;
	}
	
	private int deleteByPrimaryKey(long msgId) {
		int rowsDeleted = msgInboxDao.deleteByPrimaryKey(msgId);
		System.out.println("MsgInboxDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		System.out.println("InboxUnreadCount: "+ unreadCountDao.selectInboxUnreadCount());
		return rowsDeleted;
	}
	
	private MsgInboxVo insert(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo!=null) {
			long nextVal = msgSequenceDao.findNextValue();
			msgInboxVo.setMsgId(nextVal);
			System.out.println("InboxUnreadCount before: "+ unreadCountDao.selectInboxUnreadCount());
			msgInboxDao.insert(msgInboxVo);
			System.out.println("MsgInboxDao - insert: "+LF+msgInboxVo);
			System.out.println("InboxUnreadCount after: "+ unreadCountDao.selectInboxUnreadCount());
			return msgInboxVo;
		}
		throw new IllegalStateException("Failed to fetch message by MsgId (" + msgId + ")!");
	}

	private MsgClickCountVo insertClickCount(MsgInboxVo msgvo, long msgId) {
		MsgClickCountVo vo = msgClickCountsDao.getByPrimaryKey(msgId);
		if (vo == null) {
			vo = new MsgClickCountVo();
			vo.setMsgId(msgvo.getMsgId());
			vo.setClickCount(1);
			msgClickCountsDao.insert(vo);
			System.out.println("insertClickCount: "+LF+vo);
		}
		else {
			vo.setClickCount(vo.getClickCount() + 1);
			msgClickCountsDao.update(vo);
		}
		return vo;
	}

	private MsgClickCountVo selectClickCounts(long msgId) {
		MsgClickCountVo vo = (MsgClickCountVo)msgClickCountsDao.getByPrimaryKey(msgId);
		System.out.println("selectByPrimaryKey - "+LF+vo);
		return vo;
	}

	private int updateClickCounts(MsgClickCountVo msgClickCountsVo) {
		int rows = 0;
		msgClickCountsVo.setSentCount(msgClickCountsVo.getSentCount() + 1);
		rows += msgClickCountsDao.update(msgClickCountsVo);
		rows += msgClickCountsDao.updateOpenCount(msgClickCountsVo.getMsgId());
		rows += msgClickCountsDao.updateClickCount(msgClickCountsVo.getMsgId());
		rows += msgClickCountsDao.updateUnsubscribeCount(msgClickCountsVo.getMsgId(),1);
		rows += msgClickCountsDao.updateComplaintCount(msgClickCountsVo.getMsgId(),1);
		rows += msgClickCountsDao.updateClickCount(msgClickCountsVo.getMsgId());
		System.out.println("updateClickCounts: rows updated: "+rows+LF+msgClickCountsVo);
		return rows;
	}
	
	private int deleteClickCounts(long msgId) {
		int rowsDeleted = msgClickCountsDao.deleteByPrimaryKey(msgId);
		System.out.println("deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
}
