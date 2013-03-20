package jpa.service.task;

import java.io.IOException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.NoResultException;

import jpa.constant.Constants;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.SenderData;
import jpa.service.SenderDataService;
import jpa.service.msgout.MailSenderBo;
import jpa.service.msgout.SmtpException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("forwardToCsr")
@Transactional(propagation=Propagation.REQUIRED)
public class ForwardToCsr extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ForwardToCsr.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private MailSenderBo mailSenderBo;

	/**
	 * Forward the message to CSR input queue. Address from SenderData record
	 * will be used if the value is not passed from taskArguments.
	 * 
	 * @return number of messages forwarded.
	 * @throws IOException 
	 * @throws AddressException 
	 */
	public Integer process(MessageContext ctx) throws DataValidationException,
			IOException, AddressException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (getArgumentList(ctx.getTaskArguments()).size() == 0) {
			logger.warn("Arguments is not valued, use default csr address from SenderData");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + ctx.getTaskArguments());
		}
		
		MessageBean messageBean = ctx.getMessageBean();
		String senderId = messageBean.getSenderId();
		if (StringUtils.isBlank(senderId)) {
			messageBean.setSenderId(Constants.DEFAULT_SENDER_ID);
		}
		SenderData sender = null;
		try {
			sender = senderService.getBySenderId(messageBean.getSenderId());
		}
		catch (NoResultException e) {
			throw new DataValidationException("SenderData not found by senderId: " + messageBean.getSenderId());
		}
		String forwardAddr = null;
		if (StringUtils.isNotBlank(ctx.getTaskArguments())) {
			if (ctx.getTaskArguments().startsWith("$")) {
				String dept = ctx.getTaskArguments().substring(1);
				if (RuleNameEnum.RMA_REQUEST.getValue().equals(dept)) {
					forwardAddr = sender.getRmaDeptEmail();
				}
				else if (RuleNameEnum.CHALLENGE_RESPONSE.getValue().equals(dept)) {
					forwardAddr = sender.getChaRspHndlrEmail();
				}
				else if (RuleNameEnum.CONTACT_US.getValue().equals(dept)) {
					forwardAddr = sender.getSubrCareEmail();
				}
				else if (RuleNameEnum.MAIL_BLOCK.getValue().equals(dept)) {
					forwardAddr = sender.getSpamCntrlEmail();
				}
				else if (RuleNameEnum.SPAM_BLOCK.getValue().equals(dept)) {
					forwardAddr = sender.getSpamCntrlEmail();
				}
				else if (RuleNameEnum.VIRUS_BLOCK.getValue().equals(dept)) {
					forwardAddr = sender.getVirusCntrlEmail();
				}
			}
			else {
				forwardAddr = ctx.getTaskArguments();
			}
		}
		if (StringUtils.isBlank(forwardAddr)) {
			forwardAddr = sender.getSubrCareEmail();
		}
		// send the mail off
		messageBean.setTo(InternetAddress.parse(forwardAddr));
		try {
			mailSenderBo.process(ctx);
			if (isDebugEnabled) {
				logger.debug("Message forwarded to: " + forwardAddr);
			}
		}
		catch (SmtpException e) {
			throw new IOException(e.getMessage(), e);
		}
		return 1;
	}
}
