package jpa.service.msgin;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.mail.Address;
import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.message.BodypartBean;
import jpa.message.BodypartUtil;
import jpa.message.MessageBean;
import jpa.message.MessageBeanBuilder;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageBodyBuilder;
import jpa.message.MessageNode;
import jpa.message.MsgHeader;
import jpa.model.ClientData;
import jpa.model.CustomerData;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.message.MessageActionLog;
import jpa.model.message.MessageActionLogPK;
import jpa.model.message.MessageAddress;
import jpa.model.message.MessageAttachment;
import jpa.model.message.MessageAttachmentPK;
import jpa.model.message.MessageClickCount;
import jpa.model.message.MessageHeader;
import jpa.model.message.MessageHeaderPK;
import jpa.model.message.MessageInbox;
import jpa.model.message.MessageRfcField;
import jpa.model.message.MessageRfcFieldPK;
import jpa.model.message.MessageStream;
import jpa.model.rule.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.CustomerDataService;
import jpa.service.EmailAddressService;
import jpa.service.MailingListService;
import jpa.service.message.MessageActionLogService;
import jpa.service.message.MessageAddressService;
import jpa.service.message.MessageAttachmentService;
import jpa.service.message.MessageClickCountService;
import jpa.service.message.MessageDeliveryStatusService;
import jpa.service.message.MessageHeaderService;
import jpa.service.message.MessageInboxService;
import jpa.service.message.MessageRenderedService;
import jpa.service.message.MessageRfcFieldService;
import jpa.service.message.MessageStreamService;
import jpa.service.rule.RuleLogicService;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * save email data and properties into database.
 */
