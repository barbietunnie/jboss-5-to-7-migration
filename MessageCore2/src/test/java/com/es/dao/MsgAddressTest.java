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

import com.es.dao.inbox.MsgAddressDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddrType;
import com.es.vo.inbox.MsgAddressVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgAddressTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgAddressDao msgAddrsDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	long testMsgId = 2L;
	String testAddrType = EmailAddrType.FROM_ADDR.getValue();

	@BeforeClass
	public static void MsgAddrsPrepare() {
	}
	
	@Test
	public void insertUpdateDelete() {
		try {
			List<MsgAddressVo> list = selectByMsgId(testMsgId);
			if (list.isEmpty()) {
				testMsgId = msgInboxDao.getLastRecord().getMsgId();
				list = selectByMsgId(testMsgId);
			}
			assertTrue(list.size()>0);
			List<MsgAddressVo> list2 = selectByMsgIdAndType(testMsgId, testAddrType);
			assertTrue(list2.size()>0);
			MsgAddressVo vo = insert(testMsgId, testAddrType);
			assertNotNull(vo);
			List<MsgAddressVo> list3 = selectByMsgIdAndType(testMsgId, testAddrType);
			assertTrue(list3.size()==(list2.size()+1));
			MsgAddressVo vo2 = selectByPrimaryKey(vo);
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
			fail();
		}
	}
	
	private List<MsgAddressVo> selectByMsgId(long msgId) {
		List<MsgAddressVo> actions = msgAddrsDao.getByMsgId(msgId);
		for (Iterator<MsgAddressVo> it=actions.iterator(); it.hasNext();) {
			MsgAddressVo msgAddrsVo = it.next();
			System.out.println("MsgAddressDao - selectByMsgId: "+LF+msgAddrsVo);
		}
		return actions;
	}
	
	private MsgAddressVo selectByPrimaryKey(MsgAddressVo vo) {
		MsgAddressVo msgAddrsVo = (MsgAddressVo) msgAddrsDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAddrType(), vo.getAddrSeq());
		System.out.println("MsgAddressDao - selectByPrimaryKey: "+LF+msgAddrsVo);
		return vo;
	}
	
	private List<MsgAddressVo> selectByMsgIdAndType(long msgId, String type) {
		List<MsgAddressVo> actions  = msgAddrsDao.getByMsgIdAndType(msgId, type);
		for (Iterator<MsgAddressVo> it=actions.iterator(); it.hasNext();) {
			MsgAddressVo msgAddrsVo = it.next();
			System.out.println("MsgAddressDao - selectByMsgIdAndType: "+LF+msgAddrsVo);
		}
		return actions;
	}
	
	private int update(MsgAddressVo vo) {
		MsgAddressVo msgAddrsVo = (MsgAddressVo) msgAddrsDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAddrType(), vo.getAddrSeq());
		int rowsUpdated = 0;
		if (msgAddrsVo!=null) {
			msgAddrsVo.setAddrValue("more."+msgAddrsVo.getAddrValue());
			msgAddrsVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			msgAddrsVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			rowsUpdated = msgAddrsDao.update(msgAddrsVo);
			System.out.println("MsgAddressDao - update: "+LF+msgAddrsVo);
		}
		return rowsUpdated;
	}
	
	private int deleteByPrimaryKey(MsgAddressVo vo) {
		int rowsDeleted = msgAddrsDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAddrType(),
				vo.getAddrSeq());
		System.out.println("MsgAddressDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgAddressVo insert(long msgId, String addrType) {
		List<MsgAddressVo> list = (List<MsgAddressVo>)msgAddrsDao.getByMsgIdAndType(msgId, addrType);
		MsgAddressVo msgAddrsVo = null;
		if (list.size()>0) {
			msgAddrsVo = list.get(list.size()-1);
			msgAddrsVo.setAddrSeq(msgAddrsVo.getAddrSeq()+1);
		}
		else {
			msgAddrsVo = new MsgAddressVo();
			msgAddrsVo.setMsgId(msgId);
			msgAddrsVo.setAddrType(addrType);
			msgAddrsVo.setAddrSeq(1);
			msgAddrsVo.setAddrValue("msgaddresstest@test.com");
		}
		msgAddrsDao.insert(msgAddrsVo);
		System.out.println("MsgAddressDao - insert: "+LF+msgAddrsVo);
		return msgAddrsVo;
	}

	private void deleteLast(long msgId, String addrType) {
		List<MsgAddressVo> list = (List<MsgAddressVo>)msgAddrsDao.getByMsgIdAndType(msgId, addrType);
		if (list.size()>1) {
			MsgAddressVo vo = list.get(list.size()-1);
			int rows = msgAddrsDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAddrType(), vo.getAddrSeq());
			System.out.println("MsgAddressDao - deleteLast: "+rows);
		}
	}
}
