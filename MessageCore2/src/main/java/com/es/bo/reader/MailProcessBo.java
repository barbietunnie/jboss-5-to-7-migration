package com.es.bo.reader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Transport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.inbox.MessageParserBo;
import com.es.bo.task.TaskSchedulerBo;
import com.es.core.util.EmailSender;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.preload.RuleNameEnum;
import com.es.exception.DataValidationException;
import com.es.exception.TemplateException;
import com.es.msgbean.JavaMailParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.comm.MailBoxVo;

/**
 * process email's handed over by MailReader class.
 */
@Component("mailProcessBo")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class MailProcessBo implements java.io.Serializable {
	private static final long serialVersionUID = -2192214375199179774L;
	static final Logger logger = Logger.getLogger(MailProcessBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static Logger duplicateReport = Logger.getLogger("message.report.duplicate");

	//volatile boolean keepRunning = true;
	private final int MAX_INBOUND_BODY_SIZE = 150 * 1024; // 150K
	private final int MAX_INBOUND_CMPT_SIZE = 1024 * 1024; // 1M

	final static String LF = System.getProperty("line.separator", "\n");

	@Autowired
	private MessageParserBo msgParserBo;
	@Autowired
	private TaskSchedulerBo taskBo;
	@Autowired
	private DuplicateCheckBo dupCheckBo;
	
	private MailBoxVo mboxVo;
	
	public MailProcessBo() {}

	/**
	 * process request
	 * 
	 * @param req - request object.
	 * @throws MessagingException if any error
	 * @throws TemplateException 
	 * @throws DataValidationException 
	 */
	public void process(MessageContext req) throws MessagingException,
			IOException, DataValidationException, TemplateException {
		logger.info("Entering process() method...");
		long startProc = System.currentTimeMillis();
		if (req != null && req.getMessages()!=null && req.getMailBoxVo()!=null) {
			Message[] msgs = req.getMessages();
			mboxVo = req.getMailBoxVo();
			// Just dump out the new messages and set the delete flags
			for (int i = 0; i < msgs.length; i++) {
				if (msgs[i] != null && !msgs[i].isSet(Flags.Flag.SEEN)
						&& !msgs[i].isSet(Flags.Flag.DELETED)) {
					long start = System.currentTimeMillis();
					logger.info("Processing message number[" + (i+1) +"]...");
					processPart(msgs[i], req);
					logger.info("Completed processing of message number["
							+ (i + 1) + "], time taken: "
							+ (System.currentTimeMillis() - start) + " ms");
				}
				// release the instance for GC, not working w/pop3
				// msgs[i]=null;
			}
			logger.info("Exiting process() method, time taken: "
					+ (System.currentTimeMillis() - startProc) + " ms");
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
	 * @throws TemplateException 
	 * @throws DataValidationException 
	 */
	void processPart(Part p, MessageContext ctx) throws IOException, MessagingException, 
			DataValidationException, TemplateException {
		long start_tms = System.currentTimeMillis();
		
		// parse the MimeMessage to MessageBean
		MessageBean msgBean = JavaMailParser.processPart(p, mboxVo.getToAddrDomain());
		msgBean.setIsReceived(true);
		
		// mailbox carrierCode
		msgBean.setCarrierCode(CarrierCode.getByValue(mboxVo.getCarrierCode()));
		// internal mail only flag
		msgBean.setInternalOnly(CodeType.YES.getValue().equals(mboxVo.getInternalOnly()));
		// mailbox SSL flag
		msgBean.setUseSecureServer(CodeType.YES.getValue().equals(mboxVo.getUseSsl()));
		// MailBox Host Address
		msgBean.setMailboxHost(mboxVo.getHostName());
		// MailBox User Id
		msgBean.setMailboxUser(mboxVo.getUserId());
		// MailBox Name
		msgBean.setMailboxName(mboxVo.getDescription());
		// Folder Name
		msgBean.setFolderName(mboxVo.getFolderName());
		// to_plain_text indicator, default to "no"
		msgBean.setToPlainText(CodeType.YES.getValue().equals(mboxVo.getToPlainText()));
		
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
				Integer objSize = msgBean.getComponentsSize().get(i);
				if (objSize.intValue() > MAX_INBOUND_CMPT_SIZE) {
					isMsgSizeTooLarge = true;
					logger.warn("Message component(" + i + ") exceeded limit: "
							+ objSize.intValue());
					break;
				}
			}
		}
		
		if (isMsgSizeTooLarge) {
			msgBean.setRuleName(RuleNameEnum.SIZE_TOO_LARGE.getValue());
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
				if (CodeType.YES.getValue().equals(mboxVo.getCheckDuplicate())) {
					isDuplicate = dupCheckBo.isDuplicate(msgBean.getSmtpMessageId());
				}
			}
			else {
				logger.error("SMTP Message-id is blank or null, FROM: " + msgBean.getFromAsString());
			}
			// end of check
			if (isDuplicate) {
				String errMsg = "Duplicate Message received, messageId: " + msgBean.getSmtpMessageId();
				logger.error(errMsg);
				// issue an info_event alert
				if (CodeType.YES.getValue().equals(mboxVo.getAlertDuplicate())) {
					EmailSender.sendToUnchecked(errMsg, ExceptionUtils.getStackTrace(new Exception()));
				}
				// write raw stream to logging file
				if (CodeType.YES.getValue().equals(mboxVo.getLogDuplicate())) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					p.writeTo(baos);
					duplicateReport.info("<========== Message-id: " + msgBean.getSmtpMessageId()
							+ ", DateTime: " + (new Date()) + " ==========>");
					duplicateReport.info(baos.toString());
					logger.error("The duplicate Message has been written to report file");
				}
			}
			else { // parse the message for RuleName
				msgBean.setRuleName(msgParserBo.parse(msgBean));
				/* saveMessage() is handled by TackschedulerBo */
				MessageContext task_ctx = new MessageContext(msgBean);
				taskBo.scheduleTasks(task_ctx);
				ctx.getMsgIdList().addAll(task_ctx.getMsgIdList());
				ctx.getEmailAddrIdList().addAll(task_ctx.getEmailAddrIdList());
			}
		}
		logger.info("Number of attachments: " + msgBean.getAttachCount());

		// message has been processed, delete it from mail box
		// keep the message if it's from notes
		if (!CarrierCode.READONLY.getValue().equals(mboxVo.getCarrierCode())) {
			((Message) p).setFlag(Flags.Flag.DELETED, true);
			// may throw MessageingException, stop MailReader to
			// prevent from producing duplicate messages
			logger.info("Msg from " + msgBean.getFromAsString() + " has been marked for deletion.");
		}

		long time_spent = System.currentTimeMillis() - start_tms;
		logger.info("Msg from " + msgBean.getFromAsString() + " processed, milliseconds: "
				+ time_spent);
	}
}