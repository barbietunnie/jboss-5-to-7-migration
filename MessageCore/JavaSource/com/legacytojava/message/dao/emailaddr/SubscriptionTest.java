package com.legacytojava.message.dao.emailaddr;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.emailaddr.SubscriptionVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class SubscriptionTest {
	final static String LF = System.getProperty("line.separator","\n");
	final String listId = "SMPLLST1";
	final String testAddr = "subtest@test.com";
	@Resource
	private SubscriptionDao subscriptionDao;
	@Resource
	private EmailAddrDao emailAddrDao;
//	@Resource
//	private UnsubCommentsDao unsubCommentsDao;

	@BeforeClass
	public static void SubscriptionPrepare() throws Exception {
	}

	@Test
	public void testSelects() {
		List<SubscriptionVo> list = selectByListId();
		assertTrue(list.size()>0);
		list = selectByListId2();
		assertTrue(list.size()>0);
		list = selectByListId3();
		assertTrue(list.size()>0);
	}

	@Test
	public void insertSelectDelete() {
		try {
			int rowsInserted = subscribe();
			assertEquals(rowsInserted, 1);
			SubscriptionVo vo = selectByAddrAndListId();
			assertNotNull(vo);
			assertEquals("Y", vo.getSubscribed());
			assertEquals("Yes", vo.getSubscribedDesc());
			int rowsDeleted = unsubscribe();
			SubscriptionVo vo1 = selectByAddrAndListId();
			assertNotNull(vo1);
			assertEquals("N", vo1.getSubscribed());
			assertEquals("No", vo1.getSubscribedDesc());
			assertEquals(rowsDeleted, 1);
			rowsDeleted = delete();
			SubscriptionVo vo2 = selectByAddrAndListId();
			assertNull(vo2);
		}
		catch (RuntimeException e) {
			delete();
		}
	}

	private SubscriptionVo selectByAddrAndListId() {
		SubscriptionVo vo = subscriptionDao.getByAddrAndListId(testAddr, listId);
		if (vo != null) {
			System.out.println("getByAddrAndListId(): " + vo);
		}
		return vo;
	}
	
	private int subscribe() {
		int rows = subscriptionDao.subscribe(testAddr, listId);
		System.out.println("subscribed: " + rows);
		return rows;
	}

	private int unsubscribe() {
		int rows = subscriptionDao.unsubscribe(testAddr, listId);
		System.out.println("unsubscribed: " + rows);
		return rows;
	}

	private int delete() {
		EmailAddrVo vo = emailAddrDao.getByAddress(testAddr);
		int rows = subscriptionDao.deleteByPrimaryKey(vo.getEmailAddrId(), listId);
		System.out.println("delete: rows deleted: " + rows);
		return rows;
	}

	private List<SubscriptionVo> selectByListId() {
		subscriptionDao.optInConfirm("jsmith@test.com", listId);
		List<SubscriptionVo> list = subscriptionDao.getSubscribers(listId);
		if (list.isEmpty()) {
			subscriptionDao.subscribe("subtest@test.com", listId);
			subscriptionDao.subscribe("jsmith@test.com", listId);
			list = subscriptionDao.getSubscribers(listId);
		}
		System.out.println("getSubscribers(): number of subscribers: " + list.size());
		for (int i = 0 ; i < list.size(); i++) {
			SubscriptionVo vo = list.get(i);
			System.out.println("selectByListId() - getSubscribers(): [" + i + "] " + LF + vo);
			SubscriptionVo vo2;
			if (i == 0) {
				vo2 = subscriptionDao.getByAddrAndListId(vo.getEmailAddr(), vo.getListId());
				subscriptionDao.updateSentCount(vo.getEmailAddrId(), vo.getListId());
				subscriptionDao.updateOpenCount(vo.getEmailAddrId(), vo.getListId());
				subscriptionDao.updateClickCount(vo.getEmailAddrId(), vo.getListId());
				System.out.println("getByAddrAndListId(): " + vo2);
				vo2 = subscriptionDao.getByPrimaryKey(vo.getEmailAddrId(), vo.getListId());
				System.out.println("getByPrimaryKey(): " + vo2);
				List<SubscriptionVo>  lst2 = subscriptionDao.getByListId(vo.getListId());
				List<SubscriptionVo>  lst3 = subscriptionDao.getByAddrId(vo.getEmailAddrId());
				System.out.println("getByListId(): " + lst2.size() + ", getByAddrId(): " + lst3.size());
			}
		}
		return list;
	}
	
	private List<SubscriptionVo> selectByListId2() {
		List<SubscriptionVo> list = subscriptionDao.getSubscribersWithCustomerRecord(listId);
		for (SubscriptionVo vo : list) {
			System.out.println("getSubscribersWithCustomerRecord(): " + LF +vo);
		}
		return list;
	}
	
	private List<SubscriptionVo> selectByListId3() {
		List<SubscriptionVo> list = subscriptionDao.getSubscribersWithoutCustomerRecord(listId);
		for (SubscriptionVo vo : list) {
			System.out.println("getSubscribersWithoutCustomerRecord(): " + LF + vo);
		}
		return list;
	}
}
