package com.legacytojava.message.bo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanBuilder;
import com.legacytojava.message.bean.MessageBeanUtil;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.TableColumnName;
import com.legacytojava.message.dao.client.ClientDao;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.inbox.MsgStreamDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.ClientVo;

public class ForwardBoImpl extends TaskBaseAdaptor {
	static final Logger logger = Logger.getLogger(ForwardBoImpl.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private MsgStreamDao msgStreamDao;
	private ClientDao clientDao;

	/**
	 * Forward the message to the specified addresses. The forwarding addresses
	 * or address types are obtained from "DataTypeValues" column of MsgAction
	 * table. The original email raw stream should be included in the input
	 * MessageBean by the calling program.
	 * 
	 * @return a Long value representing number of addresses the message is
	 *         forwarded to.
	 */
	public Long process(MessageBean messageBean) throws DataValidationException,
			MessagingException, JMSException, IOException {
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
		
		String clientId = messageBean.getClientId();
		ClientVo clientVo = ClientUtil.getClientVo(clientId);
		if (clientVo == null) {
			throw new DataValidationException("Client record not found by clientId: " + clientId);
		}

		// example: $Forward,securityDept@mycompany.com
		String forwardAddrs = "";
		StringTokenizer st = new StringTokenizer(taskArguments, ",");
		while (st.hasMoreTokens()) {
			String addrs = null;
			String token = st.nextToken();
			if (token != null && token.startsWith("$")) { // address type
				token = token.substring(1);
				if (EmailAddressType.FROM_ADDR.equals(token)) {
					addrs = messageBean.getFromAsString();
				}
				else if (EmailAddressType.FINAL_RCPT_ADDR.equals(token)) {
					addrs = messageBean.getFinalRcpt();
				}
				else if (EmailAddressType.ORIG_RCPT_ADDR.equals(token)) {
					addrs = messageBean.getOrigRcpt();
				}
				else if (EmailAddressType.FORWARD_ADDR.equals(token)) {
					addrs = messageBean.getForwardAsString();
				}
				else if (EmailAddressType.TO_ADDR.equals(token)) {
					addrs = messageBean.getToAsString();
				}
				else if (EmailAddressType.REPLYTO_ADDR.equals(token)) {
					addrs = messageBean.getReplytoAsString();
				}
				// E-mail addresses from Client table
				else if (TableColumnName.CUSTOMER_CARE_ADDR.equals(token)) {
					addrs = clientVo.getCustcareEmail();
				}
				else if (TableColumnName.SECURITY_DEPT_ADDR.equals(token)) {
					addrs = clientVo.getSecurityEmail();
				}
				else if (TableColumnName.RMA_DEPT_ADDR.equals(token)) {
					addrs = clientVo.getRmaDeptEmail();
				}
				else if (TableColumnName.SPAM_CONTROL_ADDR.equals(token)) {
					addrs = clientVo.getSpamCntrlEmail();
				}
				else if (TableColumnName.VIRUS_CONTROL_ADDR.equals(token)) {
					addrs = clientVo.getSpamCntrlEmail();
				}
				else if (TableColumnName.CHALLENGE_HANDLER_ADDR.equals(token)) {
					addrs = clientVo.getChaRspHndlrEmail();
				}
				// end of Client table
			}
			else { // real email address
				addrs = token;
			}
			
			if (!StringUtil.isEmpty(addrs)) {
				try {
					InternetAddress.parse(addrs);
					if (StringUtil.isEmpty(forwardAddrs)) {
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
		
		setTargetToMailSender();
		String jmsMsgId = jmsProcessor.writeMsg(baos.toByteArray());
		if (isDebugEnabled)
			logger.debug("Jms Message Id returned: " + jmsMsgId);
		return Long.valueOf(addresses.length);
	}
	
	public MsgStreamDao getMsgStreamDao() {
		return msgStreamDao;
	}

	public void setMsgStreamDao(MsgStreamDao msgStreamDao) {
		this.msgStreamDao = msgStreamDao;
	}

	public ClientDao getClientDao() {
		return clientDao;
	}

	public void setClientDao(ClientDao clientDao) {
		this.clientDao = clientDao;
	}
}
