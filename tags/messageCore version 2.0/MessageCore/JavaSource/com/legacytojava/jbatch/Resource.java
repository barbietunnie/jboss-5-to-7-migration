package com.legacytojava.jbatch;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * provide a repository for storing and fetching resources.
 */
public class Resource implements java.io.Serializable {
	private static final long serialVersionUID = 6273146753143924178L;
	protected static final Logger logger = Logger.getLogger(Resource.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient DataSource dataSource;

	/**
	 * Constructor
	 */
	public Resource() {
		if (isDebugEnabled)
			logger.debug("Entering Constructor...");
		// set home directory for derby
		System.setProperty("derby.system.home", System.getProperty("user.dir"));
	}

	public static void main(String[] args) {
		JbMain.getInstance();
		Resource resource = (Resource)  SpringUtil.getAppContext().getBean(Resource.class);
		Properties props = System.getProperties();
		props.list(System.out);
		try {
			resource.init();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * default resource initialization, invoked by JbMain at start up
	 * @throws SQLException 
	 */
	final void init() throws SQLException {
		// create tables in the embedded database
		checkMetricsTable();
	}

	/**
	 * create metrics tables if they do not exist<br>
	 * this method only works with derby
	 * @throws SQLException 
	 */
	private void checkMetricsTable() throws SQLException {
		logger.info("Entering checkMetricsTable()...");
		/*
		 * // to connect to embedded derby use
		 * ("jdbc:derby:testdb;create=true");
		 */
		java.sql.Connection con = null;
		java.sql.PreparedStatement pstmt = null;
		try {
			con = dataSource.getConnection();
			// perform a dummy select
			pstmt = con.prepareStatement("select * from metrics_logger where SERVER_NAME = 'TEST'");
			pstmt.executeQuery();
			pstmt.close();
			if (!con.getAutoCommit())
				con.commit();
		}
		catch (java.sql.SQLException e) {
			logger.error("SQLException caught during select, create metrics_logger", e);
			if (e.getMessage().toLowerCase().indexOf("does not exist") >= 0) {
				pstmt = con.prepareStatement("create table metrics_logger ("
						+ "server_name varchar(30) not null,"
						+ "server_id varchar(100) not null,"
						+ "input_total bigint not null,"
						+ "output_total bigint not null,"
						+ "error_total bigint not null,"
						+ "worker_total bigint not null,"
						+ "proctime_total bigint not null,"
						+ "worker_count bigint not null,"
						+ "proctime_count bigint not null,"
						+ "add_time timestamp not null" + ")");
				pstmt.executeUpdate();
				pstmt.close();

				pstmt = con.prepareStatement("create index metrics_logger_index on metrics_logger ("
								+ "server_name, server_id" + ")");
				pstmt.executeUpdate();
				pstmt.close();

				pstmt = con.prepareStatement("create index metrics_logger_index_2 on metrics_logger ("
								+ "add_time" + ")");
				pstmt.executeUpdate();
				pstmt.close();

				if (!con.getAutoCommit())
					con.commit();
				logger.error("Resource: table metrics_logger/index created.");
			}
		}
		finally {
			try {
				if (con != null) {
					con.close();
				}
			}
			catch (Exception e) {
				logger.error("Exception caught during con.close()", e);
			}
		}
	}

	/**
	 * release resources, invoked by JbMain during shutdown
	 */
	final void wrapup() {
		// shutdown derby database
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		}
		catch (Exception e) {
			// shutdown may throw an exception
			logger.error("Cloudscape closed" , e);
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}