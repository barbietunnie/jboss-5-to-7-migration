package com.es.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.inbox.MsgUnreadCountDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgUnreadCountTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private MsgUnreadCountDao msgUnreadCountDao;
	
	@BeforeClass
	public static void MsgUnreadCountPrepare() {
	}
	
	@Test
	public void testUpdate() {
		int rows = resetUnreadCounts();
		assertTrue(rows==15);
		rows = updateInboxUnreadCount(1);
		assertTrue(rows==1);
		rows = updateInboxUnreadCount(-1);
		assertTrue(rows==1);
		rows = updateSentUnreadCount(1);
		assertTrue(rows==1);
		rows = updateSentUnreadCount(-1);
		assertTrue(rows==1);
	}

	private int updateInboxUnreadCount(int delta) {
		int rowsUpdated = msgUnreadCountDao.updateInboxUnreadCount(delta);
		int count = msgUnreadCountDao.selectInboxUnreadCount();
		System.out.println("updateInboxUnreadCount() " + rowsUpdated + ", count: " + count);
		return rowsUpdated;
	}
	
	private int updateSentUnreadCount(int delta) {
		int rowsUpdated = msgUnreadCountDao.updateSentUnreadCount(delta);
		int count = msgUnreadCountDao.selectSentUnreadCount();
		System.out.println("updateSentUnreadCount() " + rowsUpdated + ", count: " + count);
		return rowsUpdated;
	}
	
	private int resetUnreadCounts() {
		msgUnreadCountDao.resetUnreadCounts(10, 5);
		int inboxCount = msgUnreadCountDao.selectInboxUnreadCount();
		int sentCount = msgUnreadCountDao.selectSentUnreadCount();
		System.out.println("updateUnreadCounts() " + inboxCount + " : " + sentCount);
		return (inboxCount + sentCount);
	}
}
