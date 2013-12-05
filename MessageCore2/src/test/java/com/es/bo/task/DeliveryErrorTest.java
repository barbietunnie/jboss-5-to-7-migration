package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

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

import com.es.bo.sender.MessageBeanBo;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.outbox.DeliveryStatusDao;
import com.es.data.constant.MsgDirectionCode;
import com.es.data.constant.MsgStatusCode;
import com.es.msg.util.EmailIdParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.outbox.DeliveryStatusVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class DeliveryErrorTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(DeliveryErrorTest.class);
	
	@Resource
	private DeliveryError task;
	@Resource
	private EmailAddressDao emailDao;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private MessageBeanBo msgBeanBo;
	@Resource
	private DeliveryStatusDao dlvrStatDao;

	@BeforeClass
	public static void DeliveryErrorPrepare() {
	}

	private boolean isUpdatingSameRecord = true;
	private long msgId;
	private MessageBean mBean = null;
	private MsgInboxVo inbox = null;
	private String finalRcpt = "event.alert@localhost";
	private String id_xhdr = null;
	EmailIdParser parser = EmailIdParser.getDefaultParser();
	
	@Before
	@Rollback(false)
	public void prepare() {
		inbox = inboxDao.getRandomRecord();
		mBean = msgBeanBo.createMessageBean(inbox);
		id_xhdr = parser.parseHeaders(mBean.getHeaders());
		if (id_xhdr != null) {
			if (mBean.getMsgRefId() == null) {
				mBean.setMsgRefId(Long.parseLong(id_xhdr));
			}
		}
		else {
			mBean.setMsgRefId(inbox.getMsgId());
			id_xhdr = inbox.getMsgId()+"";
		}
		
		if (inboxDao.getByPrimaryKey(mBean.getMsgRefId())==null) {
			mBean.setMsgRefId(inbox.getMsgId());
		}

		if (!id_xhdr.equals(mBean.getMsgRefId().toString())) {
			id_xhdr = mBean.getMsgRefId().toString();
		}
		if (!mBean.getMsgRefId().equals(Long.valueOf(inbox.getMsgId()))) {
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
		assertTrue(ctx.getMsgIdList().size()==1);
		msgId = ctx.getMsgIdList().get(0);
	}

	@Test
	public void testDeliveryError() {
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertTrue(mBean.getMsgRefId().equals(Long.valueOf(msgId)));
		MsgInboxVo minbox = inboxDao.getByPrimaryKey(msgId);
		String id_bean = parser.parseMsg(mBean.getBody());
		String id_ibox = parser.parseMsg(minbox.getMsgBody());
		if (id_ibox!=null) {
			if (isUpdatingSameRecord==false) {
				assertTrue(id_ibox.equals(mBean.getMsgRefId().toString()));
				if (inbox.getMsgRefId() != null) {
					assertTrue(id_ibox.equals(inbox.getMsgRefId().toString()));
				}
				else {
					assertTrue(id_ibox.equals(inbox.getLeadMsgId()+""));
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
				assertTrue(id_xhdr.equals(inbox.getMsgId()+""));
			}
			else {
				assertTrue(id_xhdr.equals(minbox.getMsgId()+""));
			}
		}
		else if (MsgDirectionCode.RECEIVED.getValue().equals(inbox.getMsgDirection())) {
			if (id_bean!=null) {
				assertTrue(id_xhdr.equals(id_bean) || id_xhdr.equals(mBean.getMsgRefId().toString()));
			}
		}
		assertTrue(MsgStatusCode.DELIVERY_FAILED.getValue().equals(minbox.getStatusId()));
		List<DeliveryStatusVo> statusList = dlvrStatDao.getByMsgId(minbox.getMsgId());
		assertFalse(statusList.isEmpty());
		boolean finalRcptFound = false;
		for (DeliveryStatusVo status : statusList) {
			if (finalRcpt.equals(status.getFinalRecipient())) {
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
