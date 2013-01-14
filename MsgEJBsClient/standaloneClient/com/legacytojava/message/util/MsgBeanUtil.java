package com.legacytojava.message.util;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.List;

import javax.ejb.CreateException;
import javax.mail.Address;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.legacytojava.message.bean.BodypartBean;
import com.legacytojava.message.bean.BodypartUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanUtil;
import com.legacytojava.message.bean.MessageNode;
import com.legacytojava.message.bean.MsgHeader;
import com.legacytojava.message.bo.customer.CustomerBo;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.MsgDirectionCode;
import com.legacytojava.message.constant.MsgStatusCode;
import com.legacytojava.message.ejb.emailaddr.EmailAddrRemote
;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.AttachmentsVo;
import com.legacytojava.message.vo.inbox.MsgAddrsVo;
import com.legacytojava.message.vo.inbox.MsgHeadersVo;
import com.legacytojava.message.vo.inbox.MsgInboxVo;
import com.legacytojava.message.vo.inbox.RfcFieldsVo;

public class MsgBeanUtil {
	static final Logger logger = Logger.getLogger(MsgBeanUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	/**
	 * convert a MessageBean to MsgInboxVo object.
	 * TODO: please refer to MsgInboxBoImpl.java
	 * 
	 * @param msgBean -
	 *            message bean
	 * @return MsgInboxVo
	 * @throws RemoteException
	 * @throws CreateException
	 */
	public static MsgInboxVo messsageBeanToVo(MessageBean msgBean) throws RemoteException,
			CreateException {
		
		MsgInboxVo msgInboxVo = new MsgInboxVo();
		if (msgBean.getMsgId() != null) {
			msgInboxVo.setMsgId(msgBean.getMsgId());
		}
		msgInboxVo.setMsgRefId(msgBean.getMsgRefId());
		msgInboxVo.setCarrierCode(StringUtils.left(msgBean.getCarrierCode(),1));
		msgInboxVo.setMsgSubject(StringUtils.left(msgBean.getSubject(),255));
		msgInboxVo.setMsgPriority(MessageBeanUtil.getMsgPriority(msgBean.getPriority()));
		if (msgBean.getSendDate() != null) {
			msgInboxVo.setReceivedTime(new Timestamp(msgBean.getSendDate().getTime()));
		}
		else {
			msgInboxVo.setReceivedTime(new Timestamp(new java.util.Date().getTime()));
		}
		msgInboxVo.setFromAddrId(getEmailAddrId(msgBean.getFrom()));
		msgInboxVo.setReplyToAddrId(getEmailAddrId(msgBean.getReplyto()));
		msgInboxVo.setToAddrId(getEmailAddrId(msgBean.getTo()));
		
		msgInboxVo.setClientId(StringUtils.left(msgBean.getClientId(),16));
		msgInboxVo.setCustId(StringUtils.left(msgBean.getCustId(),CustomerBo.CUSTOMER_ID_MAX_LEN));
		msgInboxVo.setPurgeDate(null);
		msgInboxVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		msgInboxVo.setLockTime(null);
		msgInboxVo.setLockId(null);
		
		msgInboxVo.setMsgContentType(StringUtils.left(msgBean.getContentType(),100));
		msgInboxVo.setBodyContentType(StringUtils.left(msgBean.getBodyContentType(),50));
		msgInboxVo.setMsgBody(msgBean.getBody());
		msgInboxVo.setRuleName(StringUtils.left(msgBean.getRuleName(),26));
		msgInboxVo.setMsgDirection(MsgDirectionCode.MSG_RECEIVED);
		msgInboxVo.setStatusId(MsgStatusCode.OPENED);

		msgInboxVo.setRenderId(msgBean.getRenderId());
		msgInboxVo.setOverrideTestAddr(msgBean.getOverrideTestAddr() ? Constants.YES_CODE
				: Constants.NO_CODE);

		// retrieve attachments into an array from MessageBean
		BodypartUtil.retrieveAttachments(msgBean);
		
		List<MsgHeader> headers = msgBean.getHeaders();
		for (int i=0; i<headers.size(); i++) {
			MsgHeader header = headers.get(i);
			MsgHeadersVo msgHeadersVo = new MsgHeadersVo();
			msgHeadersVo.setHeaderName(StringUtils.left(header.getName(),100));
			msgHeadersVo.setHeaderSeq(i+1);
			msgHeadersVo.setHeaderValue(header.getValue());
			msgInboxVo.getMsgHeaders().add(msgHeadersVo);
		}
		
		setMsgAddrs(msgInboxVo, msgBean);
		
		setAttachments(msgInboxVo, msgBean, (BodypartBean) msgBean);
		
		setRfcFields(msgInboxVo, msgBean);
		
		return msgInboxVo;
	}
	
	private static void setMsgAddrs(MsgInboxVo msgInboxVo, MessageBean msgBean) {
		if (msgBean.getFrom() != null && msgBean.getFrom().length > 0) {
			for (int i = 0; i < msgBean.getFrom().length; i++) {
				MsgAddrsVo msgAddrsVo = new MsgAddrsVo();
				msgAddrsVo.setAddrType(EmailAddressType.FROM_ADDR);
				msgAddrsVo.setAddrSeq(i + 1);
				msgAddrsVo.setAddrValue(StringUtils.left(msgBean.getFrom()[i].toString(),255));
				msgInboxVo.getMsgAddrs().add(msgAddrsVo);
			}
		}
		if (msgBean.getTo() != null && msgBean.getTo().length > 0) {
			for (int i = 0; i < msgBean.getTo().length; i++) {
				MsgAddrsVo msgAddrsVo = new MsgAddrsVo();
				msgAddrsVo.setAddrType(EmailAddressType.TO_ADDR);
				msgAddrsVo.setAddrSeq(i + 1);
				msgAddrsVo.setAddrValue(StringUtils.left(msgBean.getTo()[i].toString(),255));
				msgInboxVo.getMsgAddrs().add(msgAddrsVo);
			}
		}
		if (msgBean.getReplyto() != null && msgBean.getReplyto().length > 0) {
			for (int i = 0; i < msgBean.getReplyto().length; i++) {
				MsgAddrsVo msgAddrsVo = new MsgAddrsVo();
				msgAddrsVo.setAddrType(EmailAddressType.REPLYTO_ADDR);
				msgAddrsVo.setAddrSeq(i + 1);
				msgAddrsVo.setAddrValue(StringUtils.left(msgBean.getReplyto()[i].toString(),255));
				msgInboxVo.getMsgAddrs().add(msgAddrsVo);
			}
		}
		if (msgBean.getCc() != null && msgBean.getCc().length > 0) {
			for (int i = 0; i < msgBean.getCc().length; i++) {
				MsgAddrsVo msgAddrsVo = new MsgAddrsVo();
				msgAddrsVo.setAddrType(EmailAddressType.CC_ADDR);
				msgAddrsVo.setAddrSeq(i + 1);
				msgAddrsVo.setAddrValue(StringUtils.left(msgBean.getCc()[i].toString(),255));
				msgInboxVo.getMsgAddrs().add(msgAddrsVo);
			}
		}
		if (msgBean.getBcc() != null && msgBean.getBcc().length > 0) {
			for (int i = 0; i < msgBean.getBcc().length; i++) {
				MsgAddrsVo msgAddrsVo = new MsgAddrsVo();
				msgAddrsVo.setAddrType(EmailAddressType.BCC_ADDR);
				msgAddrsVo.setAddrSeq(i + 1);
				msgAddrsVo.setAddrValue(StringUtils.left(msgBean.getBcc()[i].toString(),255));
				msgInboxVo.getMsgAddrs().add(msgAddrsVo);
			}
		}
	}
	
	private static void setAttachments(MsgInboxVo msgInboxVo, MessageBean msgBean,
			BodypartBean aNode) {
		if (aNode == null) {
			return;
		}
		List<MessageNode> nodes = msgBean.getAttachments();
		for (int i=0; nodes!=null && i<nodes.size(); i++) {
			MessageNode node = nodes.get(i);
			BodypartBean subNode = (BodypartBean)node.getBodypartNode();
			AttachmentsVo attachmentsVo = new AttachmentsVo();
			attachmentsVo.setAttchmntDepth(node.getLevel());
			attachmentsVo.setAttchmntSeq(i+1);
			attachmentsVo.setAttchmntName(StringUtils.left(subNode.getDescription(),100));
			attachmentsVo.setAttchmntType(StringUtils.left(subNode.getMimeType(),100));
			attachmentsVo.setAttchmntDisp(StringUtils.left(subNode.getDisposition(),100));
			attachmentsVo.setAttchmntValue(subNode.getValue());
			msgInboxVo.getAttachments().add(attachmentsVo);
		}
	}
	
	private static void setRfcFields(MsgInboxVo msgInboxVo, MessageBean msgBean)
			throws RemoteException, CreateException {
		if (msgBean.getReport() != null) {
			MessageNode mNode = msgBean.getReport();
			BodypartBean aNode = mNode.getBodypartNode();
			RfcFieldsVo rfcFieldsVo = new RfcFieldsVo();
			rfcFieldsVo.setMsgId(msgInboxVo.getMsgId());
			rfcFieldsVo.setRfcType(StringUtils.left(aNode.getContentType(),30));
			rfcFieldsVo.setRfcStatus(StringUtils.left(msgBean.getDsnStatus(),30));
			rfcFieldsVo.setRfcAction(StringUtils.left(msgBean.getDsnAction(),30));
			rfcFieldsVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
			rfcFieldsVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
			rfcFieldsVo.setOrigRcpt(StringUtils.left(msgBean.getOrigRcpt(),255));
			//rfcFieldsVo.setOrigMsgSubject(StringUtil.cut(msgBean.getOrigSubject(),255));
			//rfcFieldsVo.setMessageId(StringUtil.cut(msgBean.getMessageId(),255));
			rfcFieldsVo.setDsnText(msgBean.getDsnText());
			rfcFieldsVo.setDsnRfc822(msgBean.getDiagnosticCode());
			rfcFieldsVo.setDlvrStatus(msgBean.getDsnDlvrStat());
			msgInboxVo.getRfcFields().add(rfcFieldsVo);
		}
		if (msgBean.getRfc822() != null) {
			MessageNode mNode = msgBean.getRfc822();
			BodypartBean aNode = mNode.getBodypartNode();
			RfcFieldsVo rfcFieldsVo = new RfcFieldsVo();
			rfcFieldsVo.setMsgId(msgInboxVo.getMsgId());
			rfcFieldsVo.setRfcType(StringUtils.left(aNode.getContentType(),30));
			//rfcFieldsVo.setRfcStatus(StringUtil.cut(msgBean.getDsnStatus(),30));
			//rfcFieldsVo.setRfcAction(StringUtil.cut(msgBean.getDsnAction(),30));
			rfcFieldsVo.setFinalRcpt(StringUtils.left(msgBean.getFinalRcpt(),255));
			rfcFieldsVo.setFinalRcptId(getEmailAddrId(msgBean.getFinalRcpt()));
			//rfcFieldsVo.setOrigRcpt(StringUtil.cut(msgBean.getOrigRcpt(),255));
			rfcFieldsVo.setOrigMsgSubject(StringUtils.left(msgBean.getOrigSubject(),255));
			rfcFieldsVo.setMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			rfcFieldsVo.setDsnText(msgBean.getDsnText());
			rfcFieldsVo.setDsnRfc822(msgBean.getDsnRfc822());
			msgInboxVo.getRfcFields().add(rfcFieldsVo);
		}
	}
	
	
	private static EmailAddrRemote emailAddrHome = null;
	
	private static EmailAddrRemote getEmailAddr() throws RemoteException, CreateException {
		if (emailAddrHome == null) {
			emailAddrHome = (EmailAddrRemote) LookupUtil.lookupRemoteEjb("ejb/EmailAddr");
		}
		return emailAddrHome;
	}
	
	private static Long getEmailAddrId(Address[] addresses) throws RemoteException, CreateException {
		if (addresses != null && addresses.length > 0) {
			return getEmailAddrId(addresses[0].toString());
		}
		else {
			return null;
		}
	}
	
	private static Long getEmailAddrId(String emailAddress) throws RemoteException, CreateException {
		if (!StringUtil.isEmpty(emailAddress)) {
			EmailAddrVo emailAddrVo = getEmailAddr().findByAddress(emailAddress);
			if (emailAddrVo != null) {
				return Long.valueOf(emailAddrVo.getEmailAddrId());
			}
		}
		return null;
	}
}
