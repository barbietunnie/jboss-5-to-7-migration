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

import com.es.dao.inbox.MsgAttachmentDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.data.constant.Constants;
import com.es.vo.inbox.MsgAttachmentVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class MsgAttachmentTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgAttachmentDao msgAttachmentDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	long testMsgId = 2L;
	
	@BeforeClass
	public static void AttachmentsPrepare(){
	}
	
	@Test
	public void insertUpdateDelete() {
		try {
			List<MsgAttachmentVo> list = selectByMsgId(testMsgId);
			if (list.isEmpty()) {
				testMsgId = msgInboxDao.getLastRecord().getMsgId();
				list = selectByMsgId(testMsgId);
			}
			assertTrue(list.size()>0);
			MsgAttachmentVo vo = insert(testMsgId);
			assertNotNull(vo);
			List<MsgAttachmentVo> list2 = selectByMsgId(testMsgId);
			assertTrue(list2.size()==(list.size()+1));
			MsgAttachmentVo vo2 = selectByPrimaryKey(vo);
			assertNotNull(vo2);
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = deleteByPrimaryKey(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			deleteLast(testMsgId);
			e.printStackTrace();
			fail();
		}
	}
	
	private List<MsgAttachmentVo> selectByMsgId(long msgId) {
		List<MsgAttachmentVo> list = msgAttachmentDao.getByMsgId(msgId);
		for (Iterator<MsgAttachmentVo> it=list.iterator(); it.hasNext();) {
			MsgAttachmentVo attachmentsVo = it.next();
			System.out.println("MsgAttachmentDao - selectByMsgId: "+LF+attachmentsVo);
		}
		return list;
	}
	
	private MsgAttachmentVo selectByPrimaryKey(MsgAttachmentVo vo) {
		MsgAttachmentVo attachmentsVo = (MsgAttachmentVo) msgAttachmentDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAttchmntDepth(), vo.getAttchmntSeq());
		System.out.println("MsgAttachmentDao - selectByPrimaryKey: "+LF+attachmentsVo);
		return attachmentsVo;
	}
	
	private int update(MsgAttachmentVo vo) {
		MsgAttachmentVo attachmentsVo = (MsgAttachmentVo) msgAttachmentDao.getByPrimaryKey(vo.getMsgId(),
				vo.getAttchmntDepth(), vo.getAttchmntSeq());
		int rowsUpdated = 0;
		if (attachmentsVo!=null) {
			attachmentsVo.setAttchmntType("text/plain");
			attachmentsVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			attachmentsVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
			rowsUpdated = msgAttachmentDao.update(attachmentsVo);
			System.out.println("MsgAttachmentDao - update: "+LF+attachmentsVo);
		}
		return rowsUpdated;
	}
	
	private int deleteByPrimaryKey(MsgAttachmentVo vo) {
		int rowsDeleted = msgAttachmentDao.deleteByPrimaryKey(vo.getMsgId(), vo.getAttchmntDepth(),
				vo.getAttchmntSeq());
		System.out.println("MsgAttachmentDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgAttachmentVo insert(long msgId) {
		List<MsgAttachmentVo> list = (List<MsgAttachmentVo>)msgAttachmentDao.getByMsgId(msgId);
		MsgAttachmentVo attachmentsVo = null;
		if (list.size()>0) {
			attachmentsVo = list.get(list.size()-1);
			attachmentsVo.setAttchmntSeq(attachmentsVo.getAttchmntSeq()+1);
		}
		else {
			attachmentsVo = new MsgAttachmentVo();
			attachmentsVo.setMsgId(msgId);
			attachmentsVo.setAttchmntDepth(1);
			attachmentsVo.setAttchmntSeq(1);
			attachmentsVo.setAttchmntName("test.txt");
			attachmentsVo.setAttchmntType("text/plain; name=\"test.txt\"");
			attachmentsVo.setAttchmntValue("Test blob content goes here.".getBytes());
		}
		msgAttachmentDao.insert(attachmentsVo);
		System.out.println("MsgAttachmentDao - insert: "+LF+attachmentsVo);
		return attachmentsVo;
	}

	private void deleteLast(long msgId) {
		List<MsgAttachmentVo> list = (List<MsgAttachmentVo>)msgAttachmentDao.getByMsgId(msgId);
		if (list.size()>1) {
			MsgAttachmentVo attachmentsVo = list.get(list.size()-1);
			int rows = deleteByPrimaryKey(attachmentsVo);
			System.out.println("MsgAttachmentDao - deleteLast: "+rows);
		}
	}
}
