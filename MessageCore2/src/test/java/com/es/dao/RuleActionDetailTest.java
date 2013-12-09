package com.es.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.action.RuleActionDetailDao;
import com.es.vo.action.RuleActionDetailVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class RuleActionDetailTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private RuleActionDetailDao ruleActionDetailDao;
	final String testActionId = "CLOSE";
	@BeforeClass
	public static void MsgActionDetailPrepare() {
	}
	@Test
	public void testRuleActionDetail() throws Exception {
		try {
			RuleActionDetailVo vo0 = selectByActionId("SUSPEND");
			assertNotNull(vo0);
			vo0 = selectByActionId("SAVE");
			assertNotNull(vo0);
			vo0 = selectByActionId(testActionId);
			assertNotNull(vo0);
			RuleActionDetailVo vo = selectByPrimaryKey(vo0.getRowId());
			assertNotNull(vo);
			RuleActionDetailVo vo2 = insert(vo.getActionId());
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
			fail();
		}
	}
	
	private RuleActionDetailVo selectByPrimaryKey(int rowId) {
		RuleActionDetailVo vo = ruleActionDetailDao.getByPrimaryKey(rowId);
		if (vo!=null) {
			System.out.println("MsgActionDetailDao - selectByPrimaryKey: "+LF+vo);
		}
		return vo;
	}
	
	private RuleActionDetailVo selectByActionId(String actionId) {
		RuleActionDetailVo vo = ruleActionDetailDao.getByActionId(actionId);
		if (vo!=null) {
			System.out.println("MsgActionDetailDao - selectByActionId: "+vo);
		}
		return vo;
	}
	
	private int update(RuleActionDetailVo ruleActionDetailVo) {
		ruleActionDetailVo.setDescription("Close the Email");
		int rows = ruleActionDetailDao.update(ruleActionDetailVo);
		System.out.println("MsgActionDetailDao - update: "+rows);
		return rows;
	}
	
	private RuleActionDetailVo insert(String actionId) {
		RuleActionDetailVo msgActionDetailVo = ruleActionDetailDao.getByActionId(actionId);
		if (msgActionDetailVo!=null) {
			msgActionDetailVo.setActionId(msgActionDetailVo.getActionId()+"_v2");
			int rows = ruleActionDetailDao.insert(msgActionDetailVo);
			System.out.println("MsgActionDetailDao - insert: rows inserted "+rows);
			return selectByActionId(msgActionDetailVo.getActionId());
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}
	
	private int deleteByActionId(RuleActionDetailVo vo) {
		int rows = ruleActionDetailDao.deleteByActionId(vo.getActionId());
		System.out.println("MsgActionDetailDao - deleteByPrimaryKey: Rows Deleted: "+rows);
		return rows;
	}
}
