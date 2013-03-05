package jpa.service.msgout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.EmailIdToken;
import jpa.constant.MsgStatusCode;
import jpa.constant.XHeaderName;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageBeanBuilder;
import jpa.message.MessageBeanUtil;
import jpa.message.MsgHeader;
import jpa.message.util.MsgIdCipher;
import jpa.model.ClientData;
import jpa.model.EmailAddress;
import jpa.model.message.MessageInbox;
import jpa.model.message.MessageStream;
import jpa.service.ClientDataService;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageDeliveryStatusService;
import jpa.service.message.MessageInboxService;
import jpa.service.message.MessageParser;
import jpa.service.message.MessageStreamService;
import jpa.service.msgin.MessageInboxBo;
import jpa.util.EmailAddrUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * process queue messages handed over by QueueListener.
 * 
 * @author Administrator
 */
public abstract class MailSenderBase {
	protected static final Logger logger = Logger.getLogger(MailSenderBase.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	protected boolean debugSession = false;
	protected ClientData clientVo = null;

	@Autowired
	protected MessageInboxBo msgInboxBo;
	@Autowired
	protected MessageInboxService msgInboxDao;
	@Autowired
	protected MsgOutboxBo msgOutboxBo;
	@Autowired
	protected MessageDeliveryStatusService deliveryStatusDao;
	@Autowired
	protected EmailAddressService emailAddrDao;
	@Autowired
	protected MessageStreamService msgStreamDao;
	@Autowired
	protected ClientDataService clientService;
	@Autowired
	private MessageParser parser;

	//private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static final String LF = System.getProperty("line.separator", "\n");

	public MailSenderBase() {
		if (isDebugEnabled)
			logger.debug("Entering constructor...");
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
		// was the outgoing message rendered?
		if (msgBean.getRenderId() == null) {
			logger.warn("process() - Render Id is null, the message was not rendered");
		}
		// set rule name to SEND_MAIL
		msgBean.setRuleName(RuleNameEnum.SEND_MAIL.getValue());
		try {
			clientVo = clientService.getByClientId(msgBean.getClientId());
		}
		catch (NoResultException e) {
			throw new DataValidationException("ClientId (" + msgBean.getClientId() + ") not found.");
		}
		msgBean.setIsReceived(false); // out going message
		if (msgBean.getEmBedEmailId() == null) { // not provided by calling program
			// use system default
			msgBean.setEmBedEmailId(Boolean.valueOf(clientVo.isEmbedEmailId()));
		}
		msgInboxBo.saveMessage(msgBean);
		// check if VERP is enabled
		if (clientVo.isVerpEnabled()) {
			// set return path with VERP, msgBean.msgId must be valued.
			String emailId = EmailIdToken.XHDR_BEGIN + MsgIdCipher.encode(msgBean.getMsgId())
					+ EmailIdToken.XHDR_END;
			Address[] addrs = msgBean.getTo();
			if (addrs == null || addrs.length == 0 || addrs[0] == null) {
				throw new DataValidationException("TO address is not provided.");
			}
			String recipient = EmailAddrUtil.removeDisplayName(addrs[0].toString());
			if (StringUtils.isBlank(clientVo.getVerpInboxName())) {
				throw new DataValidationException("VERP inbox name is blank in ClientData table.");
			}
			String left = clientVo.getVerpInboxName() + "-" + emailId + "-"
					+ recipient.replaceAll("@", "=");
			String verpDomain = clientVo.getDomainName();
			if (StringUtils.isNotBlank(clientVo.getVerpSubDomain())) {
				verpDomain = clientVo.getVerpSubDomain() + "." + verpDomain;
			}
			msgBean.setReturnPath("<" + left + "@" + verpDomain + ">");
			// set List-Unsubscribe VERP header
			if (StringUtils.isNotBlank(msgBean.getMailingListId())) {
				if (StringUtils.isBlank(clientVo.getVerpRemoveInbox())) {
					throw new DataValidationException("VERP remove inbox is blank in ClientData table.");
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
			logger.error("SendFailedException caught: ", sfex);
			updtDlvrStatAndLoopback(msgBean, sfex, errors);
			if (errors.containsKey("validSent")) {
				sendDeliveryReport(msgBean);
			}
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
		String[] renderId = mimeMsg.getHeader(XHeaderName.RENDER_ID.getValue());
		if (renderId != null && renderId.length > 0) {
			String renderIdStr = renderId[0];
			try {
				msgBean.setRenderId(Integer.valueOf(renderIdStr));
			}
			catch (NumberFormatException e) {
				logger.error("addXHeadersToBean() - NumberFormatException caught from converting "
						+ XHeaderName.RENDER_ID.getValue() + ": " + renderIdStr);
			}
		}
		
		String[] msgRefId = mimeMsg.getHeader(XHeaderName.MSG_REF_ID.getValue());
		if (msgRefId != null && msgRefId.length > 0) {
			String msgRefIdStr = msgRefId[0];
			try {
				msgBean.setMsgRefId(Integer.valueOf(msgRefIdStr));
			}
			catch (NumberFormatException e) {
				logger.error("addXHeadersToBean() - NumberFormatException caught from converting "
						+ XHeaderName.MSG_REF_ID.getValue() + ": " + msgRefIdStr);
			}
		}
		
		boolean isSecure = false;
		// retrieve secure transport flag from X-Header
		String[] st = mimeMsg.getHeader(XHeaderName.USE_SECURE_SMTP.getValue());
		if (st != null && st.length > 0) {
			if (CodeType.YES.getValue().equals(st[0])) {
				isSecure = true;
			}
		}
		msgBean.setUseSecureServer(isSecure);
		
		//String[] ruleName = mimeMsg.getHeader(RULE_NAME);
		//if (ruleName != null && ruleName.length > 0) {
		//	msgBean.setRuleName(ruleName[0]);
		//}
		
		String[] overrideTestAddr = mimeMsg.getHeader(XHeaderName.OVERRIDE_TEST_ADDR.getValue());
		if (overrideTestAddr != null && overrideTestAddr.length > 0) {
			if (CodeType.YES.getValue().equalsIgnoreCase(overrideTestAddr[0]))
				msgBean.setOverrideTestAddr(true);
		}
		
		String[] saveRawStream = mimeMsg.getHeader(XHeaderName.SAVE_RAW_STREAM.getValue());
		if (saveRawStream != null && saveRawStream.length > 0) {
			if (CodeType.NO.getValue().equalsIgnoreCase(saveRawStream[0]))
				msgBean.setSaveMsgStream(false);
		}
		
		String[] embedEmailId = mimeMsg.getHeader(XHeaderName.EMBED_EMAILID.getValue());
		if (embedEmailId != null && embedEmailId.length > 0) {
			if (CodeType.NO.getValue().equalsIgnoreCase(embedEmailId[0]))
				msgBean.setEmBedEmailId(Boolean.valueOf(false));
			else if (CodeType.YES.getValue().equalsIgnoreCase(embedEmailId[0]))
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
	 * @param isOverrideTestAddr -
	 *            if true, do not convert original addresses.
	 * @throws MessagingException
	 */
	protected void rebuildAddresses(javax.mail.Message m, boolean isOverrideTestAddr)
			throws MessagingException {
		if (isDebugEnabled) {
			logger.debug("Entering rebuildAddresses method...");
		}
		// set TO address to Test Address if it's a test run
		if (clientVo.isUseTestAddr() && !isOverrideTestAddr) {
			if (isDebugEnabled) {
				logger.debug("rebuildAddresses() - Replace original TO: "
						+ EmailAddrUtil.emailAddrToString(m.getRecipients(javax.mail.Message.RecipientType.TO))
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
					if (StringUtils.isNotBlank(addr)) {
						displayName = EmailAddrUtil.removeDisplayName(addr);
						//displayName = StringUtil.replaceAll(displayName, "@", ".at.");
						toAddrIsLocal = addr.toLowerCase().endsWith("@localhost");
					}
				}
			}
			if (!toAddrIsLocal) { // DO NOT override if TO address is local
				if (displayName == null) {
					m.setRecipients(RecipientType.TO, InternetAddress.parse(clientVo.getTestToAddr()));
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
		if (clientVo.isUseTestAddr() && !isOverrideTestAddr
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
		if (clientVo.isUseTestAddr() && !isOverrideTestAddr
				&& (m.getReplyTo() == null || m.getReplyTo().length == 0)) {
			if (StringUtils.isNotBlank(clientVo.getTestReplytoAddr())) {
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
	protected void saveMsgStream(javax.mail.Message msg, int msgId) throws MessagingException,
			IOException {
		if (isDebugEnabled)
			logger.debug("saveMsgStream() - msgId: " + msgId);
		MessageInbox msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			logger.error("saveMsgStream() - MsgInbox record not found by MsgId: " + msgId);
			return;
		}
		MessageStream msgStreamVo = new MessageStream();
		msgStreamVo.setMessageInbox(msgInboxVo);
		Address[] fromAddrs = msg.getFrom();
		if (fromAddrs != null && fromAddrs.length > 0) {
			EmailAddress emailAddrVo = emailAddrDao.findSertAddress(fromAddrs[0].toString());
			msgStreamVo.setFromAddrRowId(Integer.valueOf(emailAddrVo.getRowId()));
		}
		Address[] toAddrs = msg.getRecipients(RecipientType.TO);
		if (toAddrs != null && toAddrs.length > 0) {
			EmailAddress emailAddrVo = emailAddrDao.findSertAddress(toAddrs[0].toString());
			msgStreamVo.setToAddrRowId(Integer.valueOf(emailAddrVo.getRowId()));
		}
		msgStreamVo.setMsgSubject(msg.getSubject());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		msg.writeTo(baos);
		msgStreamVo.setMsgStream(baos.toByteArray());
		msgStreamDao.insert(msgStreamVo);
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
		
		MessageInbox msgInboxVo = msgInboxDao.getByPrimaryKey(msgBean.getMsgId());
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
			if (failedAddr == null || StringUtils.isBlank(failedAddr.toString())) {
				continue;
			}
			try {
				// loop back unsent messages as soft bounce
				loopbackMail(msgBean, exp.getMessage(), failedAddr, reason);
			}
			catch (DataValidationException e) {
				logger.error("DataValidationException caught, ignore.", e);
			}
		}
	}
	
	protected void invalid(MessageBean msgBean, SendFailedException exp, Address[] invalid)
			throws MessagingException, IOException {
		
		MessageInbox msgInboxVo = msgInboxDao.getByPrimaryKey(msgBean.getMsgId());
		if (msgInboxVo == null) {
			logger.error("invalid() - MsgInbox record not found for MsgId: " + msgBean.getMsgId());
			return;
		}
		String reason = "5.1.1 Invalid Destination Mailbox Address: " + exp.toString();
		for (int i = 0; i < invalid.length; i++) {
			logger.info("invalid() -  Addr [" + i + "]: " + invalid[i]
					+ ", insert into DeliveryStatus msgId=" + msgBean.getMsgId());
			Address failedAddr = invalid[i];
			if (failedAddr == null || StringUtils.isBlank(failedAddr.toString())) {
				continue;
			}
			try {
				// loop back invalid messages as hard bounce
				loopbackMail(msgBean, exp.getMessage(), failedAddr, reason);
			}
			catch (DataValidationException e) {
				logger.error("DataValidationException caught, ignore.", e);
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
		if (CarrierCode.SMTPMAIL.equals(m.getCarrierCode())) {
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
	protected int updateMsgStatus(int msgId) throws MessagingException {
		// update MsgInbox status (to delivered)
		MessageInbox msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			logger.error("updateMsgStatus() - MsgInbox record not found for MsgId: " + msgId);
			return 0;
		}
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		msgInboxVo.setStatusId(MsgStatusCode.DELIVERED.getValue());
		msgInboxVo.setDeliveryTime(ts);
		msgInboxVo.setUpdtTime(ts);
		msgInboxVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		msgInboxDao.update(msgInboxVo);
		return 1;
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
			String reason) throws MessagingException, IOException, DataValidationException {
		
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
		// use MessageParser to invoke rule engine
		parser.parse(loopBackBean);
		// use TaskScheduler to schedule tasks
//		TaskScheduler scheduler = new TaskScheduler(factory); TODO
//		scheduler.scheduleTasks(loopBackBean);
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

}