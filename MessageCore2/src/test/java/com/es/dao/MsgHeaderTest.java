package com.es.dao;

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

import com.es.dao.inbox.MsgHeaderDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.vo.inbox.MsgHeaderVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgHeaderTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgHeaderDao msgHeaderDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	long testMsgId = 2L;

	@BeforeClass
	public static void MsgHeadersPrepare() {
	}
	
	@Test
	public void testMsgHeaders() {
		try {
			List<MsgHeaderVo> lst1 = selectByMsgId(testMsgId);
			if (lst1.isEmpty()) {
				testMsgId = msgInboxDao.getLastRecord().getMsgId();
				lst1 = selectByMsgId(testMsgId);
			}
			assertTrue(lst1.size()>0);
			MsgHeaderVo vo1 = lst1.get(lst1.size()-1);
			MsgHeaderVo vo0 = selectByPrimaryKey(vo1.getMsgId(), vo1.getHeaderSeq());
			assertNotNull(vo0);
			assertTrue(vo1.equalsTo(vo0));
			MsgHeaderVo vo2 = insert(testMsgId);
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
			fail();
		}
	}
	
	private List<MsgHeaderVo> selectByMsgId(long msgId) {
		List<MsgHeaderVo> actions = msgHeaderDao.getByMsgId(msgId);
		for (Iterator<MsgHeaderVo> it=actions.iterator(); it.hasNext();) {
			MsgHeaderVo msgHeadersVo = it.next();
			System.out.println("MsgHeaderDao - selectByMsgId: "+LF+msgHeadersVo);
		}
		return actions;
	}
	
	private MsgHeaderVo selectByPrimaryKey(long msgId, int seq) {
		MsgHeaderVo msgHeadersVo = (MsgHeaderVo)msgHeaderDao.getByPrimaryKey(msgId,seq);
		System.out.println("MsgHeaderDao - selectByPrimaryKey: "+LF+msgHeadersVo);
		return msgHeadersVo;
	}
	
	private int update(MsgHeaderVo msgHeadersVo) {
		msgHeadersVo.setHeaderValue(msgHeadersVo.getHeaderValue()+".");
		int rows = msgHeaderDao.update(msgHeadersVo);
		System.out.println("MsgHeaderDao - update: rows updated "+rows);
		return rows;
	}
	
	private int deleteByPrimaryKey(MsgHeaderVo vo) {
		int rowsDeleted = msgHeaderDao.deleteByPrimaryKey(vo.getMsgId(),vo.getHeaderSeq());
		System.out.println("MsgHeaderDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgHeaderVo insert(long msgId) {
		List<MsgHeaderVo> list = (List<MsgHeaderVo>)msgHeaderDao.getByMsgId(msgId);
		MsgHeaderVo msgHeadersVo = null;
		if (list.size()>0) {
			msgHeadersVo = list.get(list.size()-1);
			msgHeadersVo.setHeaderSeq(msgHeadersVo.getHeaderSeq()+1);
		}
		else {
			msgHeadersVo = new MsgHeaderVo();
			msgHeadersVo.setMsgId(msgId);
			msgHeadersVo.setHeaderSeq(0);
			msgHeadersVo.setHeaderName("TestHeader");
			msgHeadersVo.setHeaderValue("Test Header Value");
		}
		int rows = msgHeaderDao.insert(msgHeadersVo);
		System.out.println("MsgHeaderDao - insert: rows inserted "+rows);
		return selectByPrimaryKey(msgHeadersVo.getMsgId(), msgHeadersVo.getHeaderSeq());
	}
}
