package com.es.dao.subscriber;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.es.core.util.EmailAddrUtil;
import com.es.core.util.StringUtil;
import com.es.dao.abst.AbstractDao;
import com.es.dao.address.EmailAddressDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.comm.PagingSubscriberVo;
import com.es.vo.comm.PagingVo;
import com.es.vo.comm.SubscriberVo;

@Repository
@Component("subscriberDao")
public class SubscriberDao extends AbstractDao {

	public SubscriberVo getBySubscriberId(String subrId) {
		String sql = 
			"select * " +
				"from Subscriber where subrid=? ";
		
		Object[] parms = new Object[] {subrId};
		try {
			SubscriberVo vo =  getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<SubscriberVo> getBySenderId(String senderId) {
		String sql = 
			"select * " +
				"from Subscriber where senderid=? ";
		Object[] parms = new Object[] {senderId};
		List<SubscriberVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
		return list;
	}
	
	public SubscriberVo getByEmailAddrId(long emailAddrId) {
		String sql = 
			"select * " +
			" from Subscriber where emailAddrId=? ";
		Object[] parms = new Object[] {Long.valueOf(emailAddrId)};
		try {
			SubscriberVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public SubscriberVo getByEmailAddress(String emailAddr) {
		String sql = 
			"select a.* " +
			" from Subscriber a, Email_Address b " +
			" where a.EmailAddrId=b.EmailAddrId " +
			" and a.EmailAddr=? ";
		Object[] parms = new Object[] {EmailAddrUtil.removeDisplayName(emailAddr)};
		try {
			SubscriberVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<SubscriberVo> getAll() {
		String sql = 
			"select * " +
				"from Subscriber";
		
		List<SubscriberVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
		return list;
	}
	
	public int getSubscriberCount(PagingSubscriberVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(vo, parms);
		String sql = 
			"select count(*) from Subscriber a " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForObject(sql, parms.toArray(), Integer.class);
		return rowCount;
	}
	
	public List<SubscriberVo> getSubscribersWithPaging(PagingSubscriberVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(vo, parms);
		/*
		 * paging logic
		 */
		String fetchOrder = "asc";
		if (vo.getPageAction().equals(PagingVo.PageAction.FIRST)) {
			// do nothing
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.NEXT)) {
			if (vo.getStrIdLast() != null) {
				whereSql += CRIT[parms.size()] + " a.SubrId > ? ";
				parms.add(vo.getStrIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getStrIdFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.Subrd < ? ";
				parms.add(vo.getStrIdFirst());
				fetchOrder = "desc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<SubscriberVo> lastList = new ArrayList<SubscriberVo>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<SubscriberVo> nextList = getSubscribersWithPaging(vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setStrIdLast(nextList.get(nextList.size() - 1).getSubrId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getStrIdFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.SubrId >= ? ";
				parms.add(vo.getStrIdFirst());
			}
		}
		String sql = 
			"select a.*, b.StatusId as EmailStatusId, b.BounceCount, b.AcceptHtml " +
			" from Subscriber a " +
				" LEFT OUTER JOIN Email_Address b on a.EmailAddrId=b.EmailAddrId " +
			whereSql +
			" order by a.SubrId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<SubscriberVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			// reverse the list
			Collections.reverse(list);
		}
		return list;
	}

	static String[] CRIT = { " where ", " and ", " and ", " and ", " and ", " and ", " and ",
		" and ", " and ", " and ", " and " };
	
	private String buildWhereClause(PagingSubscriberVo vo, List<Object> parms) {
		String whereSql = "";
		if (!StringUtils.isEmpty(vo.getSenderId())) {
			whereSql += CRIT[parms.size()] + " a.SenderId = ? ";
			parms.add(vo.getSenderId());
		}
		if (!StringUtils.isEmpty(vo.getSsnNumber())) {
			whereSql += CRIT[parms.size()] + " a.SsnNumber = ? ";
			parms.add(vo.getSsnNumber());
		}
		if (!StringUtils.isEmpty(vo.getLastName())) {
			whereSql += CRIT[parms.size()] + " a.LastName = ? ";
			parms.add(vo.getLastName());
		}
		if (!StringUtils.isEmpty(vo.getFirstName())) {
			whereSql += CRIT[parms.size()] + " a.FirstName = ? ";
			parms.add(vo.getFirstName());
		}
		if (!StringUtils.isEmpty(vo.getDayPhone())) {
			whereSql += CRIT[parms.size()] + " a.DayPhone = ? ";
			parms.add(vo.getDayPhone());
		}
		if (!StringUtils.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " a.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		// search by email address
		if (!StringUtils.isEmpty(vo.getSearchString())) {
			String addr = vo.getSearchString().trim();
			if (addr.indexOf(" ") < 0) {
				whereSql += CRIT[parms.size()] + " a.EmailAddr LIKE '%" + addr + "%' ";
			}
			else {
				String regex = StringUtil.replaceAll(addr, " ", ".+");
				whereSql += CRIT[parms.size()] + " a.EmailAddr REGEXP '" + regex + "' ";
			}
		}
		return whereSql;
	}
	
	public int update(SubscriberVo subrVo) {
		subrVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		syncupEmailFields(subrVo);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(subrVo);
		
		String sql = MetaDataUtil.buildUpdateStatement("Subscriber", subrVo);
		if (subrVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		subrVo.setOrigUpdtTime(subrVo.getUpdtTime());
		subrVo.setOrigSubrId(subrVo.getSubrId());
		return rowsUpadted;
	}
	
	public int delete(String subrId) {
		String sql = 
			"delete from Subscriber where subrid=? ";
		
		int rowsDeleted = getJdbcTemplate().update(sql, new Object[] {subrId});
		return rowsDeleted;
	}

	public int deleteByEmailAddr(String emailAddr) {
		EmailAddressVo addrVo = getEmailAddressDao().getByAddress(emailAddr);
		if (addrVo == null) {
			return 0;
		}
		String sql = 
			"delete from Subscriber where EmailAddrId=? ";
		
		int rowsDeleted = getJdbcTemplate().update(sql, new Object[] {addrVo.getEmailAddrId()});
		return rowsDeleted;
	}

	public int insert(SubscriberVo subrVo) {
		subrVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		syncupEmailFields(subrVo);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(subrVo);
		
		String sql = MetaDataUtil.buildInsertStatement("Subscriber", subrVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		
		subrVo.setRowId(retrieveRowId());
		subrVo.setOrigUpdtTime(subrVo.getUpdtTime());
		subrVo.setOrigSubrId(subrVo.getSubrId());
		return rowsInserted;
	}
	
	private void syncupEmailFields(SubscriberVo vo) {
		if (!StringUtils.isEmpty(vo.getEmailAddr())) {
			EmailAddressVo addrVo = getEmailAddressDao().findSertAddress(vo.getEmailAddr());
			vo.setEmailAddrId(addrVo.getEmailAddrId());
		}
		else {
			EmailAddressVo addrVo = getEmailAddressDao().getByAddrId(vo.getEmailAddrId());
			if (addrVo != null) {
				vo.setEmailAddr(addrVo.getEmailAddr());
			}
		}
	}
	
	@Autowired
	private EmailAddressDao emailAddressDao = null;
	private EmailAddressDao getEmailAddressDao() {
		return emailAddressDao;
	}
	
}
