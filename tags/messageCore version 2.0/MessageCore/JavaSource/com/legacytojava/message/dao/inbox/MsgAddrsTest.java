package com.legacytojava.message.dao.inbox;

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

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.vo.inbox.MsgAddrsVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class MsgAddrsTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgAddrsDao msgAddrsDao;
	long testMsgId = 2L;
	String testAddrType = EmailAddressType.FROM_ADDR;

	@BeforeClass
	public static void MsgAddrsPrepare() {
	}
	
	@Test
	public void insertUpdateDelete() {
		try {
			List<MsgAddrsVo> list = selectByMsgId(testMsgId);
			assertTrue(list.size()>0);
			List<MsgAddrsVo> list2 = selectByMsgIdAndType(testMsgId, testAddrType);
			assertTrue(list2.size()>0);
			MsgAddrsVo vo = insert(testMsgId, testAddrType);
			assertNotNull(vo);
			List<MsgAddrsVo> list3 = selectByMsgIdAndType(testMsgId, testAddrType);
			assertTrue(list3.size()==(list2.size()+1));
			MsgAddrsVo vo2 = selectByPrimaryKey(vo);
			assertNotNull(vo2);
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			deleteLast(testMsgId, testAddrType);
			e.printStackTrace();
		}
	}
	
	private List<MsgAddrsVo> selectByMsgId(long msgId) {
		List<MsgAddrsVo> actions = msgAddrsDao.getByMsgId(msgId);
		for (Iterator<MsgAddrsVo> it=actions.iterator(); it.hasNext();) {
			MsgAddrsVo msgAddrsVo = it.next();
			System.out.println("MsgAddrsDao - selectByMsgId: "+LF+msgAddrsVo);
		}
		return actions;
	}
	
	private MsgAddrsVo selectByPrimaryKey(MsgAddrsVo vo) {
		MsgAddrsVo msgAddrsVo = (MsgAddrsVo) msgAddrsDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAddrType(), vo.getAddrSeq());
		System.out.println("MsgAddrsDao - selectByPrimaryKey: "+LF+msgAddrsVo);
		return vo;
	}
	
	private List<MsgAddrsVo> selectByMsgIdAndType(long msgId, String type) {
		List<MsgAddrsVo> actions  = msgAddrsDao.getByMsgIdAndType(msgId, type);
		for (Iterator<MsgAddrsVo> it=actions.iterator(); it.hasNext();) {
			MsgAddrsVo msgAddrsVo = it.next();
			System.out.println("MsgAddrsDao - selectByMsgIdAndType: "+LF+msgAddrsVo);
		}
		return actions;
	}
	
	private int update(MsgAddrsVo vo) {
		MsgAddrsVo msgAddrsVo = (MsgAddrsVo) msgAddrsDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAddrType(), vo.getAddrSeq());
		int rowsUpdated = 0;
		if (msgAddrsVo!=null) {
			msgAddrsVo.setAddrValue("more."+msgAddrsVo.getAddrValue());
			msgAddrsVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			msgAddrsVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			rowsUpdated = msgAddrsDao.update(msgAddrsVo);
			System.out.println("MsgAddrsDao - update: "+LF+msgAddrsVo);
		}
		return rowsUpdated;
	}
	
	private int deleteByPrimaryKey(MsgAddrsVo vo) {
		int rowsDeleted = msgAddrsDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAddrType(),
				vo.getAddrSeq());
		System.out.println("MsgAddrsDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgAddrsVo insert(long msgId, String addrType) {
		List<MsgAddrsVo> list = (List<MsgAddrsVo>)msgAddrsDao.getByMsgIdAndType(msgId, addrType);
		if (list.size()>0) {
			MsgAddrsVo msgAddrsVo = list.get(list.size()-1);
			msgAddrsVo.setAddrSeq(msgAddrsVo.getAddrSeq()+1);
			msgAddrsDao.insert(msgAddrsVo);
			System.out.println("MsgAddrsDao - insert: "+LF+msgAddrsVo);
			return msgAddrsVo;
		}
		return null;
	}

	private void deleteLast(long msgId, String addrType) {
		List<MsgAddrsVo> list = (List<MsgAddrsVo>)msgAddrsDao.getByMsgIdAndType(msgId, addrType);
		if (list.size()>1) {
			MsgAddrsVo vo = list.get(list.size()-1);
			int rows = msgAddrsDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAddrType(), vo.getAddrSeq());
			System.out.println("MsgAddrsDao - deleteLast: "+rows);
		}
	}
}
