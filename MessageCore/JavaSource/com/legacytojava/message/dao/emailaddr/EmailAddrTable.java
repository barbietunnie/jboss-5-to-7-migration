package com.legacytojava.message.dao.emailaddr;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MailingListDeliveryOption;
import com.legacytojava.message.constant.MailingListType;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.main.CreateTableBase;
import com.legacytojava.message.util.BlobUtil;
import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;

public class EmailAddrTable extends CreateTableBase {

	/** Creates a new instance of EmailAddrTables 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public EmailAddrTable() throws ClassNotFoundException, SQLException {
		init();
	}

	public void createTables() throws SQLException {
		createEmailTable();
		createMailingListTable();
		createSubscriptionTable();
		createEmailVariableTable();
		createEmailTemplateTable();
		createFindByAddressSP();
		createUnsubCommentsTable();
	}

	public void dropTables() {
		try {
			stm.execute("DROP TABLE UnsubComments");
			System.out.println("Dropped UnsubComments Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE EmailTemplate");
			System.out.println("Dropped EmailTemplate Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE EmailVariable");
			System.out.println("Dropped EmailVariable Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE SUBSCRIPTION");
			System.out.println("Dropped SUBSCRIPTION Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE MAILINGLIST");
			System.out.println("Dropped MAILINGLIST Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE EMAILADDR");
			System.out.println("Dropped EMAILADDR Table...");
		}
		catch (SQLException e) {
		}
	}
	
	void createEmailTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE EMAILADDR ( "
					+ "EmailAddrId bigint AUTO_INCREMENT NOT NULL PRIMARY KEY, "
					+ "EmailAddr varchar(255) NOT NULL, "
					+ "OrigEmailAddr varchar(255) NOT NULL, "
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " // A - active, S - suspended, I - Inactive
					+ "StatusChangeTime datetime, "
					+ "StatusChangeUserId varchar(10), "
					+ "BounceCount decimal(3) NOT NULL DEFAULT 0, "
					+ "LastBounceTime datetime, "
					+ "LastSentTime datetime, "
					+ "LastRcptTime datetime, "
					+ "AcceptHtml char(1) not null default '" + Constants.YES_CODE + "', "
					+ "UpdtTime datetime NOT NULL, "
					+ "UpdtUserId char(10) NOT NULL, "
					+ "UNIQUE INDEX (EmailAddr) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created EMAILADDR Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMailingListTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MAILINGLIST ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "ListId varchar(8) NOT NULL, "
					+ "DisplayName varchar(50), "
					+ "AcctUserName varchar(100) NOT NULL, " 
						// left part of email address, right part from Clients table's DomainName
					+ "Description varchar(500), "
					+ "ClientId varchar(16) NOT NULL, "
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " 
						// A - active, I - Inactive
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', "
					+ "IsSendText char(1), "
					+ "CreateTime datetime NOT NULL, "
					+ "ListMasterEmailAddr varchar(255), "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (ClientId) REFERENCES CLIENTS (ClientId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (AcctUserName), "
					+ "UNIQUE INDEX (ListId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created MAILINGLIST Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createSubscriptionTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE SUBSCRIPTION ( "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8) NOT NULL, "
					+ "Subscribed char(1) NOT NULL, " 
						// Y - subscribed, N - not subscribed, P - Pending Confirmation
					+ "CreateTime datetime NOT NULL, "
					+ "SentCount int NOT NULL DEFAULT 0, "
					+ "LastSentTime datetime, "
					+ "OpenCount int NOT NULL DEFAULT 0, "
					+ "LastOpenTime datetime, "
					+ "ClickCount int NOT NULL DEFAULT 0, "
					+ "LastClickTime datetime, "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "FOREIGN KEY (ListId) REFERENCES MAILINGLIST (ListId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "UNIQUE INDEX (EmailAddrId,ListId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created SUBSCRIPTION Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createEmailVariableTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE EmailVariable ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "VariableName varchar(26) NOT NULL, "
					+ "VariableType char(1) NOT NULL, " 
						// S - system, C - customer (individual)
					+ "TableName varchar(50), " // document only
					+ "ColumnName varchar(50), " // document only
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " 
						// A - active, I - Inactive
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', "
					+ "DefaultValue varchar(255), "
					+ "VariableQuery varchar(255), " // 1) provides TO emailAddId as query criteria
													// 2) returns a single field called "ResultStr"
					+ "VariableProc varchar(100), " // when Query is null or returns no result
					+ "PRIMARY KEY (RowId), "
					+ "UNIQUE INDEX (VariableName) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created EmailVariable Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createEmailTemplateTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE EmailTemplate ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "TemplateId varchar(26) NOT NULL, "
					+ "ListId varchar(8) NOT NULL, "
					+ "Subject varchar(255), "
					+ "BodyText mediumtext, "
					+ "IsHtml char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', " // Y or N
					+ "ListType varchar(12) NOT NULL, " // Traditional/Personalized
					+ "DeliveryOption varchar(4) NOT NULL DEFAULT '" + MailingListDeliveryOption.ALL_ON_LIST + "', " // when ListType is Personalized
						// ALL - all on list, CUST - only email addresses with customer record
					+ "SelectCriteria varchar(100), " 
						// additional selection criteria - to be implemented
					+ "EmbedEmailId char(1) NOT NULL DEFAULT '', " // Y, N, or <Blank> - use system default
					+ "IsBuiltIn char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', "
					+ "Schedules blob, " // store a java object
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (ListId) REFERENCES MAILINGLIST (ListId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "UNIQUE INDEX (TemplateId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created EmailTemplate Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createUnsubCommentsTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE UnsubComments ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "EmailAddrId bigint NOT NULL, "
					+ "ListId varchar(8), "
					+ "Comments varchar(500) NOT NULL, "
					+ "AddTime datetime NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "INDEX (EmailAddrId) "
					+ ") ENGINE=InnoDB");
			System.out.println("Created UnsubComments Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}


/* MySQL Stored Procedure:
DELIMITER $$

DROP PROCEDURE IF EXISTS `message`.`FindByAddress` $$
CREATE DEFINER=`email`@`%` PROCEDURE `FindByAddress`(
  IN iEmailAddr VARCHAR(255),
  OUT oEmailAddrId LONG,
  OUT oEmailAddr VARCHAR(255),
  OUT oOrigEmailAddr VARCHAR(255),
  OUT oStatusId CHAR(1),
  OUT oStatusChangeTime DATETIME,
  OUT oStatusChangeUserId VARCHAR(10),
  OUT oBounceCount DECIMAL(3,0),
  OUT oLastBounceTime DATETIME,
  OUT oLastSentTime DATETIME,
  OUT oLastRcptTime DATETIME,
  OUT oAcceptHtml CHAR(1),
  OUT oUpdtTime DATETIME,
  OUT oUpdtUserId VARCHAR(10)
 )
 MODIFIES SQL DATA
BEGIN
  declare pEmailAddrId long default 0;
  declare currTime DATETIME;
  declare pEmailAddr varchar(255) default null;
  select EmailAddrId, EmailAddr, OrigEmailAddr, StatusId, StatusChangeTime, StatusChangeUserId,
          BounceCount, LastBounceTime, LastSentTime, LastRcptTime, AcceptHtml,
          UpdtTime, UpdtUserId
    into oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId,
          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml,
          oUpdtTime, oUpdtUserId
    from EmailAddr where EmailAddr=TRIM(iEmailAddr);
  select now() into currTime;
  if oEmailAddr is NULL then
    insert into EmailAddr (EmailAddr, OrigEmailAddr, StatusChangeTime,
                          StatusChangeUserId, UpdtTime, UpdtUserId)
      values (iEmailAddr, iEmailAddr, currTime, 'StoredProc', currTime, 'StoredProc');
    select last_insert_id() into oEmailAddrId;
    select iEmailAddr, iEmailAddr into oEmailAddr, oOrigEmailAddr;
    select 'A' into oStatusId;
    select currTime, 'StoredProc' into oStatusChangeTime, oStatusChangeUserId;
    select 0 into oBounceCount;
    select null, null, null into oLastBounceTime, oLastSentTime, oLastRcptTime;
    select 'Y' into oAcceptHtml;
    select currTime, 'StoredProc' into oUpdtTime, oUpdtUserId;
  end if;
  select oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId,
          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml,
          oUpdtTime, oUpdtUserId;
END $$

DELIMITER ;
*/
	
