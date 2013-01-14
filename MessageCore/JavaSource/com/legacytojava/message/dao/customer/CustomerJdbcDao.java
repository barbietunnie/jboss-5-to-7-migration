package com.legacytojava.message.dao.customer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.util.EmailAddrUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.CustomerVo;
import com.legacytojava.message.vo.PagingCustomerVo;
import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

@Repository
@Component("customerDao")
public class CustomerJdbcDao implements CustomerDao {

	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class CustomerMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			CustomerVo customerVo = new CustomerVo();
			
			customerVo.setPrimaryKey(rs.getString("CustId"));
			
			customerVo.setRowId(rs.getInt("RowId"));
			customerVo.setCustId(rs.getString("CustId"));
			customerVo.setClientId(rs.getString("ClientId"));
			customerVo.setSsnNumber(rs.getString("SsnNumber"));
			customerVo.setTaxId(rs.getString("TaxId"));
			customerVo.setProfession(rs.getString("Profession"));
			customerVo.setFirstName(rs.getString("FirstName"));
			customerVo.setMiddleName(rs.getString("MiddleName"));
			customerVo.setLastName(rs.getString("LastName"));
			customerVo.setAlias(rs.getString("Alias"));
			customerVo.setStreetAddress(rs.getString("StreetAddress"));
			customerVo.setStreetAddress2(rs.getString("StreetAddress2"));
			customerVo.setCityName(rs.getString("CityName"));
			customerVo.setStateCode(rs.getString("StateCode"));
			customerVo.setZipCode5(rs.getString("ZipCode5"));
			customerVo.setZipCode4(rs.getString("ZipCode4"));
			customerVo.setProvinceName(rs.getString("ProvinceName"));
			customerVo.setPostalCode(rs.getString("PostalCode"));
			customerVo.setCountry(rs.getString("Country"));
			customerVo.setDayPhone(rs.getString("DayPhone"));
			customerVo.setEveningPhone(rs.getString("EveningPhone"));
			customerVo.setMobilePhone(rs.getString("MobilePhone"));
			customerVo.setBirthDate(rs.getDate("BirthDate"));
			customerVo.setStartDate(rs.getDate("StartDate"));
			customerVo.setEndDate(rs.getDate("EndDate"));
			customerVo.setFaxNumber(rs.getString("FaxNumber"));
			customerVo.setMsgHeader(rs.getString("MsgHeader"));
			customerVo.setMsgDetail(rs.getString("MsgDetail"));
			customerVo.setMsgOptional(rs.getString("MsgOptional"));
			customerVo.setMsgFooter(rs.getString("MsgFooter"));
			customerVo.setTimeZoneCode(rs.getString("TimeZoneCode"));
			customerVo.setMemoText(rs.getString("MemoText"));
			customerVo.setStatusId(rs.getString("StatusId"));
			customerVo.setSecurityQuestion(rs.getString("SecurityQuestion"));
			customerVo.setSecurityAnswer(rs.getString("SecurityAnswer"));
			customerVo.setEmailAddr(rs.getString("EmailAddr"));
			customerVo.setEmailAddrId(rs.getLong("EmailAddrId"));
			customerVo.setPrevEmailAddr(rs.getString("PrevEmailAddr"));
			customerVo.setPasswordChangeTime(rs.getTimestamp("PasswordChangeTime"));
			customerVo.setUserPassword(rs.getString("UserPassword"));
			customerVo.setUpdtTime(rs.getTimestamp("UpdtTime"));
			customerVo.setUpdtUserId(rs.getString("UpdtUserId"));
			
			customerVo.setOrigUpdtTime(customerVo.getUpdtTime());
			customerVo.setOrigCustId(customerVo.getCustId());
			
