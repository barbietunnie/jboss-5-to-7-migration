package com.es.data.loader;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.mail.Part;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.inbox.MsgAddressDao;
import com.es.dao.inbox.MsgAttachmentDao;
import com.es.dao.inbox.MsgClickCountDao;
import com.es.dao.inbox.MsgHeaderDao;
import com.es.dao.inbox.MsgInboxDao;
import com.es.dao.inbox.MsgStreamDao;
import com.es.dao.inbox.MsgRfcFieldDao;
import com.es.dao.outbox.DeliveryStatusDao;
import com.es.dao.outbox.MsgRenderedDao;
import com.es.dao.outbox.MsgSequenceDao;
import com.es.dao.rule.RuleLogicDao;
import com.es.dao.sender.SenderDataDao;
import com.es.dao.template.MsgSourceDao;
import com.es.dao.template.TemplateDataDao;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailAddressType;
import com.es.data.constant.MailingListDeliveryType;
import com.es.data.constant.MsgDirectionCode;
import com.es.data.constant.MsgStatusCode;
import com.es.data.constant.XHeaderName;
import com.es.data.preload.MailingListEnum;
import com.es.data.preload.RuleNameEnum;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.comm.SenderDataVo;
import com.es.vo.inbox.MsgAddressVo;
import com.es.vo.inbox.MsgAttachmentVo;
import com.es.vo.inbox.MsgClickCountVo;
import com.es.vo.inbox.MsgHeaderVo;
import com.es.vo.inbox.MsgInboxVo;
import com.es.vo.inbox.MsgRfcFieldVo;
import com.es.vo.outbox.DeliveryStatusVo;
import com.es.vo.outbox.MsgRenderedVo;
import com.es.vo.outbox.MsgStreamVo;
import com.es.vo.rule.RuleLogicVo;
import com.es.vo.template.MsgSourceVo;
import com.es.vo.template.TemplateDataVo;

public class InboxTables extends AbstractTableBase {

	public void dropTables() {
		try {
			getJdbcTemplate().execute("DROP TABLE Msg_Unsub_Comment");
			System.out.println("Dropped Msg_Unsub_Comment Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_CLICK_COUNT");
			System.out.println("Dropped MSG_CLICK_COUNT Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_ACTION_LOG");
			System.out.println("Dropped MSG_ACTION_LOG Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE DELIVERY_STATUS");
			System.out.println("Dropped DELIVERY_STATUS Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_ATTACHMENT");
			System.out.println("Dropped MSG_ATTACHMENT Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_ADDRESS");
			System.out.println("Dropped MSG_ADDRESS Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_HEADER");
			System.out.println("Dropped MSG_HEADER Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_RFC_FIELD");
			System.out.println("Dropped MSG_RFC_FIELD Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_STREAM");
			System.out.println("Dropped MSG_STREAM Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_INBOX");
			System.out.println("Dropped MSG_INBOX Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_UNREAD_COUNT");
			System.out.println("Dropped MSG_UNREAD_COUNT Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_SEQUENCE");
			System.out.println("Dropped MSG_SEQUENCE Table...");
		}
		catch (DataAccessException e) {
		}
		
		try {
			getJdbcTemplate().execute("DROP TABLE RENDER_ATTACHMENT");
			System.out.println("Dropped RENDER_ATTACHMENT Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE RENDER_VARIABLE");
			System.out.println("Dropped RENDER_VARIABLE Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE RENDER_OBJECT");
			System.out.println("Dropped RENDER_OBJECT Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE MSG_RENDERED");
			System.out.println("Dropped MSG_RENDERED Table...");
		}
		catch (DataAccessException e) {
		}
	}

	public void createTables() throws DataAccessException {
		createMSGRENDEREDTable();
		createRENDERATTACHMENTTable();
		createRENDERVARIABLETable();
		createRENDEROBJECTTable();
		
		createMSGSEQUENCETable();
		createMSGUNREADCOUNTTable();
		createMSGINBOXTable();
		createMSGATTACHMENTTable();
		createMSGADDRESSTable();
		createMSGHEADERTable();
		createMSGRFCFIELDTable();
		createMSGSTREAMTable();
		createDELIVERYSTATUSTable();
		createMSGACTIONLOGTable();
		createMSGCLICKCOUNTTable();
		createMsgUnsubCommentTable();
	}
	
