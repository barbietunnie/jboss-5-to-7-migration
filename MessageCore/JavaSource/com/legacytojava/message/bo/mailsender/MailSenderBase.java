package com.legacytojava.message.bo.mailsender;

import static com.legacytojava.message.constant.Constants.DEFAULT_USER_ID;
import static com.legacytojava.message.constant.Constants.NO;
import static com.legacytojava.message.constant.Constants.YES;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import com.legacytojava.jbatch.smtp.SmtpException;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanBuilder;
import com.legacytojava.message.bean.MessageBeanUtil;
import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.bo.TaskScheduler;
import com.legacytojava.message.bo.inbox.MessageParser;
import com.legacytojava.message.bo.inbox.MsgInboxBo;
import com.legacytojava.message.bo.outbox.MsgOutboxBo;
import com.legacytojava.message.constant.CarrierCode;
import com.legacytojava.message.constant.EmailIDToken;
import com.legacytojava.message.constant.MsgStatusCode;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.constant.XHeaderName;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.idtokens.MsgIdCipher;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.dao.inbox.MsgStreamDao;
import com.legacytojava.message.dao.outbox.DeliveryStatusDao;
import com.legacytojava.message.dao.outbox.MsgSequenceDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.ClientVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.MsgInboxVo;
import com.legacytojava.message.vo.outbox.MsgStreamVo;

/**
 * process queue messages handed over by QueueListener.
 * 
 * @author Administrator
 */
