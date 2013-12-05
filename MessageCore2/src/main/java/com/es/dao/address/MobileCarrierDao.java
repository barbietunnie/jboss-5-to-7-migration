package com.es.dao.address;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.data.constant.Constants;
import com.es.data.constant.StatusId;
import com.es.vo.address.MobileCarrierVo;

@Component("mobileCarrierDao")
public class MobileCarrierDao {
	static final Logger logger = Logger.getLogger(MobileCarrierDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public MobileCarrierVo getByRowId(int rowId) {
		String sql = "select * from mobile_carrier a " +
				" where a.RowId = ? ";
		Object[] parms = new Object[] {rowId};
		try {
			MobileCarrierVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MobileCarrierVo>(MobileCarrierVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public MobileCarrierVo getByCarrierName(String carrierName) {
		String sql = "select * from mobile_carrier a " +
				" where a.CarrierName = ? ";
		Object[] parms = new Object[] {carrierName};
		try {
			MobileCarrierVo vo = getJdbcTemplate().queryForObject(sql, parms,
					new BeanPropertyRowMapper<MobileCarrierVo>(MobileCarrierVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<MobileCarrierVo> getAll(boolean onlyActive) {
		List<Object> parms = new ArrayList<Object>();
		String sql = "select * from mobile_carrier a ";
		if (onlyActive) {
			sql += " where a.StatusId = ? ";
			parms.add(StatusId.ACTIVE.getValue());
		}
		sql += " order by a.RowId ";
		List<MobileCarrierVo> list = getJdbcTemplate().query(sql, parms.toArray(),
				new BeanPropertyRowMapper<MobileCarrierVo>(MobileCarrierVo.class));
		return list;
	}

	public int update(MobileCarrierVo mcVo) {
		if (StringUtils.isBlank(mcVo.getUpdtUserId())) {
			mcVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		}
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(mcVo.getCarrierName());
		keys.add(mcVo.getCountryCode());
		keys.add(mcVo.getMultiMediaAddress());
		keys.add(mcVo.getStatusId());
		keys.add(mcVo.getTextAddress());
		keys.add(new Timestamp(System.currentTimeMillis()));
		keys.add(mcVo.getUpdtUserId());
		keys.add(mcVo.getRowId());
		String sql = "update Mobile_Carrier set " +
			"CarrierName=?," +
			"CountryCode=?," +
			"MultiMediaAddress=?," +
			"StatusId=?," +
			"TextAddress=?, " +
			"UpdtTime=?, " +
			"UpdtUserId=? " +
			" where RowId=?";
		
		Object[] parms = keys.toArray();

		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		return rowsUpadted;
	}
	
	public int deleteByRowId(int rowId) {
		String sql = "delete from Mobile_Carrier where RowId=?";
		Object[] parms = new Object[] {rowId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int deleteByCarrierName(String carrierName) {
		String sql = "delete from Mobile_Carrier where CarrierName=?";
		Object[] parms = new Object[] {carrierName};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		return rowsDeleted;
	}
	
	public int insert(MobileCarrierVo mcVo) {
		mcVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		if (StringUtils.isBlank(mcVo.getUpdtUserId())) {
			mcVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		}
		Object[] parms = {
				mcVo.getCarrierName(),
				mcVo.getCountryCode(),
				mcVo.getMultiMediaAddress(),
				mcVo.getStatusId(),
				mcVo.getTextAddress(),
				mcVo.getUpdtTime(),
				mcVo.getUpdtUserId()
			};
		
		String sql = "INSERT INTO Mobile_Carrier (" +
			"CarrierName," +
			"CountryCode," +
			"MultiMediaAddress," +
			"StatusId," +
			"TextAddress," +
			"UpdtTime," +
			"UpdtUserId " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ? "+
				")";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		mcVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForObject(getRowIdSql(), Integer.class);
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
