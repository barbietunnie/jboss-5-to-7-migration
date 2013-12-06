package com.es.dao.subscriber;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.es.dao.abst.AbstractDao;

@Repository
@Component("subrSequenceDao")
public class SubrSequenceDao extends AbstractDao {
	protected static final Logger logger = Logger.getLogger(SubrSequenceDao.class);
	
	public long findNextValue() {
		/* simulate a sequence table */
		String sql1 = "update Subr_Sequence set seqId = LAST_INSERT_ID(seqId + 1)";
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
			"select max(SeqId) from Subr_Sequence ";
		long currValue = getJdbcTemplate().queryForObject(sql, Long.class);
		sql = "delete from CustSequence";
		getJdbcTemplate().update(sql);
		sql = "insert into Subr_Sequence (SeqId) values(" +(currValue + 1)+ ")";
		getJdbcTemplate().update(sql);
		sql = "select LAST_INSERT_ID()";
		long nextValue = getJdbcTemplate().queryForObject(sql, Long.class);
		return nextValue;
	}
}
