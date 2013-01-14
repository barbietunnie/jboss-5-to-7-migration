package com.legacytojava.message.dao.inbox;
import java.sql.SQLException;

import com.legacytojava.message.constant.CarrierCode;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MailingListDeliveryOption;
import com.legacytojava.message.main.CreateTableBase;
public class InboxTables extends CreateTableBase
{
	/**
	 * Creates a new instance of InboxTables 
	 * @throws SQLException 
	 * @throws ClassNotFoundException
	 */
	public InboxTables() throws ClassNotFoundException, SQLException {
		init();
	}

	public void dropTables() {
		try {
			stm.execute("DROP TABLE MsgUnsubComments");
			System.out.println("Dropped MsgUnsubComments Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGCLICKCOUNTS");
			System.out.println("Dropped MSGCLICKCOUNTS Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGACTIONLOGS");
			System.out.println("Dropped MSGACTIONLOGS Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE DELIVERYSTATUS");
			System.out.println("Dropped DELIVERYSTATUS Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE ATTACHMENTS");
			System.out.println("Dropped ATTACHMENTS Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGADDRS");
			System.out.println("Dropped MSGADDRS Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGHEADERS");
			System.out.println("Dropped MSGHEADERS Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE RFCFIELDS");
			System.out.println("Dropped RFCFIELDS Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGSTREAM");
			System.out.println("Dropped MSGSTREAM Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGINBOX");
			System.out.println("Dropped MSGINBOX Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGUNREADCOUNT");
			System.out.println("Dropped MSGUNREADCOUNT Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGSEQUENCE");
			System.out.println("Dropped MSGSEQUENCE Table...");
		}
		catch (SQLException e) {
		}
		
		try {
			stm.execute("DROP TABLE RENDERATTACHMENT");
			System.out.println("Dropped RENDERATTACHMENT Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE RENDERVARIABLE");
			System.out.println("Dropped RENDERVARIABLE Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE RENDEROBJECT");
			System.out.println("Dropped RENDEROBJECT Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MSGRENDERED");
			System.out.println("Dropped MSGRENDERED Table...");
		}
		catch (SQLException e) {
		}
	}

	public void createTables() throws SQLException {
		createMSGRENDEREDTable();
		createRENDERATTACHMENTTable();
		createRENDERVARIABLETable();
		createRENDEROBJECTTable();
		
		createMSGSEQUENCETable();
		createMSGUNREADCOUNTTable();
		createMSGINBOXTable();
		createATTACHMENTSTable();
		createMSGADDRSTable();
		createMSGHEADERSTable();
		createRFCFIELDSTable();
		createMSGSTREAMTable();
		createDELIVERYSTATUSTable();
		createMSGACTIONLOGSTable();
		createMSGCLICKCOUNTSTable();
		createMsgUnsubCommentsTable();
	}
	
	public void loadTestData() throws SQLException {
		// dummy method to satisfy the super class
	}
	
