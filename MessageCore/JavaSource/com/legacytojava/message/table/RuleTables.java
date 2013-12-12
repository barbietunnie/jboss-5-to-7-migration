package com.legacytojava.message.table;
/**
 * The RuleBean referenced by ActionBean with a FOREIGN key restraint.
 */
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.legacytojava.message.bo.rule.RuleBase;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.constant.RuleNameType;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.constant.XHeaderName;
import com.legacytojava.message.main.CreateTableBase;
public class RuleTables extends CreateTableBase
{
	/**
	 * Creates a new instance of RuleTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public RuleTables() throws ClassNotFoundException, SQLException {
		init();
	}

	public void dropTables() {
		try {
			stm.execute("DROP TABLE RULESUBRULEMAP");
			System.out.println("Dropped RULESUBRULEMAP Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE RULEELEMENT");
			System.out.println("Dropped RULEELEMENT Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE RULELOGIC");
			System.out.println("Dropped RULELOGIC Table...");
		}
		catch (SQLException e) {
		}
	}
	
	public void createTables() throws SQLException {
		createRuleLogicTable();
		createRuleElementTable();
		createRuleSubRuleMapTable();
	}
	
	public void loadTestData() throws SQLException {
		insertRuleLogicData();
		insertRuleElementData();
		insertRuleSubRuleMapData();
	}
	
	void createRuleLogicTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RULELOGIC ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"RuleSeq int NOT NULL, " +
			"RuleType varchar(8) NOT NULL, " + // simple/or/and/none
			"StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " +
			"StartTime datetime NOT NULL, " +
			"MailType varchar(8) NOT NULL, " + // smtpmail, webmail, ...
			"RuleCategory char(1) DEFAULT '" + RuleBase.MAIN_RULE + "', " + // E - Pre Scan, 'M' - Main Rule, P - Post Scan
			"IsSubRule char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', " +
			"builtInRule char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', " +
			"Description varchar(255), " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (RuleName, RuleSeq) " + // use index to allow update to rule name
			") ENGINE=InnoDB");
			System.out.println("Created RULELOGIC Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createRuleElementTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RULEELEMENT ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"ElementSeq int NOT NULL, " +
			"DataName varchar(26) NOT NULL, " +
			"HeaderName varchar(50), " + // X-Header name if DataName is X-Header
			"Criteria varchar(16) NOT NULL, " +
			"CaseSensitive char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', " + // Y/N
			"TargetText varchar(2000), " + 
			"TargetProc varchar(100), " +
			"Exclusions text, " + // delimited
			"ExclListProc varchar(100), " + // valid bean id
			"Delimiter char(5) DEFAULT ',', " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RULELOGIC (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX(RuleName), " +
			"UNIQUE INDEX (RuleName, ElementSeq) " +
			") ENGINE=InnoDB");
			System.out.println("Created RULEELEMENT Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createRuleSubRuleMapTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RULESUBRULEMAP ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"SubRuleName varchar(26) NOT NULL, " +
			"SubRuleSeq int NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RULELOGIC (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"FOREIGN KEY (SubRuleName) REFERENCES RULELOGIC (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (RuleName, SubRuleName) " +
			") ENGINE=InnoDB");
			System.out.println("Created RULESUBRULEMAP Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertRuleLogicData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO RULELOGIC (" +
					"RuleName, " +
					"RuleSeq, " +
					"RuleType, " + 
					"StatusId, " +
					"StartTime, " +
					"MailType, " + 
					"RuleCategory, " + 
					"IsSubRule, " +
					"builtInRule, " +
					"Description) " +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, "Unattended_Mailbox");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(4, StatusIdCode.ACTIVE);
			ps.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.PRE_RULE);
			ps.setString(8, Constants.NO_CODE); // sub rule?
			ps.setString(9, Constants.NO_CODE); // built-in rule?
			ps.setString(10, "simply get rid of the messages from the mailbox.");
			ps.execute();
			
			// built-in rules
			ps.setString(1, RuleNameType.HARD_BOUNCE.toString());
			ps.setInt(2, 101);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(9, Constants.YES_CODE); // built-in rule?
			ps.setString(10, "from RFC Scan Routine, or from postmaster with sub-rules");
			ps.execute();
			
			ps.setString(1, RuleNameType.SOFT_BOUNCE.toString());
			ps.setInt(2, 102);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "Soft bounce, from RFC scan routine");
			ps.execute();
			
			ps.setString(1, RuleNameType.MAILBOX_FULL.toString());
			ps.setInt(2, 103);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "Mailbox full from postmaster with sub-rules");
			ps.execute();
			
			ps.setString(1, RuleNameType.MSGSIZE_TOO_BIG.toString());
			ps.setInt(2, 104);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "Message size too large");
			ps.execute();
			
			ps.setString(1, RuleNameType.MAIL_BLOCK.toString());
			ps.setInt(2, 105);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "Bounced from Bulk Email Filter");
			ps.execute();
			
			ps.setString(1, RuleNameType.SPAM_BLOCK.toString());
			ps.setInt(2, 106);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(10, "Bounced from Spam blocker");
			ps.execute();
			
			ps.setString(1, RuleNameType.VIRUS_BLOCK.toString());
			ps.setInt(2, 107);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(10, "Bounced from Virus blocker");
			ps.execute();
			
			ps.setString(1, RuleNameType.CHALLENGE_RESPONSE.toString());
			ps.setInt(2, 108);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(10, "Bounced from Challenge Response");
			ps.execute();
			
			ps.setString(1, RuleNameType.AUTO_REPLY.toString());
			ps.setInt(2, 109);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(10, "Auto reply from email client software");
			ps.execute();
			
			ps.setString(1, RuleNameType.CC_USER.toString());
			ps.setInt(2, 110);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "from scan routine, message received as recipient of CC or BCC");
			ps.execute();
			
			ps.setString(1, RuleNameType.MDN_RECEIPT.toString());
			ps.setInt(2, 111);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "from RFC scan, Message Delivery Notification, a positive receipt");
			ps.execute();
			
			ps.setString(1, RuleNameType.GENERIC.toString());
			ps.setInt(2, 112);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "Non bounce or system could not recognize it");
			ps.execute();
			
			ps.setString(1, RuleNameType.UNSUBSCRIBE.toString());
			ps.setInt(2, 113);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "remove from a mailing list");
			ps.execute();
			
			ps.setString(1, RuleNameType.SUBSCRIBE.toString());
			ps.setInt(2, 114);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "subscribe to a mailing list");
			ps.execute();
			
			ps.setString(1, RuleNameType.RMA_REQUEST.toString());
			ps.setInt(2, 115);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "RMA request, internal only");
			ps.execute();
			
			ps.setString(1, RuleNameType.CSR_REPLY.toString());
			ps.setInt(2, 116);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "called from internal program");
			ps.execute();
			
			ps.setString(1, RuleNameType.BROADCAST.toString());
			ps.setInt(2, 117);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "called from internal program");
			ps.execute();
			
			ps.setString(1, RuleNameType.SEND_MAIL.toString());
			ps.setInt(2, 118);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(10, "called from internal program");
			ps.execute();
			// end of built-in rules
			
			// Custom Rules
			ps.setString(1, "Executable_Attachment");
			ps.setInt(2, 200);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(9, Constants.NO_CODE); // built-in rule?
			ps.setString(10, "Emails with executable attachment file(s)");
			ps.execute();
			
			ps.setString(1, "Contact_Us");
			ps.setInt(2, 201);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(9, Constants.NO_CODE);
			ps.setString(10, "Contact Us Form submitted from web site");
			ps.execute();
			
			ps.setString(1, "OutOfOffice_AutoReply");
			ps.setInt(2, 205);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(9, Constants.NO_CODE);
			ps.setString(10, "ouf of the office auto reply");
			ps.execute();
			
			ps.setString(1, "XHeader_SpamScore");
			ps.setInt(2, 210);
			ps.setString(3, RuleBase.SIMPLE_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(9, Constants.NO_CODE);
			ps.setString(10, "Examine x-headers for SPAM score.");
			ps.execute();
			
			ps.setString(1, "HardBouce_WatchedMailbox");
			ps.setInt(2, 215);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.POST_RULE);
			ps.setString(9, Constants.NO_CODE);
			ps.setString(10, "post rule for hard bounced emails.");
			ps.execute();
			
			ps.setString(1, "HardBounce_NoFinalRcpt");
			ps.setInt(2, 216);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.POST_RULE);
			ps.setString(9, Constants.NO_CODE);
			ps.setString(10, "post rule for hard bounces without final recipient.");
			ps.execute();
			
			/*
			 * define SubRules
			 */
			ps.setString(1, "HardBounce_Subj_Match");
			ps.setInt(2, 218);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(8, Constants.YES_CODE); // sub rule?
			ps.setString(9, Constants.YES_CODE); // built-in rule?
			ps.setString(10, "Sub rule for hard bounces from postmaster");
			ps.execute();
			
