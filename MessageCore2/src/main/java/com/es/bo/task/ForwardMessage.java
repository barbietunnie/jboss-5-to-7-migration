package com.es.bo.task;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.sender.MailSenderBo;
import com.es.bo.smtp.SmtpException;
import com.es.dao.inbox.MsgStreamDao;
import com.es.dao.sender.SenderDataDao;
import com.es.data.constant.EmailAddrType;
import com.es.data.constant.TableColumnName;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.comm.SenderDataVo;

@Component("forwardMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class ForwardMessage extends TaskBaseAdaptor {
	private static final long serialVersionUID = 4223082303129172619L;
	static final Logger logger = Logger.getLogger(ForwardMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MsgStreamDao msgStreamDao;
	@Autowired
	private SenderDataDao senderDao;
	@Autowired
	private MailSenderBo mailSenderBo;

	/**
	 * Forward the message to the specified addresses. The forwarding addresses
	 * or address types are obtained from "DataTypeValues" column of MsgAction
	 * table.
	 * 
	 * @return a Long value representing number of addresses the message is
	 *         forwarded to.
	 */
	public Long process(MessageContext ctx) throws DataValidationException,
			MessagingException, IOException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (StringUtils.isBlank(ctx.getTaskArguments())) {
			throw new DataValidationException("Arguments is not valued, can't forward");
		}
		
		MessageBean messageBean = ctx.getMessageBean();
		String senderId = messageBean.getSenderId();
		SenderDataVo senderVo = senderDao.getBySenderId(senderId);
		if (senderVo == null) {
			throw new DataValidationException("SenderData record not found by senderId: " + senderId);
		}

		// example: $Forward,securityDept@mycompany.com
		String forwardAddrs = "";
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
				// E-mail addresses from Client table
				else if (TableColumnName.SUBSCRIBER_CARE_ADDR.getValue().equals(token)) {
					addrs = senderVo.getCustcareEmail();
				}
				else if (TableColumnName.SECURITY_DEPT_ADDR.getValue().equals(token)) {
					addrs = senderVo.getSecurityEmail();
				}
				else if (TableColumnName.RMA_DEPT_ADDR.getValue().equals(token)) {
					addrs = senderVo.getRmaDeptEmail();
				}
				else if (TableColumnName.SPAM_CONTROL_ADDR.getValue().equals(token)) {
					addrs = senderVo.getSpamCntrlEmail();
				}
				else if (TableColumnName.VIRUS_CONTROL_ADDR.getValue().equals(token)) {
					addrs = senderVo.getSpamCntrlEmail();
				}
				else if (TableColumnName.CHALLENGE_HANDLER_ADDR.getValue().equals(token)) {
					addrs = senderVo.getChaRspHndlrEmail();
				}
				// end of Client table
			}
			else { // real email address
				addrs = token;
			}
			
			if (StringUtils.isNotBlank(addrs)) {
				try {
					InternetAddress.parse(addrs);
					if (StringUtils.isBlank(forwardAddrs)) {
						forwardAddrs += addrs;
					}
					else {
						forwardAddrs += "," + addrs;
					}
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
				}
			}
		} // end of while
		if (isDebugEnabled) {
			logger.debug("Address(es) to forward to: " + forwardAddrs);
		}
		if (StringUtils.isBlank(forwardAddrs)) {
			throw new DataValidationException("forward address is not valued");
		}
		
		if (messageBean.getMsgId() != null) {
			if (messageBean.getMsgRefId() == null) {
				messageBean.setMsgRefId(messageBean.getMsgId());
			}
		}
		Address[] addresses = InternetAddress.parse(forwardAddrs);
		
		messageBean.setSubject("Fwd: " + messageBean.getSubject());
		messageBean.setTo(addresses);
		messageBean.setEmBedEmailId(Boolean.FALSE);
		
		// send the message off
		try {
			mailSenderBo.process(ctx);
			if (isDebugEnabled) {
				logger.debug("Message forwarded to: " + messageBean.getToAsString());
			}
		}
		catch (SmtpException e) {
			throw new IOException(e.getMessage(), e);
		}
		return Long.valueOf(addresses.length);
	}
}
