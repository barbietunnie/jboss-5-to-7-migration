package com.es.dao.address;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.data.constant.Constants;
import com.es.data.constant.StatusId;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.address.MobileCarrierVo;

@Component("mobileCarrierDao")
public class MobileCarrierDao extends AbstractDao {
	static final Logger logger = Logger.getLogger(MobileCarrierDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
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
		mcVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		if (StringUtils.isBlank(mcVo.getUpdtUserId())) {
			mcVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		}
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mcVo);

		String sql = MetaDataUtil.buildUpdateStatement("Mobile_Carrier", mcVo);		

		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mcVo);

		String sql = MetaDataUtil.buildInsertStatement("Mobile_Carrier", mcVo);		
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		mcVo.setRowId(retrieveRowId());
		return rowsInserted;
	}
}
