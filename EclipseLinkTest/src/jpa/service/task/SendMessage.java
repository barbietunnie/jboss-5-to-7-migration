package jpa.service.task;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.StatusId;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.model.EmailAddress;
import jpa.service.EmailAddressService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("sendMessage")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class SendMessage extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(SendMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressService emailAddrDao;

	/**
	 * Send the email off by writing the MessageBean object to MailSender input
	 * queue.
	 * 
	 * @param messageBean -
	 *            MsgRefId that links to a received message must be populated.
	 * @return a Integer value representing number of addresses the message has
	 *         been sent to.
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException,
			AddressException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (messageBean.getMsgRefId()==null) {
			logger.warn("messageBean.getMsgRefId() returned null");
			//throw new DataValidationException("messageBean.getMsgRefId() returned null");
		}
		
		if (isDebugEnabled) {
			logger.debug("Sending email to: " + messageBean.getToAsString());
		}
		// validate the TO address, just for safety
		Address[] addrs = InternetAddress.parse(messageBean.getToAsString());
		int mailsSent = 0;
		for (Address addr : addrs) {
			if (addr == null) continue;
			EmailAddress vo = emailAddrDao.findSertAddress(addr.toString());
			if (StatusId.ACTIVE.getValue().equals(vo.getStatusId())) {
				// TODO send the mail off
				mailsSent++;
				if (isDebugEnabled)
					logger.debug("Jms Message Id returned: ");
			}
		}
		return Integer.valueOf(mailsSent);
	}
}
