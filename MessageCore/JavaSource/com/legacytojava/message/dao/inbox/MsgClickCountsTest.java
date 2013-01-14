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

import com.legacytojava.message.vo.inbox.MsgClickCountsVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MsgClickCountsTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgClickCountsDao msgClickCountsDao;
	
	@BeforeClass
	public static void MsgClickCountsPrepare() {
	}
	
	@Test
	public void insertUpdate() {
		try {
			MsgClickCountsVo vo = selectAll();
			assertNotNull(vo);
			MsgClickCountsVo vo2 = selectByPrimaryKey(vo.getMsgId());
			assertNotNull(vo2);
			vo2.setComplaintCount(vo.getComplaintCount());
			vo2.setUnsubscribeCount(vo.getUnsubscribeCount());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertTrue(rows>0);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private MsgClickCountsVo selectAll() {
		List<MsgClickCountsVo> actions  = msgClickCountsDao.getAll();
		for (Iterator<MsgClickCountsVo> it=actions.iterator(); it.hasNext();) {
			MsgClickCountsVo msgClickCountsVo = it.next();
			System.out.println("selectAll - : " + LF + msgClickCountsVo);
		}
		if (actions.size() > 0) {
			return actions.get(0);
		}
		return null;
	}
	
	private MsgClickCountsVo selectByPrimaryKey(long msgId) {
		MsgClickCountsVo vo = (MsgClickCountsVo)msgClickCountsDao.getByPrimaryKey(msgId);
		System.out.println("selectByPrimaryKey - "+LF+vo);
		return vo;
	}
	
	private int update(MsgClickCountsVo msgClickCountsVo) {
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

	void deleteByPrimaryKey(MsgClickCountsVo vo) {
		try {
			int rowsDeleted = msgClickCountsDao.deleteByPrimaryKey(vo.getMsgId());
			System.out.println("deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	MsgClickCountsVo insert() {
		List<MsgClickCountsVo> list = msgClickCountsDao.getAll();
		if (list.size()>0) {
			MsgClickCountsVo msgClickCountsVo = list.get(list.size()-1);
			msgClickCountsVo.setMsgId(msgClickCountsVo.getMsgId()+1);
			msgClickCountsDao.insert(msgClickCountsVo);
			System.out.println("insert: "+LF+msgClickCountsVo);
			return msgClickCountsVo;
		}
		return null;
	}
}
