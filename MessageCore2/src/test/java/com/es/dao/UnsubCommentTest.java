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
import com.es.dao.address.UnsubCommentDao;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.address.UnsubCommentVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class UnsubCommentTest {
	final static String LF = System.getProperty("line.separator","\n");
	final String listId = "SMPLLST1";
	final String emailAddr = "jsmith@test.com";
	final int testRowId = 1;
	@Resource
	private UnsubCommentDao unsubCommentDao;
	@Resource
	private EmailAddressDao emailAddrDao;

	@BeforeClass
	public static void UnsubCommentsPrepare() {
	}
	
	@Test
	@Rollback(true)
	public void testUnsubComments() {
		List<UnsubCommentVo> list = selectAll();
		if (list.size()==0) {
			insert();
			list = selectAll();
		}
		assertTrue(list.size()>0);
		UnsubCommentVo vo = selectByPrimaryKey(list.get(0).getRowId());
		assertNotNull(vo);
		UnsubCommentVo vo2 = insert();
		assertNotNull(vo2);
		vo.setAddTime(vo2.getAddTime());
		vo.setRowId(vo2.getRowId());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		List<UnsubCommentVo> list2 = selectByEmailAddrId(vo2.getEmailAddrId());
		assertTrue(list2.size()>list.size());
		int rowsDeleted = delete(vo2.getRowId());
		assertEquals(rowsDeleted, 1);
	}
	
	private UnsubCommentVo selectByPrimaryKey(int rowId) {
		UnsubCommentVo unsubComments = unsubCommentDao.getByPrimaryKey(rowId);
		System.out.println("UnsubCommentsDao - selectByPrimaryKey: "+LF+unsubComments);
		return unsubComments;
	}
	
	private List<UnsubCommentVo> selectAll() {
		List<UnsubCommentVo> list = unsubCommentDao.getAll();
		System.out.println("UnsubCommentsDao - getAll() - size: " + list.size());
		list = unsubCommentDao.getAll();
		for (UnsubCommentVo vo : list) {
			System.out.println("UnsubCommentsDao - select All: "+LF+vo);
			break;
		}
		return list;
	}
	
	private int update(UnsubCommentVo vo) {
		vo.setComments(vo.getComments() + LF + " new comments.");
		int rowsUpdated = unsubCommentDao.update(vo);
		System.out.println("UnsubCommentsDao - rows updated: "+rowsUpdated);
		return rowsUpdated;
	}
	
	private int delete(int rowId) {
		int rowsDeleted = unsubCommentDao.deleteByPrimaryKey(rowId);
		System.out.println("UnsubCommentsDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private UnsubCommentVo insert() {
		List<UnsubCommentVo> list = unsubCommentDao.getAll();
		if (list.size() > 0) {
			UnsubCommentVo vo = list.get(list.size() - 1);
			vo.setComments("Test Comments.");
			unsubCommentDao.insert(vo);
			System.out.println("UnsubCommentsDao - insert: "+LF+vo);
			return vo;
		}
		else {
			UnsubCommentVo vo = new UnsubCommentVo();
			vo.setComments("Test Comments.");
			EmailAddressVo addr = emailAddrDao.getByAddress(emailAddr);
			vo.setEmailAddrId(addr.getEmailAddrId());
			vo.setListId(listId);
			unsubCommentDao.insert(vo);
			System.out.println("UnsubCommentsDao - insert: (empty table) "+LF+vo);
			return vo;
		}
	}

	private List<UnsubCommentVo> selectByEmailAddrId(long emailAddrId) {
		List<UnsubCommentVo> list = unsubCommentDao.getByEmailAddrId(emailAddrId);
		for (UnsubCommentVo vo2 : list) {
			System.out.println("UnsubCommentsDao - selectByEmailAddrId: rows returned "+list.size()+LF+vo2);			
		}
		return list;
	}
	
}
