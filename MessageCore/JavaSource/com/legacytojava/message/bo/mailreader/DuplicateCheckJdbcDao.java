package com.legacytojava.message.bo.mailreader;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.JbMain;

/**
 * This class is used to check duplicate messages. It uses derby database as its
 * persistent storage. For every email received, method isDuplicate() should be
 * called with its SMTP message-id, and the return should be evaluated to see if
 * it's a duplicate message.
 */
public class DuplicateCheckJdbcDao implements DuplicateCheckDao {
	static final Logger logger = Logger.getLogger(DuplicateCheckJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private DataSource dataSource;
	private String purgeAfter;
	
	/**
	DROP INDEX MSGIDDUP_index;
	DROP TABLE MSGIDDUP;
	CREATE TABLE MSGIDDUP (
		message_id varchar(200) not null primary key,
		add_time timestamp not null);
	CREATE INDEX MSGIDDUP_index ON MSGIDDUP (
		add_time);
	
	// to connect to embedded CloudScape
	use ("jdbc:cloudscape:dbcache;create=true");
	*/
	
	/** 
	 * Constructor
	 */
	public DuplicateCheckJdbcDao() {
	}
	
	public static void main(String[] args) {
		DuplicateCheckDao dCheck = (DuplicateCheckDao) JbMain.getBatchAppContext()
				.getBean("duplicateCheck");
		try {
			String msgId = "1223344556788990";
			boolean isDuplicate = dCheck.isDuplicate(msgId);
			System.out.println("Is " + msgId + " duplicate? " + isDuplicate);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	/**
	 * processor entry point called by Timer task
	 * 
	 * @param req -
	 *            a java TimerTask object
	 * @throws Exception
	 *             if any error
	 */
	public void process(Object req) throws Exception {
		if (req!=null && req instanceof java.util.TimerTask) {
			// perform purge (on MSGIDDUP table) hourly
			logger.info("Prepare to purge aged records...");
			int hours=0;
			try	{
				hours=Integer.parseInt(purgeAfter);
			}
			catch (NumberFormatException e)	{
				hours=24;
			}
			purge((hours<1?1:hours)); // purge records older than 24 hours
		}
	}
	
	/**
	 * @param msg_id -
	 *            smtp message id
	 * @return - true - duplicate key
	 */
	public synchronized boolean isDuplicate(String msg_id) {
		boolean duplicate = false;
		if (dataSource==null) return duplicate;
		
		java.sql.Connection con=null;
		try {
			con = dataSource.getConnection();
			// prepare for insert of a message-
			java.sql.PreparedStatement pstmt = con.prepareStatement(
				"insert into MSGIDDUP values "
				+ "(?"
				+ ", ?"
				+ ")");
			
			java.sql.Timestamp tms = new java.sql.Timestamp(new Date().getTime());
			pstmt.clearParameters();
			pstmt.setString(1,msg_id);
			pstmt.setTimestamp(2,tms);
			
			pstmt.executeUpdate();
			pstmt.close();
			if (!con.getAutoCommit())
				con.commit();
		}
		catch (java.sql.SQLException e)	{
			logger.error("SQLException caught during insert", e);
			if (e.getMessage().toLowerCase().indexOf("does not exist") >= 0 /* Derby */
					|| e.getMessage().toLowerCase().indexOf("doesn't exist") >= 0 /* MySQL */) {
				// table does not exist, create one.
				try {
					java.sql.PreparedStatement pstmt = con.prepareStatement(
						"create table MSGIDDUP ("
						+ "message_id varchar(200) not null primary key "
						+ ",add_time timestamp not null"
						+ ")");
					pstmt.executeUpdate();
					pstmt.close();
					
					pstmt = con.prepareStatement(
						"create index MSGIDDUP_INDEX on MSGIDDUP ("
						+ "add_time"
						+ ")");
					pstmt.executeUpdate();
					pstmt.close();
					if (!con.getAutoCommit())
						con.commit();
					
					logger.info("DuplicateCheck: table MSGIDDUP and its index created.");
				}
				catch (java.sql.SQLException se) {
					logger.error("Failed to create MSGIDDUP table/index", se);
				}
			}
			else if (e.getMessage().indexOf("duplicate key") >= 0 /* Derby */
					|| e.getMessage().indexOf("Duplicate entry") >= 0 /* MySQL */) {
				duplicate = true;
			}
			
			try { 
				con.rollback(); 
			}
			catch (Exception e2) {}
		}
		catch (Exception e)	{
			logger.error("Exception caught during insert", e);
			try { 
				con.rollback();
			}
			catch (Exception e2) {}
		}
		finally	{
			try	{
				if (con!=null)
					con.close();
			}
			catch (Exception e) {}
		}
		return duplicate;
	}
	
	/**
	 * purge records older than specified hours
	 * 
	 * @param hours -
	 *            records older than the hours will be purged
	 */
	public synchronized void purge(int hours) {
		if (dataSource==null) return;
		logger.info("purge() - purge records older than " + hours + " hours...");
		java.sql.Connection con=null;
		try	{
			con = dataSource.getConnection();
			java.sql.PreparedStatement pstmt = null;
			// prepare for delete of aged records
			pstmt = con.prepareStatement(
				"delete from MSGIDDUP"
				+ " where ADD_TIME < ?"
				);
			pstmt.clearParameters();
			
			Calendar calendar = new GregorianCalendar();
			calendar.add(Calendar.HOUR,-hours);
			Date go_back=calendar.getTime();
			pstmt.setTimestamp(1,new java.sql.Timestamp(go_back.getTime()));
			
			int rows = pstmt.executeUpdate();
			pstmt.close();
			if (!con.getAutoCommit())
				con.commit();
			logger.info("purge() - number of records purged: "+rows);
		}
		catch (java.sql.SQLException e)	{
			logger.error("SQLException caught during delete", e);
			try {
				con.rollback();
			}
			catch (Exception e2) {}
		}
		catch (Exception e)	{
			logger.error("Exception caught during delete", e);
			try {
				con.rollback();
			}
			catch (Exception e2) {}
		}
		finally	{
			try	{
				if (con!=null)
					con.close();
			}
			catch (Exception e) {}
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getPurgeAfter() {
		return purgeAfter;
	}

	public void setPurgeAfter(String purgeAfter) {
		this.purgeAfter = purgeAfter;
	}
}
