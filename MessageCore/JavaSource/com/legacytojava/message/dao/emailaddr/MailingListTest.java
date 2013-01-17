package com.legacytojava.message.dao.emailaddr;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.vo.emailaddr.MailingListVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
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
			List<MailingListVo> list = selectByAddr(vo.getEmailAddr());
			assertTrue(list.size()>0);
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
			MailingListVo vo = new MailingListVo();
			vo.setListId(StringUtils.left(listId, 5) + "_v2");
			delete(vo);
		}
	}
	
	private MailingListVo selectByListId(MailingListVo vo) {
		MailingListVo mailingList = mailingListDao.getByListId(vo.getListId());
		if (mailingList != null) {
			System.out.println("MailingListDao - selectByListId: " + LF + mailingList);
		}
		return mailingList;
	}
	
	private List<MailingListVo> selectByAddr(String emailAddr) {
		List<MailingListVo> list = mailingListDao.getByAddress(emailAddr);
		for (MailingListVo vo : list) {
			System.out.println("MailingListDao - selectByAddr: " + LF + vo);
		}
		mailingListDao.getSubscribedLists(1);
		mailingListDao.getAll(false);
		mailingListDao.getAll(true);
		return list;
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
		mailingListDao.insert(mailingListVo);
		System.out.println("MailingListDao - insert: "+mailingListVo);
		return mailingListVo;
	}
}