			ps.setString(1, "HardBounce_Body_Match");
			ps.setInt(2, 219);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.YES_CODE);
			ps.setString(10, "Sub rule for hard bounces from postmaster");
			ps.execute();
			
			ps.setString(1, "MailboxFull_Body_Match");
			ps.setInt(2, 220);
			ps.setString(3, RuleBase.ALL_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.YES_CODE);
			ps.setString(10, "Sub rule for mailbox full");
			ps.execute();
			
			ps.setString(1, "SpamBlock_Body_Match");
			ps.setInt(2, 221);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.YES_CODE);
			ps.setString(10, "Sub rule for spam block");
			ps.execute();
			
			ps.setString(1, "VirusBlock_Body_Match");
			ps.setInt(2, 222);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.YES_CODE);
			ps.setString(10, "Sub rule for virus block");
			ps.execute();
			
			ps.setString(1, "ChalResp_Body_Match");
			ps.setInt(2, 223);
			ps.setString(3, RuleBase.ANY_RULE);
			ps.setString(6, Constants.SMTP_MAIL);
			ps.setString(7, RuleBase.MAIN_RULE);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.YES_CODE);
			ps.setString(10, "Sub rule for challenge response");
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all RuleLogic rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertRuleElementData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO RULEELEMENT (" +
					"RuleName, " +
					"ElementSeq , " +
					"DataName, " +
					"HeaderName, " + 
					"Criteria, " +
					"CaseSensitive, " + 
					"TargetText, " + 
					"TargetProc, " +
					"Exclusions, " + 
					"ExclListProc, " + 
					"Delimiter) " +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, "Unattended_Mailbox");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.MAILBOX_USER);
			ps.setString(4, null);
			ps.setString(5, RuleBase.EQUALS);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "noreply");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "Unattended_Mailbox");
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.RETURN_PATH);
			ps.setString(4, null);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^<?.+@.+>?$"); // make sure the return path is not blank or <>
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.HARD_BOUNCE.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.FROM_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^(?:postmaster|mailmaster|mailadmin|administrator)\\S*\\@");
			ps.setString(9, "postmaster@legacytojava.com,postmaster@" + Constants.VENDER_DOMAIN_NAME);
			ps.setString(10, "excludingPostmastersBo");
			ps.setString(11, ",");
			ps.execute();
			
			ps.setString(1, RuleNameType.HARD_BOUNCE.toString());
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.FROM_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^(?:mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@");
			ps.setString(9, "mailer-daemon@legacytojave.com,mailer-daemon@" + Constants.VENDER_DOMAIN_NAME);
			ps.setString(10, null);
			ps.setString(11, ",");
			ps.execute();
			
			ps.setString(1, RuleNameType.MAILBOX_FULL.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.FROM_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
					"^(?:postmaster|mailmaster|mailadmin|administrator" +
					"|mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@");
			ps.setString(9, "postmaster@legacytojava.com,postmaster@" + Constants.VENDER_DOMAIN_NAME);
			ps.setString(10, null);
			ps.setString(11, ",");
			ps.execute();
			
			ps.setString(1, RuleNameType.SPAM_BLOCK.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
					"^Spam rapport \\/ Spam report \\S+ -\\s+\\(\\S+\\)$" +
					"|^GWAVA Sender Notification .(?:RBL block|Spam|Content filter).$" +
					"|^\\[MailServer Notification\\]" +
					"|^MailMarshal has detected possible spam in your message");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.SPAM_BLOCK.toString());
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "EarthLink\\b.*(?:spamBlocker|spamArrest)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.SPAM_BLOCK.toString());
			ps.setInt(2, 2);
			ps.setString(3, RuleBase.FROM_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "(?:^surfcontrol|.*You_Got_Spammed)\\S*\\@");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.SPAM_BLOCK.toString());
			ps.setInt(2, 3);
			ps.setString(3, RuleBase.X_HEADER);
			ps.setString(4, XHeaderName.RETURN_PATH);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^(?:pleaseforward|quotaagent)\\S*\\@");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.SPAM_BLOCK.toString());
			ps.setInt(2, 4);
			ps.setString(3, RuleBase.X_HEADER);
			ps.setString(4, "Precedence");
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^(?:spam)$");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.CHALLENGE_RESPONSE.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.X_HEADER);
			ps.setString(4, XHeaderName.RETURN_PATH);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
					"(?:spamblocker-challenge|spamhippo|devnull-quarantine)\\@" +
					"|\\@(?:spamstomp\\.com|ipermitmail\\.com)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.CHALLENGE_RESPONSE.toString());
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(4, null);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
					"^(?:Your email requires verification verify:" +
					"|Please Verify Your Email Address" +
					"|Unverified email to " +
					"|Your mail to .* requires confirmation)" +
					"|\\[Qurb .\\d+\\]$");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.CHALLENGE_RESPONSE.toString());
			ps.setInt(2, 2);
			ps.setString(3, RuleBase.FROM_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "confirm-\\S+\\@spamguard\\.vanquish\\.com");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.AUTO_REPLY.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
					"(?:Exception.*(?:Out\\b.*of\\b.*Office|Autoreply:)|\\(Auto Response\\))" +
				 	"|^(?:Automatically Generated Response from|Auto-Respond E-?Mail from" +
				 	"|AutoResponse - Email Returned|automated response|Yahoo! Auto Response)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.AUTO_REPLY.toString());
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.FROM_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^(?:automated-response|autoresponder|autoresponse-\\S+)\\S*\\@");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.AUTO_REPLY.toString());
			ps.setInt(2, 2);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
					"^This messages was created automatically by mail delivery software" +
					"|(?:\\bThis is an autoresponder. I'll never see your message\\b" +
					"|(?:\\bI(?:.m|\\s+am|\\s+will\\s+be|.ll\\s+be)\\s+(?:(?:out\\s+of|away\\s+from)\\s+the\\s+office|on\\s+vacation)\\s+(?:from|to|until|after)\\b)" +
					"|\\bI\\s+am\\s+currently\\s+out\\s+of\\s+the\\s+office\\b" +
					"|\\bI ?.m\\s+away\\s+until\\s+.{10,20}\\s+and\\s+am\\s+unable\\s+to\\s+read\\s+your\\s+message\\b)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.VIRUS_BLOCK.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
					"^(?:Disallowed attachment type found" +
					"|Norton Anti.?Virus failed to scan an attachment in a message you sent" +
					"|Norton Anti.?Virus detected and quarantined" +
					"|Warning - You sent a Virus Infected Email to " +
					"|Warning:\\s*E-?mail virus(es)? detected" +
					"|MailMarshal has detected a Virus in your message" +
					"|Banned or potentially offensive material" +
					"|Failed to clean virus\\b" +
					"|Virus Alert\\b" +
					"|Virus detected " +
					"|Virus to sender" +
					"|NAV detected a virus in a document " +
					"|InterScan MSS for SMTP has delivered a message" +
					"|InterScan NT Alert" +
					"|Antigen found\\b" +
					"|MMS Notification" +
					"|VIRUS IN YOUR MAIL " +
					"|Scan.?Mail Message: ?.{0,30} virus found " +
					"|McAfee GroupShield Alert)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.VIRUS_BLOCK.toString());
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
				"^(?:Undeliverable mail, invalid characters in header" +
					"|Delivery (?:warning|error) report id=" +
					"|The MIME information you requested" +
					"|Content violation" +
					"|Report to Sender" +
					"|RAV Anti.?Virus scan results" +
					"|Symantec AVF detected " +
					"|Symantec E-Mail-Proxy " +
					"|Virus Found in message" +
					"|Inflex scan report \\[" +
					"|\\[Mail Delivery .{10,100} infected attachment.*removed)" +
				"|(?:(Re: ?)+Wicked screensaver\\b" +
					"|\\bmailsweeper\\b" +
					"|\\bFile type Forbidden\\b" +
					"|AntiVirus scan results" +
					"|Security.?Scan Anti.?Virus" +
					"|Norton\\sAntiVirus\\b.*detected)" +
				"|^(?:Message Undeliverable: Possible Junk\\/Spam Mail Identified" +
					"|EMAIL REJECTED" +
					"|Virusmelding)$");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.VIRUS_BLOCK.toString());
			ps.setInt(2, 2);
			ps.setString(3, RuleBase.FROM_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "(?:virus|scanner|devnull)\\S*\\@");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.MAIL_BLOCK.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "Message\\b.*blocked\\b.*bulk email filter");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.MAIL_BLOCK.toString());
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "blocked by\\b.*Spam Firewall");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.BROADCAST.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.RULE_NAME);
			ps.setString(5, RuleBase.EQUALS);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, RuleNameType.BROADCAST.toString());
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.UNSUBSCRIBE.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.TO_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^mailinglist@.*|^jwang@localhost$");
			ps.setString(8, "mailingListRegExBo");
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.UNSUBSCRIBE.toString());
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.EQUALS);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "unsubscribe");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.SUBSCRIBE.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.TO_ADDR);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^mailinglist@.*|^jwang@localhost$");
			ps.setString(8, "mailingListRegExBo");
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.SUBSCRIBE.toString());
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "\\s*subscribe\\s*");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, RuleNameType.RMA_REQUEST.toString());
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.RULE_NAME);
			ps.setString(5, RuleBase.EQUALS);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, RuleNameType.RMA_REQUEST.toString());
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "OutOfOffice_AutoReply");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "(?:out\\s+of\\s+.*office|\\(away from the office\\)$)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "OutOfOffice_AutoReply");
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "^.{0,100}\\bwill\\b.{0,50}return|^.{4,100}\\breturning\\b|^.{2,100}\\bvacation\\b");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "Contact_Us");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.MAILBOX_USER);
			ps.setString(5, RuleBase.EQUALS);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "support");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "Contact_Us");
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.STARTS_WITH);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "Inquiry About:");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "Executable_Attachment");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(5, RuleBase.VALUED);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "dummy");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "Executable_Attachment");
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.FILE_NAME);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, ".*\\.(?:exe|bat|cmd|com|msi|ocx)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "XHeader_SpamScore");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.X_HEADER);
			ps.setString(4,"X_Spam_Score");
			ps.setString(5, RuleBase.GREATER_THAN);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "100");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "HardBouce_WatchedMailbox");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.RULE_NAME);
			ps.setString(5, RuleBase.EQUALS);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, RuleNameType.HARD_BOUNCE.toString());
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "HardBouce_WatchedMailbox");
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.TO_ADDR);
			ps.setString(5, RuleBase.STARTS_WITH);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "watched_maibox@");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "HardBounce_NoFinalRcpt");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.RULE_NAME);
			ps.setString(5, RuleBase.EQUALS);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, RuleNameType.HARD_BOUNCE.toString());
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "HardBounce_NoFinalRcpt");
			ps.setInt(2, 1);
			ps.setString(3, EmailAddressType.FINAL_RCPT_ADDR);
			ps.setString(5, RuleBase.NOT_VALUED);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "HardBounce_NoFinalRcpt");
			ps.setInt(2, 2);
			ps.setString(3, EmailAddressType.ORIG_RCPT_ADDR);
			ps.setString(5, RuleBase.NOT_VALUED);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "HardBounce_Subj_Match");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.SUBJECT);
			ps.setString(4, null);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
				"^(?:Returned mail:\\s(?:User unknown|Data format error)" +
					"|Undeliverable: |Undeliver(?:able|ed) Mail\\b|Undeliverable Message" +
					"|Returned mail.{0,5}(?:Error During Delivery|see transcript for details)" +
					"|e-?mail addressing error \\(|No valid recipient in )" +
				"|(?:User.*unknown|failed.*delivery|delivery.*(?:failed|failure|problem)" +
					"|Returned mail:.*(?:failed|failure|error)|\\(Failure\\)|failure notice" +
					"|not.*delivered)" );
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "HardBounce_Body_Match");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
				"(?:\\bYou(?:.ve| have) reached a non.?working address\\.\\s+Please check\\b" +
				"|eTrust Secure Content Manager SMTPMAIL could not deliver the e-?mail" +
				"|\\bPlease do not resend your original message\\." +
				"|\\s[45]\\.\\d{1,3}\\.\\d{1,3}\\s)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "MailboxFull_Body_Match");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "(?:mailbox|inbox|account).{1,50}(?:exceed|is|was).{1,40}(?:storage|full|limit|size|quota)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "MailboxFull_Body_Match");
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "(?:storage|full|limit|size|quota)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "SpamBlock_Body_Match");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.MSG_REF_ID);
			ps.setString(5, RuleBase.NOT_VALUED);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "dummy");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "ChalResp_Body_Match");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.BODY);
			ps.setString(4, null);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
				"(?:Your mail .* requires your confirmation" +
				"|Your message .* anti-spam system.* iPermitMail" +
				"|apologize .* automatic reply.* control spam.* approved senders" +
				"|Vanquish to avoid spam.* automated message" +
				"|automated message.* apologize .* approved senders)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "VirusBlock_Body_Match");
			ps.setInt(2, 0);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
					"(?:a potentially executable attachment " +
					"|\\bhas stripped one or more attachments from the following message\\b" +
					"|message contains file attachments that are not permitted" +
					"|host \\S+ said: 5\\d\\d\\s+Error: Message content rejected" +
					"|TRANSACTION FAILED - Unrepairable Virus Detected. " +
					"|Mail.?Marshal Rule: Inbound Messages : Block Dangerous Attachments" +
					"|The mail message \\S+ \\S+ you sent to \\S+ contains the virus" +
					"|mailsweeper has found that a \\S+ \\S+ \\S+ \\S+ one or more virus" +
					"|Attachment.{0,40}was Deleted" +
					"|Virus.{1,40}was found" +
					"|\\bblocked by Mailsweeper\\b" +
					"|\\bvirus scanner deleted your message\\b" +
					"|\\bThe attachment was quarantined\\b" +
					"|\\bGROUP securiQ.Wall\\b)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.setString(1, "VirusBlock_Body_Match");
			ps.setInt(2, 1);
			ps.setString(3, RuleBase.BODY);
			ps.setString(5, RuleBase.REG_EX);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, 
				"(?:Reason: Rejected by filter" +
					"|antivirus system report" +
					"|the antivirus module has" +
					"|the infected attachment" +
					"|illegal attachment" +
					"|Unrepairable Virus Detected" +
					"|Reporting-MTA: Norton Anti.?Virus Gateway" +
					"|\\bV I R U S\\b)" +
				"|^(?:Found virus \\S+ in file \\S+" +
					"|Incident Information:)");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.setString(10, null);
			ps.setString(11, null);
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all RuleElement rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void insertRuleSubRuleMapData() throws SQLException {
		try {
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO RULESUBRULEMAP (" +
					"RuleName, " +
					"SubRuleName, " +
					"SubRuleSeq) " +
				" VALUES (?, ?, ?)");
			
			ps.setString(1, RuleNameType.HARD_BOUNCE.toString());
			ps.setString(2, "HardBounce_Subj_Match");
			ps.setInt(3,0);
			ps.execute();
			
			ps.setString(1, RuleNameType.HARD_BOUNCE.toString());
			ps.setString(2, "HardBounce_Body_Match");
			ps.setInt(3,1);
			ps.execute();
			
			ps.setString(1, RuleNameType.MAILBOX_FULL.toString());
			ps.setString(2, "MailboxFull_Body_Match");
			ps.setInt(3,2);
			ps.execute();
			
			ps.setString(1, RuleNameType.SPAM_BLOCK.toString());
			ps.setString(2, "SpamBlock_Body_Match");
			ps.setInt(3,0);
			ps.execute();
			
			ps.setString(1, RuleNameType.CHALLENGE_RESPONSE.toString());
			ps.setString(2, "ChalResp_Body_Match");
			ps.setInt(3,0);
			ps.execute();
			
			ps.setString(1, RuleNameType.VIRUS_BLOCK.toString());
			ps.setString(2, "VirusBlock_Body_Match");
			ps.setInt(3,0);
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all RuleSubRuleMap rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param args the command line arguments
	 */
//	public static void main(String[] args) {
//		try {
//			RuleTables ct = new RuleTables();
//			ct.dropTables();
//			ct.createTables();
//			ct.insertData();
//			ct.wrapup();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}