public abstract class MailSenderBase {
	protected static final Logger logger = Logger.getLogger(MailSenderBase.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	protected boolean debugSession = false;
	protected ClientVo clientVo = null;

	protected MsgInboxBo msgInboxBo = null;
	protected MsgInboxDao msgInboxDao = null;
	protected MsgOutboxBo msgOutboxBo = null;
	protected DeliveryStatusDao deliveryStatusDao = null;
	protected EmailAddrDao emailAddrDao = null;
	protected MsgStreamDao msgStreamDao = null;
	protected MsgSequenceDao msgSequenceDao = null;
	
	private AbstractApplicationContext factory;
	private MessageParser parser = null;

	//private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static final String LF = System.getProperty("line.separator", "\n");

	public MailSenderBase() {
		if (isDebugEnabled)
			logger.debug("Entering constructor...");
	}
	
	protected abstract AbstractApplicationContext loadFactory();

	protected void loadBosAndDaos() {
		factory = loadFactory();
		msgInboxBo = (MsgInboxBo) factory.getBean("msgInboxBo");
		msgOutboxBo = (MsgOutboxBo) factory.getBean("msgOutboxBo");
		msgInboxDao = (MsgInboxDao) factory.getBean("msgInboxDao");
		deliveryStatusDao = (DeliveryStatusDao) factory.getBean("deliveryStatusDao");
		emailAddrDao = (EmailAddrDao) factory.getBean("emailAddrDao");
		msgStreamDao = (MsgStreamDao) factory.getBean("msgStreamDao");
		msgSequenceDao = (MsgSequenceDao) factory.getBean("msgSequenceDao");
	}

	/**
	 * send a message off and update delivery status and message tables.
	 * 
	 * @param msgBean -
	 *            a MessageBean object
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SmtpException
	 * @throws InterruptedException
	 * @throws DataValidationException 
	 */
	public void process(MessageBean msgBean) throws MessagingException, IOException, SmtpException,
			InterruptedException, DataValidationException {

		if (msgBean == null) {
			throw new DataValidationException("Input MessageBean is null");
		}
		if (ClientUtil.isTrialPeriodEnded() && !ClientUtil.isProductKeyValid()) {
			try {
				Thread.sleep(1000); // delay for 1 second
			}
			catch (InterruptedException e) {}
		}
		// was the outgoing message rendered?
		if (msgBean.getRenderId() == null) {
			logger.warn("process() - Render Id is null, the message was not rendered");
		}
		// set rule name to SEND_MAIL
		msgBean.setRuleName(RuleNameType.SEND_MAIL.toString());
		clientVo = ClientUtil.getClientVo(msgBean.getClientId());
		msgBean.setIsReceived(false); // out going message
		if (msgBean.getEmBedEmailId() == null) { // not provided by calling program
			// use system default
			msgBean.setEmBedEmailId(Boolean.valueOf(clientVo.getIsEmbedEmailId()));
		}
		getMsgInboxBo().saveMessage(msgBean);
		// check if VERP is enabled
		if (clientVo.getIsVerpAddressEnabled()) {
			// set return path with VERP, msgBean.msgId must be valued.
			String emailId = EmailIDToken.XHDR_BEGIN + MsgIdCipher.encode(msgBean.getMsgId())
					+ EmailIDToken.XHDR_END;
			Address[] addrs = msgBean.getTo();
			if (addrs == null || addrs.length == 0 || addrs[0] == null) {
				throw new DataValidationException("TO address is not provided.");
			}
			String recipient = EmailAddrUtil.removeDisplayName(addrs[0].toString());
			if (StringUtil.isEmpty(clientVo.getVerpInboxName())) {
				throw new DataValidationException("VERP inbox name is blank in Client table.");
			}
			String left = clientVo.getVerpInboxName() + "-" + emailId + "-"
					+ recipient.replaceAll("@", "=");
			String verpDomain = clientVo.getDomainName();
			if (!StringUtil.isEmpty(clientVo.getVerpSubDomain())) {
				verpDomain = clientVo.getVerpSubDomain() + "." + verpDomain;
			}
			msgBean.setReturnPath("<" + left + "@" + verpDomain + ">");
			// set List-Unsubscribe VERP header
			if (!StringUtil.isEmpty(msgBean.getMailingListId())) {
				if (StringUtil.isEmpty(clientVo.getVerpRemoveInbox())) {
					throw new DataValidationException("VERP remove inbox is blank in Client table.");
				}
				left = clientVo.getVerpRemoveInbox() + "-" + msgBean.getMailingListId() + "-"
						+ recipient.replaceAll("@", "=");
				MsgHeader header = new MsgHeader();
				header.setName("List-Unsubscribe");
				header.setValue("<mailto:" + left + "@" + verpDomain + ">");
				msgBean.getHeaders().add(header);
			}
		}
		// build a MimeMessage from the MessageBean
		javax.mail.Message mimeMsg = MessageBeanUtil.createMimeMessage(msgBean);
		// override mimeMessage.TO with test address if this is a test run
		rebuildAddresses(mimeMsg, msgBean.getOverrideTestAddr());
		// send the message off
		Map<String, Address[]> errors = new HashMap<String, Address[]>();
		try {
			sendMail(mimeMsg, msgBean.isUseSecureServer(), errors);
			/* Update message delivery status */
			updateMsgStatus(msgBean.getMsgId());
		}
		catch (SendFailedException sfex) {
			// failed to send the message to certain recipients
			logger.error("SendFailedException caught: " + sfex);
			updtDlvrStatAndLoopback(msgBean, sfex, errors);
			if (errors.containsKey("validSent"))
				sendDeliveryReport(msgBean);
		}
		// save message raw stream to database
		if (msgBean.getSaveMsgStream()) {
			saveMsgStream(mimeMsg, msgBean.getMsgId());
		}
	}
	
	/**
	 * send a message off and update delivery status and message tables.
	 * 
	 * @param msgStream -
	 *            an email raw stream
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SmtpException
	 * @throws InterruptedException
	 * @throws DataValidationException 
	 */
	public void process(byte[] msgStream) throws MessagingException, IOException, SmtpException,
			InterruptedException, DataValidationException {

		javax.mail.Message mimeMsg = MessageBeanUtil.createMimeMessage(msgStream);
		
		/*
		 * In order to save the message to database, a MessageBean is required
		 * by saveMessage method. So first we convert the JavaMail message to a
		 * MessageBean and save it. Second we convert the MessageBean back to
		 * JavaMail message again and send it off (as an Email_Id may have been
		 * added to the message body and X-header).
		 */
		// convert the JavaMail message to a MessageBean
		MessageBean msgBean = MessageBeanBuilder.processPart(mimeMsg, null);
		// convert extra mimeMessage headers
		addXHeadersToBean(msgBean, mimeMsg);
		// save the message and send it off
		process(msgBean);
	}
	
	private void addXHeadersToBean(MessageBean msgBean, javax.mail.Message mimeMsg)
			throws MessagingException {
		String[] renderId = mimeMsg.getHeader(XHeaderName.XHEADER_RENDER_ID);
		if (renderId != null && renderId.length > 0) {
			String renderIdStr = renderId[0];
			try {
				msgBean.setRenderId(Long.valueOf(renderIdStr));
			}
			catch (NumberFormatException e) {
				logger.error("addXHeadersToBean() - NumberFormatException caught from converting "
						+ XHeaderName.XHEADER_RENDER_ID + ": " + renderIdStr);
			}
		}
		
		String[] msgRefId = mimeMsg.getHeader(XHeaderName.XHEADER_MSG_REF_ID);
		if (msgRefId != null && msgRefId.length > 0) {
			String msgRefIdStr = msgRefId[0];
			try {
				msgBean.setMsgRefId(Long.valueOf(msgRefIdStr));
			}
			catch (NumberFormatException e) {
				logger.error("addXHeadersToBean() - NumberFormatException caught from converting "
						+ XHeaderName.XHEADER_MSG_REF_ID + ": " + msgRefIdStr);
			}
		}
		
		boolean isSecure = false;
		// retrieve secure transport flag from X-Header
		String[] st = mimeMsg.getHeader(XHeaderName.XHEADER_USE_SECURE_SMTP);
		if (st != null && st.length > 0) {
			if (YES.equals(st[0])) {
				isSecure = true;
			}
		}
		msgBean.setUseSecureServer(isSecure);
		
		//String[] ruleName = mimeMsg.getHeader(XHEADER_RULE_NAME);
		//if (ruleName != null && ruleName.length > 0) {
		//	msgBean.setRuleName(ruleName[0]);
		//}
		
		String[] overrideTestAddr = mimeMsg.getHeader(XHeaderName.XHEADER_OVERRIDE_TEST_ADDR);
		if (overrideTestAddr != null && overrideTestAddr.length > 0) {
			if (YES.equalsIgnoreCase(overrideTestAddr[0]))
				msgBean.setOverrideTestAddr(true);
		}
		
		String[] saveRawStream = mimeMsg.getHeader(XHeaderName.XHEADER_SAVE_RAW_STREAM);
		if (saveRawStream != null && saveRawStream.length > 0) {
			if (NO.equalsIgnoreCase(saveRawStream[0]))
				msgBean.setSaveMsgStream(false);
		}
		
		String[] embedEmailId = mimeMsg.getHeader(XHeaderName.XHEADER_EMBED_EMAILID);
		if (embedEmailId != null && embedEmailId.length > 0) {
			if (NO.equalsIgnoreCase(embedEmailId[0]))
				msgBean.setEmBedEmailId(Boolean.valueOf(false));
			else if (YES.equalsIgnoreCase(embedEmailId[0]))
				msgBean.setEmBedEmailId(Boolean.valueOf(true));
		}
	}
	
	/**
	 * Convert FROM and TO addresses to testing addresses if needed. When
	 * original addresses are converted to testing addresses, the original
	 * addresses are not completed lost, they are shown as "Display Name".
	 * 
	 * Exceptions: if the TO address is a local address (xxxxxxxxx@localhost),
	 * it is not converted to testing TO address regardless.
	 * 
	 * @param m -
	 *            a javax.mail.Message object
	 * @param overrideTestAddr -
	 *            if true, do not convert original addresses.
	 * @throws MessagingException
	 */
	protected void rebuildAddresses(javax.mail.Message m, boolean overrideTestAddr)
			throws MessagingException {
		if (isDebugEnabled) {
			logger.debug("Entering rebuildAddresses method...");
		}
		// set TO address to Test Address if it's a test run
		if (clientVo.getUseTestAddress() && !overrideTestAddr) {
			if (isDebugEnabled) {
				logger.debug("rebuildAddresses() - Replace original TO: "
						+ EmailAddrUtil.emailAddrToString(m
								.getRecipients(javax.mail.Message.RecipientType.TO))
						+ ", with testing address: " + clientVo.getTestToAddr());
			}
			boolean toAddrIsLocal = false;
			String displayName = null;
			// use the original address as Display Name 
			Address[] to_addrs = m.getRecipients(javax.mail.Message.RecipientType.TO);
			if (to_addrs != null && to_addrs.length > 0) {
				Address to_addr = to_addrs[0];
				if (to_addr != null) {
					String addr = to_addr.toString();
					if (!StringUtil.isEmpty(addr)) {
						displayName = EmailAddrUtil.removeDisplayName(addr);
						//displayName = StringUtil.replaceAll(displayName, "@", ".at.");
						toAddrIsLocal = addr.toLowerCase().endsWith("@localhost");
					}
				}
			}
			if (!toAddrIsLocal) { // DO NOT override if TO address is local
				if (displayName == null) {
					m.setRecipients(RecipientType.TO, InternetAddress.parse(clientVo
							.getTestToAddr()));
				}
				else {
					m.setRecipients(RecipientType.TO, InternetAddress.parse("\""
								+ displayName + "\" <"
								+ EmailAddrUtil.removeDisplayName(clientVo.getTestToAddr()) + ">"));
				}
			}
		}
		// validate TO address
		if (m.getRecipients(RecipientType.TO) == null
				|| m.getRecipients(RecipientType.TO).length == 0) {
			throw new AddressException("TO address is blank!");
		}
		// Set From address to Test Address if it's a test run and not provided
		if (clientVo.getUseTestAddress() && !overrideTestAddr
				&& (m.getFrom() == null || m.getFrom().length == 0)) {
			if (isDebugEnabled) {
				logger.debug("rebuildAddresses() - Original From is missing, use testing address: "
						+  clientVo.getTestFromAddr());
			}
			if (EmailAddrUtil.hasDisplayName(clientVo.getTestFromAddr())) {
				m.setFrom(InternetAddress.parse(clientVo.getTestFromAddr())[0]);
			}
			else {
				m.setFrom(InternetAddress.parse("\"MailSender\" <" + clientVo.getTestFromAddr()
						+ ">")[0]);
			}
		}
		// validate FROM address
		if (m.getFrom() == null || m.getFrom().length == 0) { // just for safety
			throw new AddressException("FROM address is blank!");
		}
		// set ReplyTo address to Test Address if it's a test run and not provided
		if (clientVo.getUseTestAddress() && !overrideTestAddr
				&& (m.getReplyTo() == null || m.getReplyTo().length == 0)) {
			if (!StringUtil.isEmpty(clientVo.getTestReplytoAddr())) {
				if (EmailAddrUtil.hasDisplayName(clientVo.getTestReplytoAddr())) {
					m.setReplyTo(InternetAddress.parse(clientVo.getTestReplytoAddr()));
				}
				else {
					m.setReplyTo(InternetAddress.parse("\"MailSender Reply\" " + "<"
							+ clientVo.getTestReplytoAddr() + ">"));
				}
			}
		}
	}

	/**
	 * Save a JavaMail message in a raw stream format into database.
	 * 
	 * @param msg -
	 *            a JavaMail message object
	 * @param msgId -
	 *            primary key of the database table.
	 * @throws MessagingException
	 * @throws IOException
	 */
	protected void saveMsgStream(javax.mail.Message msg, long msgId) throws MessagingException,
			IOException {
		if (isDebugEnabled)
			logger.debug("saveMsgStream() - msgId: " + msgId);
		MsgInboxVo msgInboxVo = getMsgInboxDao().getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			logger.error("saveMsgStream() - MsgInbox record not found by MsgId: " + msgId);
			return;
		}
		MsgStreamVo msgStreamVo = new MsgStreamVo();
		msgStreamVo.setMsgId(msgId);
		Address[] fromAddrs = msg.getFrom();
		if (fromAddrs != null && fromAddrs.length > 0) {
			EmailAddrVo emailAddrVo = getEmailAddrDao().findByAddress(fromAddrs[0].toString());
			msgStreamVo.setFromAddrId(Long.valueOf(emailAddrVo.getEmailAddrId()));
		}
		Address[] toAddrs = msg.getRecipients(RecipientType.TO);
		if (toAddrs != null && toAddrs.length > 0) {
			EmailAddrVo emailAddrVo = getEmailAddrDao().findByAddress(toAddrs[0].toString());
			msgStreamVo.setToAddrId(Long.valueOf(emailAddrVo.getEmailAddrId()));
		}
		msgStreamVo.setMsgSubject(msg.getSubject());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		msg.writeTo(baos);
		msgStreamVo.setMsgStream(baos.toByteArray());
		getMsgStreamDao().insert(msgStreamVo);
	}
	
