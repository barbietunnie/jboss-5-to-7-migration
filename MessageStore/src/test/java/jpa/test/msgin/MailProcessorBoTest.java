package jpa.test.msgin;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import jpa.constant.MsgStatusCode;
import jpa.data.preload.MailInboxEnum;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageContext;
import jpa.model.MailInbox;
import jpa.model.MailInboxPK;
import jpa.model.message.MessageInbox;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageInboxService;
import jpa.service.msgin.MailInboxService;
import jpa.service.msgin.MailProcessorBo;
import jpa.service.msgin.MessageParserBo;
import jpa.util.TestUtil;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional(propagation=Propagation.REQUIRED)
public class MailProcessorBoTest {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MailProcessorBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageParserBo msgParser;
	@Autowired
	private MailProcessorBo mailProcBo;
	@Autowired
	private MailInboxService mailboxService;
	@Autowired
	private MessageInboxService inboxService;
	@Autowired
	private EmailAddressService emailService;
	
	private List<Integer> rowIds_1 = null;
	private List<Integer> rowIds_2 = null;
	
	@BeforeTransaction
	public void prepare() {
		try {
			List<Integer> rowids = persistRecord("BouncedMail_1.txt");
			assertTrue(rowids.size()>0);
			rowIds_1 = rowids;
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
		try {
			List<Integer> rowids = persistRecord("BouncedMail_2.txt");
			assertTrue(rowids.size()>0);
			rowIds_2 = rowids;
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	private List<Integer> persistRecord(String fileName)
			throws DataValidationException, MessagingException, IOException,
			TemplateException {
		javax.mail.Message message = readFromFile(fileName);
		MailInboxPK pk = new MailInboxPK(MailInboxEnum.BOUNCE.getUserId(), MailInboxEnum.BOUNCE.getHostName());
		MailInbox mailbox = mailboxService.getByPrimaryKey(pk);

		javax.mail.Message[] messages = {message};

		MessageContext ctx = new MessageContext(messages, mailbox);
		mailProcBo.process(ctx);
		return ctx.getRowIds();
	}
	
	@Test
	public void testMailProcessorBo1() throws MessagingException, IOException {
		testBouncedMail(rowIds_1, 1);
	}

	@Test
	public void testMailProcessorBo2() throws MessagingException, IOException {
		testBouncedMail(rowIds_2, 2);
	}

	@AfterTransaction
	public void cleanup() {
	}

	private void testBouncedMail(List<Integer> rowIds, int fileNbr) throws MessagingException, IOException {
		try {
			if (fileNbr == 1) {
				logger.info("Row_Id_1 = " + rowIds.get(0));
				MessageInbox inbox = TestUtil.verifyBouncedMail_1(rowIds.get(0), inboxService, emailService);
				assertTrue(MsgStatusCode.CLOSED.getValue().equals(inbox.getStatusId()));
			}
			else if (fileNbr == 2) {
				logger.info("Row_Id_2 = " + rowIds.get(0));
				MessageInbox inbox = TestUtil.verifyBouncedMail_2(rowIds.get(0), inboxService, emailService);
				assertTrue(MsgStatusCode.CLOSED.getValue().equals(inbox.getStatusId()));
				if (rowIds.size()==2) { // Message Delivery Status
					logger.info("Row_Id_2 for DeliveryStatus = " + rowIds.get(1));
					TestUtil.verifyDeliveryStatus4BounceMail_2(rowIds.get(1), inboxService);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	private javax.mail.Message readFromFile(String fileName) throws MessagingException {
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		javax.mail.Message message = MessageBeanUtil.createMimeMessage(mailStream);
		return message;
	}	
}
