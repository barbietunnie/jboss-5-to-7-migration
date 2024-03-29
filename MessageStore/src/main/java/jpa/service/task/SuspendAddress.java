package jpa.service.task;

import java.sql.Timestamp;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.StatusId;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.model.message.MessageInbox;
import jpa.service.common.EmailAddressService;
import jpa.service.message.MessageInboxService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("suspendAddress")
@Transactional(propagation=Propagation.REQUIRED)
public class SuspendAddress extends TaskBaseAdapter {
	private static final long serialVersionUID = -1661554229653181139L;
	static final Logger logger = Logger.getLogger(SuspendAddress.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private MessageInboxService msgInboxDao;

	/**
	 * Suspend email addresses. The column "DataTypeValues" from MsgAction table
	 * contains address types (FROM, TO, etc) that to be suspended.
	 * 
	 * @return a Integer value representing the number of addresses that have been
	 *         suspended.
	 */
	public Integer process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		if (StringUtils.isBlank(ctx.getTaskArguments())) {
			throw new DataValidationException("Arguments is not valued, nothing to suspend");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + ctx.getTaskArguments());
		}
		
		MessageBean messageBean = ctx.getMessageBean();
		// example: $FinalRcpt,$OriginalRcpt,badaddress@badcompany.com
		int addrsSuspended = 0;
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		StringTokenizer st = new StringTokenizer(ctx.getTaskArguments(), ",");
		while (st.hasMoreTokens()) {
			String addrs = null;
			String token = st.nextToken();
			if (token != null && token.startsWith("$")) { // address type
				token = token.substring(1);
				if (EmailAddrType.FROM_ADDR.getValue().equals(token)) {
					addrs = messageBean.getFromAsString();
				}
				else if (EmailAddrType.FINAL_RCPT_ADDR.getValue().equals(token)) {
					addrs = messageBean.getFinalRcpt();
				}
				else if (EmailAddrType.ORIG_RCPT_ADDR.getValue().equals(token)) {
					addrs = messageBean.getOrigRcpt();
				}
				else if (EmailAddrType.FORWARD_ADDR.getValue().equals(token)) {
					addrs = messageBean.getForwardAsString();
				}
				else if (EmailAddrType.TO_ADDR.getValue().equals(token)) {
					addrs = messageBean.getToAsString();
				}
				else if (EmailAddrType.REPLYTO_ADDR.getValue().equals(token)) {
					addrs = messageBean.getReplytoAsString();
				}
			}
			else { // real email address
				addrs = token;
			}
			
			if (StringUtils.isNotBlank(addrs)) {
				try {
					InternetAddress.parse(addrs);
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
					addrs = null;
				}
			}
			if (StringUtils.isBlank(addrs)) {
				// just for safety
				continue;
			}
			if (isDebugEnabled) {
				logger.debug("Address(es) to suspend: " + addrs);
			}
			StringTokenizer st2 = new StringTokenizer(addrs, ",");
			while (st2.hasMoreTokens()) {
				String addr = st2.nextToken();
				try {
					EmailAddress emailAddrVo = emailAddrDao.getByAddress(addr);
					if (!StatusId.SUSPENDED.getValue().equals(emailAddrVo.getStatusId())) {
						if (isDebugEnabled)
							logger.debug("Suspending EmailAddr: " + addr);
						emailAddrVo.setStatusId(StatusId.SUSPENDED.getValue());
						emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
						emailAddrVo.setStatusChangeTime(updtTime);
						emailAddrDao.update(emailAddrVo);
						addrsSuspended++;
					}
				}
				catch (NoResultException e) {}
			}
		} // end of while loop
		// if failed to suspend any address, check if MsgRefId is valued
		if (addrsSuspended == 0 && messageBean.getMsgRefId() != null) {
			// -- this has been taken care of by MessageParser
			//addrsSuspended = suspendByMsgRefId(messageBean, updtTime);
		}
		return Integer.valueOf(addrsSuspended);
	}

	int suspendByMsgRefId(MessageBean messageBean, Timestamp updtTime) {
		int addrsSuspended = 0;
		if (messageBean.getMsgRefId() == null) {
			return addrsSuspended;
		}
		// find the "to address" by MsgRefId and suspend it
		int msgId = messageBean.getMsgRefId().intValue();
		MessageInbox msgInboxVo = msgInboxDao.getAllDataByPrimaryKey(msgId);
		if (msgInboxVo == null) {
			logger.warn("Failed to find MsgInbox record by MsgId: " + msgId);
		}
		else if (!RuleNameEnum.SEND_MAIL.getValue().equals(msgInboxVo.getRuleLogic().getRuleName())) {
			logger.error("Message from MsgRefId is not a 'SEND_MAIL', ignored." + LF
					+ messageBean);
		}
		else if (msgInboxVo.getToAddrRowId() != null) { // should always valued
			int toAddr = msgInboxVo.getToAddrRowId().intValue();
			try {
				EmailAddress emailAddrVo = emailAddrDao.getByRowId(toAddr);
				if (!StatusId.SUSPENDED.getValue().equals(emailAddrVo.getStatusId())) {
					if (isDebugEnabled)
						logger.debug("Suspending EmailAddr: " + emailAddrVo.getAddress());
					emailAddrVo.setStatusId(StatusId.SUSPENDED.getValue());
					emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
					emailAddrVo.setStatusChangeTime(updtTime);
					emailAddrDao.update(emailAddrVo);
					addrsSuspended++;
				}
			}
			catch (NoResultException e) {}
		}
		return addrsSuspended;
	}
}
