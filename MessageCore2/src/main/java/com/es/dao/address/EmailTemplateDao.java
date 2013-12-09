package com.es.dao.address;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.es.core.util.BlobUtil;
import com.es.dao.abst.AbstractDao;
import com.es.dao.action.RuleDataTypeDao;
import com.es.dao.sender.ReloadFlagsDao;
import com.es.data.constant.CodeType;
import com.es.data.preload.RuleDataTypeEnum;
import com.es.vo.action.RuleDataTypeVo;
import com.es.vo.address.EmailTemplateVo;
import com.es.vo.address.SchedulesBlob;

@Component("emailTemplateDao")
public class EmailTemplateDao extends AbstractDao {
	static final Logger logger = Logger.getLogger(EmailTemplateDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private static final class EmailTemplateMapper implements RowMapper<EmailTemplateVo> {
		
		public EmailTemplateVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			EmailTemplateVo emailTemplateVo = new EmailTemplateVo();
			
			emailTemplateVo.setRowId(rs.getInt("RowId"));
			emailTemplateVo.setTemplateId(rs.getString("TemplateId"));
			emailTemplateVo.setListId(rs.getString("ListId"));
			emailTemplateVo.setSubject(rs.getString("Subject"));
			emailTemplateVo.setBodyText(rs.getString("BodyText"));
			String isHtml = rs.getString("IsHtml");
			emailTemplateVo.setIsHtml(CodeType.YES_CODE.getValue().equals(isHtml) ? true : false);
			emailTemplateVo.setListType(rs.getString("ListType"));
			emailTemplateVo.setDeliveryOption(rs.getString("DeliveryOption"));
			emailTemplateVo.setSelectCriteria(rs.getString("SelectCriteria"));
			emailTemplateVo.setEmbedEmailId(rs.getString("EmbedEmailId"));
			emailTemplateVo.setIsBuiltIn(rs.getString("IsBuiltIn"));
			emailTemplateVo.setOrigTemplateId(emailTemplateVo.getTemplateId());
			emailTemplateVo.setSenderId(rs.getString("SenderId"));
			// retrieve SchedulesBlob class
			byte[] bytes = rs.getBytes("Schedules");
			try {
				SchedulesBlob blob = (SchedulesBlob) BlobUtil.bytesToObject(bytes);
				emailTemplateVo.setSchedulesBlob(blob);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Exception caught - " + e.toString());
			}
			return emailTemplateVo;
		}
	}
	
