package com.es.data.loader;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.dao.subscriber.SubscriberDao;
import com.es.data.constant.Constants;
import com.es.data.constant.MobileCarrierEnum;
import com.es.data.constant.StatusId;
import com.es.vo.comm.SubscriberVo;

public class SubscriberTable extends AbstractTableBase {
	
	public void dropTables() {
		try {
			getJdbcTemplate().execute("DROP TABLE SUBSCRIBER");
			System.out.println("Dropped SUBSCRIBER Table...");
		}
		catch (DataAccessException e) {
		}
		try {
			getJdbcTemplate().execute("DROP TABLE SUBR_SEQUENCE");
			System.out.println("Dropped SUBR_SEQUENCE Table...");
		}
		catch (DataAccessException e) {
		}
	}
	
	public void createTables() throws DataAccessException {
		createSubrSequenceTable();
		createSubscriberTable();
	}

	void createSubscriberTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE SUBSCRIBER ( "
					+ "RowId int AUTO_INCREMENT not null, "
					+ "SubrId varchar(16) NOT NULL, "	//1
					+ "SenderId varchar(16) NOT NULL, "
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
					+ "TimeZoneCode varchar(50), " // 30
					+ "MemoText varchar(255), "
					+ "StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " 
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
					+ "UNIQUE INDEX (SubrId), "
					+ "INDEX (SenderId), "
					+ "INDEX (SsnNumber),"
					+ "UNIQUE INDEX (EmailAddrId), "
					+ "FOREIGN KEY (SenderId) REFERENCES Sender_Data(SenderId) ON DELETE CASCADE ON UPDATE CASCADE, "
					+ "FOREIGN KEY (EmailAddrId) REFERENCES Email_Address(EmailAddrId) ON DELETE CASCADE ON UPDATE CASCADE "
					+ ") ENGINE=InnoDB");
			System.out.println("Created SUBSCRIBER Table...");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createSubrSequenceTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE SUBR_SEQUENCE ( " +
			"SeqId bigint NOT NULL " +
			") ENGINE=MyISAM"); // table-level locking ?
			System.out.println("Created SUBR_SEQUENCE Table...");
			getJdbcTemplate().execute("INSERT INTO SUBR_SEQUENCE (SeqId) VALUES(0)");
		}
		catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws DataAccessException {
		SubscriberDao service = SpringUtil.getAppContext().getBean(SubscriberDao.class);
		
		String addr = getProperty("subscriber.email.1");
		SubscriberVo data = new SubscriberVo();
		data.setSenderId(Constants.DEFAULT_SENDER_ID);
		data.setEmailAddr(addr);
		data.setSubrId(getProperty("subscriber.id.1"));
		data.setSsnNumber("123-45-6789");
		data.setTaxId(null);
		data.setProfession("Software Consultant");
		data.setFirstName("Joe");
		data.setLastName("Smith");
		data.setStreetAddress("123 Main St.");
		data.setCityName("Dublin");
		data.setStateCode("OH");
		data.setZipCode5("43071");
		data.setPostalCode("43071");
		data.setCountry("US");
		data.setDayPhone("614-234-5678");
		data.setEveningPhone("614-789-6543");
		data.setMobilePhone("614-264-4056");
		data.setMobileCarrier(MobileCarrierEnum.TMobile.getValue());
		data.setBirthDate(new java.sql.Date(new GregorianCalendar(1980,01,01).getTimeInMillis()));
		data.setStartDate(new java.sql.Date(new GregorianCalendar(2004,05,10).getTimeInMillis()));
		data.setEndDate(new java.sql.Date(new GregorianCalendar(2016,05,10).getTimeInMillis()));
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data.setMsgHeader("Joe's Message Header");
		data.setMsgDetail("Dear Joe,");
		data.setMsgFooter("Have a nice day.");
		data.setTimeZoneCode(TimeZone.getDefault().getID());
		data.setMemoText("E-Sphere Pilot subscriber");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setSecurityQuestion("What is your favorite movie?");
		data.setSecurityAnswer("Rambo");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		service.insert(data);
		System.out.println("Subscriber record inserted.");
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			SubscriberTable ct = new SubscriberTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}