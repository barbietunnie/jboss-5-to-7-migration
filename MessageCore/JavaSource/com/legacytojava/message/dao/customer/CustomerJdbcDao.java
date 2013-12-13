package com.legacytojava.message.dao.customer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.legacytojava.message.dao.abstrct.AbstractDao;
import com.legacytojava.message.dao.abstrct.MetaDataUtil;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.CustomerVo;
import com.legacytojava.message.vo.PagingCustomerVo;
import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

@Repository
@Component("customerDao")
public class CustomerJdbcDao extends AbstractDao implements CustomerDao {

	public CustomerVo getByCustId(String custId) {
		String sql = 
			"select *, CustId as OrigCustId, UpdtTime as OrigUpdtTime " +
				"from Customers where custid=? ";
		
		Object[] parms = new Object[] {custId};
		try {
			CustomerVo vo =  getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<CustomerVo> getByClientId(String clientId) {
		String sql = 
			"select *, CustId as OrigCustId, UpdtTime as OrigUpdtTime " +
				"from Customers where clientid=? ";
		Object[] parms = new Object[] {clientId};
		List<CustomerVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		return list;
	}
	
	public CustomerVo getByEmailAddrId(long emailAddrId) {
		String sql = 
			"select *, CustId as OrigCustId, UpdtTime as OrigUpdtTime " +
			" from customers where emailAddrId=? ";
		Object[] parms = new Object[] {Long.valueOf(emailAddrId)};
		List<CustomerVo> list =getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		if (list == null || list.isEmpty()) {
			return null;
		}
		else {
			return list.get(0);
		}
	}
	
	public CustomerVo getByEmailAddress(String emailAddr) {
		String sql = 
			"select a.*, a.CustId as OrigCustId, a.UpdtTime as OrigUpdtTime " +
			" from Customers a, EmailAddr b " +
			" where a.EmailAddrId=b.EmailAddrId " +
			" and a.EmailAddr=? ";
		Object[] parms = new Object[] {EmailAddrUtil.removeDisplayName(emailAddr)};
		List<CustomerVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		if (list == null || list.isEmpty()) {
			return null;
		}
		else {
			return list.get(0);
		}
	}
	
	public List<CustomerVo> getAll() {
		String sql = 
			"select *, CustId as OrigCustId, UpdtTime as OrigUpdtTime " +
				"from Customers";
		
		List<CustomerVo> list = getJdbcTemplate().query(sql, 
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
		return list;
	}
	
	public int getCustomerCount(PagingCustomerVo vo) {
		List<Object> parms = new ArrayList<Object>();
		String whereSql = buildWhereClause(vo, parms);
		String sql = 
			"select count(*) from Customers a " +
			whereSql;
		int rowCount = getJdbcTemplate().queryForInt(sql, parms.toArray());
		return rowCount;
	}
	
	public List<CustomerVo> getCustomersWithPaging(PagingCustomerVo vo) {
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
				whereSql += CRIT[parms.size()] + " a.CustId > ? ";
				parms.add(vo.getStrIdLast());
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.PREVIOUS)) {
			if (vo.getStrIdFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.CustId < ? ";
				parms.add(vo.getStrIdFirst());
				fetchOrder = "desc";
			}
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.LAST)) {
			List<CustomerVo> lastList = new ArrayList<CustomerVo>();
			vo.setPageAction(PagingVo.PageAction.NEXT);
			while (true) {
				List<CustomerVo> nextList = getCustomersWithPaging(vo);
				if (!nextList.isEmpty()) {
					lastList = nextList;
					vo.setStrIdLast(nextList.get(nextList.size() - 1).getCustId());
				}
				else {
					break;
				}
			}
			return lastList;
		}
		else if (vo.getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			if (vo.getStrIdFirst() != null) {
				whereSql += CRIT[parms.size()] + " a.CustId >= ? ";
				parms.add(vo.getStrIdFirst());
			}
		}
		String sql = 
			"select a.*, a.CustId as OrigCustId, a.UpdtTime as OrigUpdtTime, " +
			"b.StatusId as EmailStatusId, b.BounceCount, b.AcceptHtml " +
			" from Customers a " +
				" LEFT OUTER JOIN EmailAddr b on a.EmailAddrId=b.EmailAddrId " +
			whereSql +
			" order by a.CustId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		List<CustomerVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<CustomerVo>(CustomerVo.class));
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
	
	private String buildWhereClause(PagingCustomerVo vo, List<Object> parms) {
		String whereSql = "";
		if (!StringUtil.isEmpty(vo.getClientId())) {
			whereSql += CRIT[parms.size()] + " a.ClientId = ? ";
			parms.add(vo.getClientId());
		}
		if (!StringUtil.isEmpty(vo.getSsnNumber())) {
			whereSql += CRIT[parms.size()] + " a.SsnNumber = ? ";
			parms.add(vo.getSsnNumber());
		}
		if (!StringUtil.isEmpty(vo.getLastName())) {
			whereSql += CRIT[parms.size()] + " a.LastName = ? ";
			parms.add(vo.getLastName());
		}
		if (!StringUtil.isEmpty(vo.getFirstName())) {
			whereSql += CRIT[parms.size()] + " a.FirstName = ? ";
			parms.add(vo.getFirstName());
		}
		if (!StringUtil.isEmpty(vo.getDayPhone())) {
			whereSql += CRIT[parms.size()] + " a.DayPhone = ? ";
			parms.add(vo.getDayPhone());
		}
		if (!StringUtil.isEmpty(vo.getStatusId())) {
			whereSql += CRIT[parms.size()] + " a.StatusId = ? ";
			parms.add(vo.getStatusId());
		}
		// search by email address
		if (!StringUtil.isEmpty(vo.getSearchString())) {
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
	
	public int update(CustomerVo customerVo) {
		customerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		syncupEmailFields(customerVo);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(customerVo);
		String sql = MetaDataUtil.buildUpdateStatement("Customers", customerVo);

		if (customerVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=:origUpdtTime ";
		}
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		customerVo.setOrigUpdtTime(customerVo.getUpdtTime());
		customerVo.setOrigCustId(customerVo.getCustId());
		return rowsUpadted;
	}
	
	public int delete(String custId) {
		String sql = 
			"delete from Customers where custid=? ";
		
		int rowsDeleted = getJdbcTemplate().update(sql, new Object[] {custId});
		return rowsDeleted;
	}

	public int deleteByEmailAddr(String emailAddr) {
		EmailAddrVo addrVo = getEmailAddrDao().getByAddress(emailAddr);
		if (addrVo == null) {
			return 0;
		}
		String sql = 
			"delete from Customers where EmailAddrId=? ";
		
		int rowsDeleted = getJdbcTemplate().update(sql, new Object[] {addrVo.getEmailAddrId()});
		return rowsDeleted;
	}

	public int insert(CustomerVo customerVo) {
		customerVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		syncupEmailFields(customerVo);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(customerVo);
		String sql = MetaDataUtil.buildInsertStatement("Customers", customerVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		customerVo.setRowId(retrieveRowId());
		customerVo.setOrigUpdtTime(customerVo.getUpdtTime());
		customerVo.setOrigCustId(customerVo.getCustId());
		return rowsInserted;
	}
	
	private void syncupEmailFields(CustomerVo vo) {
		if (!StringUtil.isEmpty(vo.getEmailAddr())) {
			EmailAddrVo addrVo = getEmailAddrDao().findByAddress(vo.getEmailAddr());
			vo.setEmailAddrId(addrVo.getEmailAddrId());
		}
		else {
			EmailAddrVo addrVo = getEmailAddrDao().getByAddrId(vo.getEmailAddrId());
			if (addrVo != null) {
				vo.setEmailAddr(addrVo.getEmailAddr());
			}
		}
	}
	
	@Autowired
	private EmailAddrDao emailAddrDao;
	private EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}
}
