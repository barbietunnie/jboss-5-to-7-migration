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

import com.legacytojava.message.vo.inbox.MsgHeadersVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MsgHeadersTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgHeadersDao msgHeadersDao;
	long testMsgId = 2L;

	@BeforeClass
	public static void MsgHeadersPrepare() {
	}
	
	@Test
	public void testMsgHeaders() throws Exception {
		try {
			List<MsgHeadersVo> lst1 = selectByMsgId(testMsgId);
			assertTrue(lst1.size()>0);
			MsgHeadersVo vo1 = lst1.get(0);
			MsgHeadersVo vo0 = selectByPrimaryKey(vo1.getMsgId(), vo1.getHeaderSeq());
			assertNotNull(vo0);
			assertTrue(vo1.equalsTo(vo0));
			MsgHeadersVo vo2 = insert(testMsgId);
			assertNotNull(vo2);
			vo1.setHeaderSeq(vo2.getHeaderSeq());
			assertTrue(vo1.equalsTo(vo2));
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
	
	private List<MsgHeadersVo> selectByMsgId(long msgId) {
		List<MsgHeadersVo> actions = msgHeadersDao.getByMsgId(msgId);
		for (Iterator<MsgHeadersVo> it=actions.iterator(); it.hasNext();) {
			MsgHeadersVo msgHeadersVo = it.next();
			System.out.println("MsgHeadersDao - selectByMsgId: "+LF+msgHeadersVo);
		}
		return actions;
	}
	
	private MsgHeadersVo selectByPrimaryKey(long msgId, int seq) {
		MsgHeadersVo msgHeadersVo = (MsgHeadersVo)msgHeadersDao.getByPrimaryKey(msgId,seq);
		System.out.println("MsgHeadersDao - selectByPrimaryKey: "+LF+msgHeadersVo);
		return msgHeadersVo;
	}
	
	private int update(MsgHeadersVo msgHeadersVo) {
		msgHeadersVo.setHeaderValue(msgHeadersVo.getHeaderValue()+".");
		int rows = msgHeadersDao.update(msgHeadersVo);
		System.out.println("MsgHeadersDao - update: rows updated "+rows);
		return rows;
	}
	
	private int deleteByPrimaryKey(MsgHeadersVo vo) {
		int rowsDeleted = msgHeadersDao.deleteByPrimaryKey(vo.getMsgId(),vo.getHeaderSeq());
		System.out.println("MsgHeadersDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgHeadersVo insert(long msgId) {
		List<MsgHeadersVo> list = (List<MsgHeadersVo>)msgHeadersDao.getByMsgId(msgId);
		if (list.size()>0) {
			MsgHeadersVo msgHeadersVo = list.get(list.size()-1);
			msgHeadersVo.setHeaderSeq(msgHeadersVo.getHeaderSeq()+1);
			int rows = msgHeadersDao.insert(msgHeadersVo);
			System.out.println("MsgHeadersDao - insert: rows inserted "+rows);
			return selectByPrimaryKey(msgHeadersVo.getMsgId(), msgHeadersVo.getHeaderSeq());
		}
		return null;
	}
}
