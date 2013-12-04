package com.es.bo.sender;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.core.util.HtmlUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.constant.EmailAddressType;
import com.es.data.constant.MsgDirectionCode;
import com.es.exception.DataValidationException;
import com.es.msg.util.RfcHeaderParser;
import com.es.msgbean.BodypartBean;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MsgHeader;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.inbox.MsgAddressVo;
import com.es.vo.inbox.MsgAttachmentVo;
import com.es.vo.inbox.MsgHeaderVo;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.inbox.MsgRfcFieldVo;
import com.es.vo.outbox.DeliveryStatusVo;

@Component("messageBeanBo")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageBeanBo implements java.io.Serializable {
	private static final long serialVersionUID = 6112004375703794740L;
	static Logger logger = Logger.getLogger(MessageBeanBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressDao emailAddrDao;
	
	/**
	 * create a MessageBean object from a MsgInboxVo object.
	 * 
	 * @param msgVo -
	 *            MsgInboxVo
	 * @return MessageBean
	 * @throws DataValidationException
	 */
	public MessageBean createMessageBean(MsgInboxVo msgVo) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering createMessageBean() method...");
		}
		if (msgVo == null) {
			throw new DataValidationException("Input msgInboxVo is null");
		}

		MessageBean msgBean = new MessageBean();
		msgBean.setMsgId(Long.valueOf(msgVo.getMsgId()));
		msgBean.setMsgRefId(msgVo.getMsgRefId());
		msgBean.setCarrierCode(CarrierCode.getByValue(msgVo.getCarrierCode()));
		msgBean.setSubject(msgVo.getMsgSubject());
		msgBean.setPriority(new String[] {msgVo.getMsgPriority()});
		msgBean.setSendDate(msgVo.getReceivedTime());
		
		msgBean.setIsReceived(MsgDirectionCode.RECEIVED.getValue().equals(msgVo.getMsgDirection()));
		msgBean.setSenderId(msgVo.getSenderId());
		msgBean.setSubrId(msgVo.getSubrId());
		msgBean.setSmtpMessageId(msgVo.getSmtpMessageId());
		msgBean.setRenderId(msgVo.getRenderId());
		msgBean.setOverrideTestAddr(CodeType.YES_CODE.getValue().equals(msgVo.getOverrideTestAddr()));

		msgBean.setRuleName(msgVo.getRuleName());
		
		// set message body and attachments
		String msgBody = msgVo.getMsgBody();
		msgBean.setContentType(msgVo.getMsgContentType());
		List<MsgAttachmentVo> attchs = msgVo.getAttachments();
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
				MsgAttachmentVo vo = attchs.get(i);
				BodypartBean subNode = new BodypartBean();
				subNode.setContentType(vo.getAttchmntType());
				subNode.setDisposition(vo.getAttchmntDisp());
				subNode.setDescription(vo.getAttchmntName());
				byte[] bytes = vo.getAttchmntValue();
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
		
		List<MsgRfcFieldVo> rfcList = msgVo.getRfcFields();
		if (rfcList!=null && rfcList.size()>0) {
			for (MsgRfcFieldVo rfc : rfcList) {
				BodypartBean aNode = new BodypartBean();
				aNode.setContentType(rfc.getRfcType());
				if (isRfc822(rfc)) {
					msgBean.setOrigSubject(rfc.getOrigMsgSubject());
					msgBean.setSmtpMessageId(rfc.getMessageId());
					msgBean.setDsnRfc822(rfc.getDsnRfc822());
					msgBean.setDsnText(rfc.getDsnText());
					aNode.setValue(rfc.getDsnRfc822());
				}
				if (StringUtils.isNotBlank(rfc.getOrigRcpt())) {
					msgBean.setOrigRcpt(rfc.getOrigRcpt());
				}
				msgBean.setFinalRcpt(rfc.getFinalRcpt());
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
						List<MsgHeader> headers = RfcHeaderParser.parseRfc822Headers(rfc.getDsnRfc822());
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
						if (rfc.getOrigMsgSubject()!=null) {
							MsgHeader header = new MsgHeader();
							header.setName("Subject");
							header.setValue(rfc.getOrigMsgSubject());
							headers.add(header);
						}
						if (rfc.getOrigRcpt()!=null) {
							MsgHeader header = new MsgHeader();
							header.setName("To");
							header.setValue(rfc.getOrigRcpt());
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
		List<MsgHeaderVo> headersVo = msgVo.getMsgHeaders();
		if (headersVo != null) {
			List<MsgHeader> headers = new ArrayList<MsgHeader>(); 
			for (int i = 0; i < headersVo.size(); i++) {
				MsgHeaderVo msgHeadersVo = headersVo.get(i);
				MsgHeader header = new MsgHeader();
				header.setName(msgHeadersVo.getHeaderName());
				header.setValue(msgHeadersVo.getHeaderValue());
				headers.add(header);
			}
			msgBean.setHeaders(headers);
		}

		// set addresses
		List<MsgAddressVo> addrsVo = msgVo.getMsgAddrs();
		if (addrsVo != null) {
			String fromAddr = null;
			String toAddr = null;
			String replyToAddr = null;
			String ccAddr = null;
			String bccAddr = null;
			for (int i = 0; i < addrsVo.size(); i++) {
				MsgAddressVo addrVo = addrsVo.get(i);
				if (EmailAddressType.FROM_ADDR.getValue().equalsIgnoreCase(addrVo.getAddrType())) {
					if (fromAddr == null) {
						fromAddr = addrVo.getAddrValue();
					}
					else {
						fromAddr += "," + addrVo.getAddrValue();
					}
				}
				else if (EmailAddressType.TO_ADDR.getValue().equalsIgnoreCase(addrVo.getAddrType())) {
					if (toAddr == null)
						toAddr = addrVo.getAddrValue();
					else
						toAddr += "," + addrVo.getAddrValue();
				}
				else if (EmailAddressType.REPLYTO_ADDR.getValue().equalsIgnoreCase(addrVo.getAddrType())) {
					if (replyToAddr == null)
						replyToAddr = addrVo.getAddrValue();
					else
						replyToAddr += "," + addrVo.getAddrValue();
				}
				else if (EmailAddressType.CC_ADDR.getValue().equalsIgnoreCase(addrVo.getAddrType())) {
					if (ccAddr == null)
						ccAddr = addrVo.getAddrValue();
					else
						ccAddr += "," + addrVo.getAddrValue();
				}
				else if (EmailAddressType.BCC_ADDR.getValue().equalsIgnoreCase(addrVo.getAddrType())) {
					if (bccAddr == null)
						bccAddr = addrVo.getAddrValue();
					else
						bccAddr += "," + addrVo.getAddrValue();
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

	private void setDeliveryStatus(MsgInboxVo msgVo, MessageBean msgBean, BodypartBean subNode) {
		List<DeliveryStatusVo> statusList = msgVo.getDeliveryStatus();
		if (statusList!=null && statusList.size()>0) {
			for (DeliveryStatusVo status : statusList) {
				BodypartBean aNode = new BodypartBean();
				aNode.setContentType("message/delivery-status");
				aNode.setValue(status.getDeliveryStatus());
				aNode.setSize(aNode.getValue().length);
				subNode.put(aNode);
				msgBean.setSmtpMessageId(status.getMessageId());
				msgBean.setDsnDlvrStat(status.getDeliveryStatus());
				msgBean.setDiagnosticCode(status.getDsnReason());
				msgBean.setDsnStatus(status.getDsnStatus());
				msgBean.setDsnText(status.getDsnText());
				msgBean.setFinalRcpt(status.getFinalRecipient());
				if (status.getOriginalRecipientId()!=null) {
					EmailAddressVo origAddr = emailAddrDao.getByAddrId(status.getOriginalRecipientId());
					msgBean.setOrigRcpt(origAddr.getEmailAddr());
				}
 				List<MsgHeader> headers = new ArrayList<MsgHeader>(); 
				if (status.getMessageId()!=null) {
					MsgHeader header = new MsgHeader();
					header.setName("Message-Id");
					header.setValue(status.getMessageId());
					headers.add(header);
				}
				if (status.getFinalRecipient()!=null) {
					MsgHeader header = new MsgHeader();
					header.setName("Final-Recipient");
					header.setValue("rfc822;" + status.getFinalRecipient());
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

	boolean isReport(DeliveryStatusVo rfc) {
		if (StringUtils.isNotBlank(rfc.getDsnReason())
				|| StringUtils.isNotBlank(rfc.getDsnStatus())
				|| StringUtils.isNotBlank(rfc.getDeliveryStatus())) {
			return true;
		}
		return false;
	}

	boolean isRfc822(MsgRfcFieldVo rfc) {
		if (StringUtils.isNotBlank(rfc.getMessageId())
				|| StringUtils.isNotBlank(rfc.getOrigMsgSubject())
				|| StringUtils.isNotBlank(rfc.getDsnRfc822())
				|| StringUtils.isNotBlank(rfc.getDsnText())) {
			return true;
		}
		return false;
	}
}
