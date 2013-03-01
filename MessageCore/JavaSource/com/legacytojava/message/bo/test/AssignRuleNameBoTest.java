package com.legacytojava.message.bo.test;

import static org.junit.Assert.*;

import java.util.Random;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.vo.inbox.MsgInboxVo;

/*** Please deploy MailEngine and start JBoss before running this test ***/
public class AssignRuleNameBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo assignRuleNameBo;
	private static RuleNameType testRuleName;
	private static Long msgId;
	
	@BeforeClass
	public static void prepare() {
		Random random = new Random();
		testRuleName = RuleNames[random.nextInt(RuleNames.length - 1)];
	}
	@Test
	@Rollback(false)
	public void assignRuleName() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		assignRuleNameBo.setTaskArguments(testRuleName.name());
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		/*
		 * this step will place a MessageBean with the Assigned RuleName in a
		 * queue for MailEngine to pickup, the MailEngine will then process the
		 * MessageBean and add a record to MsgInbox table.
		 */
		assignRuleNameBo.process(messageBean);
		msgId = messageBean.getMsgId();
	}
	@Test
	public void waitForMailEngine() {
		// wait for the MailEngine to add a record with Assigned RuleName to MsgInbox
		try {
			Thread.sleep(5 * 1000);
		}
		catch (InterruptedException e) {}
	}
	@Test
	public void verifyDatabaseRecord() {
		// now verify the database record added
		MsgInboxVo vo1 = selectMsgInboxByMsgId(msgId);
		assertTrue(vo1!=null);
		MsgInboxVo vo2 = selectLastMsgInboxRecord();
		assertTrue(testRuleName.name().equals(vo2.getRuleName()));
	}
	private MsgInboxVo selectLastMsgInboxRecord() {
		MsgInboxVo vo = (MsgInboxVo)msgInboxDao.getLastRecord();
		if (vo!=null) {
			System.out.println("MsgInboxDao - selectLastRecord: "+LF+vo);
		}
		return vo;
	}
}
