package jpa.service.msgout;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.EmailAddrType;
import jpa.constant.MsgDirectionCode;
import jpa.exception.DataValidationException;
import jpa.message.BodypartBean;
import jpa.message.MessageBean;
import jpa.message.MsgHeader;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.model.message.MessageAddress;
import jpa.model.message.MessageAttachment;
import jpa.model.message.MessageHeader;
import jpa.model.message.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.EmailAddressService;
import jpa.service.SenderDataService;
import jpa.service.SubscriberDataService;
import jpa.service.rule.RuleLogicService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageBeanBo")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageBeanBo {
	static Logger logger = Logger.getLogger(MessageBeanBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * create a MessageBean object from a MessageInbox object.
	 * 
	 * @param msgVo -
	 *            MsgInboxVo
	 * @return MessageBean
	 * @throws DataValidationException
	 */
	public MessageBean createMessageBean(MessageInbox msgVo) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering createMessageBean() method...");
		if (msgVo == null) {
			throw new DataValidationException("Input msgInboxVo is null");
		}

		MessageBean msgBean = new MessageBean();
		msgBean.setMsgId(Integer.valueOf(msgVo.getRowId()));
		msgBean.setMsgRefId(msgVo.getReferringMessageRowId());
		msgBean.setCarrierCode(CarrierCode.getByValue(msgVo.getCarrierCode()));
		msgBean.setSubject(msgVo.getMsgSubject());
		msgBean.setPriority(new String[] {msgVo.getMsgPriority()});
		msgBean.setSendDate(msgVo.getReceivedTime());
		
		msgBean.setIsReceived(MsgDirectionCode.RECEIVED.getValue().equals(msgVo.getMsgDirection()));
		if (msgVo.getSenderData()==null && msgVo.getSenderDataRowId()!=null) {
			SenderDataService senderService = (SenderDataService) SpringUtil.getAppContext().getBean("senderDataService");
			try {
				SenderData sender = senderService.getByRowId(msgVo.getSenderDataRowId());
				msgVo.setSenderData(sender);
			}
			catch (NoResultException e) {}
		}
		if (msgVo.getSenderData()!=null) {
			msgBean.setSenderId(msgVo.getSenderData().getSenderId());
		}
		if (msgVo.getSubscriberData()==null && msgVo.getSubscriberDataRowId()!=null) {
			SubscriberDataService subrService = (SubscriberDataService) SpringUtil.getAppContext().getBean("subscriberDataService");
			try {
				SubscriberData subr = subrService.getByRowId(msgVo.getSubscriberDataRowId());
				msgVo.setSubscriberData(subr);
			}
			catch (NoResultException e) {}
		}
		if (msgVo.getSubscriberData()!=null) {
			msgBean.setSubrId(msgVo.getSubscriberData().getSubscriberId());
		}
		msgBean.setSmtpMessageId(msgVo.getSmtpMessageId());
		msgBean.setRenderId(msgVo.getRenderId());
		msgBean.setOverrideTestAddr(msgVo.isOverrideTestAddr());
		if (msgVo.getRuleLogic()==null) {
			RuleLogicService logicService = (RuleLogicService) SpringUtil.getAppContext().getBean("ruleLogicService");
			try {
				RuleLogic logic = logicService.getByRowId(msgVo.getRuleLogicRowId());
				msgVo.setRuleLogic(logic);
			}
			catch (NoResultException e) {}
		}
		if (msgVo.getRuleLogic()!=null) {
			msgBean.setRuleName(msgVo.getRuleLogic().getRuleName());
		}
		
		// set message body and attachments
		String msgBody = msgVo.getMsgBody();
		msgBean.setContentType(msgVo.getMsgContentType());
		List<MessageAttachment> attchs = msgVo.getMessageAttachmentList();
		if (attchs != null && !attchs.isEmpty()) {
			// construct a multipart (/mixed)
			// message body part
			BodypartBean aNode = new BodypartBean();
			aNode.setContentType(msgVo.getBodyContentType());
			aNode.setValue(msgBody);
			aNode.setSize(msgBody == null ? 0 : msgBody.length());
			msgBean.put(aNode);
			// attachments
			for (int i = 0; i < attchs.size(); i++) {
				MessageAttachment vo = attchs.get(i);
				BodypartBean subNode = new BodypartBean();
				subNode.setContentType(vo.getAttachmentType());
				subNode.setDisposition(vo.getAttachmentDisp());
				subNode.setDescription(vo.getAttachmentName());
				byte[] bytes = vo.getAttachmentValue();
				if (bytes != null) {
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					subNode.setValue(bais);
				}
				subNode.setSize(vo.getAttachmentSize());
				msgBean.put(subNode);
				msgBean.updateAttachCount(1);
			}
		}
		else if (msgVo.getMsgContentType().startsWith("multipart/")) {
			// multipart/alternative
			BodypartBean aNode = new BodypartBean();
			aNode.setContentType(msgVo.getBodyContentType());
			aNode.setValue(msgBody);
			aNode.setSize(msgBody == null ? 0 : msgBody.length());
			msgBean.put(aNode);
		}
		else {
			msgBean.setBody(msgBody);
		}
		
		// set message headers
		List<MessageHeader> headersVo = msgVo.getMessageHeaderList();
		if (headersVo != null) {
			List<MsgHeader> headers = new ArrayList<MsgHeader>(); 
			for (int i = 0; i < headersVo.size(); i++) {
				MessageHeader msgHeadersVo = headersVo.get(i);
				MsgHeader header = new MsgHeader();
				header.setName(msgHeadersVo.getHeaderName());
				header.setValue(msgHeadersVo.getHeaderValue());
				headers.add(header);
			}
			msgBean.setHeaders(headers);
		}

		// set addresses
		List<MessageAddress> addrsVo = msgVo.getMessageAddressList();
		if (addrsVo != null) {
			EmailAddressService emailService = (EmailAddressService) SpringUtil.getAppContext().getBean("emailAddressService");
			String fromAddr = null;
			String toAddr = null;
			String replyToAddr = null;
			String ccAddr = null;
			String bccAddr = null;
			for (int i = 0; i < addrsVo.size(); i++) {
				MessageAddress addrVo = addrsVo.get(i);
				EmailAddress addr = emailService.getByRowId(addrVo.getEmailAddrRowId());
				if (EmailAddrType.FROM_ADDR.getValue().equalsIgnoreCase(addrVo.getAddressType())) {
					if (fromAddr == null) {
						fromAddr = addr.getAddress();
					}
					else {
						fromAddr += "," + addr.getAddress();
					}
				}
				else if (EmailAddrType.TO_ADDR.getValue().equalsIgnoreCase(addrVo.getAddressType())) {
					if (toAddr == null)
						toAddr = addr.getAddress();
					else
						toAddr += "," + addr.getAddress();
				}
				else if (EmailAddrType.REPLYTO_ADDR.getValue().equalsIgnoreCase(addrVo.getAddressType())) {
					if (replyToAddr == null)
						replyToAddr = addr.getAddress();
					else
						replyToAddr += "," + addr.getAddress();
				}
				else if (EmailAddrType.CC_ADDR.getValue().equalsIgnoreCase(addrVo.getAddressType())) {
					if (ccAddr == null)
						ccAddr = addr.getAddress();
					else
						ccAddr += "," + addr.getAddress();
				}
				else if (EmailAddrType.BCC_ADDR.getValue().equalsIgnoreCase(addrVo.getAddressType())) {
					if (bccAddr == null)
						bccAddr = addr.getAddress();
					else
						bccAddr += "," + addr.getAddress();
				}
			}
			if (fromAddr != null) {
				try {
					Address[] from = InternetAddress.parse(fromAddr);
					msgBean.setFrom(from);
				}
				catch (AddressException e) {
					logger.error("AddressException caught parsing From Address", e);
				}
			}
			if (toAddr != null) {
				try {
					Address[] to = InternetAddress.parse(toAddr);
					msgBean.setTo(to);
				}
				catch (AddressException e) {
					logger.error("AddressException caught parsing To Address", e);
				}
			}
			if (replyToAddr != null) {
				try {
					Address[] replyTo = InternetAddress.parse(replyToAddr);
					msgBean.setReplyto(replyTo);
				}
				catch (AddressException e) {
					logger.error("AddressException caught parsing ReplyTo Address", e);
				}
			}
			if (ccAddr != null) {
				try {
					Address[] cc = InternetAddress.parse(ccAddr);
					msgBean.setCc(cc);
				}
				catch (AddressException e) {
					logger.error("AddressException caught parsing Cc Address", e);
				}
			}
			if (bccAddr != null) {
				try {
					Address[] bcc = InternetAddress.parse(bccAddr);
					msgBean.setBcc(bcc);
				}
				catch (AddressException e) {
					logger.error("AddressException caught parsing Bcc Address", e);
				}
			}
		}
		
		//if (isDebugEnabled) {
		//	logger.debug("createMessageBean() - MessageBean created:" + LF + msgBean);
		//}
		return msgBean;
	}

}
