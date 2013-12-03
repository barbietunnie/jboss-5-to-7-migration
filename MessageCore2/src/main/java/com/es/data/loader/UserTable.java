package com.es.data.loader;
import java.sql.Timestamp;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.dao.user.SessionUploadDao;
import com.es.dao.user.UserDataDao;
import com.es.data.constant.Constants;
import com.es.data.constant.StatusId;
import com.es.vo.comm.SessionUploadVo;
import com.es.vo.comm.UserDataVo;

public class UserTable extends AbstractTableBase {
	
	public void dropTables() {
		try {
			getJdbcTemplate().execute("DROP TABLE SESSION_DATA");
			System.out.println("Dropped SESSION_DATA Table...");
		} catch (DataAccessException e) {}
		try {
			getJdbcTemplate().execute("DROP TABLE SESSION_UPLOAD");
			System.out.println("Dropped SESSION_UPLOAD Table...");
		} catch (DataAccessException e) {}
		try {
			getJdbcTemplate().execute("DROP TABLE USER_DATA");
			System.out.println("Dropped USER_DATA Table...");
		} catch (DataAccessException e) {}
	}
	
	public void createTables() throws DataAccessException
	{
		createUserTable();
		createSessionTable();
		createSessionUploadTable();
	}

	void createUserTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE USER_DATA ( " +
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
			"SenderId varchar(16) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (SenderId) REFERENCES Sender_Data(SenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"Constraint UNIQUE INDEX (UserId) " +
			") ENGINE=InnoDB");
			System.out.println("Created USER_DATA Table...");
		} catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createSessionTable() throws DataAccessException {
		try	{
			getJdbcTemplate().execute("CREATE TABLE SESSION_DATA ( " +
			"SessionId varchar(50) NOT NULL, " + 
			"SessionName varchar(50), " +
			"SessionValue text, " +
			"UserId varchar(10) NOT NULL, " +
			"CreateTime datetime NOT NULL, " +
			"INDEX (UserId), " +
			"UNIQUE INDEX (SessionId, SessionName) " +
			") ENGINE=InnoDB");
			System.out.println("Created SESSION_DATA Table...");
		} catch (DataAccessException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	void createSessionUploadTable() throws DataAccessException {
		try {
			getJdbcTemplate().execute("CREATE TABLE SESSION_UPLOAD ( " +
			"SessionId varchar(50) NOT NULL, " + 
			"SessionSeq int NOT NULL, " +
			"FileName varchar(100) NOT NULL, " +
			"ContentType varchar(100), " +
			"UserId varchar(10) NOT NULL, " +
			"CreateTime datetime NOT NULL, " +
			"SessionValue mediumblob, " +
			"INDEX (UserId), " +
			"UNIQUE INDEX (SessionId, SessionSeq) " +
			") ENGINE=InnoDB");
			System.out.println("Created SESSION_UPLOAD Table...");
		} catch (DataAccessException e)
		{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	private void loadUserData() {
		UserDataDao service = SpringUtil.getAppContext().getBean(UserDataDao.class);
		UserDataVo data = new UserDataVo();
		data.setSenderId(Constants.DEFAULT_SENDER_ID);
		data.setUserId("admin");
		data.setPassword("admin");
		data.setFirstName("default");
		data.setLastName("admin");
		data.setCreateTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setRole(Constants.ADMIN_ROLE);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new UserDataVo();
		data.setSenderId(Constants.DEFAULT_SENDER_ID);
		data.setUserId(getProperty("user.id.1"));
		data.setPassword(getProperty("user.password.1"));
		data.setFirstName("default");
		data.setLastName("user");
		data.setCreateTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setRole(Constants.USER_ROLE);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		System.out.println("EntityManager persisted the record.");
	}

	private void loadSessionUpload() {
		SessionUploadDao uploadService = SpringUtil.getAppContext().getBean(SessionUploadDao.class);

		SessionUploadVo data = new SessionUploadVo();
		data.setSessionId("test_session_id");
		data.setSessionSeq(0);
		data.setFileName("test1.txt");
		data.setContentType("text/plain");
		data.setUserId(getProperty("user.id.1"));
		data.setSessionValue("test upload text 1".getBytes());
		uploadService.insert(data);

		data = new SessionUploadVo();
		data.setSessionId("test_session_id");
		data.setSessionSeq(1);
		data.setFileName("test2.txt");
		data.setContentType("text/plain");
		data.setUserId(getProperty("user.id.1"));
		data.setSessionValue("test upload text 2".getBytes());
		uploadService.insert(data);
	}

	public void loadTestData() throws DataAccessException {
		loadUserData();
		loadSessionUpload();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {
			UserTable ct = new UserTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}