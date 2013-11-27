package com.es.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.core.util.TimestampUtil;
import com.es.dao.sender.SenderDao;
import com.es.data.constant.Constants;
import com.es.vo.comm.SenderVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class SenderTest {
	@Resource
	private SenderDao senderDao;
	final String DefaultSenderId = Constants.DEFAULT_SENDER_ID;

	@BeforeClass
	public static void SenderPrepare() throws Exception {
	}

	@Test
	@Rollback(true)
	public void insertSelectDelete() {
		try {
			SenderVo vo = insert();
			assertNotNull(vo);
			assertTrue(vo.getSenderId().endsWith("_v2"));
			assertTrue(vo.getDomainName().endsWith(".v2"));
			SenderVo vo2 = select(vo);
			assertNotNull(vo2);
			// sync-up next four fields since differences are expected
			vo.setSystemId(vo2.getSystemId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setPrimaryKey(vo2.getPrimaryKey());
			// end of sync-up
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			delete(vo);
		}
		catch (Exception e) {
			SenderVo v = new SenderVo();
			v.setSenderId(DefaultSenderId + "_v2");
			delete(v);
			fail();
		}
	}

	private SenderVo select(SenderVo vo) {
		SenderVo vo2 = senderDao.getBySenderId(vo.getSenderId());
		if (vo2 != null) {
			System.out.println("SenderDao - select: "+vo2);
		}
		return vo2;
	}
	
	private int update(SenderVo vo) {
		SenderVo senderVo = senderDao.getBySenderId(vo.getSenderId());
		int rows = 0;
		if (senderVo!=null) {
			senderVo.setStatusId("A");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -31);
			String systemId = TimestampUtil.db2ToDecimalString(TimestampUtil.timestampToDb2(new java.sql.Timestamp(cal.getTimeInMillis())));
			System.out.println("SystemId: " + systemId);
			senderVo.setSystemId(systemId);
			rows = senderDao.update(senderVo);
			System.out.println("SenderDao - update: rows updated: "+rows);
		}
		return rows;
	}
	
	private int delete(SenderVo vo) {
		int rowsDeleted = senderDao.delete(vo.getSenderId());
		System.out.println("SenderDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private SenderVo insert() {
		SenderVo senderVo = senderDao.getBySenderId(DefaultSenderId);
		if (senderVo!=null) {
			senderVo.setSenderId(senderVo.getSenderId()+"_v2");
			senderVo.setDomainName(senderVo.getDomainName()+".v2");
			senderDao.insert(senderVo);
			System.out.println("SenderDao - insert: "+senderVo);
			return senderVo;
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}
}
