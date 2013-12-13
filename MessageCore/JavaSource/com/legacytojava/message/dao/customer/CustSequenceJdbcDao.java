package com.legacytojava.message.dao.customer;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.legacytojava.message.dao.abstrct.AbstractDao;

@Repository
@Component("custSequenceDao")
public class CustSequenceJdbcDao extends AbstractDao implements CustSequenceDao {
	protected static final Logger logger = Logger.getLogger(CustSequenceJdbcDao.class);
	
	public long findNextValue() {
		/* simulate a sequence table */
		String sql1 = "update CustSequence set seqId = LAST_INSERT_ID(seqId + 1)";
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
			"select max(SeqId) from CustSequence ";
		long currValue = getJdbcTemplate().queryForLong(sql);
		sql = "delete from CustSequence";
		getJdbcTemplate().update(sql);
		sql = "insert into CustSequence (SeqId) values(" +(currValue + 1)+ ")";
		getJdbcTemplate().update(sql);
		sql = "select LAST_INSERT_ID()";
		long nextValue = getJdbcTemplate().queryForLong(sql);
		return nextValue;
	}
}
