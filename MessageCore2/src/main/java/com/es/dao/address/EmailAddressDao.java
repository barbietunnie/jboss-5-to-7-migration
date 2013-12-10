package com.es.dao.address;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.es.core.util.EmailAddrUtil;
import com.es.core.util.SpringUtil;
import com.es.core.util.StringUtil;
import com.es.dao.abst.AbstractDao;
import com.es.data.constant.CodeType;
import com.es.data.constant.Constants;
import com.es.data.constant.StatusId;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.comm.PagingVo;

@Component("emailAddressDao")
public class EmailAddressDao extends AbstractDao {
	static final Logger logger = Logger.getLogger(EmailAddressDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	public EmailAddressVo getByAddrId(long addrId) {
		String sql = "select * from Email_Address where emailAddrId=?";
		Object[] parms = new Object[] { addrId };
		try {
			EmailAddressVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public EmailAddressVo getByAddress(String address) {
		String sql = "select * from Email_Address where EmailAddr=?";
		String emailAddress = EmailAddrUtil.removeDisplayName(address);
		Object[] parms = new Object[] { emailAddress };
		try {
			EmailAddressVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<EmailAddressVo> getByAddressDomain(String domain) {
		return getByAddressPattern(domain + "$");
	}
	
	public List<EmailAddressVo> getByAddressUser(String user) {
		return getByAddressPattern("^" + user);
	}
	
	/*
	 * Sample address regex patterns
	 * 1) find by domain name - '@test.com$' or '@yahoo.com'
	 * 2) find by email user name - '^myname@' or 'noreply@'
	 */
	public List<EmailAddressVo> getByAddressPattern(String addressPattern) {
		// MySQL
		String sql = "select t.* from Email_Address t where t.emailAddr REGEXP '" + addressPattern + "' ";
		if (Constants.DB_PRODNAME_PSQL.equalsIgnoreCase(SpringUtil.getDBProductName())) {
			sql = "select t.* from Email_Address t where t.emailAddr ~ '" + addressPattern + "' ";
		}
		else if (Constants.DB_PRODNAME_DERBY.equalsIgnoreCase(SpringUtil.getDBProductName())) {
			String pattern = StringUtils.remove(addressPattern, "^");
			pattern = StringUtils.remove(pattern, "$");
			pattern = StringUtils.removeStart(pattern, "(");
			pattern = StringUtils.removeEnd(pattern, ")");
			sql = "select t.* from Email_Address t " ; //where address like '" + pattern + "' ";
			String whereClause = "";
			StringTokenizer st = new StringTokenizer(pattern, "|");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (addressPattern.startsWith("^")) {
					token = token + "%";
				}
				else if (addressPattern.endsWith("$")) {
					token = "%" + token;
				}
				if (StringUtils.isBlank(whereClause)) {
					whereClause = " where (t.emailAddr like '" + token + "') ";
				}
				else {
					whereClause += " or (t.emailAddr like '" + token + "') ";
				}
			}
			sql += whereClause;
		}
		List<EmailAddressVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		return list;
	}

	public int getEmailAddressCount(PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(vo, parms);
		String sql = "select count(*) from Email_Address a " + whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}

	public List<EmailAddressVo> getEmailAddrsWithPaging(PagingVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(vo, parms);
		/*
		 * paging logic, sort by Email Address
		 */
		String fetchOrder = "asc";
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		} else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getStrIdLast() != null) {
				whereSql += CRIT[parms.size()] + " a.EmailAddr > ? ";
				parms.add(vo.getStrIdLast());
			}
		} else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getStrIdFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.EmailAddr < ? ";
				parms.add(vo.getStrIdFirst());
				fetchOrder = "desc";
			}
		} else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<EmailAddressVo> lastList = new ArrayList<EmailAddressVo>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<EmailAddressVo> nextList = getEmailAddrsWithPaging(vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setStrIdLast(nextList.get(nextList.size() - 1)
							.getEmailAddr());
				} else {
					break;
				}
			}
			return lastList;
		} else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getStrIdFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.EmailAddr >= ? ";
				parms.add(vo.getStrIdFirst());
			}
		}
		String sql = "select a.EmailAddrId, a.EmailAddr, a.OrigEmailAddr, a.StatusId, "
				+ " a.StatusChangeTime, a.StatusChangeUserId, a.BounceCount, "
				+ " a.LastBounceTime, a.LastSentTime, a.LastRcptTime, a.AcceptHtml, "
				+ " a.UpdtTime, a.UpdtUserId, "
				+ " b.SubrId, b.FirstName, b.MiddleName, b.LastName, "
				+ " sum(c.SentCount) as SentCount, sum(c.OpenCount) as OpenCount, "
				+ " sum(c.ClickCount) as ClickCount "
				+ "from Email_Address a "
				+ " LEFT OUTER JOIN Subscriber b on a.EmailAddrId=b.EmailAddrId "
				+ " LEFT OUTER JOIN Subscription c on a.EmailAddrId=c.EmailAddrId "
				+ whereSql
				+ "group by "
				+ " a.EmailAddrId, a.EmailAddr, a.OrigEmailAddr, a.StatusId, a.StatusChangeTime, "
				+ " a.StatusChangeUserId, a.BounceCount, a.LastBounceTime, a.LastSentTime, "
				+ " a.LastRcptTime, a.AcceptHtml, a.UpdtTime, a.UpdtUserId, "
				+ " b.SubrId, b.FirstName, b.MiddleName, b.LastName "
				+ " order by a.EmailAddr "
				+ fetchOrder
				+ " limit "
				+ vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<EmailAddressVo> list = getJdbcTemplate().query(sql,
				parms.toArray(), new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ",
			" and ", " and ", " and ", " and ", " and ", " and " };

	private String buildWhereClause(PagingVo vo, List<Object> parms) {
		String whereSql = "";
		if (!StringUtils.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " a.StatusId = ? ";
			parms.add(vo.getStatusId());
		} else { // make sure parms.size() is greater than zero
			whereSql += CRIT[parms.size()] + " a.StatusId >= ? ";
			parms.add("");
		}
		// search by address
		if (vo.getSearchString() != null
				&& vo.getSearchString().trim().length() > 0) {
			String addr = vo.getSearchString().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " a.OrigEmailAddr LIKE '%"
						+ addr + "%' ";
			} else {
				String regex = StringUtil.replaceAll(addr, " ", ".+");
				whereSql += CRIT[parms.size()] + " a.OrigEmailAddr REGEXP '"
						+ regex + "' ";
			}
		}
		return whereSql;
	}

	public long getEmailAddrIdForPreview() {
		String sql = "SELECT Ceil(max(e.emailaddrid) * RAND()) as emailaddrid "
				+ " FROM email_address e, subscriber c "
				+ " where e.emailaddrid=c.emailaddrid ";
		Long emailAddrId = getJdbcTemplate().queryForObject(sql, Long.class);
		if (emailAddrId == null || emailAddrId == 0) {
			sql = "SELECT Ceil(max(e.emailaddrid) * RAND()) as emailaddrid "
					+ " FROM email_address e ";
			emailAddrId = getJdbcTemplate().queryForObject(sql, Long.class);
		}
		return emailAddrId;
	}

	/**
	 * find by email address. if it does not exist, add it to database.
	 */
	// @Transactional(isolation=Isolation.REPEATABLE_READ,propagation=Propagation.REQUIRED)
	/*
	 * Standard JPA does not support custom isolation levels.
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	/*
	 * Just did the test. The Isolation level is still set to READ COMMITTED
	 * inside the DAO method with @Transactional set as you described it. JDBC
	 * Statement javadocs also states that isolation level can only be set when
	 * the transaction is started, not after. For now I've just created a method
	 * that check for the isolation level and throws a RuntimeException if it's
	 * not SERIALIZABLE. But this requires an extra trip to the DB to query the
	 * current isolation level. � David Parks
	 */
	public EmailAddressVo findSertAddress(String address) {
		return findByAddress(address, 0);
		// return findByAddressSP(address);
	}

	private static Object lock = new Object();

	/*
	 * We can also apply "synchronized" at method level, and that will eliminate
	 * concurrency issue as this class is a singleton. (!!!Tried under JBoss and
	 * it failed to eliminate concurrency issue for some reason.)
	 * 
	 * Update: I was running JUnit tests (SendMailBoTest.java) from another JVM
	 * to feed the MailSender queue which is consumed by MailSender MDB running
	 * in JBoss. So the concurrency issue was really happening between two JVMs,
	 * that's why "synchronized" block did not work.
	 * 
	 * Update: running MailSenderListener from JBoss, multiple MDB sessions were
	 * started, and each MDB session probably had its own instance as the method
	 * was obviously running in parallel, applying "synchronized" at the method
	 * level did not help. The JMS messages were then rolled back by MDB, and
	 * were processed successfully after one or two re-deliveries.
	 */
	private EmailAddressVo findByAddress(String address, int retries) {
		EmailAddressVo vo = getByAddress(address);
		if (vo != null) {
			return vo;
		}
		// record not found, insert one
		synchronized (lock) {
			// concurrency issue still pops up but it is much better
			// controlled
			Timestamp updtTime = new Timestamp(System.currentTimeMillis());
			EmailAddressVo emailAddrVo = new EmailAddressVo();
			emailAddrVo.setEmailAddr(address);
			emailAddrVo.setBounceCount(0);
			emailAddrVo.setStatusId(StatusId.ACTIVE.getValue());
			emailAddrVo.setStatusChangeTime(updtTime);
			emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
			emailAddrVo.setAcceptHtml(CodeType.YES_CODE.getValue());
			emailAddrVo.setUpdtTime(updtTime);
			emailAddrVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
			try {
				insert(emailAddrVo);
				return emailAddrVo;
			}
			catch (DuplicateKeyException e) {
				logger.error("findByAddress() - DuplicateKeyException caught", e);
				if (retries < 0) {
					// retry once may overcome concurrency issue. (the retry
					// never worked and the reason might be that it is under
					// a same transaction). So no retry from now on.
					logger.info("findByAddress() - duplicate key error, retry...");
					return findByAddress(address, retries + 1);
				} else {
					throw e;
				}
			}
			catch (DataIntegrityViolationException e) {
				// shouldn't reach here, should be caught by DuplicateKeyException
				logger.error("findByAddress() - DataIntegrityViolationException caught", e);
				String err = e.toString() + "";
				if (err.toLowerCase().indexOf("duplicate entry") > 0
						&& retries < 0) {
					// retry once may overcome concurrency issue. (the retry
					// never worked and the reason might be that it is under
					// a same transaction). So no retry from now on.
					logger.info("findByAddress() - duplicate key error, retry...");
					return findByAddress(address, retries + 1);
				} else {
					throw e;
				}
			}
		} // end of synchronized block
	}

	EmailAddressVo findByAddress_deadlock(String address, int retries) {
		EmailAddressVo vo = getByAddress(address);
		if (vo != null) {
			return vo;
		}
		// record not found, insert one
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		EmailAddressVo emailAddrVo = new EmailAddressVo();
		emailAddrVo.setEmailAddr(address);
		emailAddrVo.setBounceCount(0);
		emailAddrVo.setStatusId(StatusId.ACTIVE.getValue());
		emailAddrVo.setStatusChangeTime(updtTime);
		emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
		emailAddrVo.setAcceptHtml(CodeType.YES_CODE.getValue());
		emailAddrVo.setUpdtTime(updtTime);
		emailAddrVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		// concurrency issue still pops up but it is much better controlled
		synchronized (lock) {
			insert(emailAddrVo, true);
			// return emailAddrVo;
		} // end of synchronized block
		return getByAddress(address);
	}

	private static class FindByAddressProcedure extends StoredProcedure {
		public FindByAddressProcedure(DataSource dataSource, String sprocName) {
			super(dataSource, sprocName);
			// declareParameter(new SqlReturnResultSet("rs", new
			// EmailAddrMapper()));
			declareParameter(new SqlParameter("iEmailAddr", Types.VARCHAR));
			declareParameter(new SqlParameter("iOrigEmailAddr", Types.VARCHAR));
			declareParameter(new SqlOutParameter("oEmailAddrId", Types.DECIMAL));
			declareParameter(new SqlOutParameter("oEmailAddr", Types.VARCHAR));
			declareParameter(new SqlOutParameter("oOrigEmailAddr",
					Types.VARCHAR));
			declareParameter(new SqlOutParameter("oStatusId", Types.CHAR));
			declareParameter(new SqlOutParameter("oStatusChangeTime",
					Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oStatusChangeUserId",
					Types.VARCHAR));
			declareParameter(new SqlOutParameter("oBounceCount", Types.INTEGER));
			declareParameter(new SqlOutParameter("oLastBounceTime",
					Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oLastSentTime",
					Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oLastRcptTime",
					Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oAcceptHtml", Types.CHAR));
			declareParameter(new SqlOutParameter("oUpdtTime", Types.TIMESTAMP));
			declareParameter(new SqlOutParameter("oUpdtUserId", Types.VARCHAR));
			compile();
		}

		public Map<String, Object> execute(String address) {
			Map<String, Object> inputs = new HashMap<String, Object>();
			inputs.put("iEmailAddr", EmailAddrUtil.removeDisplayName(address));
			inputs.put("iOrigEmailAddr", address);
			Map<String, Object> output = super.execute(inputs);
			return output;
		}
	}

	/*
	 * The stored procedure did not resolve the "Duplicate Key" problem.
	 */
	EmailAddressVo findByAddressSP(String address) {
		FindByAddressProcedure sp = new FindByAddressProcedure(
				getJdbcTemplate().getDataSource(), "FindByAddress");
		Map<String, Object> map = sp.execute(address);
		EmailAddressVo vo = new EmailAddressVo();
		vo.setEmailAddrId(((BigDecimal) map.get("oEmailAddrId")).longValue());
		vo.setEmailAddr((String) map.get("oOrigEmailAddr"));
		vo.setStatusId((String) map.get("oStatusId"));
		vo.setStatusChangeTime((Timestamp) map.get("oStatusChangeTime"));
		vo.setStatusChangeUserId((String) map.get("oStatusChangeUserId"));
		vo.setBounceCount(((Integer) map.get("oBounceCount")));
		vo.setLastBounceTime((Timestamp) map.get("oLastBounceTime"));
		vo.setLastSentTime((Timestamp) map.get("oLastSentTime"));
		vo.setLastRcptTime((Timestamp) map.get("oLastRcptTime"));
		vo.setAcceptHtml((String) map.get("oAcceptHtml"));
		vo.setUpdtTime((Timestamp) map.get("oUpdtTime"));
		vo.setUpdtUserId((String) map.get("oUpdtUserId"));

		vo.setCurrEmailAddr(vo.getEmailAddr());
		vo.setOrigUpdtTime(vo.getUpdtTime());
		return vo;
	}

	public EmailAddressVo getFromByMsgRefId(Long msgRefId) {
		String sql = "select a.*, b.RuleName "
				+ " from "
				+ " Email_Address a "
				+ " inner join Msg_Inbox b on a.EmailAddrId = b.FromAddrId "
				+ " where"
				+ " b.MsgId = ?";
		Object[] parms = new Object[] { msgRefId };
		List<EmailAddressVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public EmailAddressVo getToByMsgRefId(Long msgRefId) {
		String sql = "select a.*, b.RuleName "
				+ " from "
				+ " Email_Address a "
				+ " inner join Msg_Inbox b on a.EmailAddrId = b.ToAddrId "
				+ " where"
				+ " b.MsgId = ?";
		Object[] parms = new Object[] { msgRefId };
		List<EmailAddressVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<EmailAddressVo>(EmailAddressVo.class));
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public EmailAddressVo saveEmailAddress(String address) {
		return findSertAddress(address);
	}

	public int update(EmailAddressVo emailAddrVo) {
		emailAddrVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));

		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(EmailAddrUtil.removeDisplayName(emailAddrVo.getEmailAddr()));
		keys.add(emailAddrVo.getEmailAddr());
		keys.add(emailAddrVo.getStatusId());
		keys.add(emailAddrVo.getStatusChangeTime());
		keys.add(emailAddrVo.getStatusChangeUserId());
		keys.add(emailAddrVo.getBounceCount());
		keys.add(emailAddrVo.getLastBounceTime());
		keys.add(emailAddrVo.getLastSentTime());
		keys.add(emailAddrVo.getLastRcptTime());
		keys.add(emailAddrVo.getAcceptHtml());
		keys.add(emailAddrVo.getUpdtTime());
		keys.add(emailAddrVo.getUpdtUserId());
		keys.add(emailAddrVo.getEmailAddrId());

		String sql = "update Email_Address set "
				+ "EmailAddr=?,"
				+ "OrigEmailAddr=?,"
				+ "StatusId=?," 
				+ "StatusChangeTime=?,"
				+ "StatusChangeUserId=?,"
				+ "BounceCount=?,"
				+ "LastBounceTime=?,"
				+ "LastSentTime=?,"
				+ "LastRcptTime=?,"
				+ "AcceptHtml=?,"
				+ "UpdtTime=?,"
				+ "UpdtUserId=? "
				+ " where emailAddrId=?";

		if (emailAddrVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(emailAddrVo.getOrigUpdtTime());
		}
		Object[] parms = keys.toArray();

		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		emailAddrVo.setCurrEmailAddr(emailAddrVo.getEmailAddr());
		emailAddrVo.setOrigUpdtTime(emailAddrVo.getUpdtTime());
		return rowsUpadted;
	}

	public int updateLastRcptTime(long addrId) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(new Timestamp(System.currentTimeMillis()));
		keys.add(addrId);

		String sql = "update Email_Address set "
				+ " LastRcptTime=? "
				+ " where emailAddrId=?";

		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}

	public int updateLastSentTime(long addrId) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(new Timestamp(System.currentTimeMillis()));
		keys.add(addrId);

		String sql = "update Email_Address set "
				+ " LastSentTime=? "
				+ " where emailAddrId=?";

		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}

	public int updateAcceptHtml(long addrId, boolean acceptHtml) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(acceptHtml ? CodeType.YES_CODE.getValue() : CodeType.NO_CODE.getValue());
		keys.add(addrId);

		String sql = "update Email_Address set "
				+ " AcceptHtml=? "
				+ " where emailAddrId=?";

		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		return rowsUpadted;
	}

	public int updateBounceCount(EmailAddressVo emailAddrVo) {
		emailAddrVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		List<Object> keys = new ArrayList<Object>();

		String sql = "update Email_Address set BounceCount=?,";
		emailAddrVo.setBounceCount(emailAddrVo.getBounceCount() + 1);
		keys.add(emailAddrVo.getBounceCount());

		if (emailAddrVo.getBounceCount() > Constants.BOUNCE_SUSPEND_THRESHOLD) {
			if (!StatusId.SUSPENDED.getValue().equals(emailAddrVo.getStatusId())) {
				emailAddrVo.setStatusId(StatusId.SUSPENDED.getValue());
				if (!StringUtils.isEmpty(emailAddrVo.getUpdtUserId())) {
					emailAddrVo.setStatusChangeUserId(emailAddrVo.getUpdtUserId());
				} else {
					emailAddrVo.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
				}
				emailAddrVo.setStatusChangeTime(emailAddrVo.getUpdtTime());
				sql += "StatusId=?,"
						+ "StatusChangeUserId=?,"
						+ "StatusChangeTime=?,";
				keys.add(emailAddrVo.getStatusId());
				keys.add(emailAddrVo.getStatusChangeUserId());
				keys.add(emailAddrVo.getStatusChangeTime());
			}
		}

		sql += "UpdtTime=?,"
				+ "UpdtUserId=? "
				+ " where emailAddrId=?";

		keys.add(emailAddrVo.getUpdtTime());
		keys.add(emailAddrVo.getUpdtUserId());
		keys.add(emailAddrVo.getEmailAddrId());

		if (emailAddrVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(emailAddrVo.getOrigUpdtTime());
		}

		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
		emailAddrVo.setOrigUpdtTime(emailAddrVo.getUpdtTime());
		return rowsUpadted;
	}

	public int deleteByAddrId(long addrId) {
		String sql = "delete from Email_Address where emailAddrId=?";
		Object[] parms = new Object[] { addrId + "" };
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}

	public int deleteByAddress(String address) {
		String sql = "delete from Email_Address where emailAddr=?";
		Object[] parms = new Object[] { address };
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}

	public int insert(EmailAddressVo emailAddrVo) {
		return insert(emailAddrVo, false);
	}

	/*
	 * When "withUpdate" is true, the SQL will perform update if record already
	 * exists, but the "update" will not return the RowId. Use with caution.
	 */
	private int insert(EmailAddressVo emailAddrVo, boolean withUpdate) {
		emailAddrVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(EmailAddrUtil.removeDisplayName(emailAddrVo.getEmailAddr()));
		keys.add(emailAddrVo.getEmailAddr());
		keys.add(emailAddrVo.getStatusId());
		keys.add(emailAddrVo.getStatusChangeTime());
		keys.add(emailAddrVo.getStatusChangeUserId());
		keys.add(emailAddrVo.getBounceCount());
		keys.add(emailAddrVo.getLastBounceTime());
		keys.add(emailAddrVo.getLastSentTime());
		keys.add(emailAddrVo.getLastRcptTime());
		keys.add(emailAddrVo.getAcceptHtml());
		keys.add(emailAddrVo.getUpdtTime());
		keys.add(emailAddrVo.getUpdtUserId());

		String sql = "INSERT INTO Email_Address ("
				+ "EmailAddr,"
				+ "OrigEmailAddr,"
				+ "StatusId,"
				+ "StatusChangeTime,"
				+ "StatusChangeUserId,"
				+ "BounceCount,"
				+ "LastBounceTime,"
				+ "LastSentTime,"
				+ "LastRcptTime,"
				+ "AcceptHtml,"
				+ "UpdtTime,"
				+ "UpdtUserId "
				+ ") VALUES ("
				+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
				+ ",?, ? "
				+ ")";

		if (withUpdate) {
			sql += " ON duplicate KEY UPDATE UpdtTime=?";
			keys.add(emailAddrVo.getUpdtTime());
		}

		int rowsInserted = getJdbcTemplate().update(sql, keys.toArray());
		emailAddrVo.setEmailAddrId(retrieveRowId());
		emailAddrVo.setCurrEmailAddr(emailAddrVo.getEmailAddr());
		emailAddrVo.setOrigUpdtTime(emailAddrVo.getUpdtTime());
		return rowsInserted;
	}

	/**
	 * use KeyHolder to get generated Row Id.
	 */
	public int insert_keyholder(final EmailAddressVo emailAddrVo,
			final boolean withUpdate) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int rowsInserted = getJdbcTemplate().update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(
					Connection connection) throws SQLException {
				emailAddrVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
				ArrayList<Object> keys = new ArrayList<Object>();
				keys.add(EmailAddrUtil.removeDisplayName(emailAddrVo.getEmailAddr()));
				keys.add(emailAddrVo.getEmailAddr());
				keys.add(emailAddrVo.getStatusId());
				keys.add(emailAddrVo.getStatusChangeTime());
				keys.add(emailAddrVo.getStatusChangeUserId());
				keys.add(emailAddrVo.getBounceCount());
				keys.add(emailAddrVo.getLastBounceTime());
				keys.add(emailAddrVo.getLastSentTime());
				keys.add(emailAddrVo.getLastRcptTime());
				keys.add(emailAddrVo.getAcceptHtml());
				keys.add(emailAddrVo.getUpdtTime());
				keys.add(emailAddrVo.getUpdtUserId());

				String sql = "INSERT INTO Email_Address (" 
						+ "EmailAddr,"
						+ "OrigEmailAddr,"
						+ "StatusId,"
						+ "StatusChangeTime,"
						+ "StatusChangeUserId,"
						+ "BounceCount,"
						+ "LastBounceTime,"
						+ "LastSentTime,"
						+ "LastRcptTime,"
						+ "AcceptHtml,"
						+ "UpdtTime,"
						+ "UpdtUserId "
						+ ") VALUES ("
						+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
						+ ",?, ? "
						+ ")";
				if (withUpdate) {
					sql += " ON duplicate KEY UPDATE UpdtTime=?";
					keys.add(emailAddrVo.getUpdtTime());
				}
				PreparedStatement ps = connection.prepareStatement(sql,
						new String[] { "id" });
				for (int i = 0; i < keys.size(); i++) {
					ps.setObject(i + 1, keys.get(i));
				}
				return ps;
			}
		}, keyHolder);
		Number number = keyHolder.getKey();
		emailAddrVo.setEmailAddrId(number.longValue());
		emailAddrVo.setCurrEmailAddr(emailAddrVo.getEmailAddr());
		emailAddrVo.setOrigUpdtTime(emailAddrVo.getUpdtTime());
		return rowsInserted;
	}
}
