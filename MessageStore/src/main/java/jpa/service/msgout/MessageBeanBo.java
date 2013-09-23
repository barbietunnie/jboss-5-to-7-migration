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
import jpa.message.util.MsgHeaderUtil;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.model.message.MessageAddress;
import jpa.model.message.MessageAttachment;
import jpa.model.message.MessageDeliveryStatus;
import jpa.model.message.MessageHeader;
import jpa.model.message.MessageInbox;
import jpa.model.message.MessageRfcField;
import jpa.model.rule.RuleLogic;
import jpa.service.EmailAddressService;
import jpa.service.SenderDataService;
import jpa.service.SubscriberDataService;
import jpa.service.rule.RuleLogicService;
import jpa.util.HtmlUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("messageBeanBo")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageBeanBo {
	static Logger logger = Logger.getLogger(MessageBeanBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private SubscriberDataService subrService;
	@Autowired
	private RuleLogicService logicService;
	@Autowired
	private EmailAddressService emailService;
	
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
		
		setDeliveryStatus(msgVo, msgBean, msgBean);
		
		List<MessageRfcField> rfcList = msgVo.getMessageRfcFieldList();
		if (rfcList!=null && rfcList.size()>0) {
			for (MessageRfcField rfc : rfcList) {
				BodypartBean aNode = new BodypartBean();
				aNode.setContentType(rfc.getMessageRfcFieldPK().getRfcType());
				if (isRfc822(rfc)) {
					msgBean.setOrigSubject(rfc.getOriginalMsgSubject());
					msgBean.setSmtpMessageId(rfc.getMessageId());
					msgBean.setDsnRfc822(rfc.getDsnRfc822());
					msgBean.setDsnText(rfc.getDsnText());
					aNode.setValue(rfc.getDsnRfc822());
				}
				if (StringUtils.isNotBlank(rfc.getOriginalRecipient())) {
					msgBean.setOrigRcpt(rfc.getOriginalRecipient());
				}
				if (rfc.getFinalRcptAddrRowId()!=null) {
					try {
						EmailAddress finalRcpt = emailService.getByRowId(rfc.getFinalRcptAddrRowId());
						msgBean.setFinalRcpt(finalRcpt.getAddress());
					}
					catch (NoResultException e) {}
				}
				aNode.setSize(aNode.getValue()==null?0:aNode.getValue().length);
				if (aNode.getValue()!=null || StringUtils.isNotBlank(msgBean.getDsnText())) {
					BodypartBean textNode = new BodypartBean();
					if (HtmlUtil.isHTML(msgBean.getDsnText())) {
						textNode.setContentType("text/html");
					}
					else {
						textNode.setContentType("text/plain");
					}
					String value = "";
					if (StringUtils.isNotBlank(msgBean.getDsnRfc822())) {
						value = msgBean.getDsnRfc822();
					}
					if (StringUtils.isNotBlank(msgBean.getDsnText())) {
						value += msgBean.getDsnText();
					}
					textNode.setValue(value);
					textNode.setSize(textNode.getValue().length);
					aNode.put(textNode);
					if (rfc.getDsnRfc822()!=null) {
						List<MsgHeader> headers = MsgHeaderUtil.parseRfc822Headers(rfc.getDsnRfc822());
						textNode.setHeaders(headers);
					}
					else {
						List<MsgHeader> headers = new ArrayList<MsgHeader>();
						if (rfc.getMessageId()!=null) {
							MsgHeader header = new MsgHeader();
							header.setName("Message-Id");
							header.setValue(rfc.getMessageId());
							headers.add(header);
						}
						if (rfc.getOriginalMsgSubject()!=null) {
							MsgHeader header = new MsgHeader();
							header.setName("Subject");
							header.setValue(rfc.getOriginalMsgSubject());
							headers.add(header);
						}
						if (rfc.getOriginalRecipient()!=null) {
							MsgHeader header = new MsgHeader();
							header.setName("To");
							header.setValue(rfc.getOriginalRecipient());
							headers.add(header);
						}
						if (StringUtils.isNotBlank(msgBean.getFinalRcpt())) {
							MsgHeader header = new MsgHeader();
							header.setName("Final-Recipient");
							header.setValue(msgBean.getFinalRcpt());
							headers.add(header);
						}
						if (StringUtils.isNotBlank(msgBean.getOrigRcpt())) {
							MsgHeader header = new MsgHeader();
							header.setName("Original-Recipient");
							header.setValue(msgBean.getOrigRcpt());
							headers.add(header);
						}
						if (headers.size()>0) {
							textNode.setHeaders(headers);
						}
					}
				}
				msgBean.put(aNode);
			}
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

	private void setDeliveryStatus(MessageInbox msgVo, MessageBean msgBean, BodypartBean subNode) {
		List<MessageDeliveryStatus> statusList = msgVo.getMessageDeliveryStatusList();
		if (statusList!=null && statusList.size()>0) {
			for (MessageDeliveryStatus status : statusList) {
				BodypartBean aNode = new BodypartBean();
				aNode.setContentType("message/delivery-status");
				aNode.setValue(status.getDeliveryStatus());
				aNode.setSize(aNode.getValue().length);
				subNode.put(aNode);
				msgBean.setSmtpMessageId(status.getSmtpMessageId());
				msgBean.setDsnDlvrStat(status.getDeliveryStatus());
				msgBean.setDiagnosticCode(status.getDsnReason());
				msgBean.setDsnStatus(status.getDsnStatus());
				msgBean.setDsnText(status.getDsnText());
				msgBean.setFinalRcpt(status.getFinalRecipientAddress());
				if (status.getOriginalRcptAddrRowId()!=null) {
					try {
						EmailAddress origAddr = emailService.getByRowId(status.getOriginalRcptAddrRowId());
						msgBean.setOrigRcpt(origAddr.getAddress());
					}
					catch (NoResultException e) {}
				}
 				List<MsgHeader> headers = new ArrayList<MsgHeader>(); 
				if (status.getSmtpMessageId()!=null) {
					MsgHeader header = new MsgHeader();
					header.setName("Message-Id");
					header.setValue(status.getSmtpMessageId());
					headers.add(header);
				}
				if (status.getFinalRecipientAddress()!=null) {
					MsgHeader header = new MsgHeader();
					header.setName("Final-Recipient");
					header.setValue("rfc822;" + status.getFinalRecipientAddress());
					headers.add(header);
				}
				if (StringUtils.isNotBlank(msgBean.getOrigRcpt())) {
					MsgHeader header = new MsgHeader();
					header.setName("To");
					header.setValue(msgBean.getOrigRcpt());
					headers.add(header);
				}
				if (status.getDsnReason()!=null) {
					MsgHeader header = new MsgHeader();
					header.setName("Action");
					header.setValue(status.getDsnReason());
					headers.add(header);
				}
				if (status.getDsnStatus()!=null) {
					MsgHeader header = new MsgHeader();
					header.setName("Status");
					header.setValue(status.getDsnStatus());
					headers.add(header);
				}
				if (headers.size()>0) {
					subNode.setHeaders(headers);
				}
			}
		}
	}

	boolean isReport(MessageDeliveryStatus rfc) {
		if (StringUtils.isNotBlank(rfc.getDsnReason())
				|| StringUtils.isNotBlank(rfc.getDsnStatus())
				|| StringUtils.isNotBlank(rfc.getDeliveryStatus())) {
			return true;
		}
		return false;
	}

	boolean isRfc822(MessageRfcField rfc) {
		if (StringUtils.isNotBlank(rfc.getMessageId())
				|| StringUtils.isNotBlank(rfc.getOriginalMsgSubject())
				|| StringUtils.isNotBlank(rfc.getDsnRfc822())
				|| StringUtils.isNotBlank(rfc.getDsnText())) {
			return true;
		}
		return false;
	}
}