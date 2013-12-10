package com.es.data.loader;
import java.sql.Timestamp;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;
import com.es.dao.idtoken.IdTokensDao;
import com.es.data.constant.Constants;
import com.es.data.constant.EmailIdToken;
import com.es.vo.comm.IdTokensVo;

public class IdTokensTable extends AbstractTableBase {
	
	public void dropTables() {
		try	{
			getJdbcTemplate().execute("DROP TABLE ID_TOKENS");
			System.out.println("Dropped ID_TOKENS Table...");
		} catch (DataAccessException e) {}
	}
	
	public void createTables() throws DataAccessException {
		try	{
			getJdbcTemplate().execute("CREATE TABLE ID_TOKENS ( " +
			"RowId int AUTO_INCREMENT not null, " +
			"SenderId varchar(16) NOT NULL, " + 
			"Description varchar(100), " +
			"BodyBeginToken varchar(16) NOT NULL, " +
			"BodyEndToken varchar(4) NOT NULL, " +
			"XHeaderName varchar(20), " +
			"XhdrBeginToken varchar(16), " +
			"XhdrEndToken varchar(4), " +
			"MaxLength integer NOT NULL, " +
			"UpdtTime datetime NOT NULL, " +
			"UpdtUserId char(10) NOT NULL, " +
			"PRIMARY KEY (RowId), " +
			"FOREIGN KEY (SenderId) REFERENCES Sender_Data(SenderId) ON DELETE CASCADE ON UPDATE CASCADE, " +
			"Constraint UNIQUE INDEX ID_TOKENS_IDX1 (SenderId) " +
			") ENGINE=InnoDB");
			System.out.println("Created ID_TOKENS Table...");
		} 
		catch (DataAccessException e)	{
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}
	
	public void loadTestData() throws DataAccessException {
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		IdTokensDao itService = SpringUtil.getAppContext().getBean(IdTokensDao.class);

		IdTokensVo in = new IdTokensVo();
		in.setSenderId(Constants.DEFAULT_SENDER_ID);
		in.setDescription("Default SenderId");
		in.setBodyBeginToken(EmailIdToken.BODY_BEGIN);
		in.setBodyEndToken(EmailIdToken.BODY_END);
		in.setXHeaderName(EmailIdToken.XHEADER_NAME);
		in.setXhdrBeginToken(EmailIdToken.XHDR_BEGIN);
		in.setXhdrEndToken(EmailIdToken.XHDR_END);
		in.setMaxLength(EmailIdToken.MAXIMUM_LENGTH);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId("SysAdmin");
		itService.insert(in);
		System.out.println("IdTokens record inserted.");
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try {
			IdTokensTable ct = new IdTokensTable();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}