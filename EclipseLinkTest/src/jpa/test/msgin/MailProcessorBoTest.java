package jpa.test.msgin;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.mail.MessagingException;

import jpa.constant.MsgStatusCode;
import jpa.data.preload.MailInboxEnum;
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
	
	@Test
	public void testMailProcessorBo1() throws MessagingException, IOException {
		testBouncedMail("BouncedMail_1.txt");
	}

	@Test
	public void testMailProcessorBo2() throws MessagingException, IOException {
		testBouncedMail("BouncedMail_2.txt");
	}

	private void testBouncedMail(String fileName) throws MessagingException, IOException {
		javax.mail.Message message = readFromFile(fileName);
		MailInboxPK pk = new MailInboxPK(MailInboxEnum.BOUNCE.getUserId(), MailInboxEnum.BOUNCE.getHostName());
		MailInbox mailbox = mailboxService.getByPrimaryKey(pk);

		javax.mail.Message[] messages = {message};

		MessageContext ctx = new MessageContext(messages, mailbox);
		try {
			mailProcBo.process(ctx);
			assertTrue(ctx.getRowIds().size()>=1);
			if (fileName.indexOf("_1")>0) {
				MessageInbox inbox = TestUtil.verifyBouncedMail_1(ctx.getRowIds().get(0), inboxService, emailService);
				assertTrue(MsgStatusCode.CLOSED.getValue().equals(inbox.getStatusId()));
			}
			else if (fileName.indexOf("_2")>0) {
				MessageInbox inbox = TestUtil.verifyBouncedMail_2(ctx.getRowIds().get(0), inboxService, emailService);
				assertTrue(MsgStatusCode.CLOSED.getValue().equals(inbox.getStatusId()));
				if (ctx.getRowIds().size()==2) { // Message Delivery Status
					TestUtil.verifyDeliveryStatus4BounceMail_2(ctx.getRowIds().get(1), inboxService);
				}
			}
		}
		catch (Exception e) {
			fail();
		}
	}

	private javax.mail.Message readFromFile(String fileName) throws MessagingException {
		byte[] mailStream = TestUtil.loadFromFile(fileName);
		javax.mail.Message message = MessageBeanUtil.createMimeMessage(mailStream);
		return message;
	}	
}
