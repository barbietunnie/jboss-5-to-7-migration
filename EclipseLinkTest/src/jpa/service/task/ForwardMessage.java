package jpa.service.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.NoResultException;

import jpa.constant.EmailAddrType;
import jpa.constant.TableColumnName;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageBeanBuilder;
import jpa.message.MessageBeanUtil;
import jpa.model.SenderData;
import jpa.service.SenderDataService;
import jpa.service.message.MessageStreamService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("forwardMessage")
@Scope(value="prototype")
@Transactional(propagation=Propagation.REQUIRED)
public class ForwardMessage extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ForwardMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageStreamService msgStreamDao;
	@Autowired
	private SenderDataService senderDao;

	/**
	 * Forward the message to the specified addresses. The forwarding addresses
	 * or address types are obtained from "DataTypeValues" column of MsgAction
	 * table. The original email raw stream should be included in the input
	 * MessageBean by the calling program.
	 * 
	 * @return a Integer value representing number of addresses the message is
	 *         forwarded to.
	 */
	public Integer process(MessageBean messageBean) throws DataValidationException,
			MessagingException, IOException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (messageBean==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (!messageBean.getHashMap().containsKey(MessageBeanBuilder.MSG_RAW_STREAM)) {
			logger.warn("Email Raw Stream not found in MessageBean.hashMap");
		}
		if (taskArguments == null || taskArguments.trim().length() == 0) {
			throw new DataValidationException("Arguments is not valued, can't forward");
		}
		
		String senderId = messageBean.getSenderId();
		SenderData senderVo = null;
		try {
			senderVo = senderDao.getBySenderId(senderId);
		}
		catch (NoResultException e) {
			throw new DataValidationException("Client record not found by clientId: " + senderId);
		}

		// example: $Forward,securityDept@mycompany.com
		String forwardAddrs = "";
		StringTokenizer st = new StringTokenizer(taskArguments, ",");
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
					addrs = senderVo.getSubrCareEmail();
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
		if (forwardAddrs.trim().length() == 0) {
			throw new DataValidationException("forward address is not valued");
		}
		
		if (messageBean.getMsgId() != null) {
			messageBean.setMsgRefId(messageBean.getMsgId());
		}
		Address[] addresses = InternetAddress.parse(forwardAddrs);
		Message msg = null;
		byte[] stream = (byte[]) messageBean.getHashMap().get(MessageBeanBuilder.MSG_RAW_STREAM);
		if (stream == null) { // just for safety
			try {
				msg = MessageBeanUtil.createMimeMessage(messageBean);
			}
			catch (IOException e) {
				logger.error("IOException caught", e);
				throw new MessagingException("IOException caught, " + e);
			}
		}
		else {
			msg = MessageBeanUtil.createMimeMessage(stream);
			MessageBeanUtil.addBeanFieldsToHeader(messageBean, msg);
		}
		//msg.removeHeader("Received"); // remove "Received" history
		//msg.removeHeader("Delivered-To"); // remove delivery history
		
		msg.setSubject("Fwd: " + messageBean.getSubject());
		msg.setRecipients(Message.RecipientType.TO, addresses);
		msg.setRecipients(Message.RecipientType.CC, null);
		msg.setRecipients(Message.RecipientType.BCC, null);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		msg.writeTo(baos);
		
		// TODO send the message off
		if (isDebugEnabled)
			logger.debug("Jms Message Id returned: ");
		return Integer.valueOf(addresses.length);
	}
}
