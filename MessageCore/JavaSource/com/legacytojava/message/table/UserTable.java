package com.legacytojava.message.table;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.main.CreateTableBase;
import com.legacytojava.message.vo.inbox.SearchFieldsVo;
public class UserTable extends CreateTableBase
{
	/** Creates a new instance of MailTables 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public UserTable() throws ClassNotFoundException, SQLException
	{
		init();
	}
	
	public void dropTables() {
		try
		{
			stm.execute("DROP TABLE SESSIONS");
			System.out.println("Dropped SESSIONS Table...");
		} catch (SQLException e) {}
		try
		{
			stm.execute("DROP TABLE SESSIONUPLOADS");
			System.out.println("Dropped SESSIONUPLOADS Table...");
		} catch (SQLException e) {}
		try
		{
			stm.execute("DROP TABLE USERS");
			System.out.println("Dropped USERS Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException
	{
		createUserTable();
		createSessionTable();
		createSessionUploadTable();
	}

	void createUserTable() throws SQLException
	{
		try
		{
			stm.execute("CREATE TABLE USERS ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"UserId varchar(10) NOT NULL, " + 
			"Password varchar(32) NOT NULL, " +
			"SessionId varchar(50), " +
			"FirstName varchar(32), " +
			"LastName varchar(32), " +
			"MiddleInit char(1), " +
			"CreateTime datetime NOT NULL, " +
			"LastVisitTime datetime, " +
			"hits Integer NOT NULL DEFAULT 0, " +
			"StatusId char(1) NOT NULL, " + // A - active, I - Inactive
			"Role varchar(5) NOT NULL, " + // admin/user
			"EmailAddr varchar(255), " +
			"DefaultFolder varchar(8), " + // All/Received/Sent/Closed
			"DefaultRuleName varchar(26), " + // All/...
			"DefaultToAddr varchar(255), " +
			"ClientId varchar(16) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (ClientId) REFERENCES Clients(ClientId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"Constraint UNIQUE INDEX (UserId) " +
			") ENGINE=InnoDB");
			System.out.println("Created USERS Table...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createSessionTable() throws SQLException
	{
		try
		{
			stm.execute("CREATE TABLE SESSIONS ( " +
			"SessionId varchar(50) NOT NULL, " + 
			"SessionName varchar(50), " +
			"SessionValue text, " +
			"UserId varchar(10) NOT NULL, " +
			"CreateTime datetime NOT NULL, " +
			"INDEX (UserId), " +
			"PRIMARY KEY (SessionId, SessionName) " +
			") ENGINE=InnoDB");
			System.out.println("Created SESSIONS Table...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createSessionUploadTable() throws SQLException
	{
		try
		{
			stm.execute("CREATE TABLE SESSIONUPLOADS ( " +
			"SessionId varchar(50) NOT NULL, " + 
			"SessionSeq int NOT NULL, " +
			"FileName varchar(100) NOT NULL, " +
			"ContentType varchar(100), " +
			"UserId varchar(10) NOT NULL, " +
			"CreateTime datetime NOT NULL, " +
			"SessionValue mediumblob, " +
			"INDEX (UserId), " +
			"PRIMARY KEY (SessionId, SessionSeq) " +
			") ENGINE=InnoDB");
			System.out.println("Created SESSIONUPLOADS Table...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws SQLException
	{
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO USERS " +
				"(UserId," +
				"PassWord," +
				"FirstName," +
				"LastName," +
				"CreateTime," +
				"StatusId," +
				"Role," +
				"DefaultFolder," +
				"clientId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"); 
			
			ps.setString(1, "admin");
			ps.setString(2, "admin");
			ps.setString(3, "default");
			ps.setString(4, "admin");
			ps.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
			ps.setString(6, StatusIdCode.ACTIVE);
			ps.setString(7, Constants.ADMIN_ROLE);
			ps.setString(8, SearchFieldsVo.MsgType.Received.toString()); //"All");
			ps.setString(9, Constants.DEFAULT_CLIENTID);
			ps.execute();
			
			ps.setString(1, "user");
			ps.setString(2, "user");
			ps.setString(3, "default");
			ps.setString(4, "user");
			ps.setTimestamp(5, new Timestamp(new java.util.Date().getTime()));
			ps.setString(6, StatusIdCode.ACTIVE);
			ps.setString(7, Constants.USER_ROLE);
			ps.setString(8, SearchFieldsVo.MsgType.Received.toString());
			ps.setString(9, Constants.DEFAULT_CLIENTID);
			ps.execute();
			
			ps.close();
			System.out.println("Inserted all rows...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
		
		try
		{
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO SESSIONUPLOADS " +
				"(SessionId," +
				"SessionSeq," +
				"FileName," +
				"ContentType," +
				"UserId," +
				"CreateTime," +
				"SessionValue) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)"); 
			
			ps.setString(1, "test_session_id");
			ps.setInt(2, 0);
			ps.setString(3, "test1.txt");
			ps.setString(4, "text/plain");
			ps.setString(5, "user");
			ps.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
			ps.setBytes(7, "test upload text 1".getBytes());
			ps.execute();
			
			ps.setString(1, "test_session_id");
			ps.setInt(2, 1);
			ps.setString(3, "test2.txt");
			ps.setString(4, "text/plain");
			ps.setString(5, "user");
			ps.setTimestamp(6, new Timestamp(new java.util.Date().getTime()));
			ps.setBytes(7, "test upload text 2".getBytes());
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
			UserTable ct = new UserTable();
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