package com.legacytojava.message.dao.mailbox;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.vo.MailBoxVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MailBoxTest {
	final static String LF = System.getProperty("line.separator","\n");
	final boolean testOnlyActive = true;
	final String testUserId = "TestUserId";
	@Resource
	private MailBoxDao mailBoxDao;
	public static void MailBoxPrepare() {
	}
	
	@Test
	public void testMailbox() {
		List<MailBoxVo> list = selectForTrail(testOnlyActive);
		assertTrue(list.size()==1);
		List<MailBoxVo> list2 = select(!testOnlyActive);
		assertTrue(list2.size()>1);
		MailBoxVo vo0 = list2.get(0);
		MailBoxVo vo = selectByPrimaryKey(vo0.getUserId(), vo0.getHostName());
		assertNotNull(vo);
		MailBoxVo vo2 = insert(vo, testUserId);
		assertNotNull(vo2);
		vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
		vo.setUpdtTime(vo2.getUpdtTime());
		vo.setRowId(vo2.getRowId());
		vo.setUserId(vo2.getUserId());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = delete(vo2);
		assertEquals(rowsDeleted, 1);
	}
	
	private List<MailBoxVo> selectForTrail(boolean onlyActive) {
		List<MailBoxVo> mailBoxes = mailBoxDao.getAllForTrial(onlyActive);
		for (Iterator<MailBoxVo> it=mailBoxes.iterator(); it.hasNext();) {
			MailBoxVo mailBoxVo = it.next();
			System.out.println("MailBoxDao - getAllForTrial(): "+mailBoxVo);
		}
		return mailBoxes;
	}
	
	private List<MailBoxVo> select(boolean onlyActive) {
		List<MailBoxVo> mailBoxes = mailBoxDao.getAll(!onlyActive);
		for (Iterator<MailBoxVo> it=mailBoxes.iterator(); it.hasNext();) {
			MailBoxVo mailBoxVo = it.next();
			System.out.println("MailBoxDao - getAll(): "+mailBoxVo);
		}
		return mailBoxes;
	}
	
	private MailBoxVo selectByPrimaryKey(String userId, String hostName) {
		MailBoxVo mailBoxVo = mailBoxDao.getByPrimaryKey(userId, hostName);
		if (mailBoxVo!=null) {
			System.out.println("MailBoxDao - selectByPrimaryKey: "+LF+mailBoxVo);
		}
		return mailBoxVo;
	}
	
	private int update(MailBoxVo mailBoxVo) {
		if (!StatusIdCode.ACTIVE.equals(mailBoxVo.getStatusId())) {
			mailBoxVo.setStatusId(StatusIdCode.ACTIVE);
		}
		int rows = mailBoxDao.update(mailBoxVo);
		System.out.println("MailBoxDao - update: rows updated "+ rows);
		return rows;
	}
	
	private int delete(MailBoxVo mailBoxVo) {
		int rowsDeleted = mailBoxDao.deleteByPrimaryKey(mailBoxVo.getUserId(),
				mailBoxVo.getHostName());
		System.out.println("MailBoxDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	private MailBoxVo insert(MailBoxVo mailBoxVo, String userId) {
		mailBoxVo.setUserId(userId);
		int rows = mailBoxDao.insert(mailBoxVo);
		System.out.println("MailBoxDao - insert: rows inserted "+rows+LF+mailBoxVo);
		return mailBoxVo;
	}
}
