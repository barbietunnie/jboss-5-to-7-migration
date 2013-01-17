package com.legacytojava.message.dao.action;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.vo.action.MsgActionDetailVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=false)
@Transactional
public class MsgActionDetailTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgActionDetailDao msgActionDetailDao;
	final String testActionId = "CLOSE";
	@BeforeClass
	public static void MsgActionDetailPrepare() {
	}
	@Test
	public void testMsgActionDetail() throws Exception {
		try {
			MsgActionDetailVo vo0 = selectByActionId("SUSPEND");
			assertNotNull(vo0);
			vo0 = selectByActionId("SAVE");
			assertNotNull(vo0);
			vo0 = selectByActionId(testActionId);
			assertNotNull(vo0);
			MsgActionDetailVo vo = selectByPrimaryKey(vo0.getRowId());
			assertNotNull(vo);
			MsgActionDetailVo vo2 = insert(vo.getActionId());
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setActionId(vo2.getActionId());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertEquals(rows, 1);
			rows = deleteByActionId(vo2);
			assertEquals(rows, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private MsgActionDetailVo selectByPrimaryKey(int rowId) {
		MsgActionDetailVo vo = msgActionDetailDao.getByPrimaryKey(rowId);
		if (vo!=null) {
			System.out.println("MsgActionDetailDao - selectByPrimaryKey: "+LF+vo);
		}
		return vo;
	}
	
	private MsgActionDetailVo selectByActionId(String actionId) {
		MsgActionDetailVo vo = msgActionDetailDao.getByActionId(actionId);
		if (vo!=null) {
			System.out.println("MsgActionDetailDao - selectByActionId: "+vo);
		}
		return vo;
	}
	
	private int update(MsgActionDetailVo msgActionDetailVo) {
		msgActionDetailVo.setDescription("Close the Email");
		int rows = msgActionDetailDao.update(msgActionDetailVo);
		System.out.println("MsgActionDetailDao - update: "+rows);
		return rows;
	}
	
	private MsgActionDetailVo insert(String actionId) {
		MsgActionDetailVo msgActionDetailVo = msgActionDetailDao.getByActionId(actionId);
		if (msgActionDetailVo!=null) {
			msgActionDetailVo.setActionId(msgActionDetailVo.getActionId()+"_v2");
			int rows = msgActionDetailDao.insert(msgActionDetailVo);
			System.out.println("MsgActionDetailDao - insert: rows inserted "+rows);
			return selectByActionId(msgActionDetailVo.getActionId());
		}
		return null;
	}
	
	private int deleteByActionId(MsgActionDetailVo vo) {
		int rows = msgActionDetailDao.deleteByActionId(vo.getActionId());
		System.out.println("MsgActionDetailDao - deleteByPrimaryKey: Rows Deleted: "+rows);
		return rows;
	}
}