	/**
	 * update delivery status with delivery error, send an error message back to
	 * system via email
	 * 
	 * @param msgBean -
	 *            message bean
	 * @param exp -
	 *            exception
	 * @param errors -
	 *            error map
	 * @throws InterruptedException
	 * @throws SmtpException
	 * @throws IOException
	 * @throws MessagingException
	 */
	public void updtDlvrStatAndLoopback(MessageBean msgBean, SendFailedException exp,
			Map<String, ?> errors) throws MessagingException, IOException {
		if (errors.get("validUnsent") != null) {
			Address[] validUnsent = (Address[]) errors.get("validUnsent");
			validUnsent(msgBean, exp, validUnsent);
		}
		if (errors.get("invalid") != null) {
			Address[] invalid = (Address[]) errors.get("invalid");
			invalid(msgBean, exp, invalid);
		}
	}

	protected void validUnsent(MessageBean msgBean, SendFailedException exp, Address[] validUnsent)
			throws MessagingException, IOException {
		
		MsgInboxVo msgInboxVo = getMsgInboxDao().getByPrimaryKey(msgBean.getMsgId());
		if (msgInboxVo == null) {
			logger.error("validUnsent() - MsgInbox record not found for MsgId: "
					+ msgBean.getMsgId());
			return;
		}
		String reason = "4.1.0 Mail unsent to the address due to following error: "
				+ exp.toString();
		for (int i = 0; i < validUnsent.length; i++) {
			logger.info("validUnsent() - Addr [" + i + "]: " + validUnsent[i]
					+ ", insert into DeliveryStatus msgId=" + msgBean.getMsgId());
			Address failedAddr = validUnsent[i];
			if (failedAddr == null || StringUtil.isEmpty(failedAddr.toString())) {
				continue;
			}
			try {
				// loop back unsent messages as soft bounce
				loopbackMail(msgBean, exp.getMessage(), failedAddr, reason);
			}
			catch (DataValidationException e) {
				logger.error("DataValidationException caught, ignore.", e);
			}
			catch (JMSException e) {
				logger.error("JMSException caught, ignored", e);
			}
		}
	}
	
