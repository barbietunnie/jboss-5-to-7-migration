package com.legacytojava.message.dao.socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.main.CreateTableBase;
public class SocketServerTable extends CreateTableBase
{
	/** Creates a new instance of MailTables 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public SocketServerTable() throws SQLException, ClassNotFoundException
	{
		init();
	}
	
	public void dropTables() {
		try
		{
			stm.execute("DROP TABLE SOCKETSERVERS");
			System.out.println("Dropped SOCKETSERVERS Table...");
		} catch (SQLException e) {}
	}
	
	public void createTables() throws SQLException
	{
		/*
	 	- ServerName: Socket Server Name
	 	- SocketPort: the port number the socket server is listening to
		- Interactive: yes/no
	 	- ServerTimeout: timeout value for server socket
	 	- SocketTimeout: timeout value for socket
	 	- TimeoutUnit: minute/second
		- Connections: number of server socket connections to create
		- Priority: high/medium/low - server thread priority
		- StatusId: A - Active, I - Inactive
		- ProcessorName: processor class name
		*/
		try
		{
			stm.execute("CREATE TABLE SOCKETSERVERS ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ServerName varchar(30) NOT NULL, " + 
			"SocketPort Integer NOT NULL, " +
			"Interactive varchar(3) NOT NULL, " +
			"ServerTimeout integer NOT NULL, " +
			"SocketTimeout integer NOT NULL, " +
			"TimeoutUnit varchar(6) NOT NULL, " +
			"Connections Integer NOT NULL, " +
			"Priority varchar(10), " +
			"StatusId char(1) NOT NULL, " +
			"ProcessorName varchar(100) NOT NULL, " +
			"MessageCount integer NOT NULL, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"Constraint UNIQUE INDEX (ServerName), " +
			"Constraint UNIQUE INDEX (SocketPort) " +
			") ENGINE=InnoDB");
			System.out.println("Created SOCKETSERVERS Table...");
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
				"INSERT INTO SOCKETSERVERS " +
				"(ServerName," +
				"SocketPort," +
				"Interactive," +
				"ServerTimeout," +
				"SocketTimeout," +
				"TimeoutUnit," +
				"Connections," +
				"Priority," +
				"StatusId," +
				"ProcessorName," +
				"MessageCount," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
					", ?, ?, ?)");
			
			ps.setString(1, "SocketServer1");
			ps.setInt(2, 5444);
			ps.setString(3, Constants.YES);
			ps.setInt(4, 20);
			ps.setInt(5, 10);
			ps.setString(6, "minute");
			ps.setInt(7, 10);
			ps.setString(8, "high");
			ps.setString(9, StatusIdCode.ACTIVE);
			ps.setString(10, "socketProcessor");
			ps.setInt(11, 0);
			ps.setTimestamp(12, new Timestamp(new java.util.Date().getTime()));
			ps.setString(13, Constants.DEFAULT_USER_ID);
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
			SocketServerTable ct = new SocketServerTable();
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