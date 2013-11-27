package com.es.dao;

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

import com.es.dao.rule.RuleElementDao;
import com.es.vo.rule.RuleElementVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class RuleElementTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private RuleElementDao ruleElementDao;
	final String testRuleName = "Executable_Attachment";

	@BeforeClass
	public static void RuleElementPrepare() {
	}
	
	@Test
	public void testRuleElement() {
		try {
			List<RuleElementVo> list = selectByRuleName(testRuleName);
			assertTrue(list.size()>0);
			RuleElementVo vo0 = list.get(list.size() - 1);
			RuleElementVo vo = selectByPrimaryKey(vo0.getRuleName(), vo0.getElementSeq());
			assertNotNull(vo);
			RuleElementVo vo2 = insert(vo.getRuleName());
			assertNotNull(vo2);
			vo.setElementSeq(vo2.getElementSeq());
			vo.setRowId(vo2.getRowId());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2.getRuleName(), vo2.getElementSeq());
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private List<RuleElementVo> selectByRuleName(String ruleName) {
		List<RuleElementVo> list = ruleElementDao.getByRuleName(ruleName);
		for (RuleElementVo vo : list) {
			System.out.println("RuleElementDao - selectByRuleName: " + LF + vo);
		}
		return list;
	}

	private RuleElementVo selectByPrimaryKey(String ruleName, int seq) {
		RuleElementVo vo = (RuleElementVo) ruleElementDao.getByPrimaryKey(ruleName, seq);
		System.out.println("RuleElementDao - selectByPrimaryKey: " + LF + vo);
		return vo;
	}

	private int update(RuleElementVo ruleElementVo) {
		ruleElementVo.setDataName("Subject");
		int rows = ruleElementDao.update(ruleElementVo);
		System.out.println("RuleElementDao - update: rows updated " + rows);
		return rows;
	}

	private RuleElementVo insert(String ruleName) {
		List<RuleElementVo> list = ruleElementDao.getByRuleName(ruleName);
		if (list.size() > 0) {
			RuleElementVo ruleElementVo = list.get(list.size() - 1);
			ruleElementVo.setElementSeq(ruleElementVo.getElementSeq() + 1);
			int rows = ruleElementDao.insert(ruleElementVo);
			System.out.println("RuleElementDao - insert: rows inserted " + rows);
			return selectByPrimaryKey(ruleElementVo.getRuleName(), ruleElementVo.getElementSeq());
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}

	private int deleteByPrimaryKey(String ruleName, int seq) {
		int rowsDeleted = ruleElementDao.deleteByPrimaryKey(ruleName, seq);
		System.out.println("RuleElementDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