			return customerVo;
		}
	}
	
	public CustomerVo getByCustId(String custId) {
		String sql = 
			"select * " +
				"from Customers where custid=? ";
		
		Object[] parms = new Object[] {custId};
		List<?> list =  getJdbcTemplate().query(sql, parms, new CustomerMapper());
		if (list.size()>0)
			return (CustomerVo)list.get(0);
		else
			return null;
	}
	
	public List<CustomerVo> getByClientId(String clientId) {
		String sql = 
			"select * " +
				"from Customers where clientid=? ";
		Object[] parms = new Object[] {clientId};
		@SuppressWarnings("unchecked")
		List<CustomerVo> list = (List<CustomerVo>)getJdbcTemplate().query(sql, parms, new CustomerMapper());
		return list;
	}
	
	public CustomerVo getByEmailAddrId(long emailAddrId) {
		String sql = 
			"select * " +
			" from customers where emailAddrId=? ";
		Object[] parms = new Object[] {Long.valueOf(emailAddrId)};
		@SuppressWarnings("unchecked")
		List<CustomerVo> list = (List<CustomerVo>)getJdbcTemplate().query(sql, parms, new CustomerMapper());
		if (list == null || list.isEmpty()) {
			return null;
		}
		else {
			return (CustomerVo) list.get(0);
		}
	}
	
	public CustomerVo getByEmailAddress(String emailAddr) {
		String sql = 
			"select a.* " +
			" from Customers a, EmailAddr b " +
			" where a.EmailAddrId=b.EmailAddrId " +
			" and a.EmailAddr=? ";
		Object[] parms = new Object[] {EmailAddrUtil.removeDisplayName(emailAddr)};
		@SuppressWarnings("unchecked")
		List<CustomerVo> list = (List<CustomerVo>)getJdbcTemplate().query(sql, parms, new CustomerMapper());
		if (list == null || list.isEmpty()) {
			return null;
		}
		else {
			return (CustomerVo) list.get(0);
		}
	}
	
	public List<CustomerVo> getAll() {
		String sql = 
			"select * " +
				"from Customers";
		
		@SuppressWarnings("unchecked")
		List<CustomerVo> list = (List<CustomerVo>)getJdbcTemplate().query(sql, new CustomerMapper());
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
			"select a.*, b.StatusId as EmailStatusId, b.BounceCount, b.AcceptHtml " +
			" from Customers a " +
				" LEFT OUTER JOIN EmailAddr b on a.EmailAddrId=b.EmailAddrId " +
			whereSql +
			" order by a.CustId " + fetchOrder +
			" limit " + vo.getPageSize();
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(vo.getPageSize());
		getJdbcTemplate().setMaxRows(vo.getPageSize());
		@SuppressWarnings("unchecked")
		List<CustomerVo> list = (List<CustomerVo>) getJdbcTemplate().query(sql, parms.toArray(),
				new CustomerMapper());
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
		customerVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		syncupEmailFields(customerVo);
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(customerVo.getCustId());
		keys.add(customerVo.getClientId());
		keys.add(customerVo.getSsnNumber());
		keys.add(customerVo.getTaxId());
		keys.add(customerVo.getProfession());
		keys.add(customerVo.getFirstName());
		keys.add(customerVo.getMiddleName());
		keys.add(customerVo.getLastName());
		keys.add(customerVo.getAlias());
		keys.add(customerVo.getStreetAddress());
		keys.add(customerVo.getStreetAddress2());
		keys.add(customerVo.getCityName());
		keys.add(customerVo.getStateCode());
		keys.add(customerVo.getZipCode5());
		keys.add(customerVo.getZipCode4());
		keys.add(customerVo.getProvinceName());
		keys.add(customerVo.getPostalCode());
		keys.add(customerVo.getCountry());
		keys.add(customerVo.getDayPhone());
		keys.add(customerVo.getEveningPhone());
		keys.add(customerVo.getMobilePhone());
		keys.add(customerVo.getBirthDate());
		keys.add(customerVo.getStartDate());
		keys.add(customerVo.getEndDate());
		keys.add(customerVo.getFaxNumber());
		keys.add(customerVo.getMsgHeader());
		keys.add(customerVo.getMsgDetail());
		keys.add(customerVo.getMsgOptional());
		keys.add(customerVo.getMsgFooter());
		keys.add(customerVo.getTimeZoneCode());
		keys.add(customerVo.getMemoText());
		keys.add(customerVo.getStatusId());
		keys.add(customerVo.getSecurityQuestion());
		keys.add(customerVo.getSecurityAnswer());
		keys.add(customerVo.getEmailAddr());
		keys.add(customerVo.getEmailAddrId());
		keys.add(customerVo.getPrevEmailAddr());
		keys.add(customerVo.getPasswordChangeTime());
		keys.add(customerVo.getUserPassword());
		keys.add(customerVo.getUpdtTime());
		keys.add(customerVo.getUpdtUserId());
		keys.add(customerVo.getRowId());
		
		String sql = "update Customers set "
			+ "CustId=?, "
			+ "ClientId=?, "
			+ "SsnNumber=?, "
			+ "TaxId=?, "
			+ "Profession=?, "	//5
			+ "FirstName=?, "
			+ "MiddleName=?, "
			+ "LastName=?, "
			+ "Alias=?, "
			+ "StreetAddress=?, "	//10
			+ "StreetAddress2=?, "
			+ "CityName=?, "
			+ "StateCode=?, "
			+ "ZipCode5=?, "
			+ "ZipCode4=?, "	//15
			+ "ProvinceName=?, "
			+ "PostalCode=?, "
			+ "Country=?, "
			+ "DayPhone=?, "
			+ "EveningPhone=?, "	//20
			+ "MobilePhone=?, "
			+ "BirthDate=?, "
			+ "StartDate=?, "
			+ "EndDate=?, "
			+ "FaxNumber=?, " // 25
			+ "MsgHeader=?, "
			+ "MsgDetail=?, "
			+ "MsgOptional=?, "
			+ "MsgFooter=?, "
			+ "TimeZoneCode=?, " // 30
			+ "MemoText=?, "
			+ "StatusId=?, "
			+ "SecurityQuestion=?, "
			+ "SecurityAnswer=?, "
			+ "EmailAddr=?, " // 35
			+ "EmailAddrId=?, "
			+ "PrevEmailAddr=?, "
			+ "PasswordChangeTime=?, "
			+ "UserPassword=?, "
			+ "UpdtTime=?," // 40
			+ "UpdtUserId=? "
			+ " where Rowid=?";
		
		if (customerVo.getOrigUpdtTime() != null) {
			sql += " and UpdtTime=?";
			keys.add(customerVo.getOrigUpdtTime());
		}
		int rowsUpadted = getJdbcTemplate().update(sql, keys.toArray());
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
		customerVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		syncupEmailFields(customerVo);
		Object[] parms = {
				customerVo.getCustId(),
				customerVo.getClientId(),
				customerVo.getSsnNumber(),
				customerVo.getTaxId(),
				customerVo.getProfession(),
				customerVo.getFirstName(),
				customerVo.getMiddleName(),
				customerVo.getLastName(),
				customerVo.getAlias(),
				customerVo.getStreetAddress(),
				customerVo.getStreetAddress2(),
				customerVo.getCityName(),
				customerVo.getStateCode(),
				customerVo.getZipCode5(),
				customerVo.getZipCode4(),
				customerVo.getProvinceName(),
				customerVo.getPostalCode(),
				customerVo.getCountry(),
				customerVo.getDayPhone(),
				customerVo.getEveningPhone(),
				customerVo.getMobilePhone(),
				customerVo.getBirthDate(),
				customerVo.getStartDate(),
				customerVo.getEndDate(),
				customerVo.getFaxNumber(),
				customerVo.getMsgHeader(),
				customerVo.getMsgDetail(),
				customerVo.getMsgOptional(),
				customerVo.getMsgFooter(),
				customerVo.getTimeZoneCode(),
				customerVo.getMemoText(),
				customerVo.getStatusId(),
				customerVo.getSecurityQuestion(),
				customerVo.getSecurityAnswer(),
				customerVo.getEmailAddr(),
				customerVo.getEmailAddrId(),
				customerVo.getPrevEmailAddr(),
				customerVo.getPasswordChangeTime(),
				customerVo.getUserPassword(),
				customerVo.getUpdtTime(),
				customerVo.getUpdtUserId()
			};
		
		String sql = "insert into Customers ("
			+ "CustId, "
			+ "ClientId, "
			+ "SsnNumber, "
			+ "TaxId, "
			+ "Profession, "	//5
			+ "FirstName, "
			+ "MiddleName, "
			+ "LastName, "
			+ "Alias, "
			+ "StreetAddress, "	//10
			+ "StreetAddress2, "
			+ "CityName, "
			+ "StateCode, "
			+ "ZipCode5, "
			+ "ZipCode4, "	//15
			+ "ProvinceName, "
			+ "PostalCode, "
			+ "Country, "
			+ "DayPhone, "
			+ "EveningPhone, "	//20
			+ "MobilePhone, "
			+ "BirthDate, "
			+ "StartDate, "
			+ "EndDate, "
			+ "FaxNumber, " // 25
			+ "MsgHeader, "
			+ "MsgDetail, "
			+ "MsgOptional, "
			+ "MsgFooter, "
			+ "TimeZoneCode, " //30
			+ "MemoText, "
			+ "StatusId, "
			+ "SecurityQuestion, "
			+ "SecurityAnswer, "
			+ "EmailAddr, " // 35
			+ "EmailAddrId, "
			+ "PrevEmailAddr, "
			+ "PasswordChangeTime, "
			+ "UserPassword, "
			+ "UpdtTime," //40
			+ "UpdtUserId) "
			+ " VALUES ( "
			+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
			+ ",?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
			+ ",?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
			+ ",?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? )";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
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
	private EmailAddrDao emailAddrDao = null;
	private EmailAddrDao getEmailAddrDao() {
		return emailAddrDao;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}

	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
		
}
