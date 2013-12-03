package com.es.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.MailingListDao;
import com.es.vo.address.MailingListVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MailingListTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MailingListDao mailingListDao;
	private String listId = "SMPLLST1";

	@BeforeClass
	public static void MailingListPrepare() throws Exception {
	}

	@Test
	public void testInsertSelectDelete() {
		try {
			MailingListVo vo = insert();
			assertNotNull(vo);
			assertTrue(vo.getListId().endsWith("_v2"));
			MailingListVo vo1 = selectByAddr(vo.getEmailAddr());
			assertTrue(vo1!=null);
			MailingListVo vo2 = selectByListId(vo);
			assertNotNull(vo2);
			vo.setCreateTime(vo2.getCreateTime());
			vo.setClickCount(vo2.getClickCount());
			vo.setOpenCount(vo2.getOpenCount());
			vo.setSentCount(vo2.getSentCount());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			MailingListVo vo = new MailingListVo();
			vo.setListId(StringUtils.left(listId, 5) + "_v2");
			delete(vo);
			fail();
		}
	}
	
	private MailingListVo selectByListId(MailingListVo vo) {
		MailingListVo mailingList = mailingListDao.getByListId(vo.getListId());
		if (mailingList != null) {
			System.out.println("MailingListDao - selectByListId: " + LF + mailingList);
		}
		return mailingList;
	}
	
	private MailingListVo selectByAddr(String emailAddr) {
		MailingListVo vo = mailingListDao.getByListAddress(emailAddr);
		if (vo != null) {
			System.out.println("MailingListDao - selectByAddr: " + LF + vo);
		}
		mailingListDao.getSubscribedLists(1);
		mailingListDao.getAll(false);
		mailingListDao.getAll(true);
		return vo;
	}
	
	private int update(MailingListVo vo) {
		MailingListVo mailingList = mailingListDao.getByListId(vo.getListId());
		int rowsUpdated = 0;
		if (mailingList!=null) {
			mailingList.setStatusId("A");
			rowsUpdated = mailingListDao.update(mailingList);
			System.out.println("MailingListDao - update: rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int delete(MailingListVo mailingListVo) {
		int rowsDeleted = mailingListDao.deleteByListId(mailingListVo.getListId());
		System.out.println("MailingListDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MailingListVo insert() {
		MailingListVo mailingListVo = mailingListDao.getByListId(listId);
		mailingListVo.setListId(StringUtils.left(mailingListVo.getListId(),5)+"_v2");
		mailingListVo.setAcctUserName(mailingListVo.getAcctUserName() + "_v2");
		mailingListDao.insert(mailingListVo);
		System.out.println("MailingListDao - insert: "+mailingListVo);
		return mailingListVo;
	}
}
