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
	private RuleActionDao msgActionDao;
	final String testRuleName = RuleNameEnum.HARD_BOUNCE.getValue();
	
	@BeforeClass
	public static void MsgActionPrepare() throws Exception {
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
		RuleActionVo msgActionVo = insert();
		assertNotNull(msgActionVo);
		assertEquals(RuleNameEnum.CC_USER.getValue(), msgActionVo.getRuleName());
		RuleActionVo msgActionVo2 = select(msgActionVo);
		assertNotNull(msgActionVo2);
		assertTrue(msgActionVo.equalsTo(msgActionVo2));
		int rowsUpdated = update(msgActionVo);
		assertEquals(1, rowsUpdated);
		int rowsDeleted = delete(msgActionVo);
		assertEquals(1, rowsDeleted);
	}
	
	private List<RuleActionVo> selectByRuleName(String ruleName) {
		List<RuleActionVo> actions = msgActionDao.getByRuleName(ruleName);
		for (Iterator<RuleActionVo> it=actions.iterator(); it.hasNext();) {
			RuleActionVo msgActionVo = it.next();
			System.out.println("MsgActionDao - selectByRuleName: "+LF+msgActionVo);
		}
		return actions;
	}
	
	private RuleActionVo selectByUniqueKey(String ruleName, int actionSeq, String senderId) {
		RuleActionVo msgActionVo = msgActionDao.getMostCurrent(ruleName, actionSeq, senderId);
		System.out.println("MsgActionDao - selectByUniqueKey: "+LF+msgActionVo);
		return msgActionVo;
	}
	
	private List<RuleActionVo> selectByBestMatch() {
		List<RuleActionVo> list = msgActionDao.getByBestMatch(RuleNameEnum.GENERIC.getValue(), null, "JBatchCorp");
		for (int i=0; i<list.size(); i++) {
			RuleActionVo msgActionVo = list.get(i);
			System.out.println("MsgActionDao - selectByBestMatch: "+LF+msgActionVo);
		}
		return list;
	}

	private RuleActionVo select(RuleActionVo msgActionVo) {
		RuleActionVo msgActionVo2 = msgActionDao.getByUniqueKey(msgActionVo.getRuleName(),
				msgActionVo.getActionSeq(), msgActionVo.getStartTime(), msgActionVo.getSenderId());
		System.out.println("MsgActionDao - select: "+msgActionVo2);
		return msgActionVo2;
	}
	
	private int update(RuleActionVo msgActionVo) {
		int rowsUpdated = 0;
		if (msgActionVo!=null) {
			if (!StatusId.ACTIVE.getValue().equals(msgActionVo.getStatusId())) {
				msgActionVo.setStatusId(StatusId.ACTIVE.getValue());
			}
			rowsUpdated = msgActionDao.update(msgActionVo);
			System.out.println("MsgActionDao - update: Rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int deleteByRuleName(String ruleName) {
		int rowsDeleted = msgActionDao.deleteByRuleName(ruleName);
		System.out.println("MsgActionDao - deleteByRuleName: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private int delete(RuleActionVo msgActionVo) {
		int rowsDeleted = msgActionDao.deleteByUniqueKey(msgActionVo.getRuleName(),
				msgActionVo.getActionSeq(), msgActionVo.getStartTime(), msgActionVo.getSenderId());
		System.out.println("MsgActionDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}

	private RuleActionVo insert() {
		List<RuleActionVo> list = msgActionDao.getByRuleName(RuleNameEnum.CC_USER.getValue());
		if (list.size()>0) {
			RuleActionVo msgActionVo = list.get(list.size()-1);
			msgActionVo.setActionSeq((msgActionVo.getActionSeq()+1));
			msgActionDao.insert(msgActionVo);
			System.out.println("MsgActionDao - insert: "+msgActionVo);
			return msgActionVo;
		}
		throw new IllegalStateException("Should not be here, programming error!");
	}
}
