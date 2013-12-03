package com.es.bo.task;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.core.util.StringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.outbox.DeliveryStatusDao;
import com.es.data.constant.Constants;
import com.es.data.constant.MsgStatusCode;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.outbox.DeliveryStatusVo;

@Component("deliveryError")
@Transactional(propagation=Propagation.REQUIRED)
public class DeliveryError extends TaskBaseAdaptor {
	private static final long serialVersionUID = -4372604755210330099L;
	static final Logger logger = Logger.getLogger(DeliveryError.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DeliveryStatusDao deliveryStatusDao;
	@Autowired
	private EmailAddressDao emailAddrDao;
	@Autowired
	private MsgInboxDao msgInboxDao;

	/**
	 * Obtain delivery status information from MessageBean's DSN or RFC reports.
	 * Update DeliveryStatus table and MsgOutbox table by MsgRefId (the original
	 * message).
	 * 
	 * @return a Long value representing the MsgId inserted into DeliveryStatus
	 *         table, or -1 if nothing is saved.
	 */
	public Long process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean() == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		if (messageBean.getMsgRefId() == null) {
			logger.warn("Inbox MsgRefId not found, nothing to update");
			return Long.valueOf(-1);
		}
		if (StringUtils.isBlank(messageBean.getFinalRcpt())) {
			logger.warn("Final Recipient not found, nothing to update");
			return Long.valueOf(-1);
		}
		
		Long msgId = messageBean.getMsgRefId();
		// check the Final Recipient and the original TO address is the same
		
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		
		if (msgInboxVo == null) {
			logger.warn("MsgInbox record not found for MsgId: " + msgId);
			return Long.valueOf(-1);
		}
		if (msgInboxVo.getToAddrId()==null) {
			logger.error("MsgInbox record has a null TO address for MsgId: " + msgId);
			return Long.valueOf(-1);
		}
		EmailAddressVo emailAddrVo = emailAddrDao.findSertAddress(messageBean.getFinalRcpt());
		if (msgInboxVo.getToAddrId() != emailAddrVo.getEmailAddrId()) {
			String origTo = null;
			if (msgInboxVo.getToAddress() == null) {
				EmailAddressVo to_addr = emailAddrDao.getByAddrId(msgInboxVo.getToAddrId());
				origTo = to_addr.getEmailAddr();
			}
			else {
				origTo = msgInboxVo.getToAddress();
			}
			logger.warn("Final Recipient <" + messageBean.getFinalRcpt()
					+ "> is different from original email's TO address <"
					+ origTo + ">");
		}
		
		// insert into deliveryStatus
		DeliveryStatusVo deliveryStatusVo = new DeliveryStatusVo();
		deliveryStatusVo.setMsgId(msgId);
		deliveryStatusVo.setFinalRecipientId(emailAddrVo.getEmailAddrId());
		
		deliveryStatusVo.setMessageId(StringUtils.left(messageBean.getRfcMessageId(),255));
		if (StringUtils.isNotBlank(messageBean.getDsnDlvrStat())) {
			deliveryStatusVo.setDeliveryStatus(messageBean.getDsnDlvrStat());
		}
		else if (StringUtils.isNotBlank(messageBean.getDsnText())) {
			deliveryStatusVo.setDsnText(messageBean.getDsnText());
		}
		deliveryStatusVo.setDsnReason(StringUtils.left(messageBean.getDiagnosticCode(),255));
		deliveryStatusVo.setDsnStatus(StringUtils.left(messageBean.getDsnStatus(),50));
		
		deliveryStatusVo.setFinalRecipient(StringUtils.left(messageBean.getFinalRcpt(),255));
		
		if (StringUtils.isNotBlank(messageBean.getOrigRcpt())) {
			EmailAddressVo vo = emailAddrDao.findSertAddress(messageBean.getOrigRcpt());
			deliveryStatusVo.setOriginalRecipientId(vo.getEmailAddrId());
		}
		
		try {
			deliveryStatusDao.insert(deliveryStatusVo);
			msgInboxVo.getDeliveryStatus().add(deliveryStatusVo);
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
		ctx.getRowIds().add(msgInboxVo.getMsgId());
		return Long.valueOf(msgId);
	}
	
}
