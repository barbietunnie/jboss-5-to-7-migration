package com.es.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
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

import com.es.dao.inbox.MsgActionLogDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.vo.inbox.MsgActionLogVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgActionLogTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgActionLogDao actionLogDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	long testMsgId = 2L;

	@BeforeClass
	public static void MsgActionLogsPrepare() {
	}
	
	@Test
	public void testMsgActionLogs() {
		try {
			List<MsgActionLogVo> lst1 = selectByMsgId(testMsgId);
			if (lst1.isEmpty()) {
				testMsgId = msgInboxDao.getLastRecord().getMsgId();
				lst1 = selectByMsgId(testMsgId);
				if (lst1.isEmpty()) {
					insert(testMsgId);
					lst1 = selectByMsgId(testMsgId);
				}
			}
			assertTrue(lst1.size()>0);
			MsgActionLogVo vo1 = lst1.get(lst1.size()-1);
			MsgActionLogVo vo0 = selectByPrimaryKey(vo1.getMsgId(), vo1.getActionSeq(), vo1.getAddTime());
			assertNotNull(vo0);
			assertTrue(vo1.equalsTo(vo0));
			MsgActionLogVo vo2 = insert(testMsgId);
			assertNotNull(vo2);
			vo1.setActionSeq(vo2.getActionSeq());
			vo1.setAddTime(vo2.getAddTime());
			assertTrue(vo1.equalsTo(vo2));
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
	
	private List<MsgActionLogVo> selectByMsgId(long msgId) {
		List<MsgActionLogVo> actions = actionLogDao.getByMsgId(msgId);
		for (Iterator<MsgActionLogVo> it=actions.iterator(); it.hasNext();) {
			MsgActionLogVo msgHeadersVo = it.next();
			System.out.println("MsgActionLogDao - selectByMsgId: "+LF+msgHeadersVo);
		}
		return actions;
	}
	
	private MsgActionLogVo selectByPrimaryKey(long msgId, int seq, Timestamp addTime) {
		MsgActionLogVo msgHeadersVo = actionLogDao.getByPrimaryKey(msgId,seq,addTime);
		System.out.println("MsgActionLogDao - selectByPrimaryKey: "+LF+msgHeadersVo);
		return msgHeadersVo;
	}
	
	private int update(MsgActionLogVo msgHeadersVo) {
		msgHeadersVo.setActionBo(msgHeadersVo.getActionBo()+"_v2");
		int rows = actionLogDao.update(msgHeadersVo);
		System.out.println("MsgActionLogDao - update: rows updated "+rows);
		return rows;
	}
	
	private int deleteByPrimaryKey(MsgActionLogVo vo) {
		int rowsDeleted = actionLogDao.deleteByPrimaryKey(vo.getMsgId(),vo.getActionSeq(), vo.getAddTime());
		System.out.println("MsgActionLogDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgActionLogVo insert(long msgId) {
		List<MsgActionLogVo> list = (List<MsgActionLogVo>)actionLogDao.getByMsgId(msgId);
		MsgActionLogVo msgHeadersVo = null;
		if (list.size()>0) {
			msgHeadersVo = list.get(list.size()-1);
			msgHeadersVo.setActionSeq(msgHeadersVo.getActionSeq()+1);
		}
		else {
			msgHeadersVo = new MsgActionLogVo();
			msgHeadersVo.setMsgId(msgId);
			msgHeadersVo.setActionBo("TestAction");
			msgHeadersVo.setParameters("Test Parameters");
		}
		int rows = actionLogDao.insert(msgHeadersVo);
		System.out.println("MsgActionLogDao - insert: rows inserted "+rows);
		return selectByPrimaryKey(msgHeadersVo.getMsgId(), msgHeadersVo.getActionSeq(), msgHeadersVo.getAddTime());
	}
}
