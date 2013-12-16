package com.legacytojava.message.bo;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MsgStatusCode;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.dao.outbox.DeliveryStatusDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.MsgInboxVo;
import com.legacytojava.message.vo.outbox.DeliveryStatusVo;

@Component("deliveryErrorBo")
@Scope(value="prototype")
@Lazy(true)
public class DeliveryErrorBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(DeliveryErrorBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DeliveryStatusDao deliveryStatusDao;
	@Autowired
	private EmailAddrDao emailAddrDao;
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
	public Long process(MessageBean messageBean) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (messageBean.getMsgRefId() == null) {
			logger.warn("Inbox MsgRefId not found, nothing to update");
			return Long.valueOf(-1L);
		}
		if (StringUtil.isEmpty(messageBean.getFinalRcpt())) {
			logger.warn("Final Recipient not found, nothing to update");
			return Long.valueOf(-1L);
		}
		
		long msgId = messageBean.getMsgRefId();
		// check the Final Recipient and the original TO address is the same
		MsgInboxVo msgInboxVo = msgInboxDao.getByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			logger.warn("MsgInbox record not found for MsgId: " + msgId);
			return Long.valueOf(-1);
		}
		EmailAddrVo emailAddrVo = emailAddrDao.findByAddress(messageBean.getFinalRcpt());
		if (msgInboxVo.getToAddrId().longValue() != emailAddrVo.getEmailAddrId()) {
			logger.warn("Final Recipient <" + messageBean.getFinalRcpt()
					+ "> is different from original email's TO address <"
					+ msgInboxVo.getToAddress() + ">");
		}
		
		// insert into deliveryStatus
		DeliveryStatusVo deliveryStatusVo = new DeliveryStatusVo();
		deliveryStatusVo.setMsgId(msgId);
		
		deliveryStatusVo.setMessageId(StringUtils.left(messageBean.getRfcMessageId(),255));
		deliveryStatusVo.setDeliveryStatus(messageBean.getDsnDlvrStat());
		deliveryStatusVo.setDsnReason(StringUtils.left(messageBean.getDiagnosticCode(),255));
		deliveryStatusVo.setDsnRfc822(messageBean.getDsnRfc822());
		deliveryStatusVo.setDsnStatus(StringUtils.left(messageBean.getDsnStatus(),50));
		deliveryStatusVo.setDsnText(messageBean.getDsnText());
		
		deliveryStatusVo.setFinalRecipient(StringUtils.left(messageBean.getFinalRcpt(),255));
		deliveryStatusVo.setFinalRecipientId(emailAddrVo.getEmailAddrId());
		
		if (messageBean.getOrigRcpt() != null) {
			EmailAddrVo vo = emailAddrDao.findByAddress(messageBean.getOrigRcpt());
			deliveryStatusVo.setOriginalRecipientId(vo.getEmailAddrId());
		}
		
		try {
			deliveryStatusDao.insertWithDelete(deliveryStatusVo);
			if (isDebugEnabled) {
				logger.debug("DeliveryStatus inserted:" + LF + deliveryStatusVo);
			}
		}
		catch (DataIntegrityViolationException e) {
			// most likely caused by "Duplicate entry" error
			logger.error("DataIntegrityViolationException caught, ignore.", e);
			/*
			 * This wasn't working as expected, as a global rollback-only flag
			 * was set by spring framework, and UnexpectedRollbackException was
			 * thrown when spring tried to commit the transaction.
			 * 
			 * A workaround is to set "globalRollbackOnParticipationFailure" to
			 * "false" to your DataSourceTransactionManager, but it will change
			 * the behavior of your entire application globally.
			 * 
			 * A second workaround is to delete the record first. It seemed
			 * cumbersome at first, but it works fine for this special case.
			 */
		}
		// update MsgInbox status (delivery failure)
		msgInboxVo.setStatusId(MsgStatusCode.DELIVERY_FAILED);
		msgInboxVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		msgInboxVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		msgInboxDao.update(msgInboxVo);
		return Long.valueOf(msgId);
	}
}
