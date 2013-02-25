package jpa.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;

import javax.persistence.NoResultException;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.ClientData;
import jpa.model.EmailAddr;
import jpa.model.MessageRfcField;
import jpa.model.MessageRfcFieldPK;
import jpa.model.MessageInbox;
import jpa.model.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.EmailAddrService;
import jpa.service.MessageRfcFieldService;
import jpa.service.MessageInboxService;
import jpa.service.RuleLogicService;
import jpa.util.StringUtil;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class MessageRfcFieldTest {

	@BeforeClass
	public static void MessageRfcFieldPrepare() {
	}

	@Autowired
	MessageRfcFieldService service;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	EmailAddrService addrService;
	@Autowired
	ClientDataService clientService;
	@Autowired
	RuleLogicService logicService;

	private MessageInbox inbox1;
	private EmailAddr from;
	private EmailAddr to;

	@Before
	public void prepare() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		inbox1 = new MessageInbox();
		
		inbox1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		inbox1.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		inbox1.setMsgSubject("Test Subject");
		inbox1.setMsgPriority("2 (Normal)");
		inbox1.setReceivedTime(updtTime);
		
		from = addrService.findSertAddress("test@test.com");
		inbox1.setFromAddrRowId(from.getRowId());
		inbox1.setReplytoAddrRowId(null);

		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		String to_addr = client.getReturnPathLeft() + "@" + client.getDomainName();
		to = addrService.findSertAddress(to_addr);
		inbox1.setToAddrRowId(to.getRowId());
		inbox1.setClientDataRowId(client.getRowId());
		inbox1.setCustomerDataRowId(null);
		inbox1.setPurgeDate(null);
		inbox1.setUpdtTime(updtTime);
		inbox1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		inbox1.setLockTime(null);
		inbox1.setLockId(null);
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.name());
		inbox1.setRuleLogicRowId(logic.getRowId());
		inbox1.setMsgContentType("multipart/mixed");
		inbox1.setBodyContentType("text/plain");
		inbox1.setMsgBody("Test Message Body");
		inboxService.insert(inbox1);
	}
	
	private MessageRfcField hdr1;
	private MessageRfcField hdr2;
	private MessageRfcField hdr3;

	@Test
	public void messageRfcFieldService() {
		insertMessageRfcFields();
		MessageRfcField hdr11 = service.getByRowId(hdr1.getRowId());
		
		System.out.println(StringUtil.prettyPrint(hdr11,2));
		
		MessageRfcField hdr12 = service.getByPrimaryKey(hdr11.getMessageRfcFieldPK());
		assertTrue(hdr11.equals(hdr12));
		
		// test update
		hdr2.setUpdtUserId("jpa test");
		service.update(hdr2);
		MessageRfcField hdr22 = service.getByRowId(hdr2.getRowId());
		assertTrue("jpa test".equals(hdr22.getUpdtUserId()));
		
		// test delete
		service.delete(hdr11);
		try {
			service.getByRowId(hdr11.getRowId());
			fail();
		}
		catch (NoResultException e) {}
		
		assertTrue(1==service.deleteByRowId(hdr2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
		
		insertMessageRfcFields();
		assertTrue(1==service.deleteByPrimaryKey(hdr1.getMessageRfcFieldPK()));
		assertTrue(2==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private String LF = System.getProperty("line.separator", "\n");
	private void insertMessageRfcFields() {
		// test insert
		hdr1 = new MessageRfcField();
		MessageRfcFieldPK pk1 = new MessageRfcFieldPK(inbox1,"message/rfc822");
		hdr1.setMessageRfcFieldPK(pk1);
		hdr1.setRfcStatus(null);
		EmailAddr finalRcpt = addrService.findSertAddress("jackwnn@synnex.com.au");
		hdr1.setFinalRcptAddrRowId(finalRcpt.getRowId());
		hdr1.setOriginalMsgSubject("May 74% OFF");
		hdr1.setMessageId("<1252103166.01356550221562.JavaMail.wangjack@WANGJACKDEV>");
		hdr1.setDsnText("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">" + LF +
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
		hdr1.setDsnRfc822("Received: from asp-6.reflexion.net ([205.237.99.181]) by MELMX.synnex.com.au with Microsoft SMTPSVC(6.0.3790.3959);" + LF +
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
		hdr1.setDeliveryStatus("Reporting-MTA: dns;MELMX.synnex.com.au" + LF +
				"Received-From-MTA: dns;asp-6.reflexion.net" + LF +
				"Arrival-Date: Wed, 14 May 2008 08:50:31 +1000" + LF + LF +
				"Final-Recipient: rfc822;jackwnn@synnex.com.au" + LF +
				"Action: failed" + LF +
				"Status: 5.1.1"
			);
		service.insert(hdr1);
		
		hdr2 = new MessageRfcField();
		MessageRfcFieldPK pk2 = new MessageRfcFieldPK(inbox1,"multipart/report; report-type=");
		hdr2.setMessageRfcFieldPK(pk2);
		hdr2.setRfcStatus("5.1.1");
		hdr2.setRfcAction("failed");
		hdr2.setFinalRcptAddrRowId(finalRcpt.getRowId());
		hdr1.setOriginalMsgSubject("May 74% OFF");
		hdr2.setMessageId("<1631635827.01357742709854.JavaMail.wangjack@WANGJACKDEV>");
		hdr2.setDsnText(hdr1.getDsnText());
		hdr2.setDeliveryStatus(hdr1.getDeliveryStatus());
		service.insert(hdr2);
		
		hdr3 = new MessageRfcField();
		MessageRfcFieldPK pk3 = new MessageRfcFieldPK(inbox1,"text/html; charset=us-ascii");
		hdr3.setMessageRfcFieldPK(pk3);
		EmailAddr finalRcpt2 = addrService.findSertAddress("test@test.com");
		hdr3.setFinalRcptAddrRowId(finalRcpt2.getRowId());
		hdr3.setOriginalRecipient("jsmith@test.com");
		service.insert(hdr3);
		
		assertTrue(service.getByMsgInboxId(inbox1.getRowId()).size()==3);		
	}
}
