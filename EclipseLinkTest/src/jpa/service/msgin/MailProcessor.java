package jpa.service.msgin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Transport;

import jpa.constant.CarrierCode;
import jpa.message.MessageBean;
import jpa.message.MessageBeanBuilder;
import jpa.message.MessageContext;
import jpa.model.MailInbox;
import jpa.service.message.MessageInboxService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * process email's handed over by MailReader class.
 */
@Component("mailProcessor")
@Transactional(propagation=Propagation.REQUIRED)
public class MailProcessor {
	static final Logger logger = Logger.getLogger(MailProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static Logger duplicateReport = Logger.getLogger("jpa.message.report.duplicate");

	//volatile boolean keepRunning = true;
	private final int MAX_INBOUND_BODY_SIZE = 150 * 1024;
	private final int MAX_INBOUND_CMPT_SIZE = 1024 * 1024;

	final static String LF = System.getProperty("line.separator", "\n");

	@Autowired
	private MessageInboxService inboxService;
	@Autowired
	private MessageInboxBo messageInboxBo;
	
	private MailInbox mailBoxVo;

	/**
	 * process request
	 * 
	 * @param req - request object.
	 * @throws MessagingException if any error
	 */
	public void process(MessageContext req) throws MessagingException, IOException {
		logger.info("Entering process() method...");
		if (req != null && req.getMessages()!=null && req.getMailInbox()!=null) {
			Message[] msgs = req.getMessages();
			mailBoxVo = req.getMailInbox();
			// Just dump out the new messages and set the delete flags
			for (int i = 0; i < msgs.length; i++) {
				if (msgs[i] != null && !msgs[i].isSet(Flags.Flag.SEEN)
						&& !msgs[i].isSet(Flags.Flag.DELETED)) {
					processPart(msgs[i]);
				}
				// release the instance for GC, not working w/pop3
				// msgs[i]=null;
			}
		}
		else {
			logger.error("Request is null!");
		}
	}

	/**
	 * process message part and build MessageBean from message part.
	 * 
	 * @param p - part
	 * @throws MessagingException 
	 * @throws IOException if any error
	 * @return a MessageBean instance
	 */
	MessageBean processPart(Part p) throws IOException, MessagingException {
		long start_tms = System.currentTimeMillis();
		
		// parse the MimeMessage to MessageBean
		MessageBean msgBean = MessageBeanBuilder.processPart(p, mailBoxVo.getToAddressDomain());
		msgBean.setIsReceived(true);
		
		// mailbox carrierCode
		msgBean.setCarrierCode(CarrierCode.valueOf(mailBoxVo.getCarrierCode()));
		// internal mail only flag
		msgBean.setInternalOnly(mailBoxVo.getIsInternalOnly());
		// mailbox SSL flag
		msgBean.setUseSecureServer(mailBoxVo.isUseSsl());
		// MailBox Host Address
		msgBean.setMailboxHost(mailBoxVo.getMailInboxPK().getHostName());
		// MailBox User Id
		msgBean.setMailboxUser(mailBoxVo.getMailInboxPK().getUserId());
		// MailBox Name
		msgBean.setMailboxName(mailBoxVo.getDescription());
		// Folder Name
		msgBean.setFolderName(mailBoxVo.getFolderName());
		// to_plain_text indicator, default to "no"
		msgBean.setToPlainText(mailBoxVo.getIsToPlainText());
		
		// TODO for prototype only. Remove it.
		if (CarrierCode.READONLY.equals(msgBean.getCarrierCode())) {
			msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		}
		// get original body w/o possible HTML to text conversion
		String body = msgBean.getBody(true);
		String contentType = msgBean.getBodyContentType();

		// check message body and component size
		boolean isMsgSizeTooLarge = false;
		if (body.length() > MAX_INBOUND_BODY_SIZE) {
			isMsgSizeTooLarge = true;
			logger.warn("Message body size exceeded limit: " + body.length());
		}
		if (msgBean.getComponentsSize().size() > 0) {
			for (int i = 0; i < msgBean.getComponentsSize().size(); i++) {
				Integer objSize = (Integer) msgBean.getComponentsSize().get(i);
				if (objSize.intValue() > MAX_INBOUND_CMPT_SIZE) {
					isMsgSizeTooLarge = true;
					logger.warn("Message component(" + i + ") exceeded limit: "
							+ objSize.intValue());
					break;
				}
			}
		}
		
		if (isMsgSizeTooLarge) {
			try {
				// return the mail
				Message reply = new MailReaderReply().composeReply((Message) p, body, contentType);
				Transport.send(reply);
				logger.error("The email message has been rejected due to its size");
			}
			catch (MessagingException e) {
				logger.error("MessagingException caught during reply, drop the email", e);
			}
		}
		else { // email size within the limit
			boolean isDuplicate = false;
			// check for duplicate
			if (StringUtils.isNotBlank(msgBean.getSmtpMessageId())) {
				if (mailBoxVo.getIsCheckDuplicate()) {
					isDuplicate = inboxService.isMessageIdDuplicate(msgBean.getSmtpMessageId());
				}
			}
			else {
				logger.error("SMTP Message-id is blank or null, FROM: " + msgBean.getFromAsString());
			}
			// end of check
			if (isDuplicate) {
				logger.error("Duplicate Message received, messageId: " + msgBean.getSmtpMessageId());
				// issue an info_event alert
				if (mailBoxVo.getIsAlertDuplicate()) {
					// TODO
				}
				// write raw stream to logging file
				if (mailBoxVo.getIsLogDuplicate()) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					p.writeTo(baos);
					duplicateReport.info("<========== Message-id: " + msgBean.getSmtpMessageId()
							+ ", DateTime: " + (new Date()) + " ==========>");
					duplicateReport.info(baos.toString());
					logger.error("The duplicate Message has been written to report file");
				}
			}
			else { // persist to database
				int msgId = messageInboxBo.saveMessage(msgBean);
				logger.info("MessageBean saved to database, MessageInbox RowId: " + msgId);
			}
		}
		logger.info("Number of attachments: " + msgBean.getAttachCount());

		// message has been sent, delete it from mail box
		// keep the message if it's from notes
		if (!CarrierCode.READONLY.getValue().equals(mailBoxVo.getCarrierCode())) {
			((Message) p).setFlag(Flags.Flag.DELETED, true);
			// may throw MessageingException, stop MailReader to
			// prevent from producing duplicate messages
		}

		long time_spent = System.currentTimeMillis() - start_tms;
		logger.info("Msg from " + msgBean.getFromAsString() + " processed, milliseconds: "
				+ time_spent);
		
		return msgBean;
	}
}