	void createMSGRENDEREDTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGRENDERED ( " +
			"RenderId bigint NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
			"MsgSourceId varchar(16) NOT NULL, " +
			"SubjTemplateId varchar(16) NOT NULL, " +
			"BodyTemplateId varchar(16) NOT NULL, " +
			"StartTime datetime NOT NULL," +
			"ClientId varchar(16), " +
			"CustId varchar(16), " +
			"PurgeAfter int, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"INDEX (MsgSourceId) " +
			") ENGINE=InnoDB"); // row-level locking
			System.out.println("Created MSGRENDERED Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRENDERATTACHMENTTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RENDERATTACHMENT ( " +
			"RenderId bigint NOT NULL, " +
			"AttchmntSeq decimal(2) NOT NULL, " + // up to 100 attachments per message
			"AttchmntName varchar(100), " +
			"AttchmntType varchar(100), " +
			"AttchmntDisp varchar(100), " +
			"AttchmntValue mediumblob, " +
			"FOREIGN KEY (RenderId) REFERENCES MSGRENDERED (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,AttchmntSeq)) ENGINE=InnoDB");
			System.out.println("Created RENDERATTACHMENT Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRENDERVARIABLETable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RENDERVARIABLE ( " +
			"RenderId bigint NOT NULL, " +
			"VariableName varchar(26), " +
			"VariableFormat varchar(50), " +
			"VariableType char(1), " +
			// T - text, N - numeric, D - DateField/time,
			// A - address, X - X-Header, L - LOB(Attachment)
			"VariableValue text, " +
			"FOREIGN KEY (RenderId) REFERENCES MSGRENDERED (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,VariableName)) ENGINE=InnoDB");
			System.out.println("Created RENDERVARIABLE Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRENDEROBJECTTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RENDEROBJECT ( " +
			"RenderId bigint NOT NULL, " +
			"VariableName varchar(26), " +
			"VariableFormat varchar(50), " +
			"VariableType char(1), " +
			// T - text, N - numeric, D - DateField/time,
			// A - address, X - X-Header, L - LOB(Attachment), C - Collection
			"VariableValue mediumblob, " +
			"FOREIGN KEY (RenderId) REFERENCES MSGRENDERED (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"PRIMARY KEY (RenderId,VariableName)) ENGINE=InnoDB");
			System.out.println("Created RENDEROBJECT Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGSEQUENCETable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGSEQUENCE ( " +
			"SeqId bigint NOT NULL " +
			") TYPE=MyISAM"); // table-level locking ?
			System.out.println("Created MSGSEQUENCE Table...");
			stm.execute("INSERT INTO MSGSEQUENCE (SeqId) VALUES(0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGUNREADCOUNTTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGUNREADCOUNT ( " +
			"InboxUnreadCount int NOT NULL, " +
			"SentUnreadCount int NOT NULL" +
			") TYPE=MyISAM"); // table-level locking ?
			System.out.println("Created MSGUNREADCOUNT Table...");
			stm.execute("INSERT INTO MSGUNREADCOUNT (InboxUnreadCount,SentUnreadCount) VALUES(0,0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGINBOXTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGINBOX ( " +
			"MsgId bigint NOT NULL PRIMARY KEY, " +
			"MsgRefId bigint, " + // link to another MSGINBOX record (a reply or a bounce)
			"LeadMsgId bigint NOT NULL, " +
			"CarrierCode char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL + "', " + // S - SmtpMail, W - WebMail
			"MsgDirection char(1) NOT NULL, " + // R - Received, S - Sent
			"RuleName varchar(26) NOT NULL, " + // link to RuleLogic.RuleName
			"MsgSubject varchar(255), " +
			"MsgPriority varchar(10), " + // 1 (High)/2 (Normal)/3 (Low)
			"ReceivedTime datetime NOT NULL, " +
			"FromAddrId bigint, " + // link to EmailAddr
			"ReplyToAddrId bigint, " + // link to EmailAddr
			"ToAddrId bigint, " + // link to EmailAddr
			"ClientId varchar(16), " + // link to Clients - derived from OutMsgRefId
			"CustId varchar(16), " + // link to Customers - derived from OutMsgRefId
			"PurgeDate Date, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"LockTime datetime, " +
			"LockId varchar(10), " +
			"ReadCount int NOT NULL DEFAULT 0, " + // how many times it's been read
			"ReplyCount int NOT NULL DEFAULT 0, " + // how many times it's been replied
			"ForwardCount int NOT NULL DEFAULT 0, " + // how many times it's been forwarded
			"Flagged char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', " +
			"DeliveryTime datetime, " + // for out-bound messages only, updated by MailSender
			"StatusId char(1) NOT NULL, " + // P - pending, D - delivered by MailSender, F - delivery failed, C/O - closed/Open (for received mail)
			"SmtpMessageId varchar(255), " + // SMTP message Id, updated by MailSender once delivered
			"RenderId bigint, " + // link to a MsgRendered record
			"OverrideTestAddr char(1), " + // Y - tell MailSender to use TO address even under test mode
			"AttachmentCount smallint NOT NULL DEFAULT 0, " + // for UI performance
			"AttachmentSize int NOT NULL DEFAULT 0, " + // for UI performance
			"MsgBodySize int NOT NULL DEFAULT 0, " + // for UI performance
			"MsgContentType varchar(100) NOT NULL, " +
			"BodyContentType varchar(50), " +
			"MsgBody mediumtext, " +
			"INDEX (LeadMsgId), " +
			"FOREIGN KEY (RenderId) REFERENCES MSGRENDERED (RenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (RenderId), " +
			"FOREIGN KEY (FromAddrId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FromAddrId), " +
			"FOREIGN KEY (ToAddrId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (ToAddrId)" +
			") ENGINE=InnoDB");
			System.out.println("Created MSGINBOX Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createATTACHMENTSTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE ATTACHMENTS ( " +
			"MsgId bigint NOT NULL, " +
			"AttchmntDepth decimal(2) NOT NULL, " +
			"AttchmntSeq decimal(3) NOT NULL, " +
			"AttchmntName varchar(100), " +
			"AttchmntType varchar(100), " +
			"AttchmntDisp varchar(100), " +
			"AttchmntValue mediumblob, " +
			"FOREIGN KEY (MsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,AttchmntDepth,AttchmntSeq)" +
			") ENGINE=InnoDB");
			System.out.println("Created ATTACHMENTS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGADDRSTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGADDRS ( " +
			"MsgId bigint NOT NULL, " +
			"AddrType varchar(7) NOT NULL, " + // from, replyto, to, cc, bcc
			"AddrSeq decimal(4) NOT NULL, " +
			"AddrValue varchar(255), " +
			"FOREIGN KEY (MsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,AddrType,AddrSeq)" +
			") ENGINE=InnoDB");
			System.out.println("Created MSGADDRS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGHEADERSTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGHEADERS ( " +
			"MsgId bigint NOT NULL, " +
			"HeaderSeq decimal(4) NOT NULL, " +
			"HeaderName varchar(100), " +
			"HeaderValue text, " +
			"FOREIGN KEY (MsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"PRIMARY KEY (MsgId,HeaderSeq)" +
			") ENGINE=InnoDB");
			System.out.println("Created MSGHEADERS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createRFCFIELDSTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RFCFIELDS ( " +
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
			"FOREIGN KEY (MsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"FOREIGN KEY (FinalRcptId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FinalRcptId), " +
			"PRIMARY KEY (MsgId,RfcType)" +
			") ENGINE=InnoDB");
			System.out.println("Created RFCFIELDS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGSTREAMTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGSTREAM ( " +
			"MsgId bigint NOT NULL, " +
			"FromAddrId bigint, " +
			"ToAddrId bigint, " +
			"MsgSubject varchar(255), " +
			"AddTime datetime, " +
			"MsgStream mediumblob, " +
			"UNIQUE INDEX (MsgId), " +
			"FOREIGN KEY (MsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE " +
			") ENGINE=InnoDB");
			System.out.println("Created MSGSTREAM Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createDELIVERYSTATUSTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE DELIVERYSTATUS ( " +
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
			"FOREIGN KEY (MsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (MsgId), " +
			"FOREIGN KEY (FinalRecipientId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (FinalRecipientId), " +
			"PRIMARY KEY (MsgId,FinalRecipientId)) ENGINE=InnoDB");
			System.out.println("Created DELIVERYSTATUS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createMSGACTIONLOGSTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGACTIONLOGS ( " +
			"MsgId bigint NOT NULL, " +
			"MsgRefId bigint, " + // link to previous message thread
			"LeadMsgId bigint NOT NULL, " + // message that started this thread
			"ActionBo varchar(50) NOT NULL, " +
			"Parameters varchar(255), " +
			"AddTime datetime, " +
			"FOREIGN KEY (MsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			/* disable following foreign keys for performance reason */ 
			//"FOREIGN KEY (MsgRefId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			//"FOREIGN KEY (LeadMsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX (LeadMsgId), " +
			"UNIQUE INDEX (MsgId, MsgRefId) " + // use index to make MsgRefId nullable
			") ENGINE=InnoDB");
			System.out.println("Created MSGACTIONLOGS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGCLICKCOUNTSTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGCLICKCOUNTS ( " +
			"MsgId bigint NOT NULL, " +
			"ListId varchar(8) NOT NULL, " +
			"DeliveryOption varchar(4) NOT NULL DEFAULT '" + MailingListDeliveryOption.ALL_ON_LIST + "', " +
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
			"FOREIGN KEY (MsgId) REFERENCES MSGINBOX (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (MsgId) " +
			") ENGINE=InnoDB");
			System.out.println("Created MSGCLICKCOUNTS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMsgUnsubCommentsTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MsgUnsubComments ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "MsgId bigint NOT NULL, "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8), "
					+ "Comments varchar(500) NOT NULL, "
					+ "AddTime datetime NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (MsgId) REFERENCES MSGCLICKCOUNTS (MsgId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (MsgId), "
					+ "INDEX (EmailAddrId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created MsgUnsubComments Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
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
			ct.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}