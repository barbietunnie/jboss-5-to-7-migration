package com.es.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.action.RuleActionDao;
import com.es.data.constant.StatusId;
import com.es.data.preload.RuleNameEnum;
import com.es.vo.action.RuleActionVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class RuleActionTest {
	final static String LF = System.getProperty("line.separator","\n");
	// this instance will be dependency injected by name
	@Resource
	private RuleActionDao ruleActionDao;
	final String testRuleName = RuleNameEnum.HARD_BOUNCE.getValue();
	
	@BeforeClass
	public static void RuleActionPrepare() throws Exception {
	}

	@Test
	public void testSelects() {
		List<RuleActionVo> list = selectByRuleName(testRuleName);
		assertTrue(list.size()>0);
		list = selectByBestMatch();
		assertTrue(list.size()>0);
		RuleActionVo vo = selectByUniqueKey(testRuleName, 1, null);
		assertNotNull(vo);
		int rowsDeleted = deleteByRuleName("test");
		assertEquals(rowsDeleted, 0);
	}

	@Test
	@Rollback(true)
	public void insertSelectDelete() {
		RuleActionVo ruleActionVo = insert();
		assertNotNull(ruleActionVo);
		assertEquals(RuleNameEnum.CC_USER.getValue(), ruleActionVo.getRuleName());
		RuleActionVo ruleActionVo2 = select(ruleActionVo);
		assertNotNull(ruleActionVo2);
		assertTrue(ruleActionVo.equalsTo(ruleActionVo2));
		int rowsUpdated = update(ruleActionVo);
		assertEquals(1, rowsUpdated);
		int rowsDeleted = delete(ruleActionVo);
		assertEquals(1, rowsDeleted);
	}
	
	private List<RuleActionVo> selectByRuleName(String ruleName) {
		List<RuleActionVo> actions = ruleActionDao.getByRuleName(ruleName);
		for (Iterator<RuleActionVo> it=actions.iterator(); it.hasNext();) {
			RuleActionVo ruleActionVo = it.next();
			System.out.println("RuleActionDao - selectByRuleName: "+LF+ruleActionVo);
		}
		return actions;
	}
	
	private RuleActionVo selectByUniqueKey(String ruleName, int actionSeq, String senderId) {
		RuleActionVo ruleActionVo = ruleActionDao.getMostCurrent(ruleName, actionSeq, senderId);
		System.out.println("RuleActionDao - selectByUniqueKey: "+LF+ruleActionVo);
		return ruleActionVo;
	}
	
	private List<RuleActionVo> selectByBestMatch() {
		List<RuleActionVo> list = ruleActionDao.getByBestMatch(RuleNameEnum.GENERIC.getValue(), null, "JBatchCorp");
		for (int i=0; i<list.size(); i++) {
			RuleActionVo ruleActionVo = list.get(i);
			System.out.println("RuleActionDao - selectByBestMatch: "+LF+ruleActionVo);
		}
		return list;
	}

	private RuleActionVo select(RuleActionVo ruleActionVo) {
		RuleActionVo ruleActionVo2 = ruleActionDao.getByUniqueKey(ruleActionVo.getRuleName(),
				ruleActionVo.getActionSeq(), ruleActionVo.getStartTime(), ruleActionVo.getSenderId());
		System.out.println("RuleActionDao - select: "+ruleActionVo2);
		return ruleActionVo2;
	}
	
	private int update(RuleActionVo ruleActionVo) {
		int rowsUpdated = 0;
		if (ruleActionVo!=null) {
			if (!StatusId.ACTIVE.getValue().equals(ruleActionVo.getStatusId())) {
				ruleActionVo.setStatusId(StatusId.ACTIVE.getValue());
			}
			rowsUpdated = ruleActionDao.update(ruleActionVo);
			System.out.println("RuleActionDao - update: Rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int deleteByRuleName(String ruleName) {
		int rowsDeleted = ruleActionDao.deleteByRuleName(ruleName);
		System.out.println("RuleActionDao - deleteByRuleName: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private int delete(RuleActionVo ruleActionVo) {
		int rowsDeleted = ruleActionDao.deleteByUniqueKey(ruleActionVo.getRuleName(),
				ruleActionVo.getActionSeq(), ruleActionVo.getStartTime(), ruleActionVo.getSenderId());
		System.out.println("RuleActionDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private RuleActionVo insert() {
		List<RuleActionVo> list = ruleActionDao.getByRuleName(RuleNameEnum.CC_USER.getValue());
		if (list.size()>0) {
			RuleActionVo ruleActionVo = list.get(list.size()-1);
			ruleActionVo.setActionSeq((ruleActionVo.getActionSeq()+1));
			ruleActionDao.insert(ruleActionVo);
			System.out.println("RuleActionDao - insert: "+ruleActionVo);
			return ruleActionVo;
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}
}
