package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import jpa.constant.MsgStatusCode;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.message.MessageDeliveryStatus;
import jpa.model.message.MessageInbox;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageInboxService;
import jpa.service.msgout.MessageBeanBo;
import jpa.service.task.DeliveryError;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class DeliveryErrorTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(DeliveryErrorTest.class);
	
	@Resource
	private DeliveryError task;
	@Resource
	private EmailAddressService emailService;
	@Resource
	private MessageInboxService inboxService;
	@Resource
	private MessageBeanBo msgBeanBo;

	@BeforeClass
	public static void DeliveryErrorPrepare() {
	}

	@Test
	public void testDeliveryError() throws Exception {
		MessageInbox inbox = inboxService.getLastRecord();
		MessageBean mBean = msgBeanBo.createMessageBean(inbox);
		mBean.setMsgRefId(inbox.getRowId());
		mBean.setMsgId(null);
		String finalRcpt = "event.alert@localhost";
		mBean.setFinalRcpt(finalRcpt);
		mBean.setDsnDlvrStat("5.1.1");
		mBean.setDsnStatus("5.3.0");
		mBean.setDiagnosticCode("smtp; 554 delivery error: dd This user doesn't have a yahoo.com account (unknown.useraddress@yahoo.com) [0] - mta522.mail.mud.yahoo.com");
		mBean.setDsnText("The delivery of following message failed due to:" + LF +
				" 511 5.1.1 Invalid Destination Mailbox Address." + LF +
				"Invalid Addresses..., TO addr: unknown.useraddress@nc.rr.com");

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		assertFalse(ctx.getRowIds().isEmpty());
		MessageInbox minbox = inboxService.getAllDataByPrimaryKey(ctx.getRowIds().get(0));
		assertTrue(mBean.getSubject().equals(minbox.getMsgSubject()));
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id_bean = parser.parseMsg(mBean.getBody());
		String id_ibox = parser.parseMsg(minbox.getMsgBody());
		assertTrue(id_bean.equals(id_ibox));
		assertTrue(id_bean.equals(minbox.getReferringMessageRowId()+""));
		assertTrue(MsgStatusCode.DELIVERY_FAILED.getValue().equals(minbox.getStatusId()));
		assertFalse(minbox.getMessageDeliveryStatusList().isEmpty());
		boolean finalRcptFound = false;
		for (MessageDeliveryStatus status : minbox.getMessageDeliveryStatusList()) {
			if (finalRcpt.equals(status.getFinalRecipientAddress())) {
				assertTrue(mBean.getDsnDlvrStat().equals(status.getDeliveryStatus()));
				assertTrue(mBean.getDiagnosticCode().equals(status.getDsnReason()));
				assertTrue(mBean.getDsnStatus().equals(status.getDsnStatus()));
				assertTrue(mBean.getDsnText().equals(status.getDsnText()));
				finalRcptFound = true;
			}
		}
		assertTrue(finalRcptFound);
	}
}
