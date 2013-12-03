package com.es.bo.task;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.bo.sender.MailSenderBo;
import com.es.bo.smtp.SmtpException;
import com.es.dao.address.EmailAddressDao;
import com.es.data.constant.StatusId;
import com.es.exception.DataValidationException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.EmailAddressVo;

@Component("sendMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class SendMessage extends TaskBaseAdaptor {
	private static final long serialVersionUID = 8844968654130851177L;
	static final Logger logger = Logger.getLogger(SendMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressDao emailAddrDao;
	@Autowired
	private MailSenderBo mailSenderBo;

	/**
	 * Send the email off by writing the MessageBean object to MailSender input
	 * queue.
	 * 
	 * @param messageBean -
	 *            MsgRefId that links to a received message must be populated.
	 * @return a Long value representing number of addresses the message has
	 *         been sent to.
	 * @throws IOException 
	 */
	public Long process(MessageContext ctx) throws DataValidationException,
			AddressException, IOException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		if (messageBean.getMsgRefId()==null) {
			logger.warn("messageBean.getMsgRefId() returned null");
		}
		
		if (isDebugEnabled) {
			logger.debug("Sending email to: " + messageBean.getToAsString());
		}
		// validate the TO address, just for safety
		Address[] addrs = InternetAddress.parse(messageBean.getToAsString());
		int mailsSent = 0;
		for (Address addr : addrs) {
			if (addr == null) continue;
			EmailAddressVo vo = emailAddrDao.findSertAddress(addr.toString());
			if (StatusId.ACTIVE.getValue().equals(vo.getStatusId())) {
				// send the mail off
				messageBean.setTo(new Address[]{addr});
				try {
					mailSenderBo.process(ctx);
					if (isDebugEnabled) {
						logger.debug("Message sent to: " + addr.toString());
					}
				}
				catch (SmtpException e) {
					throw new IOException(e.getMessage(), e);
				}
				mailsSent++;
			}
		}
		return Long.valueOf(mailsSent);
	}
}
