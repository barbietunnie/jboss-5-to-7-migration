package com.es.data.loader;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.dao.mailbox.MailBoxDao;
import com.es.data.constant.CarrierCode;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.MailServerType;
import com.es.data.constant.StatusId;
import com.es.data.preload.MailInboxEnum;
import com.es.vo.comm.MailBoxVo;

public class MailboxTable extends AbstractTableBase {
	
	public void dropTables() {
		try
		{
			getJdbcTemplate().execute("DROP TABLE MAIL_BOX");
			System.out.println("Dropped MAIL_BOX Table...");
		} catch (DataAccessException e) {}
	}
	
	public void createTables() throws DataAccessException {
		/*
		 Required properties for mailbox
			- UserId: mailbox user name
			- UserPswd: mailbox password
		 	- HostName: host domain name or IP address
		 	- PortNumber: port number, default to -1
			- FolderName: folder name, default to INBOX
		 	- Protocol: pop3/imap
		 	- CarrierCode: I - Internet mail, W - web-mail, U - keeping mail in in-box
		 	- InternalOnly: Y - internal mail, N - otherwise
		 	- MailBoxDesc: mailbox description
		 	- StatusId: A - Active, I - Inactive
			- ReadPerPass: maximum number of messages read per processing cycle
		 	- UseSsl: yes/no, set port number to 995 if yes
			- Threads:	number of threads per mailbox, default to 1 (experimental).
		 	- RetryMax: maximum number of retries for connection, default to 5
			- MinimumWait: time to wait between cycles, default to 2 second
							for exchange server, it must be 10 or greater
			- MessageCount: for test only, number of messages to be processed. 0=unlimited.
		 	- ToPlainText: yes/no, convert message body from HTML to plain text, default to no
			- ToAddrDomain: Derived from domains of all Senders, scan "received" address chain 
							until a match is found. The	email address with the matching domain
							becomes the real TO address.
			- CheckDuplicate: yes/no, check for duplicates, only one is processed
			- AlertDuplicate: yes/no, send out alert if found duplicate
			- LogDuplicate: yes/no, log duplicate messages
			- PurgeDupsAfter: purge duplicate messages after certain hours
			- ProcessorName: Spring processor id / processor class name
		*/
		try {
			getJdbcTemplate().execute("CREATE TABLE MAIL_BOX ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"UserId varchar(30) NOT NULL, " + 
			"UserPswd varchar(32) NOT NULL, " +
			"HostName varchar(100) NOT NULL, " +
			"PortNumber integer NOT NULL, " +
			"Protocol char(4) NOT NULL, " +
			"ServerType varchar(5) DEFAULT '" + MailServerType.SMTP.getValue() + "', " +
			"FolderName varchar(30), " +
			"MailBoxDesc varchar(50), " +
			"StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " +
			"CarrierCode char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL.getValue() + "', " +
			"InternalOnly varchar(3), " +
			"ReadPerPass integer NOT NULL, " +
			"UseSsl varchar(3) NOT NULL, " +
			"Threads integer NOT NULL, " +
			"RetryMax integer, " +
			"MinimumWait integer, " +
			"MessageCount integer NOT NULL, " +
			"ToPlainText varchar(3), " +
			"CheckDuplicate varchar(3), " +
			"AlertDuplicate varchar(3), " +
			"LogDuplicate varchar(3), " +
			"PurgeDupsAfter integer, " +
			"ProcessorName varchar(100) NOT NULL, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"Constraint UNIQUE INDEX MAIL_BOX_IDX1 (UserId, HostName) " +
			") ENGINE=InnoDB");
			System.out.println("Created MAIL_BOX Table...");
		} catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	private void loadMailInboxs(boolean prodRelease) {
		for (MailInboxEnum mc : MailInboxEnum.values()) {
			if (prodRelease) {
				if (!"localhost".equals(mc.getHostName())) {
					continue;
				}
			}
			else {
				if ("localhost".equals(mc.getHostName())) {
					continue;
				}
			}
			MailBoxVo in = new MailBoxVo();
			in.setUserId(mc.getUserId());
			in.setHostName(mc.getHostName());
			in.setUserPswd(mc.getUserPswd());
			in.setPortNumber(mc.getPort());
			in.setProtocol(mc.getProtocol().getValue());
			in.setDescription(mc.getDescription());
			in.setStatusId(mc.getStatus().getValue());
			in.setInternalOnly(mc.getIsInternalOnly()?CodeType.YES.getValue():CodeType.NO.getValue());
			in.setReadPerPass(mc.getReadPerPass());
			in.setUseSsl(mc.isUseSsl()?CodeType.YES.getValue():CodeType.NO.getValue());
			in.setThreads(mc.getNumberOfThreads());
			in.setRetryMax(mc.getMaximumRetries());
			in.setMinimumWait(mc.getMinimumWait());
			in.setMessageCount(mc.getMessageCount());
			in.setToPlainText(mc.getIsToPlainText()?CodeType.YES.getValue():CodeType.NO.getValue());
			in.setCheckDuplicate(mc.getIsCheckDuplicate()?CodeType.YES.getValue():CodeType.NO.getValue());
			in.setAlertDuplicate(mc.getIsAlertDuplicate()?CodeType.YES.getValue():CodeType.NO.getValue());
			in.setLogDuplicate(mc.getIsLogDuplicate()?CodeType.YES.getValue():CodeType.NO.getValue());
			in.setPurgeDupsAfter(mc.getPurgeDupsAfter());
			in.setUpdtUserId(Constants.DEFAULT_USER_ID);
			getMailBoxDao().insert(in);
		}
		System.out.println("Mailbox records inserted.");
		
		loadMailInboxEmailAddrs();
	}

	private void loadMailInboxEmailAddrs() {
		List<MailBoxVo> mailBoxes = getMailBoxDao().getAll(false);
		int count = 0;
		for (MailBoxVo mailbox : mailBoxes) {
			getEmailAddrDao().findSertAddress(mailbox.getUserId() + "@" + mailbox.getHostName());
			count ++;
		}
		System.out.println("Inserted/Upadted (" + count + ") EmailAddress records.");
	}
	

	public void loadTestData() throws DataAccessException {
		try { 
			loadMailInboxs(true);
			loadMailInboxs(false);
		} catch (DataAccessException e)	{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadReleaseData() throws DataAccessException {
		try {
			loadMailInboxs(true);
		} catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	private MailBoxDao mailBoxDao = null;
	private MailBoxDao getMailBoxDao() {
		if (mailBoxDao == null) {
			mailBoxDao = SpringUtil.getAppContext().getBean(MailBoxDao.class);
		}
		return mailBoxDao;
	}
	
	private EmailAddressDao emailAddrDao = null;
	private EmailAddressDao getEmailAddrDao() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getAppContext().getBean(EmailAddressDao.class);
		}
		return emailAddrDao;
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try {
			MailboxTable ct = new MailboxTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}