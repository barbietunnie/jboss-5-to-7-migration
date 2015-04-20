package com.es.bo.inbox;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.mail.Address;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.es.core.util.StringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgActionLogDao;
import com.es.dao.inbox.MsgAddressDao;
import com.es.dao.inbox.MsgAttachmentDao;
import com.es.dao.inbox.MsgClickCountDao;
import com.es.dao.inbox.MsgHeaderDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.inbox.MsgRfcFieldDao;
import com.es.dao.inbox.MsgStreamDao;
import com.es.dao.outbox.DeliveryStatusDao;
import com.es.dao.outbox.MsgRenderedDao;
import com.es.dao.outbox.MsgSequenceDao;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddrType;
import com.es.data.constant.MailingListDeliveryType;
import com.es.data.constant.MsgDirectionCode;
import com.es.data.constant.MsgStatusCode;
import com.es.data.preload.RuleNameEnum;
import com.es.exception.DataValidationException;
import com.es.msgbean.BodypartBean;
import com.es.msgbean.BodypartUtil;
import com.es.msgbean.JavaMailParser;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageBeanUtil;
import com.es.msgbean.MessageBodyBuilder;
import com.es.msgbean.MessageNode;
import com.es.msgbean.MsgHeader;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.inbox.MsgActionLogVo;
import com.es.vo.inbox.MsgAddressVo;
import com.es.vo.inbox.MsgAttachmentVo;
import com.es.vo.inbox.MsgClickCountVo;
import com.es.vo.inbox.MsgHeaderVo;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.inbox.MsgRfcFieldVo;
import com.es.vo.outbox.DeliveryStatusVo;
import com.es.vo.outbox.MsgRenderedVo;
import com.es.vo.outbox.MsgStreamVo;

/**
 * save email data and properties into database.
 */
