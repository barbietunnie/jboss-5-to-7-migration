package com.es.data.loader;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.es.core.util.ProductKey;
import com.es.core.util.ProductUtil;
import com.es.core.util.SpringUtil;
import com.es.core.util.TimestampUtil;
import com.es.dao.sender.SenderDataDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.SenderType;
import com.es.data.constant.StatusId;
import com.es.vo.comm.SenderDataVo;

public class SenderTable extends AbstractTableBase {
	
	public void dropTables() {
		try {
			getJdbcTemplate().execute("DROP TABLE RELOAD_FLAGS");
			System.out.println("Dropped RELOAD_FLAGS Table...");
		}
		catch (Exception e) {
		}		
		try {
			getJdbcTemplate().execute("DROP TABLE Sender_Data");
			System.out.println("Dropped Sender_Data Table...");
		}
		catch (Exception e) {
		}		
	}
	
	public void createTables() throws DataAccessException {
		createSenderTable();
		createReloadFlagsTable();
	}
	
	void createSenderTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE Sender_Data ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "SenderId varchar(16) NOT NULL, "
					+ "SenderName varchar(40) NOT NULL, "
					+ "SenderType char(1), " // TBD
					+ "DomainName varchar(100) NOT NULL, " 
						// used by VERP and System E-Mails to set Return-Path
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " // 'A' or 'I'
					+ "IrsTaxId varchar(10), " // IRS Tax Id
					+ "WebSiteUrl varchar(100), "
					+ "SaveRawMsg char(1) NOT NULL DEFAULT '" + CodeType.YES_CODE.getValue() + "', " 
						// save SMTP message stream to MSGSTREAM? used by RuleEngine
					+ "ContactName varchar(60), "
					+ "ContactPhone varchar(18), "
					+ "ContactEmail varchar(255) NOT NULL, "
					// for rule engine, email addresses used by ACTIONS
					+ "SecurityEmail varchar(255) NOT NULL, "
					+ "CustcareEmail varchar(255) NOT NULL, "
					+ "RmaDeptEmail varchar(255) NOT NULL, "
					+ "SpamCntrlEmail varchar(255) NOT NULL, "
					+ "ChaRspHndlrEmail varchar(255) NOT NULL, "
					// for mail sender, embed EmailId to the bottom of email
					+ "EmbedEmailId varchar(3) NOT NULL DEFAULT '" + CodeType.YES.getValue() + "', "
					+ "ReturnPathLeft varchar(50) NOT NULL, "
					// for mail sender, define testing addresses
					+ "UseTestAddr varchar(3) NOT NULL DEFAULT '" + CodeType.NO.getValue() + "', "
					+ "TestFromAddr varchar(255), "
					+ "TestToAddr varchar(255), "
					+ "TestReplytoAddr varchar(255), "
					// for mail sender, use VERP in Return-Path?
					+ "IsVerpEnabled varchar(3) NOT NULL DEFAULT '" + CodeType.NO.getValue() + "', "
					+ "VerpSubDomain varchar(50), " // sub domain used by VERP bounces
					/*
					 * we do not need to define a separate VERP domain. If the
					 * domain in Return-Path is different from the one in FROM
					 * address, it could trigger SPAM filter.
					 */
					+ "VerpInboxName varchar(50), " // mailbox name for VERP bounces
					+ "VerpRemoveInbox varchar(50), " // mailbox name for VERP un-subscribe
					// store time stamp when the table is initially loaded
					+ "SystemId varchar(40) NOT NULL DEFAULT ' ', "
					+ "SystemKey varchar(30), "
					// Begin -> not implemented yet
					+ "Dikm char(1), " // DIKM support - S:Send/R:Receive/B:Both/N:None
					+ "DomainKey char(1), " // DomainKey support
					+ "KeyFilePath varchar(200), " // Private Key file location
					+ "SPF char(1), " // SPF check Y/N
					// <- End
					+ "UpdtTime datetime NOT NULL, "
					+ "UpdtUserId char(10) NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					//+ "UNIQUE INDEX (DomainName), "
					+ "UNIQUE INDEX (SenderId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created Sender_Data Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createReloadFlagsTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE RELOAD_FLAGS ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "Senders int NOT NULL, "
					+ "Rules int NOT NULL, "
					+ "Actions int NOT NULL, "
					+ "Templates int NOT NULL, "
					+ "Schedules int NOT NULL, "
					+ "PRIMARY KEY (RowId) "
					+ ") ENGINE=MyISAM");
			System.out.println("Created RELOAD_FLAGS Table...");
			getJdbcTemplate().execute("INSERT INTO RELOAD_FLAGS VALUES(1,0,0,0,0,0)");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private void loadSystemSender() {
		SenderDataDao dao = SpringUtil.getAppContext().getBean(SenderDataDao.class);
		SenderDataVo data = new SenderDataVo();
		data.setSenderId(Constants.DEFAULT_SENDER_ID);
		data.setSenderName(getProperty("sender.name"));
		data.setDomainName(getProperty("sender.domain")); // domain name
		data.setSenderType(SenderType.System.getValue());
		data.setContactName(getProperty("sender.contact.name"));
		data.setContactPhone(getProperty("sender.contact.phone"));
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setIrsTaxId("0000000000");
		data.setWebSiteUrl(getProperty("sender.website.url"));
		data.setSaveRawMsg(CodeType.YES_CODE.getValue()); // save raw stream
		data.setSecurityEmail(getProperty("sender.security.email"));
		data.setCustcareEmail(getProperty("sender.subscriber.care.email"));
		data.setRmaDeptEmail(getProperty("sender.rma.dept.email"));
		data.setSpamCntrlEmail(getProperty("sender.spam.control.email"));
		data.setChaRspHndlrEmail(getProperty("sender.challenge.email"));
		data.setEmbedEmailId(CodeType.YES.getValue()); // Embed EmailId 
		data.setReturnPathLeft("support"); // return-path left
		data.setUseTestAddr(CodeType.YES.getValue()); // use testing address
		data.setTestFromAddr(getProperty("sender.test.from.address"));
		data.setTestToAddr(getProperty("sender.test.to.address"));
		data.setIsVerpEnabled(CodeType.YES.getValue()); // is VERP enabled
		data.setVerpSubDomain(null); // VERP sub-domain
		data.setVerpInboxName("bounce"); // VERP bounce mailbox
		data.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		String systemId = TimestampUtil.db2ToDecimalString(TimestampUtil.getCurrentDb2Tms());
		data.setSystemId(systemId);
		data.setSystemKey(ProductUtil.getProductKeyFromFile());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		dao.insert(data);
		System.out.println("Sender record inserted.");
	}
	
	private void loadJBatchSender() {
		SenderDataDao dao = SpringUtil.getAppContext().getBean(SenderDataDao.class);
		SenderDataVo data = new SenderDataVo();
		data.setSenderId("JBatchCorp");
		data.setSenderName("JBatch Corp. Site");
		data.setDomainName("jbatch.com"); // domain name
		data.setSenderType(SenderType.Custom.getValue());
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setIrsTaxId( "0000000000");
		data.setWebSiteUrl("http://www.jbatch.com");
		data.setSaveRawMsg(CodeType.YES_CODE.getValue()); // save raw stream
		data.setSecurityEmail("security@jbatch.com");
		data.setCustcareEmail("subrcare@jbatch.com");
		data.setRmaDeptEmail("rma.dept@jbatch.com");
		data.setSpamCntrlEmail("spam.control@jbatch.com");
		data.setChaRspHndlrEmail("challenge@jbatch.com");
		data.setEmbedEmailId(CodeType.YES.getValue());
		data.setReturnPathLeft("support"); // return-path left
		data.setUseTestAddr(CodeType.NO.getValue()); // use testing address
		data.setTestFromAddr("testfrom@jbatch.com");
		data.setTestToAddr("testto@jbatch.com");
		data.setIsVerpEnabled(CodeType.NO.getValue()); // is VERP enabled
		data.setVerpSubDomain(null); // VERP sub domain
		data.setVerpInboxName("bounce"); // VERP bounce mailbox
		data.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		data.setSystemId("");
		data.setSystemKey(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		dao.insert(data);
		System.out.println("Sender record inserted.");
	}

	public void loadTestData() throws DataAccessException {
		try {
			loadSystemSender();
			loadJBatchSender();
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadReleaseData() throws DataAccessException {
		try {
			loadSystemSender();
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public int updateSender4Prod() {
		SenderDataDao dao = SpringUtil.getAppContext().getBean(SenderDataDao.class);
		SenderDataVo vo = dao.getBySenderId(Constants.DEFAULT_SENDER_ID);
		vo.setSenderName("Emailsphere");
		vo.setSenderType(null);
		vo.setDomainName("emailsphere.com"); // domain name
		vo.setStatusId(StatusId.ACTIVE.getValue());
		vo.setIrsTaxId("0000000000");
		vo.setWebSiteUrl("http://www.emailsphere.com/newsletter");
		vo.setSaveRawMsg(CodeType.YES_CODE.getValue()); // save raw stream
		vo.setContactEmail("sitemaster@emailsphere.com");
		vo.setSecurityEmail("security@emailsphere.com");
		vo.setCustcareEmail("custcare@emailsphere.com");
		vo.setRmaDeptEmail("rma.dept@emailsphere.com");
		vo.setSpamCntrlEmail("spam.ctrl@emailsphere.com");
		vo.setChaRspHndlrEmail("challenge@emailsphere.com");
		vo.setEmbedEmailId(CodeType.YES.getValue()); // Embed EmailId 
		vo.setUseTestAddr(CodeType.NO.getValue()); // use testing address
		vo.setTestFromAddr("testfrom@emailsphere.com");
		vo.setTestToAddr("testto@emailsphere.com");
		//vo.setTestReplytoAddr(null);
		vo.setIsVerpEnabled(CodeType.YES.getValue()); // is VERP enabled
		//vo.setVerpSubDomain(null); // VERP sub domain
		vo.setVerpInboxName("bounce"); // VERP bounce mailbox
		vo.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		//String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp());
		//vo.setSystemId(systemId);
		vo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		int rowsInserted = dao.update(vo);
		if (ProductKey.validateKey(ProductUtil.getProductKeyFromFile())) {
			dao.updateSystemKey(ProductUtil.getProductKeyFromFile());
		}
		System.out.println("SenderTable: Default sender updated.");
		return rowsInserted;
	}
	
	/**
	 * update System sender to trigger the loading of Sender Variables into
	 * SenderVariable table.
	 */
	public void updateAllSenders() {
		SenderDataDao dao = SpringUtil.getAppContext().getBean(SenderDataDao.class);
		int rowsUpdated = 0;
		List<SenderDataVo> list = dao.getAll();
		for (SenderDataVo vo : list) {
			vo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
			rowsUpdated += dao.update(vo);
		}
		SenderDataVo vo = dao.getBySenderId(Constants.DEFAULT_SENDER_ID);
		if (vo == null) { // just in case
			try {
				loadSystemSender();
				rowsUpdated++;
				System.out.println("Default Sender inserted");
			}
			catch (DataAccessException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Number of Senders updated: " + rowsUpdated);
	}
	
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			SenderTable ct = new SenderTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}