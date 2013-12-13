package com.legacytojava.message.dao.outbox;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.legacytojava.message.dao.abstrct.AbstractDao;

@Component("msgSequenceDao")
public class MsgSequenceJdbcDao extends AbstractDao implements MsgSequenceDao {
	protected static final Logger logger = Logger.getLogger(MsgSequenceJdbcDao.class);
	
	public long findNextValue() {
		/* simulate a sequence table */
		String sql1 = "update MsgSequence set seqId = LAST_INSERT_ID(seqId + 1)";
		String sql2 = "select LAST_INSERT_ID()";
		try {
			getJdbcTemplate().update(sql1);
			long nextValue = getJdbcTemplate().queryForLong(sql2);
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
			"select max(SeqId) from MsgSequence ";
		long currValue = getJdbcTemplate().queryForLong(sql);
		sql = "delete from MsgSequence";
		getJdbcTemplate().update(sql);
		sql = "insert into MsgSequence (SeqId) values(" +(currValue + 1)+ ")";
		getJdbcTemplate().update(sql);
		sql = "select LAST_INSERT_ID()";
		long nextValue = getJdbcTemplate().queryForLong(sql);
		return nextValue;
	}
}
