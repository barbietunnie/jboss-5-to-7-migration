package com.legacytojava.message.dao.timer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.main.CreateTableBase;
public class TimerTable extends CreateTableBase
{
	/** Creates a new instance of MailTables 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public TimerTable() throws ClassNotFoundException, SQLException
	{
		init();
	}
	
	public void dropTables() {
		try
		{
			stm.execute("DROP TABLE TIMERSERVERS");
			System.out.println("Dropped TIMERSERVERS Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException
	{
		try
		{
			stm.execute("CREATE TABLE TIMERSERVERS ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ServerName varchar(50) NOT NULL, " + 
			"TimerInterval Integer NOT NULL, " +
			"TimerIntervalUnit varchar(6) NOT NULL, " +
			"InitialDelay Integer NOT NULL, " +
			"StartTime datetime, " +
			"Threads Integer NOT NULL, " +
			"StatusId char(1) NOT NULL, " +
			"ProcessorName varchar(100) NOT NULL, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (ServerName) " +
			") ENGINE=InnoDB");
			System.out.println("Created TIMERSERVERS Table...");
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
				"INSERT INTO TIMERSERVERS " +
				"(ServerName," +
				"TimerInterval," +
				"TimerIntervalUnit," +
				"InitialDelay," +
				"StartTime," +
				"Threads," +
				"StatusId," +
				"ProcessorName," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"); 
			
			ps.setString(1, "PurgeTimer");
			ps.setInt(2, 60);
			ps.setString(3, "minute");
			ps.setInt(4, 5000);
			ps.setTimestamp(5, null);
			ps.setInt(6, 1);
			ps.setString(7, StatusIdCode.ACTIVE);
			ps.setString(8, "timerProcessor");
			ps.setTimestamp(9, new Timestamp(new java.util.Date().getTime()));
			ps.setString(10, Constants.DEFAULT_USER_ID);
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
			TimerTable ct = new TimerTable();
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