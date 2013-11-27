package com.es.dao;

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

import com.es.dao.rule.RuleLogicDao;
import com.es.data.constant.StatusId;
import com.es.vo.rule.RuleLogicVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
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
			RuleLogicVo vo = selectByPrimaryKey(testRuleName);
			assertTrue(vo!=null);
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
			fail();
		}
	}

	private RuleLogicVo selectByPrimaryKey(String ruleName) {
		RuleLogicVo vo = ruleLogicDao.getByRuleName(ruleName);
		if (vo != null) {
			System.out.println("RuleDao - selectByPrimaryKey: " + LF + vo);
		}
		return vo;
	}

	private List<RuleLogicVo> selectActiveRules() {
		List<RuleLogicVo> list = ruleLogicDao.getActiveRules();
		if (!list.isEmpty()) {
			System.out.println("RuleDao - selectActiveRules: " + list.size() + LF + list.get(0));
		}
		return list;
	}

	private List<RuleLogicVo> selectAllRules() {
		List<RuleLogicVo> listBuiltIn = ruleLogicDao.getAll(true);
		if (!listBuiltIn.isEmpty()) {
			System.out.println("RuleDao - selectAllRules - Builtin rules: " + listBuiltIn.size());
		}
		List<RuleLogicVo> listCustom = ruleLogicDao.getAll(false);
		if (!listCustom.isEmpty()) {
			System.out.println("RuleDao - selectAllRules - Custom rules: " + listCustom.size());
		}
		List<RuleLogicVo> listAll = new ArrayList<RuleLogicVo>();
		listAll.addAll(listBuiltIn);
		listAll.addAll(listCustom);
		return listAll;
	}

	private int update(RuleLogicVo ruleLogicVo) {
		if (StatusId.ACTIVE.getValue().equals(ruleLogicVo.getStatusId())) {
			ruleLogicVo.setStatusId(StatusId.ACTIVE.getValue());
		}
		int rows = ruleLogicDao.update(ruleLogicVo);
		System.out.println("RuleDao - update: rows updated " + rows);
		return rows;
	}

	private RuleLogicVo insert(RuleLogicVo ruleLogicVo) {
		ruleLogicVo.setRuleName(ruleLogicVo.getRuleName()+"_v2");
		int rows = ruleLogicDao.insert(ruleLogicVo);
		System.out.println("RuleDao - insert: rows inserted " + rows + LF + ruleLogicVo);
		return ruleLogicVo;
	}

	private int deleteByPrimaryKey(RuleLogicVo vo) {
		System.out.println("RuleDao - deleteByPrimaryKey: " + vo.getRuleName() + "/" + vo.getRuleSeq());
		int rowsDeleted = ruleLogicDao.deleteByPrimaryKey(vo.getRuleName(), vo.getRuleSeq());
		System.out.println("RuleDao - deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		return rowsDeleted;
	}
}
