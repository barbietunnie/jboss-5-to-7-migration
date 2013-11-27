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

import com.es.dao.inbox.MsgClickCountDao;
import com.es.vo.inbox.MsgClickCountVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgClickCountTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgClickCountDao msgClickCountsDao;
	
	@BeforeClass
	public static void MsgClickCountsPrepare() {
	}
	
	@Test
	public void insertUpdate() {
		try {
			MsgClickCountVo vo = selectAll();
			assertNotNull(vo);
			MsgClickCountVo vo2 = selectByPrimaryKey(vo.getMsgId());
			assertNotNull(vo2);
			vo2.setComplaintCount(vo.getComplaintCount());
			vo2.setUnsubscribeCount(vo.getUnsubscribeCount());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertTrue(rows>0);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private MsgClickCountVo selectAll() throws Exception {
		List<MsgClickCountVo> counts  = msgClickCountsDao.getAll();
		for (Iterator<MsgClickCountVo> it=counts.iterator(); it.hasNext();) {
			MsgClickCountVo msgClickCountsVo = it.next();
			System.out.println("selectAll - : " + LF + msgClickCountsVo);
		}
		if (counts.size() > 0) {
			return counts.get(0);
		}
		throw new Exception("MSG_CLICK_COUNT Table is empty");
	}
	
	private MsgClickCountVo selectByPrimaryKey(long msgId) {
		MsgClickCountVo vo = (MsgClickCountVo)msgClickCountsDao.getByPrimaryKey(msgId);
		System.out.println("selectByPrimaryKey - "+LF+vo);
		return vo;
	}
	
	private int update(MsgClickCountVo msgClickCountsVo) {
		int rows = 0;
		msgClickCountsVo.setSentCount(msgClickCountsVo.getSentCount() + 1);
		rows += msgClickCountsDao.update(msgClickCountsVo);
		rows += msgClickCountsDao.updateOpenCount(msgClickCountsVo.getMsgId());
		rows += msgClickCountsDao.updateClickCount(msgClickCountsVo.getMsgId());
		rows += msgClickCountsDao.updateUnsubscribeCount(msgClickCountsVo.getMsgId(),1);
		rows += msgClickCountsDao.updateComplaintCount(msgClickCountsVo.getMsgId(),1);
		rows += msgClickCountsDao.updateClickCount(msgClickCountsVo.getMsgId());
		System.out.println("update: rows updated "+rows+LF+msgClickCountsVo);
		return rows;
	}

	void deleteByPrimaryKey(MsgClickCountVo vo) throws Exception {
		try {
			int rowsDeleted = msgClickCountsDao.deleteByPrimaryKey(vo.getMsgId());
			System.out.println("deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	MsgClickCountVo insert() throws Exception {
		List<MsgClickCountVo> list = msgClickCountsDao.getAll();
		if (list.size()>0) {
			MsgClickCountVo msgClickCountsVo = list.get(list.size()-1);
			msgClickCountsVo.setMsgId(msgClickCountsVo.getMsgId()+1);
			msgClickCountsDao.insert(msgClickCountsVo);
			System.out.println("insert: "+LF+msgClickCountsVo);
			return msgClickCountsVo;
		}
		throw new Exception("MSG_CLICK_COUNT Table is empty.");
	}
}