	public void loadTestData() throws DataAccessException {
		try {
			loadMessageInbox();
			loadRenderTables();
		}
		catch (IOException e) {
			throw new DataAccessException(e.getMessage()) {
				private static final long serialVersionUID = 6723066885743065081L;};
		}
	}
	
	void createMSGRENDEREDTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_RENDERED ( " +
			"RenderId bigint NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
			"MsgSourceId varchar(16) NOT NULL, " +
			"TemplateId varchar(16) NOT NULL, " +
			"StartTime datetime NOT NULL," +
			"SenderId varchar(16), " +
			"SubrId varchar(16), " +
			"PurgeAfter int, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"INDEX (MsgSourceId) " +
			") ENGINE=InnoDB"); // row-level locking
			System.out.println("Created MSG_RENDERED Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRENDERATTACHMENTTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RENDER_ATTACHMENT ( " +
			"RenderId bigint NOT NULL, " +
			"AttchmntSeq decimal(2) NOT NULL, " + // up to 100 attachments per message
			"AttchmntName varchar(100), " +
			"AttchmntType varchar(100), " +
			"AttchmntDisp varchar(100), " +
			"AttchmntValue mediumblob, " +
			"FOREIGN KEY (RenderId) REFERENCES MSG_RENDERED (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,AttchmntSeq)) ENGINE=InnoDB");
			System.out.println("Created RENDER_ATTACHMENT Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRENDERVARIABLETable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RENDER_VARIABLE ( " +
			"RenderId bigint NOT NULL, " +
			"VariableName varchar(26), " +
			"VariableFormat varchar(50), " +
			"VariableType char(1), " +
			// T - text, N - numeric, D - DateField/time,
			// A - address, X - X-Header, L - LOB(Attachment)
			"VariableValue text, " +
			"FOREIGN KEY (RenderId) REFERENCES MSG_RENDERED (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,VariableName)) ENGINE=InnoDB");
			System.out.println("Created RENDER_VARIABLE Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRENDEROBJECTTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RENDER_OBJECT ( " +
			"RenderId bigint NOT NULL, " +
			"VariableName varchar(26), " +
			"VariableFormat varchar(50), " +
			"VariableType char(1), " +
			// T - text, N - numeric, D - DateField/time,
			// A - address, X - X-Header, L - LOB(Attachment), C - Collection
			"VariableValue mediumblob, " +
			"FOREIGN KEY (RenderId) REFERENCES MSG_RENDERED (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,VariableName)) ENGINE=InnoDB");
			System.out.println("Created RENDER_OBJECT Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGSEQUENCETable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_SEQUENCE ( " +
			"SeqId bigint NOT NULL " +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created MSG_SEQUENCE Table...");
			getJdbcTemplate().execute("INSERT INTO MSG_SEQUENCE (SeqId) VALUES(0)");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGUNREADCOUNTTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_UNREAD_COUNT ( " +
			"InboxUnreadCount int NOT NULL, " +
			"SentUnreadCount int NOT NULL" +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created MSG_UNREAD_COUNT Table...");
			getJdbcTemplate().execute("INSERT INTO MSG_UNREAD_COUNT (InboxUnreadCount,SentUnreadCount) VALUES(0,0)");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGINBOXTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_INBOX ( " +
			"MsgId bigint NOT NULL PRIMARY KEY, " +
			"MsgRefId bigint, " + // link to another MSG_INBOX record (a reply or a bounce)
			"LeadMsgId bigint NOT NULL, " +
			"CarrierCode char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.getValue() + "', " + // S - SmtpMail, W - WebMail
			"MsgDirection char(1) NOT NULL, " + // R - Received, S - Sent
			"RuleName varchar(26) NOT NULL, " + // link to Rule_Logic.RuleName
			"MsgSubject varchar(255), " +
			"MsgPriority varchar(10), " + // 1 (High)/2 (Normal)/3 (Low)
			"ReceivedTime datetime NOT NULL, " +
			"FromAddrId bigint, " + // link to Email_Address
			"ReplyToAddrId bigint, " + // link to Email_Address
			"ToAddrId bigint, " + // link to Email_Address
			"SenderId varchar(16), " + // link to Sender_Data - derived from OutMsgRefId
			"SubrId varchar(16), " + // link to Subscriber - derived from OutMsgRefId
			"PurgeDate Date, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"LockTime datetime, " +
			"LockId varchar(10), " +
			"ReadCount int NOT NULL DEFAULT 0, " + // how many times it's been read
			"ReplyCount int NOT NULL DEFAULT 0, " + // how many times it's been replied
			"ForwardCount int NOT NULL DEFAULT 0, " + // how many times it's been forwarded
			"Flagged char(1) NOT NULL DEFAULT '" + CodeType.NO_CODE.getValue() + "', " +
			"DeliveryTime datetime, " + // for out-bound messages only, updated by MailSender
			"StatusId char(1) NOT NULL, " + // P - pending, D - delivered by MailSender, F - delivery failed, C/O - closed/Open (for received mail)
			"SmtpMessageId varchar(255), " + // SMTP message Id, updated by MailSender once delivered
			"RenderId bigint, " + // link to a Msg_Rendered record, for out-bound messages
			"OverrideTestAddr char(1), " + // Y - tell MailSender to use TO address even under test mode
			"AttachmentCount smallint NOT NULL DEFAULT 0, " + // for UI performance
			"AttachmentSize int NOT NULL DEFAULT 0, " + // for UI performance
			"MsgBodySize int NOT NULL DEFAULT 0, " + // for UI performance
			"MsgContentType varchar(100) NOT NULL, " +
			"BodyContentType varchar(50), " +
			"MsgBody mediumtext, " +
			"INDEX (LeadMsgId), " +
			"FOREIGN KEY (RenderId) REFERENCES MSG_RENDERED (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"FOREIGN KEY (FromAddrId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FromAddrId), " +
			"FOREIGN KEY (ToAddrId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (ToAddrId)" +
			") ENGINE=InnoDB");
			System.out.println("Created MSG_INBOX Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGATTACHMENTTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_ATTACHMENT ( " +
			"MsgId bigint NOT NULL, " +
			"AttchmntDepth decimal(2) NOT NULL, " +
			"AttchmntSeq decimal(3) NOT NULL, " +
			"AttchmntName varchar(100), " +
			"AttchmntType varchar(100), " +
			"AttchmntDisp varchar(100), " +
			"AttchmntValue mediumblob, " +
			"FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,AttchmntDepth,AttchmntSeq)" +
			") ENGINE=InnoDB");
			System.out.println("Created MSG_ATTACHMENT Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGADDRESSTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_ADDRESS ( " +
			"MsgId bigint NOT NULL, " +
			"AddrType varchar(10) NOT NULL, " + // from, reply-to, to, cc, bcc, etc.
			"AddrSeq decimal(4) NOT NULL, " +
			"AddrValue varchar(255), " +
			"FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,AddrType,AddrSeq)" +
			") ENGINE=InnoDB");
			System.out.println("Created MSG_ADDRESS Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGHEADERTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_HEADER ( " +
			"MsgId bigint NOT NULL, " +
			"HeaderSeq decimal(4) NOT NULL, " +
			"HeaderName varchar(100), " +
			"HeaderValue text, " +
			"FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,HeaderSeq)" +
			") ENGINE=InnoDB");
			System.out.println("Created MSG_HEADER Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGRFCFIELDTable() throws DataAccessException {
		// for in-bound messages
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_RFC_FIELD ( " +
			"MsgId bigint NOT NULL, " +
			"RfcType varchar(30) NOT NULL, " +
			"RfcStatus varchar(30), " +
			"RfcAction varchar(30), " +
			"FinalRcpt varchar(255), " +
			"FinalRcptId bigint, " +
			"OrigRcpt varchar(255), " +
			"OrigMsgSubject varchar(255), " +
			"MessageId varchar(255), " +
			"DsnText text, " +
			"DsnRfc822 text, " +
			"DlvrStatus text, " +
			"FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"FOREIGN KEY (FinalRcptId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FinalRcptId), " +
			"PRIMARY KEY (MsgId,RfcType)" +
			") ENGINE=InnoDB");
			System.out.println("Created MSG_RFC_FIELD Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGSTREAMTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_STREAM ( " +
			"MsgId bigint NOT NULL, " +
			"FromAddrId bigint, " +
			"ToAddrId bigint, " +
			"MsgSubject varchar(255), " +
			"AddTime datetime, " +
			"MsgStream mediumblob, " +
			"PRIMARY KEY (MsgId), " +
			"FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE " +
			") ENGINE=InnoDB");
			System.out.println("Created MSG_STREAM Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createDELIVERYSTATUSTable() throws DataAccessException {
		// for out-bound messages storing SMTP delivery failures
		try {
			getJdbcTemplate().execute("CREATE TABLE DELIVERY_STATUS ( " +
			"MsgId bigint NOT NULL, " +
			"FinalRecipientId bigint NOT NULL, " +
			"FinalRecipient varchar(255), " +
			"OriginalRecipientId bigint, " +
			"MessageId varchar(255), " + // returned SMTP message id
			"DsnStatus varchar(50), " +
			"DsnReason varchar(255), " +
			"DsnText text, " +
			"DsnRfc822 text, " +
			"DeliveryStatus text, " +
			"AddTime datetime, " +
			"FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"FOREIGN KEY (FinalRecipientId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FinalRecipientId), " +
			"PRIMARY KEY (MsgId,FinalRecipientId)) ENGINE=InnoDB");
			System.out.println("Created DELIVERY_STATUS Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createMSGACTIONLOGTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_ACTION_LOG ( " +
			"MsgId bigint NOT NULL, " +
			"ActionSeq smallint NOT NULL, " +
			"ActionBo varchar(50) NOT NULL, " +
			"Parameters varchar(255), " +
			"AddTime datetime NOT NULL, " +
			"FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId, ActionSeq) " +
			// use MyISAM engine to prevent from dead locks.
			") ENGINE=MyISAM");
			System.out.println("Created MSG_ACTION_LOG Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGCLICKCOUNTTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE MSG_CLICK_COUNT ( " +
			"MsgId bigint NOT NULL, " +
			"ListId varchar(8) NOT NULL, " +
			"DeliveryOption varchar(4) NOT NULL DEFAULT '" + MailingListDeliveryType.ALL_ON_LIST.getValue() + "', " +
			"SentCount int NOT NULL DEFAULT 0, " +
			"OpenCount int NOT NULL DEFAULT 0, " +
			"ClickCount int NOT NULL DEFAULT 0, " +
			"LastOpenTime datetime DEFAULT NULL, " +
			"LastClickTime datetime DEFAULT NULL, " +
			"StartTime datetime DEFAULT NULL, " +
			"EndTime datetime DEFAULT NULL, " +
			"UnsubscribeCount int NOT NULL DEFAULT 0, " +
			"ComplaintCount int NOT NULL DEFAULT 0, " +
			"ReferralCount int NOT NULL DEFAULT 0, " +
			"FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"PRIMARY KEY (MsgId) " +
			") ENGINE=InnoDB");
			System.out.println("Created MSG_CLICK_COUNT Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgUnsubCommentTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE Msg_Unsub_Comment ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "MsgId bigint NOT NULL, "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8), "
					+ "Comments varchar(500) NOT NULL, "
					+ "AddTime datetime NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (MsgId) REFERENCES MSG_INBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES EMAIL_ADDRESS (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (MsgId), "
					+ "INDEX (EmailAddrId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created Msg_Unsub_Comment Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private void loadMessageInbox() throws IOException {
		MsgInboxDao service = SpringUtil.getAppContext().getBean(MsgInboxDao.class);
		SenderDataDao senderService = SpringUtil.getAppContext().getBean(SenderDataDao.class);
		EmailAddressDao emailAddrService = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		RuleLogicDao logicService = SpringUtil.getAppContext().getBean(RuleLogicDao.class);
		MsgSequenceDao msgSequenceDao = SpringUtil.getAppContext().getBean(MsgSequenceDao.class);
		
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		SenderDataVo sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);

		MsgInboxVo data1 = new MsgInboxVo();
		data1.setMsgId(msgSequenceDao.findNextValue());
		data1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		data1.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		data1.setMsgSubject("Test Subject");
		data1.setMsgPriority("2 (Normal)");
		data1.setReceivedTime(updtTime);
		
		EmailAddressVo from = emailAddrService.findSertAddress("jsmith@test.com");
		data1.setFromAddrId(from.getEmailAddrId());
		data1.setReplyToAddrId(null);

		String to_addr = sender.getReturnPathLeft() + "@" + sender.getDomainName();
		EmailAddressVo to = emailAddrService.findSertAddress(to_addr);
		data1.setToAddrId(to.getEmailAddrId());
		data1.setSenderId(sender.getSenderId());
		data1.setSubrId(null);
		data1.setPurgeDate(null);
		data1.setUpdtTime(updtTime);
		data1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data1.setLockTime(null);
		data1.setLockId(null);
		
		RuleLogicVo logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		data1.setRuleName(logic.getRuleName());
		data1.setMsgContentType("multipart/mixed");
		data1.setBodyContentType(Constants.TEXT_PLAIN);
		data1.setMsgBody("Test Message Body");
		data1.setStatusId(MsgStatusCode.RECEIVED.getValue());
		service.insert(data1);

		MsgInboxVo data2 = new MsgInboxVo();
		data2.setMsgId(msgSequenceDao.findNextValue());
		data2.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		data2.setMsgDirection(MsgDirectionCode.SENT.getValue());
		data2.setMsgSubject("Test Broadcast Subject");
		data2.setMsgPriority("2 (Normal)");
		data2.setReceivedTime(updtTime);
		
		from = emailAddrService.findSertAddress("demolist1@localhost");
		data2.setFromAddrId(from.getEmailAddrId());
		data2.setReplyToAddrId(null);

		data2.setToAddrId(from.getEmailAddrId());
		data2.setSenderId(sender.getSenderId());
		data2.setSubrId(null);
		data2.setPurgeDate(null);
		data2.setUpdtTime(updtTime);
		data2.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data2.setLockTime(null);
		data2.setLockId(null);
		
		logic = logicService.getByRuleName(RuleNameEnum.BROADCAST.getValue());
		data2.setRuleName(logic.getRuleName());
		data2.setMsgContentType(Constants.TEXT_PLAIN);
		data2.setBodyContentType(Constants.TEXT_PLAIN);
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

		// load message Streams
		loadMessageStream(data1);
		loadMessageStream(data2);

		loadMessageDlvrStat(data1);
		
		loadMessageClickCount(data1);
		
		System.out.println("Message inbox records inserted.");
	}
	
	private void loadMessageAddress(MsgInboxVo inbox) {
		MsgAddressDao msgAddrService = SpringUtil.getAppContext().getBean(MsgAddressDao.class);
		EmailAddressDao emailAddrService = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		int addrSeq = 1;
		// load message addresses
		MsgAddressVo adr1 = new MsgAddressVo();
		adr1.setMsgId(inbox.getMsgId());
		adr1.setAddrSeq(addrSeq++);
		adr1.setAddrType(EmailAddressType.FROM_ADDR.getValue());
		adr1.setAddrValue(emailAddrService.getByAddrId(inbox.getFromAddrId()).getEmailAddr());
		msgAddrService.insert(adr1);
		
		MsgAddressVo adr2 = new MsgAddressVo();
		adr2.setMsgId(inbox.getMsgId());
		adr2.setAddrSeq(addrSeq++);
		adr2.setAddrType(EmailAddressType.TO_ADDR.getValue());
		adr2.setAddrValue(emailAddrService.getByAddrId(inbox.getToAddrId()).getEmailAddr());
		msgAddrService.insert(adr2);
	}
	
	private void loadMessageHeader(MsgInboxVo inbox) {
		MsgHeaderDao headerService = SpringUtil.getAppContext().getBean(MsgHeaderDao.class);

		int hdrSeq = 0;
		MsgHeaderVo hdr1 = new MsgHeaderVo();
		hdr1.setMsgId(inbox.getMsgId());
		hdr1.setHeaderSeq(hdrSeq++);
		hdr1.setHeaderName(XHeaderName.MAILER.getValue());
		hdr1.setHeaderValue("Mailserder");
		headerService.insert(hdr1);
		
		MsgHeaderVo hdr2 = new MsgHeaderVo();
		hdr2.setMsgId(inbox.getMsgId());
		hdr2.setHeaderSeq(hdrSeq++);
		hdr2.setHeaderName(XHeaderName.RETURN_PATH.getValue());
		hdr2.setHeaderValue("demolist1@localhost");
		headerService.insert(hdr2);
		
		MsgHeaderVo hdr3 = new MsgHeaderVo();
		hdr3.setMsgId(inbox.getMsgId());
		hdr3.setHeaderSeq(hdrSeq++);
		hdr3.setHeaderName(XHeaderName.SENDER_ID.getValue());
		hdr3.setHeaderValue(Constants.DEFAULT_SENDER_ID);
		headerService.insert(hdr3);
	}
	
	private void loadMessageAttachment(MsgInboxVo inbox) {
		MsgAttachmentDao attchmntService = SpringUtil.getAppContext().getBean(MsgAttachmentDao.class);
		
		int atcSeq = 1;
		MsgAttachmentVo atc1 = new MsgAttachmentVo();
		atc1.setMsgId(inbox.getMsgId());
		atc1.setAttchmntDepth(1);
		atc1.setAttchmntSeq(atcSeq++);
		atc1.setAttchmntDisp(Part.ATTACHMENT);
		atc1.setAttchmntName("test.txt");
		atc1.setAttchmntType("text/plain; name=\"test.txt\"");
		atc1.setAttchmntValue("Test blob content goes here.".getBytes());
		attchmntService.insert(atc1);
		
		MsgAttachmentVo atc2 = new MsgAttachmentVo();
		atc2.setMsgId(inbox.getMsgId());
		atc2.setAttchmntDepth(1);
		atc2.setAttchmntSeq(atcSeq++);
		atc2.setAttchmntDisp(Part.INLINE);
		atc2.setAttchmntName("one.gif");
		atc2.setAttchmntType("image/gif; name=one.gif");
		atc2.setAttchmntValue(loadFromSamples("one.gif"));
		attchmntService.insert(atc2);

		MsgAttachmentVo atc3 = new MsgAttachmentVo();
		atc3.setMsgId(inbox.getMsgId());
		atc3.setAttchmntDepth(1);
		atc3.setAttchmntSeq(atcSeq++);
		atc3.setAttchmntDisp(Part.ATTACHMENT);
		atc3.setAttchmntName("jndi.bin");
		atc3.setAttchmntType("application/octet-stream; name=\"jndi.bin\"");
		atc3.setAttchmntValue(loadFromSamples("jndi.bin"));
		attchmntService.insert(atc3);
	}

	private void loadMessageStream(MsgInboxVo inbox) throws IOException {
		MsgStreamDao streamService = SpringUtil.getAppContext().getBean(MsgStreamDao.class);
		// test insert
		MsgStreamVo strm1 = new MsgStreamVo();
		strm1.setMsgId(inbox.getMsgId());
		strm1.setMsgSubject(inbox.getMsgSubject());
		strm1.setFromAddrId(inbox.getFromAddrId());
		strm1.setToAddrId(inbox.getToAddrId());
		strm1.setMsgStream(loadFromSamples("BouncedMail_1.txt"));
		streamService.insert(strm1);
	}
	
	private void loadMessageRfcField(MsgInboxVo inbox) {
		EmailAddressDao emailAddrService = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		MsgRfcFieldDao rfcService = SpringUtil.getAppContext().getBean(MsgRfcFieldDao.class);
		
		MsgRfcFieldVo rfc1 = new MsgRfcFieldVo();
		rfc1.setMsgId(inbox.getMsgId());
		rfc1.setRfcType("message/rfc822");
		EmailAddressVo finalRcpt = emailAddrService.findSertAddress("jackwnn@synnex.com.au");
		rfc1.setFinalRcptId(finalRcpt.getEmailAddrId());
		rfc1.setFinalRcpt(finalRcpt.getEmailAddr());
		rfc1.setOrigMsgSubject("May 74% OFF");
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
		rfcService.insert(rfc1);
		
		MsgRfcFieldVo rfc2 = new MsgRfcFieldVo();
		rfc2.setMsgId(inbox.getMsgId());
		rfc2.setRfcType("multipart/report; report-type=");
		rfc2.setFinalRcptId(finalRcpt.getEmailAddrId());
		rfc2.setFinalRcpt(finalRcpt.getEmailAddr());
		rfc2.setOrigMsgSubject("May 74% OFF");
		rfc2.setMessageId("<1631635827.01357742709854.JavaMail.wangjack@WANGJACKDEV>");
		rfc2.setDsnText(rfc1.getDsnText());
		rfcService.insert(rfc2);
		
		MsgRfcFieldVo rfc3 = new MsgRfcFieldVo();
		rfc3.setMsgId(inbox.getMsgId());
		rfc3.setRfcType("text/html; charset=us-ascii");
		EmailAddressVo finalRcpt2 = emailAddrService.findSertAddress("test@test.com");
		rfc3.setFinalRcptId(finalRcpt2.getEmailAddrId());
		rfc3.setFinalRcpt(finalRcpt2.getEmailAddr());
		rfc3.setOrigRcpt("jsmith@test.com");
		rfcService.insert(rfc3);
	}
	
	private void loadMessageDlvrStat(MsgInboxVo inbox) {
		DeliveryStatusDao dlvrStatService = SpringUtil.getAppContext().getBean(DeliveryStatusDao.class);
		
		DeliveryStatusVo stat1 = new DeliveryStatusVo();
		stat1.setMsgId(inbox.getMsgId());
		stat1.setFinalRecipientId(inbox.getFromAddrId());
		stat1.setFinalRecipient(inbox.getFromAddress());
		stat1.setDeliveryStatus("Reporting-MTA: dns;MELMX.synnex.com.au" + LF +
				"Received-From-MTA: dns;asp-6.reflexion.net" + LF +
				"Arrival-Date: Wed, 14 May 2008 08:50:31 +1000" + LF + LF +
				"Final-Recipient: rfc822;jackwnn@synnex.com.au" + LF +
				"Action: failed" + LF +
				"Status: 5.1.1"
			);
		stat1.setDsnReason("smtp; 554 delivery error: This user doesn't have a synnex.com account (jackwnn@synnex.com.au) [0] - mta522.mail.synnex.com.au");
		stat1.setDsnStatus("5.1.1");
		dlvrStatService.insert(stat1);
	}

	private void loadMessageClickCount(MsgInboxVo inbox) {
		MsgClickCountDao clickCountDao = SpringUtil.getAppContext().getBean(MsgClickCountDao.class);
		
		MsgClickCountVo vo = new MsgClickCountVo();
		vo.setMsgId(inbox.getMsgId());
		vo.setListId(MailingListEnum.SMPLLST1.name());
		vo.setDeliveryOption(MailingListDeliveryType.ALL_ON_LIST.getValue());
		clickCountDao.insert(vo);
	}

	private void loadRenderTables() {
		MsgRenderedDao renderedDao = SpringUtil.getAppContext().getBean(MsgRenderedDao.class);
		MsgSourceDao msgSourceDao = SpringUtil.getAppContext().getBean(MsgSourceDao.class);
		TemplateDataDao tmpltDao = SpringUtil.getAppContext().getBean(TemplateDataDao.class);
		
		List<MsgSourceVo> srcList = msgSourceDao.getAll();
		MsgRenderedVo vo = new MsgRenderedVo();
		vo.setMsgSourceId(srcList.get(0).getMsgSourceId());
		vo.setSenderId(Constants.DEFAULT_SENDER_ID);
		List<TemplateDataVo> tmpltList = tmpltDao.getBySenderId(Constants.DEFAULT_SENDER_ID);
		vo.setTemplateId(tmpltList.get(0).getTemplateId());
		vo.setStartTime(new Timestamp(System.currentTimeMillis()));
		vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		renderedDao.insert(vo);
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			InboxTables ct = new InboxTables();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}