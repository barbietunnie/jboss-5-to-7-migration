package com.legacytojava.message.dao.inbox;

import static org.junit.Assert.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanUtil;
import com.legacytojava.message.bo.inbox.RuleEngineTest;
import com.legacytojava.message.vo.outbox.MsgStreamVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=false)
@Transactional
public class MsgStreamTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private MsgStreamDao msgStreamDao;

	@BeforeClass
	public static void MsgStreamPrepare() {
	}
	@Before
	public void checkMsgStream() {
		/*
		 * This did not work, the MsgStream record was inserted, but the
		 * testMsgStream.selectLastRecord() still returns a null.
		 */
		if (msgStreamDao.getLastRecord()==null) {
			Result result = JUnitCore.runClasses(RuleEngineTest.class);
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
		}
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