	void createFindByAddressSP() throws SQLException {
		try {
			stm.execute(
				"DROP PROCEDURE IF EXISTS `message`.`FindByAddress`"
			);
			stm.execute(
				"CREATE PROCEDURE `FindByAddress`(" + LF +
				"  IN iEmailAddr VARCHAR(255)," + LF +
				"  IN iOrigEmailAddr VARCHAR(255)," + LF +
				"  OUT oEmailAddrId LONG," + LF +
				"  OUT oEmailAddr VARCHAR(255)," + LF +
				"  OUT oOrigEmailAddr VARCHAR(255)," + LF +
				"  OUT oStatusId CHAR(1)," + LF +
				"  OUT oStatusChangeTime DATETIME," + LF +
				"  OUT oStatusChangeUserId VARCHAR(10)," + LF +
				"  OUT oBounceCount DECIMAL(3,0)," + LF +
				"  OUT oLastBounceTime DATETIME," + LF +
				"  OUT oLastSentTime DATETIME," + LF +
				"  OUT oLastRcptTime DATETIME," + LF +
				"  OUT oAcceptHtml CHAR(1)," + LF +
				"  OUT oUpdtTime DATETIME," + LF +
				"  OUT oUpdtUserId VARCHAR(10)" + LF +
				" )" + LF +
				" MODIFIES SQL DATA" + LF +
				"BEGIN" + LF +
				"  declare pEmailAddrId long default 0;" + LF +
				"  declare currTime DATETIME;" + LF +
				"  declare pEmailAddr varchar(255) default null;" + LF +
				"  select EmailAddrId, EmailAddr, OrigEmailAddr, StatusId, StatusChangeTime, StatusChangeUserId," + LF +
				"          BounceCount, LastBounceTime, LastSentTime, LastRcptTime, AcceptHtml," + LF +
				"          UpdtTime, UpdtUserId" + LF +
				"    into oEmailAddrId, oEmailAddr, oOrigEmailAddr, oStatusId, oStatusChangeTime, oStatusChangeUserId," + LF +
				"          oBounceCount, oLastBounceTime, oLastSentTime, oLastRcptTime, oAcceptHtml," + LF +
				"          oUpdtTime, oUpdtUserId" + LF +
				"    from EmailAddr where EmailAddr=TRIM(iEmailAddr);" + LF +
				"  select now() into currTime;" + LF +
				"  if oEmailAddr is NULL then" + LF +
				"    insert into EmailAddr (EmailAddr, OrigEmailAddr, StatusChangeTime," + LF +
				"                          StatusChangeUserId, UpdtTime, UpdtUserId)" + LF +
				"      values (iEmailAddr, iOrigEmailAddr, currTime, 'StoredProc', currTime, 'StoredProc');" + LF +
				"    select last_insert_id() into oEmailAddrId;" + LF +
				"    select iEmailAddr, iEmailAddr into oEmailAddr, oOrigEmailAddr;" + LF +
				"    select 'A' into oStatusId;" + LF +
				"    select currTime, 'StoredProc' into oStatusChangeTime, oStatusChangeUserId;" + LF +
				"    select 0 into oBounceCount;" + LF +
				"    select null, null, null into oLastBounceTime, oLastSentTime, oLastRcptTime;" + LF +
				"    select 'Y' into oAcceptHtml;" + LF +
				"    select currTime, 'StoredProc' into oUpdtTime, oUpdtUserId;" + LF +
				"  end if;" + LF +
				"END "
			);
			System.out.println("Created FindByAddress Stored Procedure...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void loadTestData() throws SQLException {
		insertEmailAddrs();
		insertMaillingList();
		insertProdMaillingList();
		insertEmailVariable();
		insertEmailTemplate();
		insertProdEmailTemplate();
		insertSubscribers();
	}

	public void loadReleaseData() throws SQLException {
		insertEmailAddrs();
		insertMaillingList();
		insertEmailVariable();
		insertEmailTemplate();
		insertSubscribers();
	}
	
	private void insertEmailAddrs() throws SQLException
	{
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO EMAILADDR " +
				"(EmailAddr," +
				"OrigEmailAddr," +
				"StatusId," +
				"StatusChangeTime," +
				"StatusChangeUserId," +
				"BounceCount," +
				"LastBounceTime," +
				"LastSentTime," +
				"LastRcptTime," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, "jsmith@test.com");
			ps.setString(2, "jsmith@test.com");
			ps.setString(3, "A");
			ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			ps.setString(5, "testuser 1");
			ps.setInt(6, 0);
			ps.setTimestamp(7, null);
			ps.setTimestamp(8, null);
			ps.setTimestamp(9, null);
			ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
			ps.setString(11, Constants.DEFAULT_USER_ID);
			ps.execute();
			
			ps.setString(1, "test@test.com");
			ps.setString(2, "test@test.com");
			ps.setString(3, "A");
			ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			ps.setString(5, "testuser 2");
			ps.setInt(6, 0);
			ps.setTimestamp(7, null);
			ps.setTimestamp(8, null);
			ps.setTimestamp(9, null);
			ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
			ps.setString(11, Constants.DEFAULT_USER_ID);
			ps.execute();
			
			ps.setString(1, "testuser@test.com");
			ps.setString(2, "testuser@test.com");
			ps.setString(3, "A");
			ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			ps.setString(5, "testuser 3");
			ps.setInt(6, 0);
			ps.setTimestamp(7, null);
			ps.setTimestamp(8, null);
			ps.setTimestamp(9, null);
			ps.setTimestamp(10, new Timestamp(new java.util.Date().getTime()));
			ps.setString(11, Constants.DEFAULT_USER_ID);
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private void insertMaillingList() throws SQLException {
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO MAILINGLIST " +
				"(ListId," +
				"DisplayName," +
				"AcctUserName," +
				"Description," +
				"ClientId," +
				"StatusId," +
				"IsBuiltIn," +
				"CreateTime)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, "SMPLLST1");
			ps.setString(2, "Sample List 1");
			ps.setString(3, "demolist1");
			ps.setString(4, "Sample mailing list 1");
			ps.setString(5, Constants.DEFAULT_CLIENTID);
			ps.setString(6, StatusIdCode.ACTIVE);
			ps.setString(7, Constants.NO_CODE);
			ps.setTimestamp(8, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.setString(1, "SMPLLST2");
			ps.setString(2, "Sample List 2");
			ps.setString(3, "demolist2");
			ps.setString(4, "Sample mailing list 2");
			ps.setString(5, Constants.DEFAULT_CLIENTID);
			ps.setString(6, StatusIdCode.ACTIVE);
			ps.setString(7, Constants.NO_CODE);
			ps.setTimestamp(8, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.setString(1, "SYSLIST1");
			ps.setString(2, "NOREPLY Empty List");
			ps.setString(3, "noreply");
			ps.setString(4, "Auto-Responder, used by Subscription and confirmation Templates");
			ps.setString(5, Constants.DEFAULT_CLIENTID);
			ps.setString(6, StatusIdCode.INACTIVE);
			ps.setString(7, Constants.YES_CODE);
			ps.setTimestamp(8, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	private void insertProdMaillingList() throws SQLException {
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO MAILINGLIST " +
				"(ListId," +
				"DisplayName," +
				"AcctUserName," +
				"Description," +
				"ClientId," +
				"StatusId," +
				"IsBuiltIn," +
				"CreateTime)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, "ORDERLST");
			ps.setString(2, "Sales ORDER List");
			ps.setString(3, "support");
			ps.setString(4, "Auto-Responder, used by order processing");
			ps.setString(5, Constants.DEFAULT_CLIENTID);
			ps.setString(6, StatusIdCode.INACTIVE);
			ps.setString(7, Constants.YES_CODE);
			ps.setTimestamp(8, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	private void insertSubscribers() throws SQLException {
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO SUBSCRIPTION " +
				"(EmailAddrId," +
				"ListId," +
				"Subscribed," +
				"CreateTime)" +
				"VALUES (?, ?, ?, ?)");
			
			ps.setLong(1, 1);
			ps.setString(2, "SMPLLST1");
			ps.setString(3, Constants.YES_CODE);
			ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.setLong(1, 2);
			ps.setString(2, "SMPLLST1");
			ps.setString(3, Constants.YES_CODE);
			ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.setLong(1, 3);
			ps.setString(2, "SMPLLST1");
			ps.setString(3, Constants.YES_CODE);
			ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.setLong(1, 1);
			ps.setString(2, "SMPLLST2");
			ps.setString(3, Constants.YES_CODE);
			ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.setLong(1, 2);
			ps.setString(2, "SMPLLST2");
			ps.setString(3, Constants.YES_CODE);
			ps.setTimestamp(4, new Timestamp(new java.util.Date().getTime()));
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	private void insertEmailVariable() throws SQLException {
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO EmailVariable " +
				"(VariableName," +
				"VariableType," +
				"TableName," +
				"ColumnName," +
				"StatusId," +
				"IsBuiltIn," +
				"DefaultValue," +
				"VariableQuery," +
				"VariableProc)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, "CustomerName");
			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
			ps.setString(3, "customers");
			ps.setString(4, "FirstName,LastName");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "Valued Customer");
			ps.setString(8, "SELECT CONCAT(c.FirstName, ' ', c.LastName) as ResultStr " +
					"FROM customers c, emailaddr e " +
					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
			ps.setString(9, "com.legacytojava.message.external.CustomerNameResolver");
			ps.execute();
			
			ps.setString(1, "CustomerFirstName");
			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
			ps.setString(3, "customers");
			ps.setString(4, "FirstName");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "Valued Customer");
			ps.setString(8, "SELECT c.FirstName as ResultStr " +
					"FROM customers c, emailaddr e " +
					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
			ps.setString(9, "com.legacytojava.message.external.CustomerNameResolver");
			ps.execute();
			
			ps.setString(1, "CustomerLastName");
			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
			ps.setString(3, "customers");
			ps.setString(4, "LastName");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "Valued Customer");
			ps.setString(8, "SELECT c.LastName as ResultStr " +
					"FROM customers c, emailaddr e " +
					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
			ps.setString(9, "com.legacytojava.message.external.CustomerNameResolver");
			ps.execute();
			
//			ps.setString(1, "CustomerMiddleName");
//			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
//			ps.setString(3, "customers");
//			ps.setString(4, "MiddleName");
//			ps.setString(5, Constants.ACTIVE);
//			ps.setString(6, Constants.NO_CODE);
//			ps.setString(7, "Valued Customer");
//			ps.setString(8, "SELECT c.MiddleName as ResultStr " +
//					"FROM customers c, emailaddr e " +
//					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
//			ps.setString(9, "com.legacytojava.message.external.CustomerNameResolver");
//			ps.execute();
			
			ps.setString(1, "CustomerAddress");
			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
			ps.setString(3, "customers");
			ps.setString(4, "StreetAddress");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "");
			ps.setString(8, "SELECT CONCAT_WS(',',c.StreetAddress2,c.StreetAddress) as ResultStr " +
					"FROM customers c, emailaddr e " +
					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "CustomerCityName");
			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
			ps.setString(3, "customers");
			ps.setString(4, "CityName");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "");
			ps.setString(8, "SELECT c.CityName as ResultStr " +
					"FROM customers c, emailaddr e " +
					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "CustomerStateCode");
			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
			ps.setString(3, "customers");
			ps.setString(4, "StateCode");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "");
			ps.setString(8, "SELECT CONTAC_WS(',',c.StateCode,c.ProvinceName) as ResultStr " +
					"FROM customers c, emailaddr e " +
					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "CustomerZipCode");
			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
			ps.setString(3, "customers");
			ps.setString(4, "ZipCode");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "");
			ps.setString(8, "SELECT CONCAT_WS('-',c.ZipCode5,ZipCode4) as ResultStr " +
					"FROM customers c, emailaddr e " +
					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
			ps.setString(9, null);
			ps.execute();
			
//			ps.setString(1, "CustomerPostalCode");
//			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
//			ps.setString(3, "customers");
//			ps.setString(4, "PostalCode");
//			ps.setString(5, Constants.ACTIVE);
//			ps.setString(6, Constants.NO_CODE);
//			ps.setString(7, "");
//			ps.setString(8, "SELECT c.PostalCode as ResultStr " +
//					"FROM customers c, emailaddr e " +
//					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
//			ps.setString(9, null);
//			ps.execute();
			
			ps.setString(1, "CustomerCountry");
			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
			ps.setString(3, "customers");
			ps.setString(4, "Country");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.NO_CODE);
			ps.setString(7, "");
			ps.setString(8, "SELECT c.Country as ResultStr " +
					"FROM customers c, emailaddr e " +
					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
			ps.setString(9, null);
			ps.execute();
			
//			ps.setString(1, "CustomerFullAddress");
//			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
//			ps.setString(3, "customers");
//			ps.setString(4, "Full Address");
//			ps.setString(5, Constants.ACTIVE);
//			ps.setString(6, Constants.NO_CODE);
//			ps.setString(7, "");
//			ps.setString(8, "SELECT CONCAT_WS(',',c.StreetAddress,c.StreetAddress2,c.CityName," +
//					"CONCAT(c.StateCode,c.PostalCode), c.Country) as ResultStr " +
//					"FROM customers c, emailaddr e " +
//					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
//			ps.setString(9, null);
//			ps.execute();
			
//			ps.setString(1, "CustomerProfession");
//			ps.setString(2, EmailVariableDao.CUSTOMER_VARIABLE);
//			ps.setString(3, "customers");
//			ps.setString(4, "Profession");
//			ps.setString(5, Constants.ACTIVE);
//			ps.setString(6, Constants.NO_CODE);
//			ps.setString(7, "");
//			ps.setString(8, "SELECT c.Profession as ResultStr " +
//					"FROM customers c, emailaddr e " +
//					"where e.emailaddrId=c.emailAddrId and e.emailAddrId=?;");
//			ps.setString(9, null);
//			ps.execute();
			
			ps.setString(1, "EmailOpenCountImgTag");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "<img src='${WebSiteUrl}/msgopen.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "EmailClickCountImgTag");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "<img src='${WebSiteUrl}/msgclick.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "EmailUnsubscribeImgTag");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "<img src=='${WebSiteUrl}/msgunsub.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "EmailTrackingTokens");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "FooterWithUnsubLink");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, LF + "<p>To unsubscribe from this mailing list, " + LF +
					"<a target='_blank' href='${WebSiteUrl}/MsgUnsubPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}'>click here</a>.</p>"
					+ LF);
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "FooterWithUnsubAddr");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, LF + "To unsubscribe from this mailing list, send an e-mail to: ${MailingListAddress}" + LF +
					"with \"unsubscribe\" (no quotation marks) in the subject." + LF);
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "SubscribeURL");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "${WebSiteUrl}/subscribe.jsp?sbsrid=${SubscriberAddressId}");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "ConfirmationURL");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "${WebSiteUrl}/confirmsub.jsp?sbsrid=${_EncodedSubcriberId}&listids=${_SubscribedListIds}&sbsraddr=${SubscriberAddress}");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "UnsubscribeURL");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "${WebSiteUrl}/unsubscribe.jsp?sbsrid=${_EncodedSubcriberId}&listids=${_SubscribedListIds}&sbsraddr=${SubscriberAddress}");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "UserProfileURL");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "${WebSiteUrl}/userprofile.jsp?sbsrid=${SubscriberAddressId}");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "TellAFriendURL");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "${WebSiteUrl}/referral.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.setString(1, "SiteLogoURL");
			ps.setString(2, EmailVariableDao.SYSTEM_VARIABLE);
			ps.setString(3, "");
			ps.setString(4, "");
			ps.setString(5, StatusIdCode.ACTIVE);
			ps.setString(6, Constants.YES_CODE);
			ps.setString(7, "${WebSiteUrl}/images/logo.gif");
			ps.setString(8, null);
			ps.setString(9, null);
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	private void insertEmailTemplate() throws SQLException {
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO EmailTemplate " +
				"(TemplateId," +
				"ListId," +
				"Subject," +
				"BodyText," +
				"IsHtml," +
				"ListType," +
				"DeliveryOption," +
				"IsBuiltIn," +
				"EmbedEmailId," +
				"Schedules)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, "SampleNewsletter1");
			ps.setString(2, "SMPLLST1");
			ps.setString(3, "Sample newsletter to ${SubscriberAddress} with Open/Click/Unsubscribe tracking");
			ps.setString(4,
					"Dear ${CustomerName},<p/>" + LF +
					"This is a sample newsletter message for a web-based mailing list. With a web-based " + LF +
					"mailing list, people who want to subscribe to the list must visit a web page and " + LF +
					"fill out a form with their email address. After submitting the form, they will " + LF +
					"receive a confirmation letter in their email and must activate the subscription " + LF +
					"by following the steps in the email (usually a simple click).<p/>" + LF +
					"Unsubscription information will be included in the newsletters they receive. People " + LF +
					"who want to unsubscribe can do so by simply following the steps in the newsletter.<p/>" + LF +
					"Date sent: ${CurrentDate} <p/>" + LF +
					"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}, SubscriberAddressId: ${SubscriberAddressId}<p/>" + LF +
					"Contact Email: ${ContactEmailAddress}<p>" + LF +
					"<a target='_blank' href='$%7BWebSiteUrl%7D/SamplePromoPage.jsp?msgid=$%7BBroadcastMsgId%7D&listid=$%7BMailingListId%7D&sbsrid=$%7BSubscriberAddressId%7D'>Click here</a> to see our promotions<p/>" + LF +
					"${FooterWithUnsubLink}<br/>" +
					"${EmailOpenCountImgTag}"
					);
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.PERSONALIZED);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.NO_CODE);
			ps.setString(9, " "); // use system default
			SchedulesBlob blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "SampleNewsletter2");
			ps.setString(2, "SMPLLST2");
			ps.setString(3, "Sample HTML newsletter to ${SubscriberAddress}");
			ps.setString(4, "Dear ${SubscriberAddress},<p/>" + LF +
				"This is a sample HTML newsletter message for a traditional mailing list. " + LF +
				"With a traditional mailing list, people who want to subscribe to the list " + LF +
				"must send an email from their account to the mailing list address with " + LF +
				"\"subscribe\" in the email subject.<p/>" + LF +
				"Unsubscribing from a traditional mailing list is just as easy; simply send " + LF +
				"an email to the mailing list address with \"unsubscribe\" in subject.<p/>" + LF + 
				"The mailing list address for this newsletter is: ${MailingListAddress}.<p/>" + LF +
				"Date this newsletter is sent: ${CurrentDate}.<p/>" + LF +
				"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}<p/>" + LF +
				"Contact Email: ${ContactEmailAddress}<p/>" + LF +
				"<a target='_blank' href='$%7BWebSiteUrl%7D/SamplePromoPage.jsp?msgid=$%7BBroadcastMsgId%7D&listid=$%7BMailingListId%7D&sbsrid=$%7BSubscriberAddressId%7D'>Click here</a> to see our promotions<p/>" + LF +
				"${FooterWithUnsubAddr}<br/>" +
				"${EmailOpenCountImgTag}");
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.TRADITIONAL);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.NO_CODE);
			ps.setString(9, " "); // use system default
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "SampleNewsletter3");
			ps.setString(2, "SMPLLST2");
			ps.setString(3, "Sample Plain text newsletter to ${SubscriberAddress}");
			ps.setString(4, "Dear ${SubscriberAddress}," + LF + LF + 
				"This is a sample text newsletter message for a traditional mailing list." + LF +
				"With a traditional mailing list, people who want to subscribe to the list " + LF +
				"must send an email from their account to the mailing list address with " + LF +
				"\"subscribe\" in the email subject." + LF + LF + 
				"Unsubscribing from a traditional mailing list is just as easy; simply send " + LF +
				"an email to the mailing list address with \"unsubscribe\" in subject." + LF + LF +
				"Date sent: ${CurrentDate}" + LF + LF +
				"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}" + LF + LF +
				"Contact Email: ${ContactEmailAddress}" + LF + LF +
				"To see our promotions, copy and paste the following link in your browser:" + LF +
				"${WebSiteUrl}/SamplePromoPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}" + LF +
				"${FooterWithUnsubAddr}");
			ps.setString(5, Constants.NO_CODE);
			ps.setString(6, MailingListType.TRADITIONAL);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.NO_CODE);
			ps.setString(9, " "); // use system default
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
//			ps.setString(1, "SampleNewsletter1");
//			ps.setString(2, "SMPLLST1");
//			ps.setString(3, "Sample template with Open/Click tracking to ${CustomerName}");
//			ps.setString(4,
//					"Dear ${CustomerName}," + LF +
//					"<script type=\"text/javascript\">" + LF +
//					"var http_request = false;" + LF +
//					"function makeRequest(url, async) {" + LF +
//					"  if (window.XMLHttpRequest) { // Mozilla, Safari, IE7..." + LF +
//					"      http_request = new XMLHttpRequest();" + LF +
//					"  } else if (window.ActiveXObject) { // IE6 and older" + LF +
//					"      http_request = new ActiveXObject(\"Microsoft.XMLHTTP\");" + LF +
//					"  }" + LF +
//					"  http_request.onreadystatechange = alertContents;" + LF +
//					"  http_request.open('GET', url, async);" + LF +
//					"  http_request.send(null);" + LF +
//					"}" + LF +
//					"function alertContents() {" + LF +
//					"  if (http_request.readyState == 4) {" + LF +
//					"      if (http_request.status == 200) {" + LF +
//					"          alert(http_request.responseText);" + LF +
//					"      } else {" + LF +
//					"          alert('There was a problem with the request.');" + LF +
//					"      }" + LF +
//					"  }" + LF +
//					"}" + LF +
//					"function updateClickCount() {" + LF +
//					"  makeRequest( 'http://localhost:10080/es/wsmclick.php?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}', false);" + LF +
//					"}" + LF +
//					"</script>" + LF +
//					"<p>This is test template message body to ${SubscriberAddress}. <br></p>" + LF +
//					"<p>Time sent: ${CurrentDate} <br></p>" + LF +
//					"<p>BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}, SubscriberAddressId: ${SubscriberAddressId}</p>" + LF +
//					"<p>Contact Email: ${ContactEmailAddress} <br></p>" + LF +
//					"<p><input value=\"Update Click Count\" onclick=\"updateClickCount()\" type=\"button\">" + LF +
//					"<br></p>" + LF +
//					"<p>To unsubscribe from this mailing list, send an e-mail to: ${MailingListAddress}" + LF +
//					"with \"unsubscribe\" (no quotation marks) in the subject.</p>" + LF +
//					"<img src='http://localhost:10080/es/wsmopen.php?msgid=$%7BBroadcastMsgId%7D&amp;listid=$%7BMailingListId%7D&amp;sbsrid=$%7BSubscriberAddressId%7D' alt='' height='1' width='1'>" + LF +
//					"<a href='http://localhost:10080/es/testclickcount.php?msgid=$%7BBroadcastMsgId%7D&amp;listid=$%7BMailingListId%7D&amp;sbsrid=$%7BSubscriberAddressId%7D' target='_blank'>" + LF +
//					"Click Here to Update Click Count</a>"
//					);
//			ps.setString(5, Constants.YES_CODE);
//			ps.setString(6, Constants.TRADITIONAL);
//			ps.setString(7, Constants.ALL_ON_LIST);
//			ps.setString(8, Constants.NO_CODE);
//			blob = new SchedulesBlob();
//			try {
//				byte[] baosarray = BlobUtil.objectToBytes(blob);
//				ps.setBytes(10, baosarray);
//			}
//			catch (IOException e) {
//				throw new SQLException("IOException caught - " + e.toString());
//			}
//			ps.execute();
			
			ps.setString(1, "SubscriptionConfirmation");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "Request for subscription confirmation");
			ps.setString(4, 
					"Dear ${SubscriberAddress},<br/>" + LF +
					"This is an automatically generated message to confirm that you have " + LF +
					"submitted request to add your email address to the following mailing lists:<br/>" + LF +
					"<pre>${_RequestedMailingLists}</pre>" + LF +
					"If this is correct, please <a href='$%7BConfirmationURL%7D' target='_blank'>click here</a> " + LF +
					"to confirm your subscription.<br/>" + LF +
					"If this is incorrect, you do not need to do anything, simply delete this message.<p/>" + LF +
					"Thank you" + LF
					);
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.TRADITIONAL);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.NO_CODE);
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "SubscriptionWelcomeLetter");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "Your subscription has been confirmed");
			ps.setString(4, 
					"Dear ${SubscriberAddress},<br/>" + LF +
					"Welcome to our mailing lists. Your email address has been added to the" + LF +
					"following mailing lists:<br/>" + LF +
					"<pre>${_SubscribedMailingLists}</pre>" + LF +
					"Please keep this email for latter reference.<p/>" + LF +
					"To unsubscribe please <a href='$%7BUnsubscribeURL%7D' target='_blank'>click here</a> " + LF +
					"and follow the steps.<br/>" + LF + LF +
					"To update your profile please <a href='$%7BUserProfileURL%7D' target='_blank'>click here</a>.<p/>" + LF +
					"Thank you<br/>" + LF
					);
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.TRADITIONAL);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.NO_CODE);
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "UnsubscriptionLetter");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "You have unsubscribed from our Newsletter");
			ps.setString(4, 
					"Dear ${SubscriberAddress},<br/>" + LF +
					"Goodbye from our Newsletter, sorry to see you go.<br/>" + LF +
					"You have been unsubscribed from the following newsletters:<br/>" + LF +
					"<pre>${_UnsubscribedMailingLists}</pre>" + LF +
					"If this is an error, you can re-subscribe. Please " +
					"<a href='$%7BSubscribeURL%7D' target='_blank'>click here</a>" + LF +
					" and follow the steps.<p/>" + LF +
					"Thank you<br/>" + LF
					);
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.TRADITIONAL);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.NO_CODE);
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "UserProfileChangeLetter");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "[notify] Changes of user profile details");
			ps.setString(4, 
					"Dear ${CustomerName},<br/>" + LF +
					"This message is to inform you of a change of your user profile details" + LF +
					"on our newsletter database. You are currently subscribed to our following" + LF +
					"newsletters:<br/>" + LF +
					"<pre>${_SubscribedMailingLists}</pre>" + LF +
					"The information on our system for you is as follows:<br/>" + LF +
					"<pre>${_UserProfileData}</pre>" + LF +
					"If this is not correct, please update your information by " + LF +
					"<a href='$%7BUserProfileURL%7D' target='_blank'>visiting this web page</a>.<p/>" + LF +
					"Thank you<br/>" + LF
					);
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.PERSONALIZED);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, " "); // use system default
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "EmailAddressChangeLetter");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "[notify] Change of your email address");
			ps.setString(4, 
					"Dear ${CustomerName},<br/>" + LF +
					"When updating your user profile details, your email address has changed.<br/>" + LF +
					"Please confirm your new email address by " +
					"<a href='$%7BConfirmationURL%7D' target='_blank'>visiting this web page</a>.<br/>" + LF +
					"If this is not correct, " + LF +
					"<a href='$%7BUserProfileURL%7D' target='_blank'>click here</a> to update your information.<p/>" + LF +
					"Thank you<br/>" + LF
					);
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.PERSONALIZED);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, " "); // use system default
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
//			ps.setString(1, "EmailAddressChangeLetter2");
//			ps.setString(2, "SYSLIST1");
//			ps.setString(3, "[notify] Change of your email address");
//			ps.setString(4, 
//					"Dear ${CustomerName},<br/>" + LF +
//					"Please Note: when updating your profile details, your email address has changed.<br/>" + LF +
//					"A message has been sent to your new email address with a URL to confirm" + LF + 
//					"this change. Please visit this web site to activate your new email address.<p/>" + LF +
//					"Thank you<br/>" + LF
//					);
//			ps.setString(5, Constants.YES_CODE);
//			ps.setString(6, Constants.PERSONALIZED);
//			ps.setString(7, Constants.ALL_ON_LIST);
//			ps.setString(8, Constants.YES_CODE);
//			ps.setString(9, " "); // use system default
//			blob = new SchedulesBlob();
//			try {
//				byte[] baosarray = BlobUtil.objectToBytes(blob);
//				ps.setBytes(10, baosarray);
//			}
//			catch (IOException e) {
//				throw new SQLException("IOException caught - " + e.toString());
//			}
//			ps.execute();
			
			ps.setString(1, "TellAFriendLetter");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "A web site recommendation from ${_ReferrerName}");
			ps.setString(4, 
					"Dear ${_FriendsEmailAddress},<p/>" + LF +
					"${_ReferrerName}, whose email address is ${_ReferrerEmailAddress} thought you " + LF +
					"may be interested in this web page.<p/>" + LF +
					"<a target='_blank' href='$%7BWebSiteUrl%7D'>${WebSiteUrl}</a><p/>" + LF +
					"${_ReferrerName} has used our Tell-a-Friend form to send you this note.<p/>" + LF +
					"${_ReferrerComments}" +
					"We look forward to your visit!<br/>" + LF
					);
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.PERSONALIZED);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, Constants.NO_CODE); // do not embed email id
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "SubscribeByEmailReply");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "You have subscribed to mailing list ${MailingListName}");
			ps.setString(4, 
				"Dear ${SubscriberAddress}," + LF + LF +
				"This is an automatically generated message to confirm that you have" + LF +
				"subscribed to our mailing list: ${MailingListName}" + LF + LF +
				"To ensure that you continue to receive e-mails from ${DomainName} in your " + LF +
				"inbox, you can add the sender of this e-mail to your address book or white list." + LF + LF +
				"If this in incorrect, you can un-subscribe from this mailing list." + LF +
				"Simply send an e-mail to: ${MailingListAddress}" + LF +
				"with \"unsubscribe\" (no quotation marks) in your email subject." + LF);
			ps.setString(5, Constants.NO_CODE);
			ps.setString(6, MailingListType.TRADITIONAL);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, " "); // use system default
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "SubscribeByEmailReplyHtml");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "You have subscribed to ${MailingListName} at ${DomainName}");
			ps.setString(4, 
				"Dear ${SubscriberAddress},<br>" + LF +
				"This is an automatically generated message to confirm that you have " + LF +
				"subscribed to our mailing list: <b>${MailingListName}</b>.<br>" + LF +
				"To ensure that you continue to receive e-mails from ${DomainName} in your " + LF +
				"inbox, you can add the sender of this e-mail to your address book or white list.<br>" + LF +
				"If you signed up for this subscription in error, you can un-subscribe." + LF +
				"Simply send an e-mail to <a href='mailto:$%7BMailingListAddress%7D' target='_blank'>${MailingListAddress}</a>" + LF +
				"with \"unsubscribe\" (no quotation marks) in your email subject.<br>" + LF);
			ps.setString(5, Constants.YES_CODE);
			ps.setString(6, MailingListType.TRADITIONAL);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.YES_CODE);
			ps.setString(9, " "); // use system default
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.close();
			System.out.println("Inserted EmailTemplate...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}

	void selectEmailTemplate() throws SQLException {
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"select * from EmailTemplate where TemplateId = 'test template'");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String id = rs.getString("ListId");
				byte[] bytes = rs.getBytes("Schedules");
				try {
					SchedulesBlob blob = (SchedulesBlob) BlobUtil.bytesToObject(bytes);
					System.out.println("ListId: " + id + ", blob: " + blob);
				}
				catch (Exception e) {
					throw new SQLException("Exception caught - " + e.toString());
				}
			}
			ps.close();
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}
	
	private void insertProdEmailTemplate() throws SQLException {
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO EmailTemplate " +
				"(TemplateId," +
				"ListId," +
				"Subject," +
				"BodyText," +
				"IsHtml," +
				"ListType," +
				"DeliveryOption," +
				"IsBuiltIn," +
				"EmbedEmailId," +
				"Schedules)" +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			
			ps.setString(1, "EmailsphereOrderReceipt");
			ps.setString(2, "ORDERLST");
			ps.setString(3, "Emailsphere Purchase Receipt");
			ps.setString(4,
					"Dear ${_BillingFirstName}," + LF + LF +
					"Thank you for your recent purchase from Emailsphere, your purchase, as described below, has been completed." + LF + LF +
					"Order number: ${_OrderNumber}" + LF +
					"Order Date: ${_OrderDate}" + LF + LF +
					"Billing Information:" + LF +
					"${_BillingName}" + LF +
					"${_BillingStreetAddress}" + LF +
					"${_BillingCityStateZip}" + LF + LF +
					"Item purchased: Emailsphere enterprise server." + LF + 
					"Price: ${_UnitPrice}" + LF +
					"Tax:   ${_Tax}" + LF +
					"Total Price: ${_TotalPrice}" + LF + LF +
					"Billed to ${_CardTypeName} ending in ${_CardNumberLast4}: ${_TotalPrice}" + LF + LF +
					"Please contact ${MailingListAddress} with any questions or concerns regarding this transaction." + LF + LF +
					"Your product key is: ${_ProductKey}" + LF +
					"Please login to your Emailsphere system management console, click \"Enter Product Key\", and copy this key to the input field and submit." + LF + LF +
					"If you have any technical questions, please visit our contact us page by point your browser to:" + LF +
					"${_ContactUsUrl}" + LF + LF +
					"Thank you for your purchase!" + LF + LF +
					"Emailsphere Team" + LF +
					"Legacy System Solutions, LLC" + LF
					);
			ps.setString(5, Constants.NO_CODE);
			ps.setString(6, MailingListType.PERSONALIZED);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.NO_CODE);
			ps.setString(9, " "); // use system default
			SchedulesBlob blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();
			
			ps.setString(1, "EmailsphereOrderException");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "Important Notice: Your Emailsphere Order # ${_OrderNumber}");
			ps.setString(4,
					"Regarding Order ${_OrderNumber} you placed on ${_OrderDate} from Emailsphere.com" + LF +
					"1 Emailsphere Enterprise Server" + LF + LF +
					"Greetings from Emailsphere.com," + LF + LF +
					"Your credit card payment for the above transaction could not be completed." + LF +
					"An issuing bank will often decline an attempt to charge a credit card if" + LF +
					"the name, expiration date, or ZIP Code you entered at Emailsphere.com does" + LF +
					"not exactly match the bank's information." + LF + LF +
					"Valid payment information must be received within 3 days, otherwise your" + LF + 
					"order will be canceled." + LF + LF +
					"Once you have confirmed your account information with your issuing bank," + LF +
					"please follow the link below to resubmit your payment." + LF + LF +
					"http://www.emailsphere.com/es/edit.html/?orderID=${_OrderNumber}" + LF + LF +
					"We hope that you are able to resolve this issue promptly." + LF + LF +
					"Please note: This e-mail was sent from a notification-only address that" + LF +
					"cannot accept incoming e-mail. Please do not reply to this message." + LF + LF +
					"Thank you for shopping at Emailsphere.com." + LF + LF +
					"Emailsphere.com Customer Service" + LF +
					"http://www.emailsphere.com" + LF
					);
			ps.setString(5, Constants.NO_CODE);
			ps.setString(6, MailingListType.PERSONALIZED);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.NO_CODE);
			ps.setString(9, " "); // use system default
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();

			ps.setString(1, "EmailsphereInternalAlert");
			ps.setString(2, "SYSLIST1");
			ps.setString(3, "Notify: Alert from Emailsphere.com");
			ps.setString(4,
					"Internal error or exception caught from Emailsphere.com" + LF + LF +
					"Time: ${_DateTime}" + LF +
					"Module: ${_ModuleName}" + LF +
					"Error: ${_Error}" + LF
					);
			ps.setString(5, Constants.NO_CODE);
			ps.setString(6, MailingListType.TRADITIONAL);
			ps.setString(7, MailingListDeliveryOption.ALL_ON_LIST);
			ps.setString(8, Constants.NO_CODE);
			ps.setString(9, " "); // use system default
			blob = new SchedulesBlob();
			try {
				byte[] baosarray = BlobUtil.objectToBytes(blob);
				ps.setBytes(10, baosarray);
			}
			catch (IOException e) {
				throw new SQLException("IOException caught - " + e.toString());
			}
			ps.execute();

			ps.close();
			System.out.println("Inserted Product EmailTemplate...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}	
	}
	
	/**
	 * to trigger the insert of template id's to MsgDataType table. 
	 */
	public void updateTemplates() {
		int rowsUpdated = 0;
		List<EmailTemplateVo> list = getEmailTemplateDao().getAll();
		for (EmailTemplateVo tmpltVo : list) {
			tmpltVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
			rowsUpdated += getEmailTemplateDao().update(tmpltVo);
		}
		System.out.println("Updated EmailTemplate records: " + rowsUpdated);
	}

	private EmailTemplateDao emailTemplateDao = null;
	private EmailTemplateDao getEmailTemplateDao() {
		if (emailTemplateDao == null) {
			emailTemplateDao = (EmailTemplateDao) SpringUtil.getDaoAppContext().getBean("emailTemplateDao");
		}
		return emailTemplateDao;
	}
	
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			EmailAddrTable ct = new EmailAddrTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
//			ct.createMailingListTable();
//			ct.createSubscriptionTable();
//			ct.createEmailTemplateTable();
//			ct.insertMaillingList();
//			ct.insertEmailTemplate();
//			ct.selectEmailTemplate();
//			ct.createFindByAddressSP();
//			ct.insertSubscribers();
//			ct.insertProdEmailTemplate();
//			ct.insertProdMaillingList();
			ct.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}