package jpa.service.task;

import java.sql.Timestamp;

import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.MsgStatusCode;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.model.message.MessageDeliveryStatus;
import jpa.model.message.MessageDeliveryStatusPK;
import jpa.model.message.MessageInbox;
import jpa.service.EmailAddressService;
import jpa.service.message.MessageDeliveryStatusService;
import jpa.service.message.MessageInboxService;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("deliveryError")
@Transactional(propagation=Propagation.REQUIRED)
public class DeliveryError extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(DeliveryError.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageDeliveryStatusService deliveryStatusDao;
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private MessageInboxService msgInboxDao;

	/**
	 * Obtain delivery status information from MessageBean's DSN or RFC reports.
	 * Update DeliveryStatus table and MsgOutbox table by MsgRefId (the original
	 * message).
	 * 
	 * @return a Long value representing the MsgId inserted into DeliveryStatus
	 *         table, or -1 if nothing is saved.
	 */
	public Integer process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean() == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		if (messageBean.getMsgRefId() == null) {
			logger.warn("Inbox MsgRefId not found, nothing to update");
			return Integer.valueOf(-1);
		}
		if (StringUtils.isBlank(messageBean.getFinalRcpt())) {
			logger.warn("Final Recipient not found, nothing to update");
			return Integer.valueOf(-1);
		}
		
		int msgId = messageBean.getMsgRefId();
		// check the Final Recipient and the original TO address is the same
		
		MessageInbox msgInboxVo = null;
		try {
			msgInboxVo = msgInboxDao.getByRowId(msgId);
		}
		catch (NoResultException e) {
			logger.warn("MsgInbox record not found for MsgId: " + msgId);
			return Integer.valueOf(-1);
		}
		if (msgInboxVo.getToAddrRowId()==null) {
			logger.error("MsgInbox record has a null TO address for MsgId: " + msgId);
			return Integer.valueOf(-1);
		}
		EmailAddress emailAddrVo = emailAddrDao.findSertAddress(messageBean.getFinalRcpt());
		if (msgInboxVo.getToAddrRowId() != emailAddrVo.getRowId()) {
			String origTo = null;
			if (msgInboxVo.getToAddress() == null) {
				try {
					EmailAddress to_addr = emailAddrDao.getByRowId(msgInboxVo.getToAddrRowId());
					origTo = to_addr.getAddress();
				}
				catch (NoResultException e) {}
			}
			else {
				origTo = msgInboxVo.getToAddress().getAddress();
			}
			logger.warn("Final Recipient <" + messageBean.getFinalRcpt()
					+ "> is different from original email's TO address <"
					+ origTo + ">");
		}
		
		// insert into deliveryStatus
		MessageDeliveryStatus deliveryStatusVo = new MessageDeliveryStatus();
		MessageDeliveryStatusPK pk = new MessageDeliveryStatusPK(msgInboxVo, emailAddrVo.getRowId());
		deliveryStatusVo.setMessageDeliveryStatusPK(pk);
		
		deliveryStatusVo.setSmtpMessageId(StringUtils.left(messageBean.getRfcMessageId(),255));
		deliveryStatusVo.setDeliveryStatus(messageBean.getDsnDlvrStat());
		deliveryStatusVo.setDsnReason(StringUtils.left(messageBean.getDiagnosticCode(),255));
		deliveryStatusVo.setDsnRfc822(messageBean.getDsnRfc822());
		deliveryStatusVo.setDsnStatus(StringUtils.left(messageBean.getDsnStatus(),50));
		deliveryStatusVo.setDsnText(messageBean.getDsnText());
		
		deliveryStatusVo.setFinalRecipientAddress(StringUtils.left(messageBean.getFinalRcpt(),255));
		
		if (StringUtils.isNotBlank(messageBean.getOrigRcpt())) {
			EmailAddress vo = emailAddrDao.findSertAddress(messageBean.getOrigRcpt());
			deliveryStatusVo.setOriginalRcptAddrRowId(vo.getRowId());
		}
		
		try {
			//msgInboxVo.getMessageDeliveryStatusList().add(deliveryStatusVo);
			deliveryStatusDao.insert(deliveryStatusVo);
			if (isDebugEnabled) {
				logger.debug("Insert DeliveryStatus:" + LF + StringUtil.prettyPrint(deliveryStatusVo,3));
			}
		}
		catch (DataIntegrityViolationException e) {
			logger.error("DataIntegrityViolationException caught, ignore.", e);
		}
		// update MsgInbox status (delivery failure)
		msgInboxVo.setStatusId(MsgStatusCode.DELIVERY_FAILED.getValue());
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		msgInboxVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		msgInboxDao.update(msgInboxVo);
		ctx.getRowIds().add(msgInboxVo.getRowId());
		return Integer.valueOf(msgId);
	}
	
}