	public EmailTemplateVo getByTemplateId(String templateId) {
		String sql = "select a.*, b.SenderId " +
				" from Email_Template a, Mailing_List b " +
				" where a.ListId=b.ListId and a.TemplateId=?";
		Object[] parms = new Object[] {templateId};
		try {
			EmailTemplateVo vo = getJdbcTemplate().queryForObject(sql, parms, new EmailTemplateMapper());
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<EmailTemplateVo> getByListId(String listId) {
		String sql = "select a.*, b.SenderId " +
				" from Email_Template a, Mailing_List b " +
				" where a.ListId=b.ListId and a.ListId=?" +
				" order by a.TemplateId";
		Object[] parms = new Object[] {listId};
		List<EmailTemplateVo> list = getJdbcTemplate().query(sql, parms,
				new EmailTemplateMapper());
		return list;
	}
	
	public List<EmailTemplateVo> getAll() {
		String sql = "select a.*, b.SenderId " +
				" from Email_Template a, Mailing_List b " +
				" where a.ListId=b.ListId" +
				" order by a.RowId";
		List<EmailTemplateVo> list = getJdbcTemplate().query(sql,
				new EmailTemplateMapper());
		return list;
	}

	public List<EmailTemplateVo> getAllForTrial() {
		String sql = "select a.*, b.SenderId " +
				" from Email_Template a, Mailing_List b " +
				" where a.ListId=b.ListId" +
				" order by a.RowId" +
				" limit 20";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(20);
		getJdbcTemplate().setMaxRows(20);
		List<EmailTemplateVo> list = getJdbcTemplate().query(sql,
				new EmailTemplateMapper());
		getJdbcTemplate().setFetchSize(fetchSize);
		getJdbcTemplate().setMaxRows(maxRows);
		return list;
	}

	public synchronized int update(EmailTemplateVo emailTemplateVo) {
		ArrayList<Object> keys = new ArrayList<Object>();
		keys.add(emailTemplateVo.getTemplateId());
		keys.add(emailTemplateVo.getListId());
		keys.add(emailTemplateVo.getSubject());
		keys.add(emailTemplateVo.getBodyText());
		keys.add(emailTemplateVo.getIsHtml() ? CodeType.YES_CODE.getValue() : CodeType.NO_CODE.getValue());
		keys.add(emailTemplateVo.getListType());
		keys.add(emailTemplateVo.getDeliveryOption());
		keys.add(emailTemplateVo.getSelectCriteria());
		keys.add(emailTemplateVo.getEmbedEmailId());
		keys.add(emailTemplateVo.getIsBuiltIn());
		SchedulesBlob blob = emailTemplateVo.getSchedulesBlob();
		try {
			keys.add(BlobUtil.objectToBytes(blob));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception caught - " + e.toString());
		}
		keys.add(emailTemplateVo.getRowId());
		
		String sql = "update Email_Template set " +
			"TemplateId=?," +
			"ListId=?," +
			"Subject=?," +
			"BodyText=?," +
			"IsHtml=?," +
			"ListType=?," +
			"DeliveryOption=?," +
			"SelectCriteria=?," +
			"EmbedEmailId=?," +
			"IsBuiltIn=?," +
			"Schedules=?" +
			" where RowId=?";
		
		Object[] parms = keys.toArray();
		int rowsUpadted = getJdbcTemplate().update(sql, parms);
		updateRuleDataTypeEnum(emailTemplateVo); // do it before set Original Template Id
		emailTemplateVo.setOrigTemplateId(emailTemplateVo.getTemplateId());
		updateReloadFlags();
		return rowsUpadted;
	}

	public synchronized int deleteByTemplateId(String templateId) {
		String sql = "delete from Email_Template where TemplateId=?";
		Object[] parms = new Object[] {templateId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		deleteRuleDataTypeEnum(templateId);
		updateReloadFlags();
		return rowsDeleted;
	}
	
	public synchronized int insert(EmailTemplateVo emailTemplateVo) {
		SchedulesBlob blob = emailTemplateVo.getSchedulesBlob();
		byte[] bytes = null;
		try {
			bytes = BlobUtil.objectToBytes(blob);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception caught - " + e.toString());
		}
		Object[] parms = {
				emailTemplateVo.getTemplateId(),
				emailTemplateVo.getListId(),
				emailTemplateVo.getSubject(),
				emailTemplateVo.getBodyText(),
				emailTemplateVo.getIsHtml() ? CodeType.YES_CODE.getValue() : CodeType.NO_CODE.getValue(),
				emailTemplateVo.getListType(),
				emailTemplateVo.getDeliveryOption(),
				emailTemplateVo.getSelectCriteria(),
				emailTemplateVo.getEmbedEmailId(),
				emailTemplateVo.getIsBuiltIn(),
				bytes
			};
		
		String sql = "INSERT INTO Email_Template (" +
			"TemplateId," +
			"ListId," +
			"Subject," +
			"BodyText," +
			"IsHtml," +
			"ListType," +
			"DeliveryOption," +
			"SelectCriteria," +
			"EmbedEmailId," +
			"IsBuiltIn," +
			"Schedules" +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "+
				" ? )";
		
		int rowsInserted = getJdbcTemplate().update(sql, parms);
		emailTemplateVo.setRowId(retrieveRowId());
		emailTemplateVo.setOrigTemplateId(emailTemplateVo.getTemplateId());
		insertRuleDataTypeEnum(emailTemplateVo);
		updateReloadFlags();
		return rowsInserted;
	}
	
	/*
	 * define methods that sync up template id to RuleDataTypeEnum table.
	 */
	private final String DataType = RuleDataTypeEnum.TEMPLATE_ID.name();
	private int insertRuleDataTypeEnum(EmailTemplateVo vo) {
		int rowsInserted = 0;
		// 1) retrieve the record
		RuleDataTypeVo typeVo = getRuleDataTypeDao().getByTypeValuePair(DataType, vo.getTemplateId());
		if (typeVo == null) { // not found, insert
			typeVo = new RuleDataTypeVo();
			typeVo.setDataType(DataType);
			typeVo.setDataTypeValue(vo.getTemplateId());
			rowsInserted = getRuleDataTypeDao().insert(typeVo);
		}
		return rowsInserted;
	}
	
	private int deleteRuleDataTypeEnum(String templateId) {
		int rowsDeleted = 0;
		// 1) retrieve the record
		RuleDataTypeVo vo = getRuleDataTypeDao().getByTypeValuePair(DataType, templateId);
		if (vo != null) { // found the record, delete
			rowsDeleted = getRuleDataTypeDao().deleteByPrimaryKey(vo.getRowId());
		}
		return rowsDeleted;
	}
	
	private int updateRuleDataTypeEnum(EmailTemplateVo vo) {
		int rowsUpadted = 0;
		RuleDataTypeVo typeVo = getRuleDataTypeDao().getByTypeValuePair(DataType,
				vo.getOrigTemplateId());
		if (typeVo != null) { // record found, update
			typeVo.setDataTypeValue(vo.getTemplateId());
			rowsUpadted = getRuleDataTypeDao().update(typeVo);
		}
		else { // original record not found, check the new record and
				// insert one if it does not exist
			rowsUpadted = insertRuleDataTypeEnum(vo);
		}
		return rowsUpadted;
	}
	
	private void updateReloadFlags() {
		getReloadFlagsDao().updateTemplateReloadFlag();
	}

	@Autowired
	private ReloadFlagsDao reloadFlagsDao;
	private synchronized ReloadFlagsDao getReloadFlagsDao() {
		return reloadFlagsDao;
	}

	@Autowired
	private RuleDataTypeDao ruleDataTypeDao = null;
	RuleDataTypeDao getRuleDataTypeDao() {
		return ruleDataTypeDao;
	}
}
