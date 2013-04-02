package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import jpa.constant.MsgDirectionCode;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
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

	private boolean isUpdatingSameRecord = true;
	private int rowId;
	private MessageBean mBean = null;
	private MessageInbox inbox = null;
	private String finalRcpt = "event.alert@localhost";
	
	@Before
	@Rollback(false)
	public void prepare() {
		inbox = inboxService.getLastRecord();
		mBean = msgBeanBo.createMessageBean(inbox);
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id_xhdr = parser.parseHeaders(mBean.getHeaders());
		assertNotNull(id_xhdr);
		if (mBean.getMsgRefId()==null) {
			mBean.setMsgRefId(Integer.parseInt(id_xhdr));
		}
		else if (!id_xhdr.equals(mBean.getMsgRefId()+"")) {
			id_xhdr = mBean.getMsgRefId().toString();
		}
		if (!mBean.getMsgRefId().equals(inbox.getRowId())) {
			// update delivery status to referring record.
			isUpdatingSameRecord = false;
		}
		mBean.setMsgId(null);
		mBean.setFinalRcpt(finalRcpt);
		mBean.setDsnAction("failed");
		mBean.setDsnStatus("5.1.1");
		mBean.setDiagnosticCode("smtp; 554 delivery error: dd This user doesn't have a yahoo.com account (unknown.useraddress@yahoo.com) [0] - mta522.mail.mud.yahoo.com");
		mBean.setDsnDlvrStat("The delivery of following message failed due to:" + LF +
				" 511 5.1.1 Invalid Destination Mailbox Address." + LF +
				"Invalid Addresses..., TO addr: unknown.useraddress@nc.rr.com");

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		assertTrue(ctx.getRowIds().size()==1);
		rowId = ctx.getRowIds().get(0);
	}

	@Test
	public void testDeliveryError() throws Exception {
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id_xhdr = parser.parseHeaders(mBean.getHeaders());
		// verify results
		assertTrue(mBean.getMsgRefId().equals(rowId));
		MessageInbox minbox = inboxService.getAllDataByPrimaryKey(rowId);
		String id_bean = parser.parseMsg(mBean.getBody());
		String id_ibox = parser.parseMsg(minbox.getMsgBody());
		if (id_ibox!=null) {
			if (isUpdatingSameRecord==false) {
				assertTrue(id_ibox.equals(mBean.getMsgRefId()+""));
				if (inbox.getReferringMessageRowId() != null) {
					assertTrue(id_ibox.equals(inbox.getReferringMessageRowId()+""));
				}
				else {
					assertTrue(id_ibox.equals(inbox.getLeadMessageRowId()+""));
				}
			}
			else {
				assertTrue(id_bean.equals(id_ibox));
			}
		}
		else {
			assertNull(id_bean);
		}
		if (MsgDirectionCode.SENT.getValue().equals(inbox.getMsgDirection())) {
			if (isUpdatingSameRecord) {
				assertTrue(id_xhdr.equals(inbox.getRowId()+""));
			}
			else {
				assertTrue(id_xhdr.equals(minbox.getRowId()+""));
			}
		}
		else if (MsgDirectionCode.RECEIVED.getValue().equals(inbox.getMsgDirection())) {
			if (id_bean!=null) {
				assertTrue(id_xhdr.equals(id_bean) || id_xhdr.equals(mBean.getMsgRefId()+""));
			}
		}
		assertTrue(MsgStatusCode.DELIVERY_FAILED.getValue().equals(minbox.getStatusId()));
		assertFalse(minbox.getMessageDeliveryStatusList().isEmpty());
		boolean finalRcptFound = false;
		for (MessageDeliveryStatus status : minbox.getMessageDeliveryStatusList()) {
			if (finalRcpt.equals(status.getFinalRecipientAddress())) {
				assertTrue(mBean.getDsnDlvrStat().equals(status.getDeliveryStatus()));
				assertTrue(mBean.getDiagnosticCode().equals(status.getDsnReason()));
				assertTrue(mBean.getDsnStatus().equals(status.getDsnStatus()));
				assertTrue(mBean.getDiagnosticCode().equals(status.getDsnReason()));
				finalRcptFound = true;
			}
		}
		assertTrue(finalRcptFound);
	}
}
