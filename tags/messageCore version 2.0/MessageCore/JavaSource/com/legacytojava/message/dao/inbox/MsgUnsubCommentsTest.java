package com.legacytojava.message.dao.inbox;

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

import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.MsgUnsubCommentsVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MsgUnsubCommentsTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private MsgUnsubCommentsDao msgUnsubCommentsDao;
	@Resource
	private EmailAddrDao emailAddrDao;
	
	@BeforeClass
	public static void MsgUnsubCommentsPrepare() {
	}
	
	@Test
	@Rollback(true)
	public void testUnsubComments() {
		//insertToEmptyTable();
		List<MsgUnsubCommentsVo> list = selectAll();
		if (list.size()==0) {
			insert();
			list = selectAll();
		}
		assertTrue(list.size()>0);
		MsgUnsubCommentsVo vo = select(list.get(0).getRowId());
		assertNotNull(vo);
		MsgUnsubCommentsVo vo2 = insert(vo);
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
		MsgUnsubCommentsVo vo = new MsgUnsubCommentsVo();
		vo.setComments("Test Comments");
		vo.setMsgId(2L);
		vo.setEmailAddrId(9L);
		vo.setListId("SMPLLST1");
		vo.setAddTime(new java.sql.Timestamp(System.currentTimeMillis()));
		msgUnsubCommentsDao.insert(vo);	
	}

	private MsgUnsubCommentsVo select(int rowId) {
		MsgUnsubCommentsVo msgUnsubComments = msgUnsubCommentsDao.getByPrimaryKey(rowId);
		System.out.println("MsgUnsubCommentsDao - select: "+msgUnsubComments);
		return msgUnsubComments;
	}
	
	private List<MsgUnsubCommentsVo> selectAll() {
		List<MsgUnsubCommentsVo> list = msgUnsubCommentsDao.getAll();
		System.out.println("MsgUnsubCommentsDao - getAll() - size: " + list.size());
		list = msgUnsubCommentsDao.getAll();
		for (MsgUnsubCommentsVo vo : list) {
			System.out.println("MsgUnsubCommentsDao - select All: "+vo);
			break;
		}
		return list;
	}
	
	private int update(MsgUnsubCommentsVo vo) {
		vo.setComments(vo.getComments() + LF + "Some new comments.");
		int rowsUpdated = msgUnsubCommentsDao.update(vo);
		System.out.println("MsgUnsubCommentsDao - rows updated: "+rowsUpdated);
		return rowsUpdated;
	}
	
	private int delete(int rowId) {
		int rowsDeleted = msgUnsubCommentsDao.deleteByPrimaryKey(rowId);
		System.out.println("MsgUnsubCommentsDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgUnsubCommentsVo insert(MsgUnsubCommentsVo vo) {
		vo.setComments("Another test Comment.");
		int rows = msgUnsubCommentsDao.insert(vo);
		System.out.println("MsgUnsubCommentsDao - insert: rows inserted "+rows+LF+vo);
		return vo;
	}
	private MsgUnsubCommentsVo insert() {
		List<MsgUnsubCommentsVo> list = msgUnsubCommentsDao.getAll();
		if (list.size() > 0) {
			MsgUnsubCommentsVo vo = list.get(list.size()-1);
			vo.setComments("Another test Comment.");
			int rows = msgUnsubCommentsDao.insert(vo);
			System.out.println("MsgUnsubCommentsDao - insert: rows inserted "+rows);
			return vo;
		}
		else {
			MsgUnsubCommentsVo vo = new MsgUnsubCommentsVo();
			vo.setComments("Test Comments.");
			//String listId = "SMPLLST1";
			String emailAddr = "jsmith@test.com";
			Long msgId = 2L;
			EmailAddrVo addr = emailAddrDao.getByAddress(emailAddr);
			vo.setMsgId(msgId);
			vo.setEmailAddrId(addr.getEmailAddrId());
			//vo.setListId(listId);
			int rows = msgUnsubCommentsDao.insert(vo);
			System.out.println("UnsubCommentsDao - insert: (empty table) "+rows);
			return vo;
		}
	}

}
