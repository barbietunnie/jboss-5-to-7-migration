package com.es.data.loader;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.dao.smtp.SmtpServerDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.MailServerType;
import com.es.data.constant.StatusId;
import com.es.data.preload.SmtpServerEnum;
import com.es.vo.comm.SmtpConnVo;

public class SmtpTable extends AbstractTableBase {
	
	public void dropTables() {
		try {
			getJdbcTemplate().execute("DROP TABLE MAIL_SENDER_PROPS");
			System.out.println("Dropped MAIL_SENDER_PROPS Table...");
		} catch (DataAccessException e) {}
		try {
			getJdbcTemplate().execute("DROP TABLE SMTP_SERVER");
			System.out.println("Dropped SMTP_SERVER Table...");
		} catch (DataAccessException e) {}
	}
	
	public void createTables() throws DataAccessException {
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
		try {
			getJdbcTemplate().execute("CREATE TABLE SMTP_SERVER ( " +
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
			"StatusId char(1) NOT NULL DEFAULT '" + StatusId.ACTIVE.getValue() + "', " +
			"ServerType varchar(5) DEFAULT '" + MailServerType.SMTP.getValue() + "', " +
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
			System.out.println("Created SMTP_SERVER Table...");
		} catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
		
		/*
		- InternalLoopback - internal email address for loop back
		- ExternalLoopback - external email address for loop back
		- UseTestAddr: yes/no: to override the TO address with the value from TestToAddr
		- TestToAddr: use it as the TO address when UseTestAddr is yes
		*/
		try {
			getJdbcTemplate().execute("CREATE TABLE MAIL_SENDER_PROPS ( " +
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
			"Constraint MAIL_SENDER_PROPS_PK1 primary key (RowId) " +
			") ENGINE=InnoDB");
			System.out.println("Created MAIL_SENDER_PROPS Table...");
		} catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws DataAccessException {
		loadSmtpServers(true);
		loadSmtpServers(false);
		insertMailSenderData();
	}
	
	public void loadReleaseData() throws DataAccessException {
		loadSmtpServers(true);
		insertMailSenderData();
	}
	
	private void loadSmtpServers(boolean prodRelease) {
		SmtpServerDao service = SpringUtil.getAppContext().getBean(SmtpServerDao.class);
		
		for (SmtpServerEnum mc : SmtpServerEnum.values()) {
			if (prodRelease) {
				if (!"localhost".equals(mc.getSmtpHost())) {
					continue;
				}
			}
			else {
				if ("localhost".equals(mc.getSmtpHost())) {
					continue;
				}
			}
			SmtpConnVo in = new SmtpConnVo();
			in.setSmtpHost(mc.getSmtpHost());
			in.setSmtpPort(mc.getSmtpPort());
			in.setServerName(mc.getServerName());
			in.setDescription(mc.getDescription());
			in.setUseSsl(mc.isUseSsl()?CodeType.YES.getValue():CodeType.NO.getValue());
			in.setUserId(mc.getUserId());
			in.setUserPswd(mc.getUserPswd());
			in.setPersistence(mc.isPersistence()?CodeType.YES.getValue():CodeType.NO.getValue());
			in.setStatusId(mc.getStatus().getValue());
			in.setServerType(mc.getServerType().getValue());
			in.setThreads(mc.getNumberOfThreads());
			in.setRetries(mc.getMaximumRetries());
			in.setRetryFreq(mc.getRetryFreq());
			in.setAlertAfter(mc.getAlertAfter());
			in.setAlertLevel(mc.getAlertLevel());
			in.setMessageCount(mc.getMessageCount());
			in.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(in);
		}
		System.out.println("SMTP server records inserted.");
	}

	public void UpdateSmtpData4Prod() throws DataAccessException {
		String sql = "update SMTP_SERVER set StatusId = ? where ServerName = ?";
		try {
			Map<Integer,Object> ps = new LinkedHashMap<Integer, Object>();
			
			ps.put(1, StatusId.INACTIVE.getValue()); // statusid
			ps.put(2, "smtpServer"); // smtpPort
			getJdbcTemplate().update(sql, ps.values().toArray(new Object[]{}));
			
			ps.put(1, StatusId.ACTIVE.getValue()); // statusid
			ps.put(2, "DyndnsMailRelay"); // smtpPort
			getJdbcTemplate().update(sql, ps.values().toArray(new Object[]{}));
			
			ps.put(1, StatusId.INACTIVE.getValue()); // statusid
			ps.put(2, "exchServer"); // smtpPort
			getJdbcTemplate().update(sql, ps.values().toArray(new Object[]{}));
			
			System.out.println("SmtpTable: Smtp records updated.");
		} catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void insertMailSenderData() throws DataAccessException {
		try {
			String sql = 
				"INSERT INTO MAIL_SENDER_PROPS " +
				"(InternalLoopback," +
				"ExternalLoopback," +
				"UseTestAddr," +
				"TestFromAddr, " +
				"TestToAddr," +
				"TestReplytoAddr," +
				"IsVerpEnabled," +
				"UpdtTime," +
				"UpdtUserId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

			Map<Integer,Object> ps = new LinkedHashMap<Integer, Object>();
			
			ps.put(1, "testto@localhost");
			ps.put(2, "inbox@lagacytojava.com");
			ps.put(3, CodeType.YES.getValue());
			ps.put(4, "testfrom@localhost");
			ps.put(5, "testto@localhost");
			ps.put(6, "testreplyto@localhost");
			ps.put(7, CodeType.NO.getValue());
			ps.put(8, new Timestamp(new java.util.Date().getTime()));
			ps.put(9, "SysAdmin");
			getJdbcTemplate().update(sql, ps.values().toArray(new Object[]{}));
			
			System.out.println("MailSender props record inserted.");
		} catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			SmtpTable ct = new SmtpTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}