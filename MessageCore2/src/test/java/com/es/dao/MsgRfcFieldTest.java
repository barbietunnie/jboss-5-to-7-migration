package com.es.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.inbox.MsgRfcFieldDao;
import com.es.data.constant.Constants;
import com.es.vo.inbox.MsgRfcFieldVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgRfcFieldTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgRfcFieldDao rfcFieldsDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	long testMsgId = 2L;
	String testRfcType = "message/rfc822";
	
	@BeforeClass
	public static void RfcFieldsPrepare() {
	}
	
	@Test
	public void testRfcFields() {
		List<MsgRfcFieldVo> list = selectByMsgId(testMsgId);
		if (list.isEmpty()) {
			testMsgId = msgInboxDao.getLastRecord().getMsgId();
			list = selectByMsgId(testMsgId);
		}
		assertTrue(list.size()>0);
		MsgRfcFieldVo vo = selectByPrimaryKey(testMsgId, testRfcType);
		if (vo == null) {
			testRfcType = list.get(0).getRfcType();
			vo = selectByPrimaryKey(testMsgId, testRfcType);
		}
		assertNotNull(vo);
		MsgRfcFieldVo vo2 = insert(vo.getMsgId(), testRfcType);
		assertNotNull(vo2);
		vo.setRfcType(vo2.getRfcType());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = deleteByPrimaryKey(vo2.getMsgId(), vo2.getRfcType());
		assertEquals(rowsDeleted, 1);
	}
	
	private List<MsgRfcFieldVo> selectByMsgId(long msgId) {
		List<MsgRfcFieldVo> actions = rfcFieldsDao.getByMsgId(msgId);
		for (Iterator<MsgRfcFieldVo> it=actions.iterator(); it.hasNext();) {
			MsgRfcFieldVo rfcFieldsVo = it.next();
			System.out.println("MsgRfcFieldDao - selectByMsgId: "+LF+rfcFieldsVo);
		}
		return actions;
	}
	
	private MsgRfcFieldVo selectByPrimaryKey(long msgId, String rfcType) {
		MsgRfcFieldVo rfcFieldsVo = (MsgRfcFieldVo)rfcFieldsDao.getByPrimaryKey(msgId,rfcType);
		System.out.println("MsgRfcFieldDao - selectByPrimaryKey: "+LF+rfcFieldsVo);
		return rfcFieldsVo;
	}
	
	private int update(MsgRfcFieldVo rfcFieldsVo) {
		rfcFieldsVo.setDlvrStatus(rfcFieldsVo.getDlvrStatus()+".");
		int rows = rfcFieldsDao.update(rfcFieldsVo);
		System.out.println("MsgRfcFieldDao - update: "+LF+rfcFieldsVo);
		return rows;
	}
	
	private int deleteByPrimaryKey(long msgId, String rfcType) {
		int rowsDeleted = rfcFieldsDao.deleteByPrimaryKey(msgId, rfcType);
		System.out.println("MsgRfcFieldDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgRfcFieldVo insert(long msgId, String rfcType) {
		List<MsgRfcFieldVo> list = rfcFieldsDao.getByMsgId(msgId);
		MsgRfcFieldVo rfcFieldsVo = null;
		for (MsgRfcFieldVo vo : list) {
			if (StringUtils.equals(vo.getRfcType(), rfcType)) {
				rfcFieldsVo = vo;
				break;
			}
		}
		if (rfcFieldsVo != null) {
			rfcFieldsVo.setRfcType(Constants.TEXT_PLAIN);
		}
		else {
			rfcFieldsVo = new MsgRfcFieldVo();
			rfcFieldsVo.setMsgId(msgId);
			rfcFieldsVo.setRfcType(Constants.TEXT_PLAIN);
			rfcFieldsVo.setDsnText("Test RFC DSN Text.");
		}
		int rows = rfcFieldsDao.insert(rfcFieldsVo);
		System.out.println("MsgRfcFieldDao - insert: rows inserted "+rows+LF+rfcFieldsVo);
		return rfcFieldsVo;
	}
}