	protected void invalid(MessageBean msgBean, SendFailedException exp, Address[] invalid)
			throws MessagingException, IOException {
		
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgBean.getMsgId());
		if (msgInboxVo == null) {
			logger.error("invalid() - MsgInbox record not found for MsgId: " + msgBean.getMsgId());
			return;
		}
		String reason = "5.1.1 Invalid Destination Mailbox Address: " + exp.toString();
		for (int i = 0; i < invalid.length; i++) {
			logger.info("invalid() -  Addr [" + i + "]: " + invalid[i]
					+ ", insert into DeliveryStatus msgId=" + msgBean.getMsgId());
			Address failedAddr = invalid[i];
			if (failedAddr == null || StringUtil.isEmpty(failedAddr.toString())) {
				continue;
			}
			try {
				// loop back invalid messages as hard bounce
				loopbackMail(msgBean, exp.getMessage(), failedAddr, reason);
			}
			catch (DataValidationException e) {
				logger.error("DataValidationException caught, ignore.", e);
			}
			catch (JMSException e) {
				logger.error("JMSException caught, ignored", e);
			}
		}
	}
	
	/**
	 * send delivery report to caller
	 * 
	 * @param m -
	 *            MessageBean
	 * @return number of email's that were sent
	 * @throws MessagingException
	 */
	public int sendDeliveryReport(MessageBean m) throws MessagingException {
		int rspCount = 0;
		if (CarrierCode.SMTPMAIL.equalsIgnoreCase(m.getCarrierCode())) {
			if (m.isInternalOnly()) {
				rspCount = updateMsgStatus(m.getMsgId());
			}
			else {
				rspCount = updateMsgStatus(m.getMsgId());
			}
		}
		else {
			throw new MessagingException("Invalid Carrier Code: " + m.getCarrierCode());
		}
		return rspCount;
	}

	/**
	 * update delivery status to MsgInbox.
	 * 
	 * @param msgId -
	 *            MessageId
	 * @return number records updated
	 * @throws MessagingException 
	 */
	protected int updateMsgStatus(long msgId) throws MessagingException {
		// update MsgInbox status (to delivered)
		MsgInboxVo msgInboxVo = getMsgInboxDao().getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			logger.error("updateMsgStatus() - MsgInbox record not found for MsgId: " + msgId);
			return 0;
		}
		Timestamp ts = new Timestamp(new java.util.Date().getTime());
		msgInboxVo.setStatusId(MsgStatusCode.DELIVERED);
		msgInboxVo.setDeliveryTime(ts);
		msgInboxVo.setUpdtTime(ts);
		msgInboxVo.setUpdtUserId(DEFAULT_USER_ID);
		int rowsUpdated = getMsgInboxDao().update(msgInboxVo);
		return rowsUpdated;
	}

	/**
	 * loop error report back to rule engine for further processing.
	 * @param msgBean -
	 *            message bean
	 * @param errmsg -
	 *            error text
	 * @param reason -
	 *            DSN reason code
	 * @throws IOException
	 * @throws MessagingException
	 * @throws DataValidationException 
	 * @throws JMSException 
	 */
	protected void loopbackMail(MessageBean msgBean, String errmsg, Address failedAddr,
			String reason) throws MessagingException, IOException, DataValidationException,
			JMSException {
		
		logger.info("Entering LoopbackMail method, error message: " + errmsg);
		// generate delivery failure report
		// attach the original header lines to the message body
		String reportLine1 = "The delivery of following message failed due to: " + LF;
		String reportLine2 = reason + LF;
		String reportLine3 = errmsg + ": " + failedAddr.toString() + LF;
		
		String loopbackText = reportLine1 + reportLine2 + reportLine3 + LF + LF;
		
		// assign loop-back address, for reference only, not used to deliver the message.
		msgBean.setTo(InternetAddress.parse("loopback@localhost"));
		logger.info("loopbackMail() - Undelivered message has been routed to "
				+ "loopback@localhost");
		Message msg = MessageBeanUtil.createMimeMessage(msgBean, failedAddr, loopbackText);
		MessageBean loopBackBean = MessageBeanBuilder.processPart(msg, null);
		loopBackBean.setMsgRefId(msgBean.getMsgId());
		loopBackBean.setIsReceived(true);
		if (isDebugEnabled) {
			logger.debug("loopbackMail() - The loopback MessageBean:" + LF + "<----" + LF
					+ loopBackBean + LF + "---->");
		}
		// use MessageProcessorBo to invoke rule engine
		getMessageParser().parse(loopBackBean);
		// use TaskScheduler to schedule tasks
		TaskScheduler scheduler = new TaskScheduler(factory);
		scheduler.scheduleTasks(loopBackBean);
	}

	/**
	 * send a message via a SMTP server. to be implemented by sub-class.
	 * 
	 * @param msg -
	 *            a JavaMail Message object
	 * @param isSecure -
	 *            true to use secure SMTP server
	 * @param errors -
	 *            any errors from the SMTP server
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SmtpException
	 * @throws InterruptedException
	 */
	public abstract void sendMail(Message msg, boolean isSecure, Map<String, Address[]> errors)
		throws MessagingException, IOException, SmtpException, InterruptedException;

	/**
	 * send the email off via unsecured SMTP server. to be implemented by
	 * sub-class.
	 * 
	 * @param msg -
	 *            a JavaMail message object
	 * @throws InterruptedException
	 * @throws SmtpException
	 * @throws MessagingException
	 */
	public abstract void sendMail(Message msg, Map<String, Address[]> errors)
			throws MessagingException, SmtpException, InterruptedException;

	public DeliveryStatusDao getDeliveryStatusDao() {
		return deliveryStatusDao;
	}

	public void setDeliveryStatusDao(DeliveryStatusDao deliveryStatusDao) {
		this.deliveryStatusDao = deliveryStatusDao;
	}

	public MsgInboxBo getMsgInboxBo() {
		return msgInboxBo;
	}

	public void setMsgInboxBo(MsgInboxBo msgInboxBo) {
		this.msgInboxBo = msgInboxBo;
	}

	public MsgInboxDao getMsgInboxDao() {
		return msgInboxDao;
	}

	public void setMsgInboxDao(MsgInboxDao msgInboxDao) {
		this.msgInboxDao = msgInboxDao;
	}

	public EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}

	public void setEmailAddrDao(EmailAddrDao emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
	}

	public MsgStreamDao getMsgStreamDao() {
		return msgStreamDao;
	}

	public void setMsgOBStreamDao(MsgStreamDao msgStreamDao) {
		this.msgStreamDao = msgStreamDao;
	}

	public MsgOutboxBo getMsgOutboxBo() {
		return msgOutboxBo;
	}

	public void setMsgOutboxBo(MsgOutboxBo msgOutboxBo) {
		this.msgOutboxBo = msgOutboxBo;
	}

	public MsgSequenceDao getMsgSequenceDao() {
		return msgSequenceDao;
	}

	public void setMsgSequenceDao(MsgSequenceDao msgSequenceDao) {
		this.msgSequenceDao = msgSequenceDao;
	}

	public MessageParser getMessageParser() {
		if (parser== null) {
			parser = (MessageParser) factory.getBean("messageParser");
		}
		return parser;
	}
}