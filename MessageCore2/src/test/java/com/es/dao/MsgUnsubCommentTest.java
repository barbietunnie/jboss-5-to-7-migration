package com.es.dao;

import static org.junit.Assert.*;

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
import com.es.dao.inbox.MsgUnsubCommentDao;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.inbox.MsgUnsubCommentVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgUnsubCommentTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private MsgUnsubCommentDao msgUnsubCommentDao;
	@Resource
	private EmailAddressDao emailAddrDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	private long testMsgId = 2L;
	
	@BeforeClass
	public static void MsgUnsubCommentsPrepare() {
	}
	
	@Test
	@Rollback(true)
	public void testUnsubComments() {
		//insertToEmptyTable();
		List<MsgUnsubCommentVo> list = selectAll();
		if (list.isEmpty()) {
			MsgInboxVo inbox = msgInboxDao.getByPrimaryKey(testMsgId);
			if (inbox == null) {
				inbox = msgInboxDao.getLastRecord();
				testMsgId = inbox.getMsgId();
			}
			insert(testMsgId);
			list = selectAll();
		}
		assertTrue(list.size()>0);
		MsgUnsubCommentVo vo = select(list.get(0).getRowId());
		assertNotNull(vo);
		MsgUnsubCommentVo vo2 = insert(vo);
		assertNotNull(vo2);
		vo.setAddTime(vo2.getAddTime());
		vo.setRowId(vo2.getRowId());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = delete(vo2.getRowId());
		assertEquals(rowsDeleted, 1);
	}
	
	void insertToEmptyTable() {
		MsgUnsubCommentVo vo = new MsgUnsubCommentVo();
		vo.setComments("Test Comments");
		vo.setMsgId(2L);
		vo.setEmailAddrId(9L);
		vo.setListId("SMPLLST1");
		vo.setAddTime(new java.sql.Timestamp(System.currentTimeMillis()));
		msgUnsubCommentDao.insert(vo);	
	}

	private MsgUnsubCommentVo select(int rowId) {
		MsgUnsubCommentVo msgUnsubComments = msgUnsubCommentDao.getByPrimaryKey(rowId);
		System.out.println("MsgUnsubCommentsDao - select: "+msgUnsubComments);
		return msgUnsubComments;
	}
	
	private List<MsgUnsubCommentVo> selectAll() {
		List<MsgUnsubCommentVo> list = msgUnsubCommentDao.getAll();
		System.out.println("MsgUnsubCommentsDao - getAll() - size: " + list.size());
		list = msgUnsubCommentDao.getAll();
		for (MsgUnsubCommentVo vo : list) {
			System.out.println("MsgUnsubCommentsDao - select All: "+vo);
			break;
		}
		return list;
	}
	
	private int update(MsgUnsubCommentVo vo) {
		vo.setComments(vo.getComments() + LF + "Some new comments.");
		int rowsUpdated = msgUnsubCommentDao.update(vo);
		System.out.println("MsgUnsubCommentsDao - rows updated: "+rowsUpdated);
		return rowsUpdated;
	}
	
	private int delete(int rowId) {
		int rowsDeleted = msgUnsubCommentDao.deleteByPrimaryKey(rowId);
		System.out.println("MsgUnsubCommentsDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgUnsubCommentVo insert(MsgUnsubCommentVo vo) {
		vo.setComments("Another test Comment.");
		int rows = msgUnsubCommentDao.insert(vo);
		System.out.println("MsgUnsubCommentsDao - insert: rows inserted "+rows+LF+vo);
		return vo;
	}
	private MsgUnsubCommentVo insert(long msgId) {
		List<MsgUnsubCommentVo> list = msgUnsubCommentDao.getAll();
		if (list.size() > 0) {
			MsgUnsubCommentVo vo = list.get(list.size()-1);
			vo.setComments("Another test Comment.");
			int rows = msgUnsubCommentDao.insert(vo);
			System.out.println("MsgUnsubCommentsDao - insert: rows inserted "+rows);
			return vo;
		}
		else {
			MsgUnsubCommentVo vo = new MsgUnsubCommentVo();
			vo.setComments("Test Comments.");
			//String listId = "SMPLLST1";
			String emailAddr = "jsmith@test.com";
			EmailAddressVo addr = emailAddrDao.getByAddress(emailAddr);
			vo.setMsgId(msgId);
			vo.setEmailAddrId(addr.getEmailAddrId());
			//vo.setListId(listId);
			int rows = msgUnsubCommentDao.insert(vo);
			System.out.println("UnsubCommentsDao - insert: (empty table) "+rows);
			return vo;
		}
	}

}
