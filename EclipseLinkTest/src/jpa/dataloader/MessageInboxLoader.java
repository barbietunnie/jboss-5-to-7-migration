package jpa.dataloader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

import javax.mail.Part;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.constant.XHeaderName;
import jpa.data.preload.RuleNameEnum;
import jpa.model.ClientData;
import jpa.model.EmailAddr;
import jpa.model.MessageAddress;
import jpa.model.MessageAttachment;
import jpa.model.MessageAttachmentPK;
import jpa.model.MessageHeader;
import jpa.model.MessageHeaderPK;
import jpa.model.MessageInbox;
import jpa.model.MessageRfcField;
import jpa.model.MessageRfcFieldPK;
import jpa.model.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.EmailAddrService;
import jpa.service.MessageAddressService;
import jpa.service.MessageAttachmentService;
import jpa.service.MessageHeaderService;
import jpa.service.MessageInboxService;
import jpa.service.MessageRfcFieldService;
import jpa.service.RuleLogicService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class MessageInboxLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(MessageInboxLoader.class);
	private MessageInboxService service;
	private MessageAddressService msgAddrService;
	private ClientDataService clientService;
	private EmailAddrService emailAddrService;
	private RuleLogicService logicService;
	private MessageHeaderService headerService;
	private MessageAttachmentService attchmntService;
	private MessageRfcFieldService rfcService;

	public static void main(String[] args) {
		MessageInboxLoader loader = new MessageInboxLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (MessageInboxService) SpringUtil.getAppContext().getBean("messageInboxService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		emailAddrService = (EmailAddrService) SpringUtil.getAppContext().getBean("emailAddrService");
		logicService = (RuleLogicService) SpringUtil.getAppContext().getBean("ruleLogicService");
		msgAddrService = (MessageAddressService) SpringUtil.getAppContext().getBean("messageAddressService");
		headerService = (MessageHeaderService) SpringUtil.getAppContext().getBean("messageHeaderService");
		attchmntService = (MessageAttachmentService) SpringUtil.getAppContext().getBean("messageAttachmentService");
		rfcService = (MessageRfcFieldService) SpringUtil.getAppContext().getBean("messageRfcFieldService");
		startTransaction();
		try {
			loadMessageInbox();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadMessageInbox() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);

		MessageInbox data1 = new MessageInbox();
		data1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		data1.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		data1.setMsgSubject("Test Subject");
		data1.setMsgPriority("2 (Normal)");
		data1.setReceivedTime(updtTime);
		
		EmailAddr from = emailAddrService.findSertAddress("jsmith@test.com");
		data1.setFromAddrRowId(from.getRowId());
		data1.setReplytoAddrRowId(null);

		String to_addr = client.getReturnPathLeft() + "@" + client.getDomainName();
		EmailAddr to = emailAddrService.findSertAddress(to_addr);
		data1.setToAddrRowId(to.getRowId());
		data1.setClientDataRowId(client.getRowId());
		data1.setCustomerDataRowId(null);
		data1.setPurgeDate(null);
		data1.setUpdtTime(updtTime);
		data1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data1.setLockTime(null);
		data1.setLockId(null);
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.name());
		data1.setRuleLogicRowId(logic.getRowId());
		data1.setMsgContentType("multipart/mixed");
		data1.setBodyContentType("text/plain");
		data1.setMsgBody("Test Message Body");
		data1.setStatusId(MsgStatusCode.RECEIVED.getValue());
		service.insert(data1);

		MessageInbox data2 = new MessageInbox();
		data2.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		data2.setMsgDirection(MsgDirectionCode.SENT.getValue());
		data2.setMsgSubject("Test Broadcast Subject");
		data2.setMsgPriority("2 (Normal)");
		data2.setReceivedTime(updtTime);
		
		from = emailAddrService.findSertAddress("demolist1@localhost");
		data2.setFromAddrRowId(from.getRowId());
		data2.setReplytoAddrRowId(null);

		data2.setToAddrRowId(from.getRowId());
		data2.setClientDataRowId(client.getRowId());
		data2.setCustomerDataRowId(null);
		data2.setPurgeDate(null);
		data2.setUpdtTime(updtTime);
		data2.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data2.setLockTime(null);
		data2.setLockId(null);
		
		logic = logicService.getByRuleName(RuleNameEnum.BROADCAST.name());
		data2.setRuleLogicRowId(logic.getRowId());
		data2.setMsgContentType("text/plain");
		data2.setBodyContentType("text/plain");
		data2.setMsgBody("Test Broadcast Message Body");
		data2.setStatusId(MsgStatusCode.CLOSED.getValue());
		service.insert(data2);

		// load message addresses
		loadMessageAddress(data1);
		loadMessageAddress(data2);

		// load message headers
		loadMessageHeader(data1);
		loadMessageHeader(data2);

		// load message attachments
		loadMessageAttachment(data1);
		loadMessageAttachment(data2);

		// load message RFC fields
		loadMessageRfcField(data1);
		loadMessageRfcField(data2);

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadMessageAddress(MessageInbox inbox) {
		// load message addresses
		MessageAddress adr1 = new MessageAddress();
		adr1.setMessageInbox(inbox);
		adr1.setAddressType(EmailAddrType.FROM_ADDR.getValue());
		adr1.setEmailAddrRowId(inbox.getFromAddrRowId());
		msgAddrService.insert(adr1);
		
		MessageAddress adr2 = new MessageAddress();
		adr2.setMessageInbox(inbox);
		adr2.setAddressType(EmailAddrType.TO_ADDR.getValue());
		adr2.setEmailAddrRowId(inbox.getToAddrRowId());
		msgAddrService.insert(adr2);
	}
	
	private void loadMessageHeader(MessageInbox inbox) {
		MessageHeader hdr1 = new MessageHeader();
		MessageHeaderPK pk1 = new MessageHeaderPK(inbox,1);
		hdr1.setMessageHeaderPK(pk1);
		hdr1.setHeaderName(XHeaderName.MAILER.getValue());
		hdr1.setHeaderValue("Mailserder");
		headerService.insert(hdr1);
		
		MessageHeader hdr2 = new MessageHeader();
		MessageHeaderPK pk2 = new MessageHeaderPK(inbox,2);
		hdr2.setMessageHeaderPK(pk2);
		hdr2.setHeaderName(XHeaderName.RETURN_PATH.getValue());
		hdr2.setHeaderValue("demolist1@localhost");
		headerService.insert(hdr2);
		
		MessageHeader hdr3 = new MessageHeader();
		MessageHeaderPK pk3 = new MessageHeaderPK(inbox,3);
		hdr3.setMessageHeaderPK(pk3);
		hdr3.setHeaderName(XHeaderName.CLIENT_ID.getValue());
		hdr3.setHeaderValue(Constants.DEFAULT_CLIENTID);
		headerService.insert(hdr3);
	}
	
	private void loadMessageAttachment(MessageInbox inbox) {
		MessageAttachment atc1 = new MessageAttachment();
		MessageAttachmentPK pk1 = new MessageAttachmentPK(inbox,1,1);
		atc1.setMessageAttachmentPK(pk1);
		atc1.setAttachmentDisp(Part.ATTACHMENT);
		atc1.setAttachmentName("test.txt");
		atc1.setAttachmentType("text/plain; name=\"test.txt\"");
		atc1.setAttachmentValue("Test blob content goes here.".getBytes());
		attchmntService.insert(atc1);
		
		MessageAttachment atc2 = new MessageAttachment();
		MessageAttachmentPK pk2 = new MessageAttachmentPK(inbox,1,2);
		atc2.setMessageAttachmentPK(pk2);
		atc2.setAttachmentDisp(Part.INLINE);
		atc2.setAttachmentName("one.gif");
		atc2.setAttachmentType("image/gif; name=one.gif");
		atc2.setAttachmentValue(loadFromFile("one.gif"));
		attchmntService.insert(atc2);		

		MessageAttachment atc3 = new MessageAttachment();
		MessageAttachmentPK pk3 = new MessageAttachmentPK(inbox,1,3);
		atc3.setMessageAttachmentPK(pk3);
		atc3.setAttachmentDisp(Part.ATTACHMENT);
		atc3.setAttachmentName("jndi.bin");
		atc3.setAttachmentType("application/octet-stream; name=\"jndi.bin\"");
		atc3.setAttachmentValue(loadFromFile("jndi.bin"));
		attchmntService.insert(atc3);
	}

	private byte[] loadFromFile(String fileName) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream("jpa/test/data/" + fileName);
		if (is == null) {
			throw new RuntimeException("File (" + fileName + ") not found!");
		}
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		try {
			while ((len=bis.read(buffer))>0) {
				baos.write(buffer, 0, len);
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught: " + e.getMessage());
		}
	}
	
	private void loadMessageRfcField(MessageInbox inbox) {
		MessageRfcField rfc1 = new MessageRfcField();
		MessageRfcFieldPK pk1 = new MessageRfcFieldPK(inbox,"message/rfc822");
		rfc1.setMessageRfcFieldPK(pk1);
		rfc1.setRfcStatus(null);
		EmailAddr finalRcpt = emailAddrService.findSertAddress("jackwnn@synnex.com.au");
		rfc1.setFinalRcptAddrRowId(finalRcpt.getRowId());
		rfc1.setOriginalMsgSubject("May 74% OFF");
		rfc1.setMessageId("<1252103166.01356550221562.JavaMail.wangjack@WANGJACKDEV>");
		rfc1.setDsnText("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">" + LF +
			"<html>" + LF +
			" <head>" + LF +
			"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">" + LF +
			" </head>" + LF +
			"<div>" + LF +
			"<img src=\"http://uhgupmhuwurvxaegnbayjgtsccignaadmtzrldug%.msadcenter.msn.com/lww.gif?o=1\" width=\"0\" height=\"0\">" + LF +
			"<table cellpadding=0 cellspacing=0 width=600 align=\"center\">" + LF +
			"<tr>" + LF +
			"<div style=\"background:#1766A6;border:3px solid #63AAE2;padding:10px;color:#F2EAEA;font-weight:bold;font-size:18px\">" + LF +
			"<table cellpadding=0 cellspacing=0 width=100%>" + LF +
			"<tr>" + LF +
			"<td>" + LF +
			"<div style=\"border:2px solid #F9A800; width:580px;\">" + LF +
			"<table width=580 border=0 cellpadding=12 cellspacing=0 bgcolor=\"#F9A800\">" + LF +
			"<tr>" + LF +
			"<td style=\"color:#FFFFFF;\"><div style=\"font: bold 21px/114% Verdana, Arial, Helvetica, sans-serif;\"><center><h3>Dear jackwnn@synnex.com.au</h3><center>Wed, 14 May 2008 06:47:43 +0800. Coupon No. 194<br><center><h2> Online Pharmacy Products! </h2>" + LF +
			"<div align=\"center\"> <A href=\"http://xmh.seemparty.com\" target=\"_blank\"><img src=\"http://swj.seemparty.com/10.gif\" border=0 alt=\"Click Here!\"></a> </div>" + LF +
			"</td>" + LF +
			"</tr>" + LF +
			"<tr>" + LF +
			"<td>" + LF +
			"<strong>About this mailing: </strong><br>" + LF +
			"You are receiving this e-mail because you subscribed to MSN Featured Offers. Microsoft respects your privacy. If you do not wish to receive this MSN Featured Offers e-mail, please click the \"Unsubscribe\" link below. This will not unsubscribe" + LF +
			"you from e-mail communications from third-party advertisers that may appear in MSN Feature Offers. This shall not constitute an offer by MSN. MSN shall not be responsible or liable for the advertisers' content nor any of the goods or service" + LF +
			"advertised. Prices and item availability subject to change without notice.<br><br>" + LF +
			"<center>�2008 Microsoft | <A href=\"http://aep.seemparty.com\" target=\"_blank\">Unsubscribe</a> | <A href=\"http://gil.seemparty.com\" target=\"_blank\">More Newsletters</a> | <A href=\"http://dqh.seemparty.com\" target=\"_blank\">Privacy</a><br><br>" + LF +
			"<center>Microsoft Corporation, One Microsoft Way, Redmond, iy 193" + LF +
			"</td>" + LF +
			" </div>" + LF +
			"   </div>   " + LF +
			"    </body>" + LF +
			"</html>");
		rfc1.setDsnRfc822("Received: from asp-6.reflexion.net ([205.237.99.181]) by MELMX.synnex.com.au with Microsoft SMTPSVC(6.0.3790.3959);" + LF +
			"	 Wed, 14 May 2008 08:50:31 +1000" + LF +
			"Received: (qmail 22433 invoked from network); 13 May 2008 22:47:49 -0000" + LF +
			"Received: from unknown (HELO asp-6.reflexion.net) (127.0.0.1)" + LF +
			"  by 0 (rfx-qmail) with SMTP; 13 May 2008 22:47:49 -0000" + LF +
			"Received: by asp-6.reflexion.net" + LF +
			"        (Reflexion email security v5.40.3) with SMTP;" + LF +
			"        Tue, 13 May 2008 18:47:49 -0400 (EDT)" + LF +
			"Received: (qmail 22418 invoked from network); 13 May 2008 22:47:48 -0000" + LF +
			"Received: from unknown (HELO WWW-2D1D2A59B52) (124.228.102.160)" + LF +
			"  by 0 (rfx-qmail) with SMTP; 13 May 2008 22:47:48 -0000" + LF +
			"Received: from $FROM_NAME $FROM_NAME(10.17.18.16) by WWW-2D1D2A59B52 (PowerMTA(TM) v3.2r4) id hfp02o32d12j39 for <jackwnn@synnex.com.au>; Wed, 14 May 2008 06:47:43 +0800 (envelope-from <jackwng@gmail.com>)" + LF +
			"Message-Id: <03907644185382.773588432734.799319-7043@cimail571.msn.com>" + LF +
			"To: <jackwnn@synnex.com.au>" + LF +
			"Subject: May 74% OFF" + LF +
			"From: Viagra � Official Site <jackwnn@synnex.com.au>" + LF +
			"MIME-Version: 1.0" + LF +
			"Importance: High" + LF +
			"Content-Type: text/html; charset=\"iso-8859-1\"" + LF +
			"Content-Transfer-Encoding: 8bit" + LF +
			"X-Rfx-Unknown-Address: Address <jackwnn@synnex.com.au> is not protected by Reflexion." + LF +
			"Return-Path: jackwng@gmail.com" + LF +
			"X-OriginalArrivalTime: 13 May 2008 22:50:31.0508 (UTC) FILETIME=[BF33D940:01C8B54B]" + LF +
			"Date: 14 May 2008 08:50:31 +1000");
		rfc1.setDeliveryStatus("Reporting-MTA: dns;MELMX.synnex.com.au" + LF +
				"Received-From-MTA: dns;asp-6.reflexion.net" + LF +
				"Arrival-Date: Wed, 14 May 2008 08:50:31 +1000" + LF + LF +
				"Final-Recipient: rfc822;jackwnn@synnex.com.au" + LF +
				"Action: failed" + LF +
				"Status: 5.1.1"
			);
		rfcService.insert(rfc1);
		
		MessageRfcField rfc2 = new MessageRfcField();
		MessageRfcFieldPK pk2 = new MessageRfcFieldPK(inbox,"multipart/report; report-type=");
		rfc2.setMessageRfcFieldPK(pk2);
		rfc2.setRfcStatus("5.1.1");
		rfc2.setRfcAction("failed");
		rfc2.setFinalRcptAddrRowId(finalRcpt.getRowId());
		rfc1.setOriginalMsgSubject("May 74% OFF");
		rfc2.setMessageId("<1631635827.01357742709854.JavaMail.wangjack@WANGJACKDEV>");
		rfc2.setDsnText(rfc1.getDsnText());
		rfc2.setDeliveryStatus(rfc1.getDeliveryStatus());
		rfcService.insert(rfc2);
		
		MessageRfcField rfc3 = new MessageRfcField();
		MessageRfcFieldPK pk3 = new MessageRfcFieldPK(inbox,"text/html; charset=us-ascii");
		rfc3.setMessageRfcFieldPK(pk3);
		EmailAddr finalRcpt2 = emailAddrService.findSertAddress("test@test.com");
		rfc3.setFinalRcptAddrRowId(finalRcpt2.getRowId());
		rfc3.setOriginalRecipient("jsmith@test.com");
		rfcService.insert(rfc3);
	}
}
