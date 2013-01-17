package com.legacytojava.message.dao.inbox;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.vo.inbox.RfcFieldsVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class RfcFieldsTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private RfcFieldsDao rfcFieldsDao;
	long testMsgId = 2L;
	String testRfcType = "RFC822";
	
	@BeforeClass
	public static void RfcFieldsPrepare() {
	}
	
	@Test
	public void testRfcFields() {
		List<RfcFieldsVo> list = selectByMsgId(testMsgId);
		assertTrue(list.size()>0);
		RfcFieldsVo vo = selectByPrimaryKey(testMsgId, testRfcType);
		assertNotNull(vo);
		RfcFieldsVo vo2 = insert(vo.getMsgId());
		assertNotNull(vo2);
		vo.setRfcType(vo2.getRfcType());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = deleteByPrimaryKey(vo2.getMsgId(), vo2.getRfcType());
		assertEquals(rowsDeleted, 1);
	}
	
	private List<RfcFieldsVo> selectByMsgId(long msgId) {
		List<RfcFieldsVo> actions = rfcFieldsDao.getByMsgId(msgId);
		for (Iterator<RfcFieldsVo> it=actions.iterator(); it.hasNext();) {
			RfcFieldsVo rfcFieldsVo = it.next();
			System.out.println("RfcFieldsDao - selectByMsgId: "+LF+rfcFieldsVo);
		}
		return actions;
	}
	
	private RfcFieldsVo selectByPrimaryKey(long msgId, String rfcType) {
		RfcFieldsVo rfcFieldsVo = (RfcFieldsVo)rfcFieldsDao.getByPrimaryKey(msgId,"RFC822");
		System.out.println("RfcFieldsDao - selectByPrimaryKey: "+LF+rfcFieldsVo);
		return rfcFieldsVo;
	}
	
	private int update(RfcFieldsVo rfcFieldsVo) {
		rfcFieldsVo.setDlvrStatus(rfcFieldsVo.getDlvrStatus()+".");
		int rows = rfcFieldsDao.update(rfcFieldsVo);
		System.out.println("RfcFieldsDao - update: "+LF+rfcFieldsVo);
		return rows;
	}
	
	private int deleteByPrimaryKey(long msgId, String rfcType) {
		int rowsDeleted = rfcFieldsDao.deleteByPrimaryKey(msgId, rfcType);
		System.out.println("RfcFieldsDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private RfcFieldsVo insert(long msgId) {
		List<RfcFieldsVo> list = (List<RfcFieldsVo>)rfcFieldsDao.getByMsgId(msgId);
		if (list.size()>0) {
			RfcFieldsVo rfcFieldsVo = list.get(list.size()-1);
			rfcFieldsVo.setRfcType("DSNTEXT");
			int rows = rfcFieldsDao.insert(rfcFieldsVo);
			System.out.println("RfcFieldsDao - insert: rows inserted "+rows+LF+rfcFieldsVo);
			return rfcFieldsVo;
		}
		return null;
	}
}