@Component("msgInboxBo")
public class MsgInboxBo {
	static final Logger logger = Logger.getLogger(MsgInboxBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgSequenceDao msgSequenceDao;
	@Autowired
	private MsgInboxDao msgInboxDao;
	@Autowired
	private MsgAttachmentDao attachmentDao;
	@Autowired
	private MsgAddressDao msgAddressDao;
	@Autowired
	private MsgHeaderDao msgHeaderDao;
	@Autowired
	private MsgRfcFieldDao rfcFieldDao;
	@Autowired
	private EmailAddressDao emailAddressDao;
	@Autowired
	private MsgStreamDao msgStreamDao;
	@Autowired
	private DeliveryStatusDao deliveryStatusDao;
	@Autowired
	private MsgRenderedDao msgRenderedDao;
	@Autowired
	private MsgActionLogDao msgActionLogDao;
	@Autowired
	private MsgClickCountDao msgClickCountDao;

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
	public long saveMessage(MessageBean msgBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering saveMessage() method..." + LF + msgBean);
		}
		if (msgBean == null) {
			throw new DataValidationException("Input MessageBean is null");
		}
		if (msgBean.getRuleName() == null) {
			throw new DataValidationException("MessageBean.getRuleName() returns a null");
		}
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		MsgInboxVo msgVo = new MsgInboxVo();
		long msgId = msgSequenceDao.findNextValue();
		msgBean.setMsgId(Long.valueOf(msgId));
		logger.info("saveMessage() - MsgId to be saved: " + msgId + ", From MailReader: "
				+ msgBean.getIsReceived());
		msgVo.setMsgId(msgId);
		msgVo.setMsgRefId(msgBean.getMsgRefId());
		if (msgBean.getCarrierCode() == null) {
			logger.warn("saveMessage() - carrierCode field is null, set to S (SMTP)");
			msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		}
		msgVo.setCarrierCode(StringUtils.left(msgBean.getCarrierCode().getValue(),1));
		msgVo.setMsgSubject(StringUtils.left(msgBean.getSubject(),255));
		msgVo.setMsgPriority(MessageBeanUtil.getMsgPriority(msgBean.getPriority()));
		Timestamp ts = msgBean.getSendDate() == null ? updtTime : new Timestamp(msgBean
				.getSendDate().getTime());
		msgVo.setReceivedTime(ts);
		
		Long fromAddrId = getEmailAddrId(msgBean.getFrom());
		msgVo.setFromAddrId(fromAddrId);
		Long replyToAddrId = getEmailAddrId(msgBean.getReplyto());
		msgVo.setReplyToAddrId(replyToAddrId);
		Long toAddrId = getEmailAddrId(msgBean.getTo());
		msgVo.setToAddrId(toAddrId);
		
		Calendar cal = Calendar.getInstance();
		if (msgBean.getPurgeAfter() != null) {
			cal.add(Calendar.MONTH, msgBean.getPurgeAfter());
		}
		else {
			cal.add(Calendar.MONTH, 12); // purge in 12 months - default
		}
		// set purge Date
		msgVo.setPurgeDate(new java.sql.Date(cal.getTime().getTime()));

		// find LeadMsgId. Also find sender id if it's from MailReader
		if (msgBean.getMsgRefId() != null) {
			MsgInboxVo origVo = msgInboxDao.getByPrimaryKey(msgBean.getMsgRefId());
			if (origVo == null) { // could be deleted by User or purged by Purge routine
				logger.warn("saveMessage() - MsgInbox record not found by MsgRefId: "
						+ msgBean.getMsgRefId());
			}
			else {
				msgVo.setLeadMsgId(origVo.getLeadMsgId());
				if (msgBean.getIsReceived()) { // from MailReader
					// code has been moved to MessageParserBo.parse()
				}
			}
		}
		if (msgVo.getLeadMsgId() < 0) {
			// default to myself
			msgVo.setLeadMsgId(msgBean.getMsgId());
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
			if (msgVo.getFromAddrId() != null) {
				EmailAddressVo lastRcptVo = emailAddressDao.getByAddrId(msgVo.getFromAddrId());
				long minutes = 10 * 60 * 1000; // 10 minutes
				if (lastRcptVo.getLastRcptTime() == null
						|| lastRcptVo.getLastRcptTime().getTime() < (updtTime.getTime() - minutes)) {
					emailAddressDao.updateLastRcptTime(msgVo.getFromAddrId());
				}
			}
		}
		else {
			/* from MailSender */
			msgVo.setMsgDirection(MsgDirectionCode.SENT.getValue());
			msgVo.setSmtpMessageId(null);
			msgVo.setDeliveryTime(null); // delivery time
			msgVo.setStatusId(MsgStatusCode.PENDING.getValue());
			if (msgBean.getRenderId() != null
					&& msgRenderedDao.getByPrimaryKey(msgBean.getRenderId()) != null) {
				msgVo.setRenderId(msgBean.getRenderId());
			}
			msgVo.setOverrideTestAddr(msgBean.getOverrideTestAddr() ? CodeType.YES_CODE.getValue() : CodeType.NO_CODE.getValue());
			// check original message body's content type
			if (msgBean.getOriginalMail() != null) {
				String origContentType = msgBean.getOriginalMail().getBodyContentType();
				if (contentType.indexOf("html") < 0 && origContentType.indexOf("html") >= 0) {
					// reset content type to original's
					msgVo.setBodyContentType(StringUtils.left(origContentType,50));
				}
			}
			/* Rebuild the Message Body, generate Email_Id from MsgId */
			String msgBody = MessageBodyBuilder.getBodyWithEmailId(msgBean);
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
			if (msgVo.getToAddrId() != null) {
				emailAddressDao.updateLastSentTime(msgVo.getToAddrId());
			}
		}
		
		msgVo.setSenderId(StringUtils.left(msgBean.getSenderId(),16));
		msgVo.setSubrId(StringUtils.left(msgBean.getSubrId(),Constants.SUBSCRIBER_ID_MAX_LEN));
		msgVo.setRuleName(StringUtils.left(msgBean.getRuleName(),26));
		
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
		
		// insert click count record for Broadcasting e-mail
		if (RuleNameEnum.BROADCAST.getValue().equals(msgBean.getRuleName())) {
			MsgClickCountVo msgClickCountVo = new MsgClickCountVo();
			msgClickCountVo.setMsgId(msgId);
			// msgBean.getMailingListId() should always returns a value. just for safety.
			String listId = msgBean.getMailingListId() == null ? "" : msgBean.getMailingListId();
			msgClickCountVo.setListId(listId);
			if (msgBean.getToSubscribersOnly()) {
				msgClickCountVo.setDeliveryOption(MailingListDeliveryType.SUBSCRIBERS_ONLY.getValue());
			}
			else if (msgBean.getToProspectsOnly()) {
				msgClickCountVo.setDeliveryOption(MailingListDeliveryType.PROSPECTS_ONLY.getValue());
			}
			else {
				msgClickCountVo.setDeliveryOption(MailingListDeliveryType.ALL_ON_LIST.getValue());
			}
			msgClickCountVo.setSentCount(0);
			msgClickCountVo.setClickCount(0);
			msgClickCountDao.insert(msgClickCountVo);
		}
		
		// save message headers
		List<MsgHeader> headers = msgBean.getHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.size(); i++) {
				MsgHeader header = headers.get(i);
				MsgHeaderVo msgHeaderVo= new MsgHeaderVo();
				msgHeaderVo.setMsgId(msgVo.getMsgId());
				msgHeaderVo.setHeaderName(StringUtils.left(header.getName(),100));
				msgHeaderVo.setHeaderValue(header.getValue());
				msgHeaderVo.setHeaderSeq(i+1);
				msgHeaderDao.insert(msgHeaderVo);
			}
		}

		// save attachments
		if (aNodes!=null && aNodes.size()>0) {
			for (int i=0; i<aNodes.size(); i++) {
				MessageNode mNode = aNodes.get(i);
				BodypartBean aNode = mNode.getBodypartNode();
				MsgAttachmentVo attchVo = new MsgAttachmentVo();
				attchVo.setMsgId(msgVo.getMsgId());
				attchVo.setAttchmntDepth(mNode.getLevel());
				attchVo.setAttchmntSeq(i+1);
				attchVo.setAttchmntName(StringUtils.left(aNode.getDescription(),100));
				attchVo.setAttchmntType(StringUtils.left(aNode.getContentType(),100));
				attchVo.setAttchmntDisp(StringUtils.left(aNode.getDisposition(),100));
				attchVo.setAttchmntValue(aNode.getValue());
				attachmentDao.insert(attchVo);
			}
		}
		
		// save RFC fields
		if (msgBean.getReport() != null) {
			MessageNode mNode = msgBean.getReport();
			BodypartBean aNode = mNode.getBodypartNode();
			MsgRfcFieldVo rfcFieldVo = new MsgRfcFieldVo();
			rfcFieldVo.setMsgId(msgVo.getMsgId());
			rfcFieldVo.setRfcType(StringUtils.left(aNode.getContentType(),30));
			
			rfcFieldVo.setRfcStatus(StringUtils.left(msgBean.getDsnStatus(),30));
			rfcFieldVo.setRfcAction(StringUtils.left(msgBean.getDsnAction(),30));
			rfcFieldVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
			rfcFieldVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
			rfcFieldVo.setOrigRcpt(StringUtils.left(msgBean.getOrigRcpt(),255));
			//rfcFieldsVo.setOrigMsgSubject(StringUtil.cut(msgBean.getOrigSubject(),255));
			//rfcFieldsVo.setMessageId(StringUtil.cut(msgBean.getMessageId(),255));
			rfcFieldVo.setDsnText(msgBean.getDsnText());
			rfcFieldVo.setDsnRfc822(msgBean.getDiagnosticCode()); // TODO: revisit
			rfcFieldVo.setDlvrStatus(msgBean.getDsnDlvrStat());
			rfcFieldDao.insert(rfcFieldVo);
		}
		
		if (msgBean.getRfc822() != null) {
			MessageNode mNode = msgBean.getRfc822();
			BodypartBean aNode = mNode.getBodypartNode();
			MsgRfcFieldVo rfcFieldVo = new MsgRfcFieldVo();
			rfcFieldVo.setMsgId(msgVo.getMsgId());
			rfcFieldVo.setRfcType(StringUtils.left(aNode.getContentType(),30));
			
			//rfcFieldsVo.setRfcStatus(StringUtil.cut(msgBean.getDsnStatus(),30));
			//rfcFieldsVo.setRfcAction(StringUtil.cut(msgBean.getDsnAction(),30));
			rfcFieldVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
			rfcFieldVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
			//rfcFieldsVo.setOrigRcpt(StringUtil.cut(msgBean.getOrigRcpt(),255));
			rfcFieldVo.setOrigMsgSubject(StringUtils.left(msgBean.getOrigSubject(),255));
			rfcFieldVo.setMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			rfcFieldVo.setDsnText(msgBean.getDsnText());
			rfcFieldVo.setDsnRfc822(msgBean.getDsnRfc822());
			//rfcFieldsVo.setDlvrStatus(msgBean.getDsnDlvrStat());
			rfcFieldDao.insert(rfcFieldVo);
		}
		
		// we could have found a final recipient without delivery reports
		if (msgBean.getReport() == null && msgBean.getRfc822() == null) {
			if (!StringUtils.isEmpty(msgBean.getFinalRcpt())
					|| !StringUtils.isEmpty(msgBean.getOrigRcpt())) {
				MsgRfcFieldVo rfcFieldVo = new MsgRfcFieldVo();
				rfcFieldVo.setMsgId(msgVo.getMsgId());
				rfcFieldVo.setRfcType(StringUtils.left(msgBean.getContentType(),30));
					// we don't have content type, so just stick one here.
				rfcFieldVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
				rfcFieldVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
				rfcFieldVo.setOrigRcpt(StringUtils.left(msgBean.getOrigRcpt(),255));
				rfcFieldDao.insert(rfcFieldVo);
			}
		}
		
		// save addresses
		saveAddress(msgBean.getFrom(), EmailAddrType.FROM_ADDR, msgVo.getMsgId());
		saveAddress(msgBean.getTo(), EmailAddrType.TO_ADDR, msgVo.getMsgId());
		saveAddress(msgBean.getReplyto(), EmailAddrType.REPLYTO_ADDR, msgVo.getMsgId());
		saveAddress(msgBean.getCc(), EmailAddrType.CC_ADDR, msgVo.getMsgId());
		saveAddress(msgBean.getBcc(), EmailAddrType.BCC_ADDR, msgVo.getMsgId());
		
		// save message raw stream if received by MailReader
		if (msgBean.getHashMap().containsKey(JavaMailParser.MSG_RAW_STREAM)
				&& msgBean.getIsReceived()) {
			// save raw stream for in-bound mails only
			// out-bound raw stream is saved in MailSender
			MsgStreamVo msgStreamVo = new MsgStreamVo();
			msgStreamVo.setMsgId(msgVo.getMsgId());
			msgStreamVo.setFromAddrId(msgVo.getFromAddrId());
			msgStreamVo.setToAddrId(msgVo.getToAddrId());
			msgStreamVo.setMsgSubject(msgVo.getMsgSubject());
			msgStreamVo.setMsgStream((byte[]) msgBean.getHashMap().get(
					JavaMailParser.MSG_RAW_STREAM));
			msgStreamDao.insert(msgStreamVo);
		}
		
		if (isDebugEnabled) {
			logger.debug("saveMessage() - Message has been saved to database, MsgId: "
					+ msgVo.getMsgId());
		}
		return msgVo.getMsgId();
	}
	
	/**
	 * Persisting an action log record.
	 * @param msgBean
	 */
	public void saveMessageActionLogs(MessageBean msgBean) {
		if (msgBean.getMsgId()!=null) {
			MsgActionLogVo msgActionLogVo = new MsgActionLogVo();
			msgActionLogVo.setMsgId(msgBean.getMsgId());
			msgActionLogVo.setActionSeq(0); // will be updated by the insert method
			String actionName = msgBean.getProperties().getProperty("action_bo_name", RuleNameEnum.SEND_MAIL.getValue());
			msgActionLogVo.setActionBo(actionName);
			msgActionLogVo.setParameters(msgBean.getProperties().getProperty("action_parameters"));
			msgActionLogDao.insert(msgActionLogVo);
		}
	}
	
	/**
	 * returns data from MsgInbox, MsgHeaders, Attachments, and RFCFields
	 * 
	 * @param msgId -
	 *            message Id
	 * @return a MsgInboxVo or null if not found
	 */
	public MsgInboxVo getMessageByPK(long msgId) {
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo!=null) {
			List<MsgAddressVo> msgAddrs = msgAddressDao.getByMsgId(msgId);
			msgInboxVo.setMsgAddrs(msgAddrs);
			List<MsgHeaderVo> msgHeaders = msgHeaderDao.getByMsgId(msgId);
			msgInboxVo.setMsgHeaders(msgHeaders);
			List<MsgAttachmentVo> attachments = attachmentDao.getByMsgId(msgId);
			msgInboxVo.setAttachments(attachments);
			List<MsgRfcFieldVo> rfcFields = rfcFieldDao.getByMsgId(msgId);
			msgInboxVo.setRfcFields(rfcFields);
		}
		return msgInboxVo;
	}
	
	/**
	 * Returns data from getMessageByPK plus data from DeliveryStatus,
	 * MsgRendered and MsgStream.
	 * 
	 * @param msgId -
	 *            message Id
	 * @return a MsgInboxVo or null if not found
	 */
	public MsgInboxVo getAllDataByMsgId(long msgId) {
		MsgInboxVo msgInboxVo = getMessageByPK(msgId);
		if (msgInboxVo != null) {
			List<DeliveryStatusVo> deliveryStatus = deliveryStatusDao.getByMsgId(msgId);
			msgInboxVo.setDeliveryStatus(deliveryStatus);
			if (msgInboxVo.getRenderId() != null) {
				MsgRenderedVo msgRenderedVo = msgRenderedDao.getByPrimaryKey(msgInboxVo
						.getRenderId());
				msgInboxVo.setMsgRenderedVo(msgRenderedVo);
			}
			MsgStreamVo msgStreamVo = msgStreamDao.getByPrimaryKey(msgId);
			msgInboxVo.setMsgStreamVo(msgStreamVo);
		}
		return msgInboxVo;
	}
	
	private void saveAddress(Address[] addrs, EmailAddrType addrType, long msgId) {
		if (addrs == null || addrs.length == 0) {
			return;
		}
		for (int i = 0; i < addrs.length; i++) {
			Address addr = addrs[i];
			if (addr != null) {
				MsgAddressVo addrVo = new MsgAddressVo();
				addrVo.setMsgId(msgId);
				addrVo.setAddrType(addrType.getValue());
				addrVo.setAddrSeq(i + 1);
				addrVo.setAddrValue(StringUtils.left(addr.toString(),255));
				msgAddressDao.insert(addrVo);
			}
		}
	}
	
	// get the first email address from the list and return its EmailAddrId
	private Long getEmailAddrId(Address[] addrs) {
		Long id = null;
		if (addrs != null && addrs.length > 0) {
			for (int i = 0; i < addrs.length; i++) {
				id = getEmailAddrId(addrs[i].toString());
				if (id != null)
					break;
			}
		}
		return id;
	}
	
	private Long getEmailAddrId(String addr) {
		Long id = null;
		if (addr != null && addr.trim().length() > 0) {
			EmailAddressVo emailAddrVo = emailAddressDao.findSertAddress(addr.trim());
			if (emailAddrVo != null) {
				id = Long.valueOf(emailAddrVo.getEmailAddrId());
			}
		}
		return id;
	}
}
