package com.legacytojava.message.dao.action;

import java.sql.SQLException;

import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.dao.rule.RuleTables;
import com.legacytojava.message.main.CreateTableBase;

/**
 * Dependency: RuleBean - this program runs RuleTable first before create its
 * own tables.
 */
public class ActionTables extends CreateTableBase
{
	RuleTables ruleTables;
	
	/**
	 * Creates a new instance of ActionTables
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public ActionTables() throws SQLException, ClassNotFoundException
	{
		ruleTables = new RuleTables();
		init();
	}
	
	public void dropTables()
	{
		try
		{
			stm.execute("DROP TABLE MSGACTION");
			System.out.println("Dropped MSGACTION Table...");
		} catch (SQLException e) {}
		try
		{
			stm.execute("DROP TABLE MSGACTIONDETAIL");
			System.out.println("Dropped MSGMSGACTIONDETAIL Table...");
		} catch (SQLException e) {}
		try
		{
			stm.execute("DROP TABLE MSGDATATYPE");
			System.out.println("Dropped MSGDATATYPE Table...");
		} catch (SQLException e) {}
		
		ruleTables.dropTables();
	}
	
	public void createTables() throws SQLException {
		ruleTables.createTables();
		createActionDataTypeTable();
		createActionDetailTable();
		createActionTable();
	}
	
	public void loadTestData() throws SQLException {
		ruleTables.loadTestData();
	}
	
	void createActionDataTypeTable() throws SQLException
	{
		try
		{
			stm.execute("CREATE TABLE MSGDATATYPE ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"DataType varchar(16) NOT NULL, " +
			"DataTypeValue varchar(100) NOT NULL, " +
			"MiscProperties varchar(255), " +
			"PRIMARY KEY (RowId), " +
			"UNIQUE INDEX (DataType, DataTypeValue) " +
			") ENGINE=InnoDB");
			System.out.println("Created MSGDATATYPE Table...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createActionDetailTable() throws SQLException
	{
		try
		{
			stm.execute("CREATE TABLE MSGACTIONDETAIL ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"ActionId varchar(16) NOT NULL, " +
			"Description varchar(100), " +
			"ProcessBeanId varchar(50) NOT NULL, " +
			"ProcessClassName varchar(100), " +
			"DataType varchar(16), " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId varchar(10) NOT NULL, " +
			"INDEX (DataType), " +
			"FOREIGN KEY (DataType) REFERENCES MSGDATATYPE (DataType) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (ActionId), " +
			"PRIMARY KEY (RowId) " +
			") ENGINE=InnoDB");
			System.out.println("Created MSGACTIONDETAIL Table...");
		} catch (SQLException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createActionTable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGACTION ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"RuleName varchar(26) NOT NULL, " +
			"ActionSeq int NOT NULL, " +
			"StartTime datetime NOT NULL, " +
			"ClientId varchar(16), " + 
			"StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " +
			"ActionId varchar(16) NOT NULL, " +
			"DataTypeValues text, " + // maximum size of 65,535, to accommodate template text
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (RuleName) REFERENCES RULELOGIC (RuleName) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"INDEX(RuleName), " +
			"FOREIGN KEY (ActionId) REFERENCES MSGACTIONDETAIL (ActionId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"UNIQUE INDEX (RuleName, ActionSeq, StartTime, ClientId) " +
			") ENGINE=InnoDB");
			System.out.println("Created MSGACTION Table...");
		}
		catch (SQLException e) {
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
			ActionTables ct = new ActionTables();
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