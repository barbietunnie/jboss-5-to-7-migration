package com.legacytojava.message.dao.client;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import com.legacytojava.jbatch.ProductUtil;
import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.common.ProductKey;
import com.legacytojava.jbatch.common.TimestampUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.main.CreateTableBase;
import com.legacytojava.message.vo.ClientVo;

public class ClientTable extends CreateTableBase {
	/** Creates a new instance of ClientTables 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public ClientTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE RELOADFLAGS");
			System.out.println("Dropped RELOADFLAGS Table...");
		}
		catch (Exception e) {
		}		
		try {
			stm.execute("DROP TABLE CLIENTS");
			System.out.println("Dropped CLIENTS Table...");
		}
		catch (Exception e) {
		}		
	}
	
	public void createTables() throws SQLException {
		createClientTable();
		createReloadFlagsTable();
	}
	
	void createClientTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE CLIENTS ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "ClientId varchar(16) NOT NULL, "
					+ "ClientName varchar(40) NOT NULL, "
					+ "ClientType char(1), " // TBD
					+ "DomainName varchar(100) NOT NULL, " 
						// used by VERP and System E-Mails to set Return-Path
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " // 'A' or 'I'
					+ "IrsTaxId varchar(10), " // IRS Tax Id
					+ "WebSiteUrl varchar(100), "
					+ "SaveRawMsg char(1) NOT NULL DEFAULT '" + Constants.YES_CODE + "', " 
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
					+ "EmbedEmailId varchar(3) NOT NULL DEFAULT '" + Constants.YES + "', "
					+ "ReturnPathLeft varchar(50) NOT NULL, "
					// for mail sender, define testing addresses
					+ "UseTestAddr varchar(3) NOT NULL DEFAULT '" + Constants.NO + "', "
					+ "TestFromAddr varchar(255), "
					+ "TestToAddr varchar(255), "
					+ "TestReplytoAddr varchar(255), "
					// for mail sender, use VERP in Return-Path?
					+ "IsVerpEnabled varchar(3) NOT NULL DEFAULT '" + Constants.NO + "', "
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
					+ "UNIQUE INDEX (ClientId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created CLIENTS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createReloadFlagsTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE RELOADFLAGS ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "Clients int NOT NULL, "
					+ "Rules int NOT NULL, "
					+ "Actions int NOT NULL, "
					+ "Templates int NOT NULL, "
					+ "Schedules int NOT NULL, "
					+ "PRIMARY KEY (RowId) "
					+ ") ENGINE=MyISAM");
			System.out.println("Created RELOADFLAGS Table...");
			stm.execute("INSERT INTO RELOADFLAGS VALUES(1,0,0,0,0,0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private String insertSql = 
		"INSERT INTO CLIENTS "
			+ "(ClientId, "
			+ "ClientName, "
			+ "ClientType, "
			+ "DomainName, "
			+ "StatusId, " 
			+ "IrsTaxId, "
			+ "WebSiteUrl, "
			+ "SaveRawMsg, "
			+ "ContactEmail,"
			+ "SecurityEmail,"
			+ "custcareEmail,"
			+ "RmaDeptEmail,"
			+ "SpamCntrlEmail,"
			+ "ChaRspHndlrEmail,"
			+ "EmbedEmailId,"
			+ "ReturnPathLeft,"
			+ "UseTestAddr,"
			+ "TestFromAddr, "
			+ "TestToAddr,"
			+ "TestReplytoAddr,"
			+ "IsVerpEnabled,"
			+ "VerpSubDomain,"
			+ "VerpInboxName,"
			+ "VerpRemoveInbox,"
			+ "SystemId,"
			+ "SystemKey,"
			+ "UpdtTime, "
			+ "UpdtUserId) "
			+ " VALUES("
			+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
			+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
			+ " ?, ?, ?, ?, ?, ?, ?, ? "
			+ " )";

	public void loadTestData() throws SQLException {
		try {
			insertSystemDefault(true);
			insertJBatch();
			System.out.println("Inserted all rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadReleaseData() throws SQLException {
		try {
			insertSystemDefault(false);
			System.out.println("Inserted all rows...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void insertSystemDefault(boolean loadTestData) throws SQLException {
		PreparedStatement ps = con.prepareStatement(insertSql);
		ps.setString(1, Constants.DEFAULT_CLIENTID);
		ps.setString(2, "Emailsphere Demo");
		ps.setString(3, null);
		if (loadTestData)
			ps.setString(4, "localhost"); // domain name
		else
			ps.setString(4, "espheredemo.com"); // domain name
		ps.setString(5, StatusIdCode.ACTIVE);
		ps.setString(6, "0000000000");
		ps.setString(7, "http://localhost:8080/MsgUI/publicsite");
		ps.setString(8, Constants.YES_CODE); // save raw stream
		if (loadTestData) {
			ps.setString(9, "sitemaster@emailsphere.com");
			ps.setString(10, "security@localhost");
			ps.setString(11, "custcare@localhost");
			ps.setString(12, "rma.dept@localhost");
			ps.setString(13, "spam.ctrl@localhost");
			ps.setString(14, "challenge@localhost");
		}
		else { // release data
			ps.setString(9, "sitemaster@localhost");
			ps.setString(10, "security@localhost");
			ps.setString(11, "custcare@localhost");
			ps.setString(12, "rma.dept@localhost");
			ps.setString(13, "spam.ctrl@localhost");
			ps.setString(14, "challenge@localhost");
		}
		ps.setString(15, Constants.YES); // Embed EmailId 
		ps.setString(16, "support"); // return-path left
		ps.setString(17, Constants.YES); // use testing address
		ps.setString(18, "testfrom@localhost");
		ps.setString(19, "testto@localhost");
		ps.setString(20, null);
		ps.setString(21, Constants.YES); // is VERP enabled
		ps.setString(22, null); // VERP sub domain
		ps.setString(23, "bounce"); // VERP bounce mailbox
		ps.setString(24, "remove"); // VERP un-subscribe mailbox
		Calendar cal = Calendar.getInstance();
		//cal.add(Calendar.DAY_OF_YEAR, -31);
		String systemId = TimestampUtil.db2ToDecStr(TimestampUtil.getDb2Timestamp(cal.getTime()));
		ps.setString(25, systemId);
		ps.setString(26, ProductUtil.getProductKeyFromFile());
		ps.setTimestamp(27, new Timestamp(new java.util.Date().getTime()));
		ps.setString(28, Constants.DEFAULT_USER_ID);
		ps.execute();
		ps.close();
	}

	void insertJBatch() throws SQLException {
		PreparedStatement ps = con.prepareStatement(insertSql);
		ps.setString(1, "JBatchCorp");
		ps.setString(2, "JBatch Corp. Site");
		ps.setString(3, null);
		ps.setString(4, "jbatch.com"); // domain name
		ps.setString(5, StatusIdCode.ACTIVE);
		ps.setString(6, "0000000000");
		ps.setString(7, "http://www.jbatch.com");
		ps.setString(8, Constants.YES_CODE); // save raw stream
		ps.setString(9, "sitemaster@jbatch.com");
		ps.setString(10, "security@jbatch.com");
		ps.setString(11, "custcare@jbatch.com");
		ps.setString(12, "rma.dept@jbatch.com");
		ps.setString(13, "spam.control@jbatch.com");
		ps.setString(14, "challenge@jbatch.com");
		ps.setString(15, Constants.YES);
		ps.setString(16, "support"); // return-path left
		ps.setString(17, Constants.NO); // use testing address
		ps.setString(18, "testfrom@jbatch.com");
		ps.setString(19, "testto@jbatch.com");
		ps.setString(20, null);
		ps.setString(21, Constants.NO); // is VERP enabled
		ps.setString(22, null); // VERP sub domain
		ps.setString(23, "bounce"); // VERP bounce mailbox
		ps.setString(24, "remove"); // VERP un-subscribe mailbox
		ps.setString(25, "");
		ps.setString(26, null);
		ps.setTimestamp(27, new Timestamp(new java.util.Date().getTime()));
		ps.setString(28, Constants.DEFAULT_USER_ID);
		ps.execute();
		ps.close();
	}

	public int updateClient4Prod() {
		ClientDao dao = (ClientDao) SpringUtil.getDaoAppContext().getBean("clientDao");
		ClientVo vo = dao.getByClientId(Constants.DEFAULT_CLIENTID);
		vo.setClientName("Emailsphere");
		vo.setClientType(null);
		vo.setDomainName("emailsphere.com"); // domain name
		vo.setStatusId(StatusIdCode.ACTIVE);
		vo.setIrsTaxId("0000000000");
		vo.setWebSiteUrl("http://www.emailsphere.com/newsletter");
		vo.setSaveRawMsg(Constants.YES_CODE); // save raw stream
		vo.setContactEmail("sitemaster@emailsphere.com");
		vo.setSecurityEmail("security@emailsphere.com");
		vo.setCustcareEmail("custcare@emailsphere.com");
		vo.setRmaDeptEmail("rma.dept@emailsphere.com");
		vo.setSpamCntrlEmail("spam.ctrl@emailsphere.com");
		vo.setChaRspHndlrEmail("challenge@emailsphere.com");
		vo.setEmbedEmailId(Constants.YES); // Embed EmailId 
		vo.setUseTestAddr(Constants.NO); // use testing address
		vo.setTestFromAddr("testfrom@emailsphere.com");
		vo.setTestToAddr("testto@emailsphere.com");
		//vo.setTestReplytoAddr(null);
		vo.setIsVerpEnabled(Constants.YES); // is VERP enabled
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
		System.out.println("ClientTable: Default client updated.");
		return rowsInserted;
	}
	
	/**
	 * update System client to trigger the loading of Client Variables into
	 * ClientVariable table.
	 */
	public void updateAllClients() {
		ClientDao dao = (ClientDao) SpringUtil.getDaoAppContext().getBean("clientDao");
		int rowsUpdated = 0;
		List<ClientVo> list = dao.getAll();
		for (ClientVo vo : list) {
			vo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
			rowsUpdated += dao.update(vo);
		}
		ClientVo vo = dao.getByClientId(Constants.DEFAULT_CLIENTID);
		if (vo == null) { // just in case
			try {
				insertSystemDefault(true);
				rowsUpdated++;
				System.out.println("Default Client inserted");
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Number of Clients updated: " + rowsUpdated);
	}
	
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			ClientTable ct = new ClientTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
			//ct.updateAllClients();
			ct.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}