@Component("messageInboxBo")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageInboxBo {
	static final Logger logger = Logger.getLogger(MessageInboxBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageInboxService msgInboxDao;
	@Autowired
	private MessageAttachmentService attachmentsDao;
	@Autowired
	private MessageAddressService msgAddrsDao;
	@Autowired
	private MessageHeaderService msgHeadersDao;
	@Autowired
	private MessageRfcFieldService rfcFieldsDao;
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private MessageStreamService msgStreamDao;
	@Autowired
	private MessageDeliveryStatusService deliveryStatusDao;
	@Autowired
	private MessageRenderedService msgRenderedDao;
	@Autowired
	private MessageActionLogService msgActionLogsDao;
	@Autowired
	private MessageClickCountService msgClickCountsDao;
	@Autowired
	private ClientDataService clientService;
	@Autowired
	private CustomerDataService custService;
	@Autowired
	private RuleLogicService logicService;
	@Autowired
	private MailingListService mlistService;

	static final String LF = System.getProperty("line.separator", "\n");

	/**
	 * save an email to database.
	 * 
	 * @param msgBean
	 *            MessageBean instance containing email properties
	 * @return primary key (=msgId) of the record inserted
	 * @throws DataValidationException 
	 * @throws SQLException
	 *             if SQL error occurred
	 */
	public int saveMessage(MessageBean msgBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering saveMessage() method..." + LF + msgBean);
		}
		if (msgBean == null) {
			throw new DataValidationException("Input MessageBean is null");
		}
		if (msgBean.getRuleName() == null) {
			throw new DataValidationException("MessageBean.getRuleName() returns a null");
		}
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());

		MessageInbox msgVo = new MessageInbox();
		msgVo.setReferringMessageRowId(msgBean.getMsgRefId());
		if (msgBean.getCarrierCode() == null) {
			logger.warn("saveMessage() - carrierCode field is null, set to S (SMTP)");
			msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		}
		msgVo.setCarrierCode(msgBean.getCarrierCode().getValue());
		msgVo.setMsgSubject(StringUtils.left(msgBean.getSubject(),255));
		msgVo.setMsgPriority(MessageBeanUtil.getMsgPriority(msgBean.getPriority()));
		Timestamp ts = msgBean.getSendDate() == null ? updtTime
				: new Timestamp(msgBean.getSendDate().getTime());
		msgVo.setReceivedTime(ts);
		
		Integer fromAddrId = getEmailAddrId(msgBean.getFrom());
		msgVo.setFromAddrRowId(fromAddrId);
		Integer replyToAddrId = getEmailAddrId(msgBean.getReplyto());
		msgVo.setReplytoAddrRowId(replyToAddrId);
		Integer toAddrId = getEmailAddrId(msgBean.getTo());
		msgVo.setToAddrRowId(toAddrId);
		
		Calendar cal = Calendar.getInstance();
		if (msgBean.getPurgeAfter() != null) {
			cal.add(Calendar.MONTH, msgBean.getPurgeAfter());
		}
		else {
			cal.add(Calendar.MONTH, 12); // purge in 12 months - default
		}
		// set purge Date
		msgVo.setPurgeDate(new java.sql.Date(cal.getTimeInMillis()));

		// find LeadMsgId. Also find client id if it's from MailReader
		if (msgBean.getMsgRefId() != null) {
			try {
				MessageInbox origVo = msgInboxDao.getByPrimaryKey(msgBean.getMsgRefId());
				msgVo.setLeadMessageRowId(origVo.getLeadMessageRowId());
				if (msgBean.getIsReceived()) { // from MailReader
					// code has been moved to MessageParser.parse()
				}
			}
			catch (NoResultException e) {
				// could be deleted by User or purged by Purge routine
				logger.warn("saveMessage() - MsgInbox record not found by MsgRefId: "
						+ msgBean.getMsgRefId());
			}
		}
		// end LeadMsgId
		
		msgVo.setMsgContentType(StringUtils.left(msgBean.getContentType(),100));
		String contentType = msgBean.getBodyContentType();
		msgVo.setBodyContentType(StringUtils.left(contentType,50));
		if (msgBean.getIsReceived()) {
			/* from MailReader */
			msgVo.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
			msgVo.setStatusId(MsgStatusCode.OPENED.getValue());
			msgVo.setSmtpMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			String msgBody = msgBean.getBody();
			msgVo.setMsgBody(msgBody);
			msgVo.setMsgBodySize(msgBody == null ? 0 : msgBody.length());
			// update "Last Received" time
			if (msgVo.getFromAddrRowId() != null) {
				EmailAddress lastRcptVo = emailAddrDao.getByRowId(msgVo.getFromAddrRowId());
				long minutes = 10 * 60 * 1000; // 10 minutes
				if (lastRcptVo.getLastRcptTime() == null
						|| lastRcptVo.getLastRcptTime().getTime() < (updtTime.getTime() - minutes)) {
					emailAddrDao.updateLastRcptTime(msgVo.getFromAddrRowId());
				}
			}
		}
		else {
			/* from MailSender */
			msgVo.setMsgDirection(MsgDirectionCode.SENT.getValue());
			msgVo.setSmtpMessageId(null);
			msgVo.setDeliveryTime(null); // delivery time
			msgVo.setStatusId(MsgStatusCode.PENDING.getValue());
			if (msgBean.getRenderId() != null) {
				msgVo.setRenderId(msgBean.getRenderId());
				try {
					msgRenderedDao.getByPrimaryKey(msgBean.getRenderId());
					msgVo.setMessageRenderedRowId(msgBean.getRenderId());
				}
				catch (NoResultException e) {}
			}
			msgVo.setOverrideTestAddr(msgBean.getOverrideTestAddr());
			// check original message body's content type
			if (msgBean.getOriginalMail() != null) {
				String origContentType = msgBean.getOriginalMail().getBodyContentType();
				if (contentType.indexOf("html") < 0 && origContentType.indexOf("html") >= 0) {
					// reset content type to original's
					msgVo.setBodyContentType(StringUtils.left(origContentType,50));
				}
			}
			/* Rebuild the Message Body, generate Email_Id from MsgId */
			String msgBody = MessageBodyBuilder.getBody(msgBean);
			/* end of rebuild */
			msgVo.setMsgBody(msgBody);
			msgVo.setMsgBodySize(msgBody == null ? 0 : msgBody.length());
			BodypartBean bodyNode = msgBean.getBodyNode();
			// update MessageBean.body with Email_Id
			if (bodyNode == null) {
				logger.fatal("saveMessage() - Programming error: bodyNode is null");
				msgBean.setContentType(msgVo.getMsgContentType());
				msgBean.setBody(msgVo.getMsgBody());
			}
			else {
				bodyNode.setContentType(msgVo.getBodyContentType());
				bodyNode.setValue(msgVo.getMsgBody().getBytes());
			}
			// update "Last Sent" time
			if (msgVo.getToAddrRowId() != null) {
				emailAddrDao.updateLastSentTime(msgVo.getToAddrRowId());
			}
		}
		
		if (StringUtils.isNotBlank(msgBean.getClientId())) {
			try {
				ClientData client = clientService.getByClientId(msgBean.getClientId());
				msgVo.setClientDataRowId(client.getRowId());
			}
			catch (NoResultException e) {}
		}
		if (StringUtils.isNotBlank(msgBean.getCustId())) {
			try {
				CustomerData customer = custService.getByCustomerId(msgBean.getCustId());
				msgVo.setCustomerDataRowId(customer.getRowId());
			}
			catch (NoResultException e) {}
		}
		if (StringUtils.isNotBlank(msgBean.getRuleName())) {
			try {
				RuleLogic logic = logicService.getByRuleName(msgBean.getRuleName());
				msgVo.setRuleLogicRowId(logic.getRowId());
			}
			catch (NoResultException e) {}
		}
		
		msgVo.setUpdtTime(updtTime);
		msgVo.setUpdtUserId(StringUtils.left(Constants.DEFAULT_USER_ID,10));
		msgVo.setLockTime(null); // lock time
		msgVo.setLockId(null); // lock id
		
		// retrieve attachment count and size, and gathers delivery reports
		BodypartUtil.retrieveAttachments(msgBean);
		
		List<MessageNode> aNodes = msgBean.getAttachments();
		msgVo.setAttachmentCount(aNodes == null ? 0 : aNodes.size());
		int attachmentSize = 0;
		if (aNodes != null) {
			for (MessageNode mNode : aNodes) {
				BodypartBean aNode = mNode.getBodypartNode();
				int _size = aNode.getValue() == null ? 0 : aNode.getValue().length;
				attachmentSize += _size;
			}
		}
		msgVo.setAttachmentSize(attachmentSize);
		if (isDebugEnabled) {
			logger.debug("Message to insert" + LF + StringUtil.prettyPrint(msgVo));
		}
		msgInboxDao.insert(msgVo);
		
		msgBean.setMsgId(msgVo.getRowId());
		logger.info("saveMessage() - MsgId saved as: " + msgVo.getRowId() + ", From MailReader: "
				+ msgBean.getIsReceived());

		// insert click count record for Broadcasting e-mail
		if (RuleNameEnum.BROADCAST.getValue().equals(msgBean.getRuleName())) {
			MessageClickCount msgClickCountsVo = new MessageClickCount();
			msgClickCountsVo.setMessageInbox(msgVo);
			// msgBean.getMailingListId() should always returns a value. just for safety.
			if (StringUtils.isNotBlank(msgBean.getMailingListId())) {
				try {
					MailingList mlist = mlistService.getByListId(msgBean.getMailingListId());
					msgClickCountsVo.setMailingListRowId(mlist.getRowId());
				}
				catch (NoResultException e) {}
			}
			if (msgBean.getToCustomersOnly()) {
				msgClickCountsVo.setDeliveryType(MailingListDeliveryType.CUSTOMERS_ONLY.getValue());
			}
			else if (msgBean.getToProspectsOnly()) {
				msgClickCountsVo.setDeliveryType(MailingListDeliveryType.PROSPECTS_ONLY.getValue());
			}
			else {
				msgClickCountsVo.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
			}
			msgClickCountsVo.setSentCount(0);
			msgClickCountsVo.setClickCount(0);
			msgVo.setMessageClickCount(msgClickCountsVo);
		}
		
		// save message headers
		List<MsgHeader> headers = msgBean.getHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.size(); i++) {
				MsgHeader header = headers.get(i);
				MessageHeader msgHeadersVo= new MessageHeader();
				MessageHeaderPK pk = new MessageHeaderPK(msgVo,(i+1));
				msgHeadersVo.setMessageHeaderPK(pk);
				msgHeadersVo.setHeaderName(StringUtils.left(header.getName(),100));
				msgHeadersVo.setHeaderValue(header.getValue());
				msgVo.getMessageHeaderList().add(msgHeadersVo);
			}
		}

		// save attachments
		if (aNodes!=null && aNodes.size()>0) {
			for (int i=0; i<aNodes.size(); i++) {
				MessageNode mNode = aNodes.get(i);
				BodypartBean aNode = mNode.getBodypartNode();
				MessageAttachment attchVo = new MessageAttachment();
				MessageAttachmentPK pk = new MessageAttachmentPK(msgVo, mNode.getLevel(), (i+1));
				attchVo.setMessageAttachmentPK(pk);
				attchVo.setAttachmentName(StringUtils.left(aNode.getDescription(),100));
				attchVo.setAttachmentType(StringUtils.left(aNode.getContentType(),100));
				attchVo.setAttachmentDisp(StringUtils.left(aNode.getDisposition(),100));
				attchVo.setAttachmentValue(aNode.getValue());
				msgVo.getMessageAttachmentList().add(attchVo);
			}
		}
		
		// save RFC fields
		if (msgBean.getReport() != null) {
			MessageNode mNode = msgBean.getReport();
			BodypartBean aNode = mNode.getBodypartNode();
			MessageRfcField rfcFieldsVo = new MessageRfcField();
			MessageRfcFieldPK pk = new MessageRfcFieldPK(msgVo,StringUtils.left(aNode.getContentType(),50));
			rfcFieldsVo.setMessageRfcFieldPK(pk);
			rfcFieldsVo.setRfcStatus(StringUtils.left(msgBean.getDsnStatus(),30));
			rfcFieldsVo.setRfcAction(StringUtils.left(msgBean.getDsnAction(),30));
			if (StringUtils.isNotBlank(msgBean.getFinalRcpt())) {
				EmailAddress frcpt = emailAddrDao.findSertAddress(msgBean.getFinalRcpt());
				rfcFieldsVo.setFinalRcptAddrRowId(frcpt.getRowId());
			}
			rfcFieldsVo.setOriginalRecipient(StringUtils.left(msgBean.getOrigRcpt(),255));
			//rfcFieldsVo.setOriginalMsgSubject(StringUtils.left(msgBean.getOrigSubject(),255));
			//rfcFieldsVo.setMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			rfcFieldsVo.setDsnText(msgBean.getDsnText());
			rfcFieldsVo.setDsnRfc822(msgBean.getDiagnosticCode()); // TODO: revisit
			rfcFieldsVo.setDeliveryStatus(msgBean.getDsnDlvrStat());
			msgVo.getMessageRfcFieldList().add(rfcFieldsVo);
		}
		
		if (msgBean.getRfc822() != null) {
			MessageNode mNode = msgBean.getRfc822();
			BodypartBean aNode = mNode.getBodypartNode();
			MessageRfcField rfcFieldsVo = new MessageRfcField();
			MessageRfcFieldPK pk = new MessageRfcFieldPK(msgVo,StringUtils.left(aNode.getContentType(),50));
			rfcFieldsVo.setMessageRfcFieldPK(pk);
			//rfcFieldsVo.setRfcStatus(StringUtil.cut(msgBean.getDsnStatus(),30));
			//rfcFieldsVo.setRfcAction(StringUtil.cut(msgBean.getDsnAction(),30));
			if (StringUtils.isNotBlank(msgBean.getFinalRcpt())) {
				EmailAddress frcpt = emailAddrDao.findSertAddress(msgBean.getFinalRcpt());
				rfcFieldsVo.setFinalRcptAddrRowId(frcpt.getRowId());
			}
			//rfcFieldsVo.setOrigRcpt(StringUtil.cut(msgBean.getOrigRcpt(),255));
			rfcFieldsVo.setOriginalMsgSubject(StringUtils.left(msgBean.getOrigSubject(),255));
			rfcFieldsVo.setMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			rfcFieldsVo.setDsnText(msgBean.getDsnText());
			rfcFieldsVo.setDsnRfc822(msgBean.getDsnRfc822());
			//rfcFieldsVo.setDeliveryStatus(msgBean.getDsnDlvrStat());
			msgVo.getMessageRfcFieldList().add(rfcFieldsVo);
		}
		
		// we could have found a final recipient without delivery reports
		if (msgBean.getReport() == null && msgBean.getRfc822() == null) {
			if (StringUtils.isNotBlank(msgBean.getFinalRcpt())
					|| StringUtils.isNotBlank(msgBean.getOrigRcpt())) {
				MessageRfcField rfcFieldsVo = new MessageRfcField();
				MessageRfcFieldPK pk = new MessageRfcFieldPK(msgVo,StringUtils.left(msgBean.getContentType(),50));
				rfcFieldsVo.setMessageRfcFieldPK(pk);
					// we don't have content type, so just stick one here.
				if (StringUtils.isNotBlank(msgBean.getFinalRcpt())) {
					EmailAddress frcpt = emailAddrDao.findSertAddress(msgBean.getFinalRcpt());
					rfcFieldsVo.setFinalRcptAddrRowId(frcpt.getRowId());
				}
				rfcFieldsVo.setOriginalMsgSubject(StringUtils.left(msgBean.getOrigRcpt(),255));
				msgVo.getMessageRfcFieldList().add(rfcFieldsVo);
			}
		}
		
		// save addresses
		saveAddress(msgBean.getFrom(), EmailAddrType.FROM_ADDR, msgVo);
		saveAddress(msgBean.getTo(), EmailAddrType.TO_ADDR, msgVo);
		saveAddress(msgBean.getReplyto(), EmailAddrType.REPLYTO_ADDR, msgVo);
		saveAddress(msgBean.getCc(), EmailAddrType.CC_ADDR, msgVo);
		saveAddress(msgBean.getBcc(), EmailAddrType.BCC_ADDR, msgVo);
		
		// save message raw stream if received by MailReader
		if (msgBean.getHashMap().containsKey(MessageBeanBuilder.MSG_RAW_STREAM)
				&& msgBean.getIsReceived()) {
			// save raw stream for in-bound mails only
			// out-bound raw stream is saved in MailSender
			MessageStream msgStreamVo = new MessageStream();
			msgStreamVo.setMessageInbox(msgVo);
			msgStreamVo.setFromAddrRowId(msgVo.getFromAddrRowId());
			msgStreamVo.setToAddrRowId(msgVo.getToAddrRowId());
			msgStreamVo.setMsgSubject(msgVo.getMsgSubject());
			msgStreamVo.setMsgStream((byte[]) msgBean.getHashMap().get(
					MessageBeanBuilder.MSG_RAW_STREAM));
			msgVo.setMessageStream(msgStreamVo);
		}
		msgInboxDao.update(msgVo);
		
		if (isDebugEnabled) {
			logger.debug("saveMessage() - Message has been saved to database, MsgId: "
					+ msgVo.getRowId());
		}
		return msgVo.getRowId();
	}
	
	/**
	 * not implemented yet
	 * @param msgBean
	 */
	public void saveMessageFlowLogs(MessageBean msgBean) {
		try {
			MessageInbox msgVo = msgInboxDao.getByRowId(msgBean.getMsgId());
			MessageActionLog msgActionLogsVo = new MessageActionLog();
			MessageActionLogPK pk = new MessageActionLogPK();
			msgActionLogsVo.setMessageActionLogPK(pk);
			pk.setMessageInbox(msgVo);
			// find lead message id
			if (msgBean.getMsgRefId() != null) {
				List<MessageActionLog> list = msgActionLogsDao.getByLeadMsgId(msgBean.getMsgRefId());
				if (list.isEmpty()) {
					logger.error("saveMessageFlowLogs() - record not found by MsgRefId: "
							+ msgBean.getMsgRefId());
				}
				else {
					MessageActionLog vo = list.get(0);
					pk.setLeadMessageRowId(vo.getRowId());
				}
			}
			if (pk.getLeadMessageRowId() < 0) {
				pk.setLeadMessageRowId(msgBean.getMsgId());
			}
			msgActionLogsVo.setActionService(RuleNameEnum.SEND_MAIL.getValue());
			msgActionLogsDao.insert(msgActionLogsVo);
		}
		catch (NoResultException e) {
			logger.error("MessageInbox record not found by RowId: " + msgBean.getMsgId());
		}
	}
	
	/**
	 * returns data from MsgInbox, MsgHeaders, Attachments, and RFCFields
	 * 
	 * @param msgId -
	 *            message Id
	 * @return a MessageInbox or null if not found
	 */
	public MessageInbox getMessageByPK(int msgId) {
		try {
			MessageInbox msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
			return msgInboxVo;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	/**
	 * Returns data from getMessageByPK plus data from DeliveryStatus,
	 * MsgRendered and MsgStream.
	 * 
	 * @param msgId -
	 *            message Id
	 * @return a MessageInbox or null if not found
	 */
	public MessageInbox getAllDataByMsgId(int msgId) {
		try {
			MessageInbox msgInboxVo = getAllDataByMsgId(msgId);
			return msgInboxVo;
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	private void saveAddress(Address[] addrs, EmailAddrType addrType, MessageInbox msgVo) {
		if (addrs == null || addrs.length == 0) {
			return;
		}
		for (int i = 0; i < addrs.length; i++) {
			Address addr = addrs[i];
			if (addr != null) {
				MessageAddress addrVo = new MessageAddress();
				addrVo.setMessageInbox(msgVo);
				addrVo.setAddressType(addrType.getValue());
				EmailAddress email = emailAddrDao.findSertAddress(StringUtils.left(addr.toString(),255));
				addrVo.setEmailAddrRowId(email.getRowId());
				msgVo.getMessageAddressList().add(addrVo);
				//msgAddrsDao.insert(addrVo);
			}
		}
	}
	
	// get the first email address from the list and return its EmailAddrId
	private Integer getEmailAddrId(Address[] addrs) {
		Integer id = null;
		if (addrs != null && addrs.length > 0) {
			for (int i = 0; i < addrs.length; i++) {
				id = getEmailAddrId(addrs[i].toString());
				if (id != null)
					break;
			}
		}
		return id;
	}
	
	private Integer getEmailAddrId(String addr) {
		Integer id = null;
		if (StringUtils.isNotBlank(addr)) {
			EmailAddress emailAddrVo = emailAddrDao.findSertAddress(addr.trim());
			id = Integer.valueOf(emailAddrVo.getRowId());
 		}
		return id;
	}

}
