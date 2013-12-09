package com.es.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.subscriber.SubscriberDao;
import com.es.data.constant.Constants;
import com.es.vo.comm.SubscriberVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class SubscriberTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private SubscriberDao subscriberDao;
	final String defaultSubrId = "test";

	@BeforeClass
	public static void CustomerPrepare() throws Exception {
	}

	@Test
	public void insertSelectDelete() {
		try {
			List<SubscriberVo> list = selectBySenderId(Constants.DEFAULT_SENDER_ID);
			assertTrue(list.size()>0);
			SubscriberVo vo0 = selectByEmailAddrId(1L);
			assertNotNull(vo0);
			SubscriberVo vo = insert();
			assertNotNull(vo);
			SubscriberVo vo2 = selectBySubrId(vo);
			assertNotNull(vo2);
			// sync-up next 3 fields since differences are expected
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setPrimaryKey(vo2.getPrimaryKey());
			// end of sync-up
			assertTrue(vo.equalsTo(vo2));
			vo2.setOrigUpdtTime(vo2.getUpdtTime());
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			SubscriberVo vo = new SubscriberVo();
			vo.setSubrId(defaultSubrId+"_1");
			delete(vo);
			fail();
		}
	}

	private SubscriberVo selectBySubrId(SubscriberVo vo) {
		SubscriberVo subr = subscriberDao.getBySubscriberId(vo.getSubrId());
		if (subr!=null) {
			System.out.println("SubscriberDao - selectBySubrId: "+LF+subr);
		}
		return subr;
	}
	
	private SubscriberVo selectByEmailAddrId(long emailId) {
		SubscriberVo vo = subscriberDao.getByEmailAddrId(emailId);
		if (vo != null) {
			System.out.println("SubscriberDao - selectEmailAddrId: "+LF+vo);
		}
		return vo;
	}
	
	private List<SubscriberVo> selectBySenderId(String senderId) {
		List<SubscriberVo> list = (List<SubscriberVo>)subscriberDao.getBySenderId(senderId);
		for (int i=0; i<list.size(); i++) {
			SubscriberVo subr = list.get(i);
			System.out.println("SubscriberDao - selectSenderId: "+LF+subr);
		}
		return list;
	}
	
	private int update(SubscriberVo vo) {
		SubscriberVo subr = vo; //subscriberDao.getBySubscriberId(vo.getSubrId());
		int rows = 0;
		if (subr!=null) {
			subr.setStatusId("A");
			rows = subscriberDao.update(subr);
			System.out.println("SubscriberDao - update: rows updated: "+ rows);
		}
		return rows;
	}
	
	private SubscriberVo insert() {
		SubscriberVo subr = subscriberDao.getBySubscriberId(defaultSubrId);
		if (subr!=null) {
			subr.setSubrId(subr.getSubrId()+"_1");
			subr.setEmailAddr("test."+subr.getEmailAddr());
			subr.setBirthDate(new java.util.Date());
			subscriberDao.insert(subr);
			System.out.println("SubscriberDao - insert: "+subr);
			return subr;
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}
	
	private int delete(SubscriberVo customerVo) {
		int rowsDeleted = subscriberDao.delete(customerVo.getSubrId());
		System.out.println("SubscriberDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
}
