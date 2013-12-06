package com.es.dao.outbox;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;

@Component("msgSequenceDao")
public class MsgSequenceDao extends AbstractDao {
	protected static final Logger logger = Logger.getLogger(MsgSequenceDao.class);
	
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
