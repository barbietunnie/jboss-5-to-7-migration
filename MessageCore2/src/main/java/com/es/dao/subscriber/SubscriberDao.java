package com.es.dao.subscriber;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.es.core.util.EmailAddrUtil;
import com.es.core.util.StringUtil;
import com.es.dao.address.EmailAddressDao;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.comm.PagingSubscriberVo;
import com.es.vo.comm.PagingVo;
import com.es.vo.comm.SubscriberVo;

@Repository
@Component("subscriberDao")
public class SubscriberDao {

	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public SubscriberVo getBySubscriberId(String subrId) {
		String sql = 
			"select * " +
				"from Subscriber where subrid=? ";
		
		Object[] parms = new Object[] {subrId};
		List<SubscriberVo> list =  getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
		if (list.size()>0)
			return list.get(0);
		else
			return null;
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
		List<SubscriberVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
		if (list == null || list.isEmpty()) {
			return null;
		}
		else {
			return (SubscriberVo) list.get(0);
		}
	}
	
	public SubscriberVo getByEmailAddress(String emailAddr) {
		String sql = 
			"select a.* " +
			" from Subscriber a, Email_Address b " +
			" where a.EmailAddrId=b.EmailAddrId " +
			" and a.EmailAddr=? ";
		Object[] parms = new Object[] {EmailAddrUtil.removeDisplayName(emailAddr)};
		List<SubscriberVo> list = getJdbcTemplate().query(sql, parms,
				new BeanPropertyRowMapper<SubscriberVo>(SubscriberVo.class));
		if (list == null || list.isEmpty()) {
			return null;
		}
		else {
			return (SubscriberVo) list.get(0);
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
	
	public int update(SubscriberVo customerVo) {
		customerVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		syncupEmailFields(customerVo);
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(customerVo.getSubrId());
		keys.add(customerVo.getSenderId());
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
		keys.add(customerVo.getMobileCarrier());
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
		
		String sql = "update Subscriber set "
			+ "SubrId=?, "
			+ "SenderId=?, "
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
			+ "MobileCarrier=?, " // 25
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
		customerVo.setOrigSubrId(customerVo.getSubrId());
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

	public int insert(SubscriberVo customerVo) {
		customerVo.setUpdtTime(new Timestamp(new java.util.Date().getTime()));
		syncupEmailFields(customerVo);
		Object[] parms = {
				customerVo.getSubrId(),
				customerVo.getSenderId(),
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
				customerVo.getMobileCarrier(),
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
		
		String sql = "insert into Subscriber ("
			+ "SubrId, "
			+ "SenderId, "
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
			+ "MobileCarrier, " // 25
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
		customerVo.setOrigSubrId(customerVo.getSubrId());
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
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}

	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
		
}
