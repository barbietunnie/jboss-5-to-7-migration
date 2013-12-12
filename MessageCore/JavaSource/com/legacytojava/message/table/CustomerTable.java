package com.legacytojava.message.table;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MobileCarrier;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.main.CreateTableBase;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

public class CustomerTable extends CreateTableBase {
	/**
	 * Creates a new instance of CustomerTables
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public CustomerTable() throws ClassNotFoundException, SQLException {
		init();
	}
	
	public void dropTables() {
		try {
			stm.execute("DROP TABLE CUSTOMERS");
			System.out.println("Dropped CUSTOMERS Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE CUSTSEQUENCE");
			System.out.println("Dropped CUSTSEQUENCE Table...");
		}
		catch (SQLException e) {
		}
	}
	
	public void createTables() throws SQLException {
		createCustSequenceTable();
		createCustomerTable();
	}

	void createCustomerTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE CUSTOMERS ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "CustId varchar(16) NOT NULL, "	//1
					+ "ClientId varchar(16) NOT NULL, "
					+ "SsnNumber varchar(11), "
					+ "TaxId varchar(10), "
					+ "Profession varchar(40), "	//5
					+ "FirstName varchar(32), "
					+ "MiddleName varchar(32), "
					+ "LastName varchar(32) NOT NULL, "
					+ "Alias varchar(50), " // also known as Company Name
					+ "StreetAddress varchar(60), "	//10
					+ "StreetAddress2 varchar(40), "
					+ "CityName varchar(32), "
					+ "StateCode char(2), "
					+ "ZipCode5 char(5), "
					+ "ZipCode4 varchar(4), "	//15
					+ "ProvinceName varchar(30), "
					+ "PostalCode varchar(11), "
					+ "Country varchar(5), "
					+ "DayPhone varchar(18), "
					+ "EveningPhone varchar(18), "	//20
					+ "MobilePhone varchar(18), "
					+ "BirthDate Date, "
					+ "StartDate Date NOT NULL, "
					+ "EndDate Date, "
					+ "MobileCarrier varchar(26), " // 25
					+ "MsgHeader varchar(100), "
					+ "MsgDetail varchar(255), "
					+ "MsgOptional varchar(100), "
					+ "MsgFooter varchar(100), "
					+ "TimeZoneCode char(1), " // 30
					+ "MemoText varchar(255), "
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " 
					+ "SecurityQuestion varchar(100), "
					+ "SecurityAnswer varchar(26), "
					+ "EmailAddr varchar(255) NOT NULL, " // 35
					+ "EmailAddrId bigint NOT NULL, "
					+ "PrevEmailAddr varchar(255), "
					+ "PasswordChangeTime datetime, "
					+ "UserPassword varchar(32), "
					+ "UpdtTime datetime NOT NULL, "  //40
					+ "UpdtUserId varchar(10) NOT NULL, "
					+ "PRIMARY KEY (RowId), "
					+ "UNIQUE INDEX (CustId), "
					+ "INDEX (ClientId), "
					+ "INDEX (SsnNumber),"
					+ "UNIQUE INDEX (EmailAddrId), "
					+ "FOREIGN KEY (ClientId) REFERENCES Clients(ClientId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES EmailAddr(EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE "
					+ ") ENGINE=InnoDB");
			System.out.println("Created CUSTOMERS Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createCustSequenceTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE CUSTSEQUENCE ( " +
			"SeqId bigint NOT NULL " +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created CUSTSEQUENCE Table...");
			stm.execute("INSERT INTO CUSTSEQUENCE (SeqId) VALUES(0)");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		EmailAddrDao dao = (EmailAddrDao) SpringUtil.getDaoAppContext().getBean("emailAddrDao");
		String emailAddr = "jsmith@test.com";
		EmailAddrVo vo = dao.findByAddress(emailAddr);
		// 41 fields
		try {
			PreparedStatement ps = con
					.prepareStatement("INSERT INTO CUSTOMERS (" 
							+ "CustId, "	//1
							+ "ClientId, "
							+ "SsnNumber, "
							+ "TaxId, "
							+ "Profession, "	//5
							+ "FirstName, "
							+ "MiddleName, "
							+ "LastName, "
							+ "Alias, "
							+ "StreetAddress, "	//10
							+ "StreetAddress2, "
							+ "CityName, "
							+ "StateCode, "
							+ "ZipCode5, "
							+ "ZipCode4, "	//15
							+ "ProvinceName, "
							+ "PostalCode, "
							+ "Country, "
							+ "DayPhone, "
							+ "EveningPhone, "	//20
							+ "MobilePhone, "
							+ "BirthDate, "
							+ "StartDate, "
							+ "EndDate, "
							+ "MobileCarrier, " // 25
							+ "MsgHeader, "
							+ "MsgDetail, "
							+ "MsgOptional, "
							+ "MsgFooter, "
							+ "TimeZoneCode, " // 30
							+ "MemoText, "
							+ "StatusId , " 
							+ "SecurityQuestion, "
							+ "SecurityAnswer, "
							+ "EmailAddr, " // 35
							+ "EmailAddrId, "
							+ "PrevEmailAddr, "
							+ "PasswordChangeTime, "
							+ "UserPassword, "
							+ "UpdtTime, "  //40
							+ "UpdtUserId) "
							+ " VALUES ( "
							+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
							+ ",?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
							+ ",?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
							+ ",?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
							+ ",? )");
			ps.setString(1, "test");
			ps.setString(2, Constants.DEFAULT_CLIENTID);
			ps.setString(3, "123-45-6789");
			ps.setString(4, null);
			ps.setString(5, "Software Consultant");
			ps.setString(6, "Joe");
			ps.setString(7, null);
			ps.setString(8, "Smith");
			ps.setString(9, null);
			ps.setString(10, "123 Main st");
			ps.setString(11, null);
			ps.setString(12, "Dublin");
			ps.setString(13, "OH");
			ps.setString(14, "43017");
			ps.setString(15, null);
			ps.setString(16, null);
			ps.setString(17, "43017");
			ps.setString(18, "US");
			ps.setString(19, "614-234-5678");
			ps.setString(20, "614-987-6543");
			ps.setString(21, "614-JOE-CELL");
			GregorianCalendar cal = new GregorianCalendar(1980, 01, 01);
			ps.setDate(22, new java.sql.Date(cal.getTimeInMillis()));
			cal = new GregorianCalendar(2004, 05, 10);
			ps.setDate(23, new java.sql.Date(cal.getTimeInMillis()));
			cal = new GregorianCalendar(2009, 05, 10);
			ps.setDate(24, new java.sql.Date(cal.getTimeInMillis()));
			ps.setString(25, MobileCarrier.TMobile.getValue());
			ps.setString(26, "Joe's Message Header");
			ps.setString(27, "Dear Joe,");
			ps.setString(28, null);
			ps.setString(29, "Have a Nice Day.");
			ps.setString(30, "E");
			ps.setString(31, "I-Care Pilot customer");
			ps.setString(32, "A");
			ps.setString(33, "What is your favorite movie?");
			ps.setString(34, "Rambo");
			ps.setString(35, emailAddr);
			ps.setLong(36, vo.getEmailAddrId());
			ps.setString(37, null);
			ps.setTimestamp(38, null);
			ps.setString(39, null);
			ps.setTimestamp(40, new Timestamp(new java.util.Date().getTime()));
			ps.setString(41, Constants.DEFAULT_USER_ID);
			ps.execute();
			ps.close();
			System.out.println("Inserted all rows...");
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
			CustomerTable ct = new CustomerTable();
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