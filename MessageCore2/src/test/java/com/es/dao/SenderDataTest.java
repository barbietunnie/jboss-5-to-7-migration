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
import com.es.dao.sender.SenderDataDao;
import com.es.data.constant.Constants;
import com.es.vo.comm.SenderDataVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class SenderDataTest {
	@Resource
	private SenderDataDao senderDao;
	final String DefaultSenderId = Constants.DEFAULT_SENDER_ID;

	@BeforeClass
	public static void SenderPrepare() throws Exception {
	}

	@Test
	@Rollback(true)
	public void insertSelectDelete() {
		try {
			SenderDataVo vo = insert();
			assertNotNull(vo);
			assertTrue(vo.getSenderId().endsWith("_v2"));
			assertTrue(vo.getDomainName().endsWith(".v2"));
			SenderDataVo vo2 = select(vo);
			assertNotNull(vo2);
			// sync-up next four fields since differences are expected
			vo.setSystemId(vo2.getSystemId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setPrimaryKey(vo2.getPrimaryKey());
			// end of sync-up
			assertTrue(vo.equalsTo(vo2));
			vo2.setOrigUpdtTime(vo2.getUpdtTime()); // triggers optimistic locking
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			delete(vo);
		}
		catch (Exception e) {
			SenderDataVo v = new SenderDataVo();
			v.setSenderId(DefaultSenderId + "_v2");
			delete(v);
			fail();
		}
	}

	private SenderDataVo select(SenderDataVo vo) {
		SenderDataVo vo2 = senderDao.getBySenderId(vo.getSenderId());
		if (vo2 != null) {
			System.out.println("SenderDataDao - select: "+vo2);
		}
		return vo2;
	}
	
	private int update(SenderDataVo vo) {
		SenderDataVo senderVo = vo; //senderDao.getBySenderId(vo.getSenderId());
		int rows = 0;
		if (senderVo!=null) {
			senderVo.setStatusId("A");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, -31);
			String systemId = TimestampUtil.db2ToDecimalString(TimestampUtil.timestampToDb2(new java.sql.Timestamp(cal.getTimeInMillis())));
			System.out.println("SystemId: " + systemId);
			senderVo.setSystemId(systemId);
			rows = senderDao.update(senderVo);
			System.out.println("SenderDataDao - update: rows updated: "+rows);
		}
		return rows;
	}
	
	private int delete(SenderDataVo vo) {
		int rowsDeleted = senderDao.delete(vo.getSenderId());
		System.out.println("SenderDataDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private SenderDataVo insert() {
		SenderDataVo senderVo = senderDao.getBySenderId(DefaultSenderId);
		if (senderVo!=null) {
			senderVo.setSenderId(senderVo.getSenderId()+"_v2");
			senderVo.setDomainName(senderVo.getDomainName()+".v2");
			senderDao.insert(senderVo);
			System.out.println("SenderDataDao - insert: "+senderVo);
			return senderVo;
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}
}
