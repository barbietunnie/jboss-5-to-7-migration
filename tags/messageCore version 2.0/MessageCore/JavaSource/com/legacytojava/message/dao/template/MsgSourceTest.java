package com.legacytojava.message.dao.template;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.vo.template.MsgSourceVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MsgSourceTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private MsgSourceDao msgSourceDao;
	Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
	final String testMsgSourceId = "WeekendDeals";

	@BeforeClass
	public static void MsgSourcePrepare() {
	}
	
	@Test
	public void testMsgCource() throws Exception {
		try {
			MsgSourceVo vo = selectByPrimaryKey(testMsgSourceId);
			assertNotNull(vo);
			List<MsgSourceVo> list = selectByFromAddrId(vo.getFromAddrId());
			assertTrue(list.size()>0);
			vo = selectByPrimaryKey(testMsgSourceId);
			MsgSourceVo vo2 = insert(testMsgSourceId);
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setMsgSourceId(vo2.getMsgSourceId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated,1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted,1);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private MsgSourceVo selectByPrimaryKey(String msgSourceId) {
		MsgSourceVo msgSourceVo = msgSourceDao.getByPrimaryKey(msgSourceId);
		if (msgSourceVo!=null) {
			System.out.println("MsgSourceDao - selectByPrimaryKey: " + LF + msgSourceVo);
		}
		return msgSourceVo;
	}

	private List<MsgSourceVo> selectByFromAddrId(long addrId) {
		List<MsgSourceVo> list = msgSourceDao.getByFromAddrId(addrId);
		for (MsgSourceVo vo : list) {
			System.out.println("MsgSourceDao - selectByFromAddrId: " + LF + vo);
		}
		return list;
	}

	private int update(MsgSourceVo msgSourceVo) {
		msgSourceVo.setDescription("message source for WeekendDeals");
		int rows = msgSourceDao.update(msgSourceVo);
		System.out.println("MsgSourceDao - update: rows updated " + rows);
		return rows;
	}

	private MsgSourceVo insert(String msgSourceId) {
		MsgSourceVo vo = msgSourceDao.getByPrimaryKey(msgSourceId);;
		if (vo!=null) {
			vo.setMsgSourceId(vo.getMsgSourceId()+"_v2");
			int rows = msgSourceDao.insert(vo);
			System.out.println("MsgSourceDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(vo.getMsgSourceId());
		}
		return null;
	}

	private int deleteByPrimaryKey(MsgSourceVo vo) {
		int rowsDeleted = msgSourceDao.deleteByPrimaryKey(vo.getMsgSourceId());
		System.out.println("MsgSourceDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
