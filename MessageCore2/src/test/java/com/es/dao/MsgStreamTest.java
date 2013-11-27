package com.es.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.core.util.FileUtil;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.inbox.MsgStreamDao;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageBeanUtil;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.outbox.MsgStreamVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class MsgStreamTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgStreamDao msgStreamDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	private long testMsgId = 2L;

	@BeforeClass
	public static void MsgStreamPrepare() {
	}

	@Before
	public void checkMsgStream() throws IOException {
		if (msgStreamDao.getLastRecord()==null) {
			MsgInboxVo msgInboxVo = selectByMsgId(testMsgId);
			MsgStreamVo msgStreamVo = new MsgStreamVo();
			msgStreamVo.setMsgId(msgInboxVo.getMsgId());
			msgStreamVo.setFromAddrId(msgInboxVo.getFromAddrId());
			msgStreamVo.setAddTime(msgInboxVo.getUpdtTime());
			msgStreamVo.setMsgStream(getBouncedMail(1));
			insert(msgStreamVo);
		}
	}

	private MsgInboxVo selectByMsgId(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			msgInboxVo = msgInboxDao.getLastRecord();
		}
		System.out.println("MsgInboxDao - selectByPrimaryKey: "+LF+msgInboxVo);
		return msgInboxVo;
	}

	private byte[] getBouncedMail(int fileNbr) {
		return FileUtil.loadFromFile("bouncedmails/", "BouncedMail_" + fileNbr + ".txt");
	}

	@Test
	@Rollback(true)
	public void testMsgStream() throws MessagingException {
		MsgStreamVo msgStreamVo = selectLastRecord();
		assertNotNull(msgStreamVo);
		MsgStreamVo msgStreamVo2 = selectByPrimaryKey(msgStreamVo.getMsgId());
		assertTrue(msgStreamVo.equalsTo(msgStreamVo2));
		MessageBean msgBean = createMimeMessage(msgStreamVo);
		assertNotNull(msgBean);
		int rowsUpdated = update(msgStreamVo);
		assertEquals(rowsUpdated, 1);
		int rowsDeleted = deleteByPrimaryKey(msgStreamVo);
		assertEquals(rowsDeleted, 1);
		msgStreamVo = insert(msgStreamVo);
		assertNotNull(msgStreamVo);
	}
	
	private MsgStreamVo selectByPrimaryKey(long msgId) {
		MsgStreamVo msgStreamVo = (MsgStreamVo)msgStreamDao.getByPrimaryKey(msgId);
		System.out.println("MsgStreamDao - selectByPrimaryKey: "+LF+msgStreamVo);
		return msgStreamVo;
	}
	
	private MsgStreamVo selectLastRecord() {
		MsgStreamVo msgStreamVo = (MsgStreamVo)msgStreamDao.getLastRecord();
		System.out.println("MsgStreamDao - selectLastRecord: "+LF+msgStreamVo);
		return msgStreamVo;
	}
	
	private int update(MsgStreamVo msgStreamVo) {
		msgStreamVo.setMsgSubject("Test Subject");
		int rows = msgStreamDao.update(msgStreamVo);
		System.out.println("MsgStreamDao - update: rows updated: "+rows);
		return rows;
	}
	
	private int deleteByPrimaryKey(MsgStreamVo msgStreamVo) {
		int rowsDeleted = msgStreamDao.deleteByPrimaryKey(msgStreamVo.getMsgId());
		System.out.println("MsgStreamDao - deleteByPrimaryKey: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MsgStreamVo insert(MsgStreamVo msgStreamVo) {
		int rows=msgStreamDao.insert(msgStreamVo);
		System.out.println("MsgStreamDao - insert: rows inserted " + rows + LF + msgStreamVo);
		return msgStreamVo;
	}
	
	private MessageBean createMimeMessage(MsgStreamVo msgStreamVo) throws MessagingException {
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(msgStreamVo.getMsgStream());
		System.out.println("******************************");
		System.out.println("MessageBean created: " + msgBean);
		return msgBean;
	}
}
