package com.legacytojava.message.table;

import java.sql.Timestamp;

import javax.mail.Part;

import org.springframework.context.ApplicationContext;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.constant.CarrierCode;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.MsgDirectionCode;
import com.legacytojava.message.constant.MsgStatusCode;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.constant.VariableName;
import com.legacytojava.message.constant.XHeaderName;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.dao.inbox.AttachmentsDao;
import com.legacytojava.message.dao.inbox.MsgAddrsDao;
import com.legacytojava.message.dao.inbox.MsgClickCountsDao;
import com.legacytojava.message.dao.inbox.MsgHeadersDao;
import com.legacytojava.message.dao.inbox.MsgInboxDao;
import com.legacytojava.message.dao.inbox.RfcFieldsDao;
import com.legacytojava.message.dao.outbox.MsgSequenceDao;
import com.legacytojava.message.vo.ClientVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.inbox.AttachmentsVo;
import com.legacytojava.message.vo.inbox.MsgAddrsVo;
import com.legacytojava.message.vo.inbox.MsgClickCountsVo;
import com.legacytojava.message.vo.inbox.MsgHeadersVo;
import com.legacytojava.message.vo.inbox.MsgInboxVo;
import com.legacytojava.message.vo.inbox.RfcFieldsVo;

public class LoadInboxTables
{
	static long msgId = -1L;
	MsgSequenceDao msgSequenceDao;
	EmailAddrDao emailAddrDao;
	MsgClickCountsDao msgClickCountsDao;
	//static AbstractApplicationContext factory = null;
	public static void main(String[] args)
	{
		
		LoadInboxTables loadInboxTables = new LoadInboxTables();
		try
		{
			loadInboxTables.loadData();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public void loadData() {
		loadData(null);
	}
	
	public void loadData(ApplicationContext factory) {
		if (factory == null) {
			factory = SpringUtil.getDaoAppContext();
		}
		msgSequenceDao = (MsgSequenceDao)factory.getBean("msgSequenceDao");
		emailAddrDao = (EmailAddrDao)factory.getBean("emailAddrDao");
		msgClickCountsDao = (MsgClickCountsDao)factory.getBean("msgClickCountsDao");
		MsgInboxDao msgInboxDao = (MsgInboxDao)factory.getBean("msgInboxDao");
		MsgAddrsDao msgAddrsDao = (MsgAddrsDao)factory.getBean("msgAddrsDao");
		AttachmentsDao attachmentsDao = (AttachmentsDao)factory.getBean("attachmentsDao");
		MsgHeadersDao msgHeadersDao = (MsgHeadersDao)factory.getBean("msgHeadersDao");
		RfcFieldsDao rfcFieldsDao = (RfcFieldsDao)factory.getBean("rfcFieldsDao");
		
		msgId = load(msgInboxDao);
		load(msgAddrsDao);
		load(attachmentsDao);
		load(msgHeadersDao);
		load(rfcFieldsDao);
	}
	
	long load(MsgInboxDao msgInboxDao) 
	{
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		MsgInboxVo in = new MsgInboxVo();
		
		long msgId = msgSequenceDao.findNextValue();
		in.setMsgId(msgId);
		in.setMsgRefId(null);
		in.setLeadMsgId(msgId);
		in.setCarrierCode(CarrierCode.SMTPMAIL);
		in.setMsgDirection(MsgDirectionCode.MSG_RECEIVED);
		in.setMsgSubject("Test Subject");
		in.setMsgPriority("2 (Normal)");
		in.setReceivedTime(updtTime);
		in.setFromAddrId(Long.valueOf(1));
		in.setReplyToAddrId(null);
		ClientVo clientVo = ClientUtil.getDefaultClientVo();
		String addr = clientVo.getReturnPathLeft() + "@" + clientVo.getDomainName();
		EmailAddrVo addrVo = emailAddrDao.findByAddress(addr);
		in.setToAddrId(addrVo.getEmailAddrId());
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setCustId(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setLockTime(null);
		in.setLockId(null);
		in.setRuleName(RuleNameType.GENERIC.toString());
		in.setMsgContentType("multipart/mixed");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Message Body");

		msgInboxDao.insert(in);
		
		msgId = msgSequenceDao.findNextValue();
		in.setMsgId(msgId);
		in.setMsgRefId(null);
		in.setLeadMsgId(msgId);
		in.setCarrierCode(CarrierCode.SMTPMAIL);
		in.setMsgDirection(MsgDirectionCode.MSG_SENT);
		in.setMsgSubject("Test Broadcast Subject");
		in.setMsgPriority("2 (Normal)");
		in.setReceivedTime(updtTime);
		addrVo = emailAddrDao.findByAddress("demolist1@localhost");
		in.setFromAddrId(addrVo.getEmailAddrId());
		in.setReplyToAddrId(null);
		in.setToAddrId(addrVo.getEmailAddrId());
		in.setClientId(Constants.DEFAULT_CLIENTID);
		in.setCustId(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		in.setLockTime(null);
		in.setLockId(null);
		in.setRuleName(RuleNameType.BROADCAST.toString());
		in.setMsgContentType("text/plain");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Broadcast Message Body");
		in.setStatusId(MsgStatusCode.CLOSED);

		msgInboxDao.insert(in);

		MsgClickCountsVo in2 = new MsgClickCountsVo();
		
		in2.setMsgId(msgId);
		in2.setListId("SMPLLST1");
		in2.setSentCount(1);
		in2.setOpenCount(1);
		in2.setClickCount(1);
		in2.setLastOpenTime(updtTime);
		in2.setLastClickTime(updtTime);

		msgClickCountsDao.insert(in2);

		System.out.println("load() completed.\n");
		return in.getMsgId();
	}
	
	void load(MsgAddrsDao msgAddrsDao) 
	{
		MsgAddrsVo in = new MsgAddrsVo();
		
		in.setMsgId(msgId);
		in.setAddrType(EmailAddressType.FROM_ADDR);
		in.setAddrSeq(1);
		in.setAddrValue("test@test.com");

		msgAddrsDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(AttachmentsDao attachmentrsDao) 
	{
		AttachmentsVo in = new AttachmentsVo();
		
		in.setMsgId(msgId);
		in.setAttchmntDepth(1);
		in.setAttchmntSeq(1);
		in.setAttchmntName("test.txt");
		in.setAttchmntType("text/plain; name=\"test.txt\"");
		in.setAttchmntDisp(Part.ATTACHMENT);
		in.setAttchmntValue("Test blob content goes here.".getBytes());

		attachmentrsDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(MsgHeadersDao msgHeadersDao) 
	{
		MsgHeadersVo in = new MsgHeadersVo();
		
		in.setMsgId(msgId);
		in.setHeaderSeq(1);
		in.setHeaderName(XHeaderName.XHEADER_MAILER);
		in.setHeaderValue("MailSender");

		msgHeadersDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}
	
	void load(RfcFieldsDao rfcFieldsDao) 
	{
		RfcFieldsVo in = new RfcFieldsVo();
		
		in.setMsgId(msgId);
		in.setRfcType(VariableName.RFC822);
		in.setRfcStatus(null);
		in.setRfcAction(null);
		in.setFinalRcpt(null);
		in.setFinalRcptId(null);
		in.setOrigRcpt(null);
		in.setOrigMsgSubject("original subject");
		in.setMessageId("messageId");
		in.setDsnText("dsn Text");
		in.setDsnRfc822("dsn Rfc822");
		in.setDlvrStatus(null);

		rfcFieldsDao.insert(in);

		System.out.println("load() completed.\n"+in);
	}	
}