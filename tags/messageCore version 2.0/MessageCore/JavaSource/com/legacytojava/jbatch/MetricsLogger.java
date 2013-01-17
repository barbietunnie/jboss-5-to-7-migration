package com.legacytojava.jbatch;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * repository for storing and displaying JBatch server status
 */
public class MetricsLogger implements java.io.Serializable {
	private static final long serialVersionUID = -7525717173156032898L;
	static final Logger logger = Logger.getLogger(MetricsLogger.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient final DataSource dataSource;
	private final MetricsData metricsData;

	public static final int PROC_INPUT = 0;
	public static final int PROC_OUTPUT = 1;
	public static final int PROC_ERROR = 2;
	public static final int PROC_TIME = 3;
	public static final int PROC_WORKER = 4;

	Date lastUpdt = new Date();
	final Date startTime;

	final String LF = System.getProperty("line.separator", "\n");

	/**
	 * initialize with name and id to uniquely identify a server
	 * 
	 * @param dataSource
	 *            a data source reference
	 * @param metricsData
	 *            a MetricsData instance
	 */
	public MetricsLogger(DataSource dataSource, MetricsData metricsData) {
		logger.info("Entering Constructor...");
		this.dataSource = dataSource;
		this.metricsData = metricsData;
		Calendar rightNow = Calendar.getInstance();
		//int dayofweek =
			rightNow.get(Calendar.DAY_OF_WEEK);
		startTime = new Date();
		JbMain.metricsLoggers.add(this);
	}

	void updateTable() {
		synchronized (metricsData) {
			_updateTable();
			metricsData.clear();
		}
	}

	/**
	 * DROP INDEX metrics_logger_index;
	 * DROP INDEX metrics_logger_index_2;
	 * DROP TABLE metrics_logger;
	 * CREATE TABLE metrics_logger ( 
	 * 	server_name varchar(30) not null,
	 *  server_id varchar(10) not null,
	 *  input_total bigint not null,
	 *  output_total bigint not null,
	 *  error_total bigint not null,
	 *  worker_total bigint not null,
	 *  proctime_total bigint not null,
	 *  worker_count bigint not null,
	 *  proctime_count bigint not null,
	 *  add_time datetime not null);
	 * CREATE INDEX metrics_logger_index
	 *  ON metrics_logger (server_name,server_id);
	 * CREATE INDEX metrics_logger_index_2
	 *  ON metrics_logger ( add_time);
	 */
	/**
	 * perform table update
	 */
	private void _updateTable() {
		java.sql.Connection con = null;
		try {
			con = dataSource.getConnection();
			// prepare for insert of metrics_logger record
			java.sql.PreparedStatement pstmt = 
				con.prepareStatement("insert into metrics_logger values "
					+ "(?" + ", ?" + ", ?" + ", ?" + ", ?" + ", ?" + ", ?"
					+ ", ?" + ", ?" + ", ?" + ")");
			pstmt.clearParameters();
			pstmt.setString(1, metricsData.getServerName());
			pstmt.setString(2, metricsData.getServerId());
			pstmt.setLong(3, metricsData.getInputCount());
			pstmt.setLong(4, metricsData.getOutputCount());
			pstmt.setLong(5, metricsData.getErrorCount());
			pstmt.setLong(6, metricsData.getWorkerCount());
			pstmt.setLong(7, metricsData.getTimeCount());
			pstmt.setLong(8, metricsData.getWorkerRecords());
			pstmt.setLong(9, metricsData.getProcRecords());
			pstmt.setTimestamp(10, new java.sql.Timestamp(new Date().getTime()));

			pstmt.executeUpdate();
			pstmt.close();
			if (!con.getAutoCommit())
				con.commit();
		}
		catch (java.sql.SQLException e) {
			logger.error("SQLException caught during insert ", e);
		}
		catch (Exception e) {
			logger.error("Exception caught during insert", e);
		}
		finally {
			if (con != null) {
				try {
					con.close();
				}
				catch (Exception e) {
					logger.error("Exception caught", e);
				}
			}
		}
	}

	// purge records older than specified number of days
	void purge(int days) {
		logger.info("purge() - purge records older than " + days + " days...");
		java.sql.Connection con = null;
		try {
			con = dataSource.getConnection();
			// prepare for insert of metrics_logger record
			java.sql.PreparedStatement pstmt = 
				con.prepareStatement("delete from metrics_logger"
					+ " where ADD_TIME < ?");
			pstmt.clearParameters();
			Calendar calendar = new GregorianCalendar();
			// calendar.roll(Calendar.DAY_OF_YEAR,-days);
			calendar.add(Calendar.DATE, -days);
			Date go_back = calendar.getTime();
			pstmt.setTimestamp(1, new java.sql.Timestamp(go_back.getTime()));

			int rowsDeleted = pstmt.executeUpdate();
			pstmt.close();
			if (!con.getAutoCommit())
				con.commit();
			logger.info("purge() - records deleted: " + rowsDeleted);
		}
		catch (java.sql.SQLException e) {
			logger.error("SQLException caught during delete", e);
		}
		catch (Exception e) {
			logger.error("Exception caught during delete", e);
		}
		finally {
			try {
				if (con != null) {
					con.close();
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}
		}
	}

	/**
	 * get server metrics data for the last 24 hours as a string
	 * 
	 * @return string
	 */
	public String getMetrics() {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(LF);
			sb.append("<==================== Display Server Status ====================>" + LF);
			sb.append("Server Name: " + metricsData.getServerName() + ", Id: "
					+ metricsData.getServerId() + LF);
			sb.append("Start Time: " + startTime + ", Last Update: " + lastUpdt + LF);
			sb.append("Total  inputs: " + (metricsData.getInputCount()) + LF);
			sb.append("Total outputs: " + (metricsData.getOutputCount()) + LF);
			sb.append("Total  errors: " + (metricsData.getErrorCount()) + LF);
			float avg_workers = 0;
			if (metricsData.getWorkerRecords() > 0) { // just for safety
				avg_workers = (float) ((double) (metricsData.getWorkerCount()) / (metricsData
						.getWorkerRecords()));
			}
			sb.append("Total   Process Time: " + (metricsData.getTimeCount()) + LF);
			float avg_time = 0;
			if (metricsData.getProcRecords() > 0) { // just for safety
				avg_time = (float) ((double) (metricsData.getTimeCount()) / (metricsData
						.getProcRecords()));
			}
			else {
				avg_time = (float) metricsData.getTimeCount();
			}
			sb.append("Average Process Time: " + avg_time + LF);
			sb.append("Average # of Workers: " + avg_workers + LF);
			if (avg_workers > 0) {
				sb.append("Server Throughput   : " + (avg_time / avg_workers) + LF);
			}
			sb.append("<==================== End of Display Status ====================>" + LF);
			sb.append(LF);
		}
		catch (Exception e) {
			logger.error("Exception caught during getStatus()", e);
		}
		return sb.toString();
	}

	/**
	 * get all servers' metrics data for the last 24 hours as a string
	 * 
	 * @return string
	 */
	public String getAllMetrics() {
		return getAllMetrics(1);
	}

	/**
	 * get all servers' metrics data for the past number of days as a string
	 * 
	 * @param days -
	 *            number days to be reported
	 * @return string
	 */
	public String getAllMetrics(int days) {
		if (dataSource == null) {
			return "Data source form MetricsLogger is not present.";
		}
		long total_input = 0L, total_output = 0L, total_error = 0L;
		long total_worker = 0L, total_worker_count = 0L;
		long total_proctime = 0L, total_proctime_count = 0L;
		int rows = 0;

		StringBuffer sb = new StringBuffer();
		java.sql.Connection con = null;
		java.sql.ResultSet rs = null;
		try {
			con = dataSource.getConnection();
			// prepare for insert of metrics_logger record
			java.sql.PreparedStatement pstmt = 
				con.prepareStatement("select * from metrics_logger"
					+ " where SERVER_NAME = ?" + " and SERVER_ID = ?"
					+ " and ADD_TIME > ?"
					+ " order by SERVER_NAME,SERVER_ID,ADD_TIME desc");
			pstmt.clearParameters();
			pstmt.setString(1, metricsData.getServerName());
			pstmt.setString(2, metricsData.getServerId());

			GregorianCalendar calendar = new GregorianCalendar();
			calendar.add(Calendar.DATE, -days);
			Date go_back = calendar.getTime();
			pstmt.setTimestamp(3, new java.sql.Timestamp(go_back.getTime()));
			rs = pstmt.executeQuery();

			sb.append("<br><div align=\"center\"><font size=\"+2\"><b>Key ReMetrics for "
							+ metricsData.getServerName()
							+ "/"
							+ metricsData.getServerId()
							+ "<b></font></div>"
							+ LF);
			sb.append("<table border=\"1\">" + LF);
			sb.append("<tr>" + LF);
			sb.append("<th width=10%>" + "# of Inputs" + "</th>" + LF);
			sb.append("<th width=10%>" + "# of Outputs" + "</th>" + LF);
			sb.append("<th width=10%>" + "# of Errors" + "</th>" + LF);
			sb.append("<th width=10%>" + "Process Time" + "</th>" + LF);
			sb.append("<th width=15%>" + "Average Proc Time" + "</th>" + LF);
			sb.append("<th width=15%>" + "Average # of Workers" + "</th>" + LF);
			sb.append("<th width=30%>" + "Time Inteval" + "</th>" + LF);
			sb.append("</tr>" + LF);
			while (rs.next()) {
				long input = rs.getLong("INPUT_TOTAL");
				long output = rs.getLong("OUTPUT_TOTAL");
				long error = rs.getLong("ERROR_TOTAL");
				long worker = rs.getLong("WORKER_TOTAL");
				long proctime = rs.getLong("PROCTIME_TOTAL");
				long worker_count = rs.getLong("WORKER_COUNT");
				long proctime_count = rs.getLong("PROCTIME_COUNT");
				java.sql.Timestamp add_time = rs.getTimestamp("ADD_TIME");
				sb.append("<tr>" + LF);
				sb.append("<td>" + input + "</td>" + LF);
				sb.append("<td>" + output + "</td>" + LF);
				sb.append("<td>" + error + "</td>" + LF);
				sb.append("<td>" + proctime + "</td>" + LF);
				double avg_workers = 0;
				if (worker_count > 0) {
					avg_workers = (double) worker / (double) worker_count;
				}
				double avg_time = 0;
				if (proctime_count > 1) {
					avg_time = (double) proctime / (double) proctime_count;
				}
				else {
					avg_time = proctime;
				}
				// if (avg_workers>1) avg_time=avg_time/avg_workers;
				sb.append("<td>" + avg_time + "</td>" + LF);
				sb.append("<td>" + avg_workers + "</td>" + LF);
				sb.append("<td>" + add_time + "</td>" + LF);
				sb.append("</tr>" + LF);
				rows++;
				total_input += input;
				total_output += output;
				total_error += error;
				total_proctime += proctime;
				total_proctime_count += proctime_count;
				total_worker += worker;
				total_worker_count += worker_count;
			}
			if (rows > 0) { // show totals
				sb.append("<tr>" + LF);
				sb.append("<td><b>" + total_input + "</b></td>" + LF);
				sb.append("<td><b>" + total_output + "</b></td>" + LF);
				sb.append("<td><b>" + total_error + "</b></td>" + LF);
				sb.append("<td><b>" + total_proctime + "</b></td>" + LF);
				double avg_workers = 0;
				if (total_worker_count > 0) {
					avg_workers = (double) total_worker / (double) total_worker_count;
				}
				double avg_time = 0;
				if (total_proctime_count > 1) {
					avg_time = (double) total_proctime / (double) total_proctime_count;
				}
				else {
					avg_time = total_proctime;
				}
				sb.append("<td><b>" + avg_time + "</b></td>" + LF);
				sb.append("<td><b>" + avg_workers + "</b></td>" + LF);
				sb.append("<td><b>" + "TOTAL" + "</b></td>" + LF);
				sb.append("</tr>" + LF);
			}
			sb.append("</table>" + LF);
			rs.close();
			pstmt.close();
			if (!con.getAutoCommit())
				con.commit();
		}
		catch (java.sql.SQLException e) {
			logger.error("SQLException caught during select", e);
		}
		catch (Exception e) {
			logger.error("Exception caught during select", e);
		}
		finally {
			try {
				if (con != null) {
					con.close();
				}
			}
			catch (Exception e) {
			}
		}
		return sb.toString();
	}

	public MetricsData getMetricsData() {
		return metricsData;
	}
}