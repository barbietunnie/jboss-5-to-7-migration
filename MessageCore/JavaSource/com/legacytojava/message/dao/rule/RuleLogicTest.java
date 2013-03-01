package com.legacytojava.message.dao.rule;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.vo.rule.RuleLogicVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class RuleLogicTest {
	final static String LF = System.getProperty("line.separator", "\n");
	@Resource
	private RuleLogicDao ruleLogicDao;
	final String testRuleName = "Executable_Attachment";

	@BeforeClass
	public static void RuleLogicPrepare() {
	}
	
	@Test
	public void testRuleLogic() throws Exception {
		try {
			List<RuleLogicVo> listActive = selectActiveRules();
			assertTrue(listActive.size()>0);
			List<RuleLogicVo> listAll = selectAllRules();
			assertTrue(listAll.size()>0);
			List<RuleLogicVo> list = selectByPrimaryKey(testRuleName);
			assertTrue(list.size()>0);
			RuleLogicVo vo = list.get(0);
			RuleLogicVo vo2 = insert(vo);
			assertNotNull(vo2);
			vo.setRowId(vo2.getRowId());
			vo.setRuleName(vo2.getRuleName());
			vo.setOrigRuleName(vo2.getOrigRuleName());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private List<RuleLogicVo> selectByPrimaryKey(String ruleName) {
		List<RuleLogicVo> list = ruleLogicDao.getByRuleName(ruleName);
		if (list.size() > 0) {
			RuleLogicVo ruleLogicVo = list.get(0);
			System.out.println("RuleLogicDao - selectByPrimaryKey: " + LF + ruleLogicVo);
		}
		return list;
	}

	private List<RuleLogicVo> selectActiveRules() {
		List<RuleLogicVo> list = ruleLogicDao.getActiveRules();
		if (!list.isEmpty()) {
			System.out.println("RuleLogicDao - selectActiveRules: " + list.size() + LF + list.get(0));
		}
		return list;
	}

	private List<RuleLogicVo> selectAllRules() {
		List<RuleLogicVo> listBuiltIn = ruleLogicDao.getAll(true);
		if (!listBuiltIn.isEmpty()) {
			System.out.println("RuleLogicDao - selectAllRules - Builtin rules: " + listBuiltIn.size());
		}
		List<RuleLogicVo> listCustom = ruleLogicDao.getAll(false);
		if (!listCustom.isEmpty()) {
			System.out.println("RuleLogicDao - selectAllRules - Custom rules: " + listCustom.size());
		}
		List<RuleLogicVo> listAll = new ArrayList<RuleLogicVo>();
		listAll.addAll(listBuiltIn);
		listAll.addAll(listCustom);
		return listAll;
	}

	private int update(RuleLogicVo ruleLogicVo) {
		if (StatusIdCode.ACTIVE.equals(ruleLogicVo.getStatusId())) {
			ruleLogicVo.setStatusId(StatusIdCode.ACTIVE);
		}
		int rows = ruleLogicDao.update(ruleLogicVo);
		System.out.println("RuleLogicDao - update: rows updated " + rows);
		return rows;
	}

	private RuleLogicVo insert(RuleLogicVo ruleLogicVo) {
		ruleLogicVo.setRuleName(ruleLogicVo.getRuleName()+"_v2");
		int rows = ruleLogicDao.insert(ruleLogicVo);
		System.out.println("RuleLogicDao - insert: rows inserted " + rows + LF + ruleLogicVo);
		return ruleLogicVo;
	}

	private int deleteByPrimaryKey(RuleLogicVo vo) {
		System.out.println("RuleLogicDao - deleteByPrimaryKey: " + vo.getRuleName() + "/" + vo.getRuleSeq());
		int rowsDeleted = ruleLogicDao.deleteByPrimaryKey(vo.getRuleName(), vo.getRuleSeq());
		System.out.println("RuleLogicDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
