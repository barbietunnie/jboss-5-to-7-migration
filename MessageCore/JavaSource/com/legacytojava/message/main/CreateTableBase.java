package com.legacytojava.message.main;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.legacytojava.jbatch.SpringUtil;

public abstract class CreateTableBase {
	protected static final String LF = System.getProperty("line.separator", "\n");
	protected Connection con = null;
	protected Statement stm = null;

	protected void init() throws ClassNotFoundException, SQLException {
		DataSource ds = (DataSource) SpringUtil.getDaoAppContext().getBean("mysqlDataSource");
		con = ds.getConnection();
		stm = con.createStatement();
	}

	public abstract void createTables() throws SQLException;
	
	public abstract void dropTables();
	
	public abstract void loadTestData() throws SQLException;
	
	public void loadReleaseData() throws SQLException {
		loadTestData();
	}
	
	public void wrapup() {
		if (con != null) {
			try {
				con.close();
			}
			catch (SQLException e) {
			}
		}
	}
}
