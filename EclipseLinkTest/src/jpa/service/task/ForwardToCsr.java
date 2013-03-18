package jpa.service.task;

import java.io.IOException;

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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("forwardToCsr")
@Scope(value="prototype")
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
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException, IOException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (getArgumentList().size() == 0) {
			logger.warn("Arguments is not valued, use default csr address from SenderData");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + taskArguments);
		}
		
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
		if (StringUtils.isNotBlank(taskArguments)) {
			if (taskArguments.startsWith("$")) {
				String dept = taskArguments.substring(1);
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
				forwardAddr = taskArguments;
			}
		}
		if (StringUtils.isBlank(forwardAddr)) {
			forwardAddr = sender.getSubrCareEmail();
		}
		// send the mail off
		MessageContext msgctx = new MessageContext(messageBean);
		try {
			mailSenderBo.process(msgctx);
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
