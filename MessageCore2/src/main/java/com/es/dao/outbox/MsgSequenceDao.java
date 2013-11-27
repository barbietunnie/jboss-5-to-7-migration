package com.es.dao.outbox;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("msgSequenceDao")
public class MsgSequenceDao {
	protected static final Logger logger = Logger.getLogger(MsgSequenceDao.class);
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public long findNextValue() {
		/* simulate a sequence table */
		String sql1 = "update Msg_Sequence set seqId = LAST_INSERT_ID(seqId + 1)";
		String sql2 = "select LAST_INSERT_ID()";
		try {
			getJdbcTemplate().update(sql1);
			long nextValue = getJdbcTemplate().queryForObject(sql2, Long.class);
			return nextValue;
		}
		catch (Exception e) {
			logger.error("Exception caught, repair the table.", e);
			return repair();
		}
	}
	
	/*
	 * perform delete and insert to eliminate multiple rows
	 */
	private long repair() {
		logger.info("repair() - perform delete and insert...");
		String sql = 
			"select max(SeqId) from Msg_Sequence ";
		long currValue = getJdbcTemplate().queryForObject(sql, Long.class);
		sql = "delete from Msg_Sequence";
		getJdbcTemplate().update(sql);
		sql = "insert into Msg_Sequence (SeqId) values(" +(currValue + 1)+ ")";
		getJdbcTemplate().update(sql);
		sql = "select LAST_INSERT_ID()";
		long nextValue = getJdbcTemplate().queryForObject(sql, Long.class);
		return nextValue;
	}
}
