package com.legacytojava.message.table;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MailServerType;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.main.CreateTableBase;
public class SmtpTable extends CreateTableBase
{
	/** Creates a new instance of MailTables 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public SmtpTable() throws ClassNotFoundException, SQLException
	{
		init();
	}
	
	public void dropTables() {
		try
		{
			stm.execute("DROP TABLE MAILSENDERPROPS");
			System.out.println("Dropped MAILSENDERPROPS Table...");
		} catch (SQLException e) {}
		try
		{
			stm.execute("DROP TABLE SMTPSERVERS");
			System.out.println("Dropped SMTPSERVERS Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException
	{
		/*
	 	- smtpHost: smtp host domain name or ip address
	 	- smtpPort: smtp port, default = 25
		- serverName: smtp server name
		- useSsl: yes/no, set smtpport to 465 (or -1) if yes
		- useAuth: /yes/no
		- userId: userid to login to smtp server
		- userpswd: password for the user
		- persistence: yes/no, default to yes
	 	- serverType: smtp/exch, default to smtp
		- threads: number of connections to be created, default=1
	 	- retries: number, 0(default) - no retry, n - for n minutes, -1 - infinite
	 	- retryFreq: number, 5(default): every 5 minutes, n: every n minutes
	 	- alertAfter: number, send alert after the number of retries has been attempted
	 	- alertLevel: infor/error/fatal/nolog
		- messageCount: number of messages to send before stopping the process, 0=unlimited
		*/
		try
		{
			stm.execute("CREATE TABLE SMTPSERVERS ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ServerName varchar(50) NOT NULL, " +
			"SmtpHost varchar(100) NOT NULL, " +
			"SmtpPort integer NOT NULL, " +
			"Description varchar(100), " +
			"UseSsl varchar(3) NOT NULL, " +
			"UseAuth varchar(3), " +
			"UserId varchar(30) NOT NULL, " + 
			"UserPswd varchar(30) NOT NULL, " +
			"Persistence varchar(3) NOT NULL, " +
			"StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " +
			"ServerType varchar(5) DEFAULT '" + MailServerType.SMTP + "', " +
			"Threads integer NOT NULL, " +
			"Retries integer NOT NULL, " +
			"RetryFreq integer NOT NULL, " +
			"AlertAfter integer, " +
			"AlertLevel varchar(5), " +
			"MessageCount integer NOT NULL, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (ServerName) " +
			") ENGINE=InnoDB");
			System.out.println("Created SMTPSERVERS Table...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
		
		/*
		- InternalLoopback - internal email address for loop back
		- ExternalLoopback - external email address for loop back
		- UseTestAddr: yes/no: to override the TO address with the value from TestToAddr
		- TestToAddr: use it as the TO address when UseTestAddr is yes
		*/
		try
		{
			stm.execute("CREATE TABLE MAILSENDERPROPS ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"InternalLoopback varchar(100) NOT NULL, " +
			"ExternalLoopback varchar(100) NOT NULL, " +
			"UseTestAddr varchar(3) NOT NULL, " +
			"TestFromAddr varchar(255), " +
			"TestToAddr varchar(255) NOT NULL, " +
			"TestReplytoAddr varchar(255), " + 
			"IsVerpEnabled varchar(3) NOT NULL, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId) " +
			") ENGINE=InnoDB");
			System.out.println("Created MAILSENDERPROPS Table...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException {
		insertReleaseSmtpData();
		insertTestSmtpData();
		insertMailSenderData();
	}
	
	public void loadReleaseData() throws SQLException {
		insertReleaseSmtpData();
		insertMailSenderData();
	}
	
	private String insertSql = 
		"INSERT INTO SMTPSERVERS " +
			"(SmtpHost," +
			"SmtpPort," +
			"ServerName," +
			"Description, " +
			"UseSsl," +
			"UserId," +
			"UserPswd," +
			"Persistence," +
			"StatusId," +
			"ServerType," +
			"Threads," +
			"Retries," +
			"RetryFreq," +
			"AlertAfter," +
			"AlertLevel," +
			"MessageCount," +
			"UpdtTime," +
			"UpdtUserId) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
				", ?, ?, ?, ?, ?, ?, ?, ?)";

	void insertReleaseSmtpData() throws SQLException
	{
		try
		{
			PreparedStatement ps = con.prepareStatement(insertSql);

			ps.setString(1, "localhost"); // smtpHost
			ps.setString(2, "-1"); // smtpPort
			ps.setString(3, "smtpServer"); // server name
			ps.setString(4, "smtp server on localhost"); // description
			ps.setString(5, Constants.NO); // use ssl
			ps.setString(6, "support"); // user id
			ps.setString(7, "support"); // user password
			ps.setString(8, Constants.NO); // persistence
			ps.setString(9, StatusIdCode.ACTIVE); // status id
			ps.setString(10, MailServerType.SMTP); // server type
			ps.setInt(11, 4); // Threads
			ps.setInt(12, 10); // retries
			ps.setInt(13, 6); // retry freq
			ps.setInt(14, 5); // alertAfter
			ps.setString(15, "error"); // alert level
			ps.setInt(16, 0); // message count
			ps.setTimestamp(17, new Timestamp(new java.util.Date().getTime()));
			ps.setString(18, Constants.DEFAULT_USER_ID);
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void insertTestSmtpData() throws SQLException
	{
		try
		{
			PreparedStatement ps = con.prepareStatement(insertSql);
			
			ps.setString(1, "outbound.mailhop.org"); // smtpHost
			ps.setString(2, "465"); // smtpPort
			ps.setString(3, "DyndnsMailRelay"); // server name
			ps.setString(4, "smtp server on dyndns"); // description
			ps.setString(5, Constants.YES); // use ssl
			ps.setString(6, "jackwng"); // user id
			ps.setString(7, "jackwng01"); // user password
			ps.setString(8, Constants.NO); // persistence
			ps.setString(9, StatusIdCode.INACTIVE); // status id
			ps.setString(10, MailServerType.SMTP); // server type
			ps.setInt(11, 1); // Threads
			ps.setInt(12, 10); // retries
			ps.setInt(13, 5); // retry freq
			ps.setInt(14, 5); // alertAfter
			ps.setString(15, "error"); // alert level
			ps.setInt(16, 0); // message count
			ps.setTimestamp(17, new Timestamp(new java.util.Date().getTime()));
			ps.setString(18, Constants.DEFAULT_USER_ID);
			ps.execute();
			
			ps.setString(1, "smtp.gmail.com"); // smtpHost
			ps.setString(2, "-1"); // smtpPort
			ps.setString(3, "gmailServer"); // server name
			ps.setString(4, "smtp server on gmail.com"); // description
			ps.setString(5, Constants.YES); // use ssl
			ps.setString(6, "jackwng"); // user id
			ps.setString(7, "jackwng01"); // user password
			ps.setString(8, Constants.NO); // persistence
			ps.setString(9, StatusIdCode.INACTIVE); // status id
			ps.setString(10, MailServerType.SMTP); // server type
			ps.setInt(11, 2); // Threads
			ps.setInt(12, 10); // retries
			ps.setInt(13, 5); // retry freq
			ps.setInt(14, 5); // alertAfter
			ps.setString(15, "error"); // alert level
			ps.setInt(16, 0); // message count
			ps.setTimestamp(17, new Timestamp(new java.util.Date().getTime()));
			ps.setString(18, Constants.DEFAULT_USER_ID);
			ps.execute();
			
			ps.setString(1, "localhost"); // smtpHost
			ps.setString(2, "25"); // smtpPort
			ps.setString(3, "exchServer"); // server name
			ps.setString(4, "exch server on localhost"); // description
			ps.setString(5, Constants.NO); // use ssl
			ps.setString(6, "uid"); // user id
			ps.setString(7, "pwd"); // user password
			ps.setString(8, Constants.NO); // persistence
			ps.setString(9, StatusIdCode.ACTIVE); // status id
			ps.setString(10, MailServerType.EXCH); // server type
			ps.setInt(11, 1); // Threads
			ps.setInt(12, 4); // retries
			ps.setInt(13, 1); // retry freq
			ps.setInt(14, 15); // alertAfter
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	public void UpdateSmtpData4Prod() throws SQLException
	{
		String sql = "update SMTPSERVERS set StatusId = ? where ServerName = ?";
		try
		{
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setString(1, StatusIdCode.INACTIVE); // statusid
			ps.setString(2, "smtpServer"); // smtpPort
			ps.execute();
			
			ps.setString(1, StatusIdCode.ACTIVE); // statusid
			ps.setString(2, "DyndnsMailRelay"); // smtpPort
			ps.execute();
			
			ps.setString(1, StatusIdCode.INACTIVE); // statusid
			ps.setString(2, "exchServer"); // smtpPort
			ps.execute();
			
			ps.close();
			System.out.println("SmtpTable: Smtp records updated.");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertMailSenderData() throws SQLException
	{
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO MAILSENDERPROPS " +
				"(InternalLoopback," +
				"ExternalLoopback," +
				"UseTestAddr," +
				"TestFromAddr, " +
				"TestToAddr," +
				"TestReplytoAddr," +
				"IsVerpEnabled," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

			ps.setString(1, "testto@localhost");
			ps.setString(2, "inbox@lagacytojava.com");
			ps.setString(3, Constants.YES);
			ps.setString(4, "testfrom@localhost");
			ps.setString(5, "testto@localhost");
			ps.setString(6, "testreplyto@localhost");
			ps.setString(7, Constants.NO);
			ps.setTimestamp(8, new Timestamp(new java.util.Date().getTime()));
			ps.setString(9, "SysAdmin");
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try {
			SmtpTable ct = new SmtpTable();
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