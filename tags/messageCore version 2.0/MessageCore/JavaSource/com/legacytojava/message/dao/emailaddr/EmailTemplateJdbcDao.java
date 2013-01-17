package com.legacytojava.message.dao.emailaddr;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.MsgDataType;
import com.legacytojava.message.dao.action.MsgDataTypeDao;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.util.BlobUtil;
import com.legacytojava.message.vo.action.MsgDataTypeVo;
import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;

@Component("emailTemplateDao")
public class EmailTemplateJdbcDao implements EmailTemplateDao {
	static final Logger logger = Logger.getLogger(EmailTemplateJdbcDao.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;

	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class EmailTemplateMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			EmailTemplateVo emailTemplateVo = new EmailTemplateVo();
			
			emailTemplateVo.setRowId(rs.getInt("RowId"));
			emailTemplateVo.setTemplateId(rs.getString("TemplateId"));
			emailTemplateVo.setListId(rs.getString("ListId"));
			emailTemplateVo.setSubject(rs.getString("Subject"));
			emailTemplateVo.setBodyText(rs.getString("BodyText"));
			String isHtml = rs.getString("IsHtml");
			emailTemplateVo.setIsHtml(Constants.YES_CODE.equals(isHtml) ? true : false);
			emailTemplateVo.setListType(rs.getString("ListType"));
			emailTemplateVo.setDeliveryOption(rs.getString("DeliveryOption"));
			emailTemplateVo.setSelectCriteria(rs.getString("SelectCriteria"));
			emailTemplateVo.setEmbedEmailId(rs.getString("EmbedEmailId"));
			emailTemplateVo.setIsBuiltIn(rs.getString("IsBuiltIn"));
			emailTemplateVo.setOrigTemplateId(emailTemplateVo.getTemplateId());
			emailTemplateVo.setClientId(rs.getString("ClientId"));
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
		String sql = "select a.*, b.ClientId " +
				" from EmailTemplate a, MailingList b " +
				" where a.ListId=b.ListId and a.TemplateId=?";
		Object[] parms = new Object[] {templateId};
		List<?> list = (List<?>) getJdbcTemplate().query(sql, parms, new EmailTemplateMapper());
		if (list.size()>0) {
			return (EmailTemplateVo)list.get(0);
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailTemplateVo> getByListId(String listId) {
		String sql = "select a.*, b.ClientId " +
				" from EmailTemplate a, MailingList b " +
				" where a.ListId=b.ListId and a.ListId=?" +
				" order by a.TemplateId";
		Object[] parms = new Object[] {listId};
		List<EmailTemplateVo> list = (List<EmailTemplateVo>) getJdbcTemplate().query(sql, parms,
				new EmailTemplateMapper());
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<EmailTemplateVo> getAll() {
		String sql = "select a.*, b.ClientId " +
				" from EmailTemplate a, MailingList b " +
				" where a.ListId=b.ListId" +
				" order by a.RowId";
		List<EmailTemplateVo> list = (List<EmailTemplateVo>) getJdbcTemplate().query(sql,
				new EmailTemplateMapper());
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<EmailTemplateVo> getAllForTrial() {
		String sql = "select a.*, b.ClientId " +
				" from EmailTemplate a, MailingList b " +
				" where a.ListId=b.ListId" +
				" order by a.RowId" +
				" limit 20";
		int fetchSize = getJdbcTemplate().getFetchSize();
		int maxRows = getJdbcTemplate().getMaxRows();
		getJdbcTemplate().setFetchSize(20);
		getJdbcTemplate().setMaxRows(20);
		List<EmailTemplateVo> list = (List<EmailTemplateVo>) getJdbcTemplate().query(sql,
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
		keys.add(emailTemplateVo.getIsHtml() ? Constants.YES_CODE : Constants.NO_CODE);
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
		
		String sql = "update EmailTemplate set " +
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
		updateMsgDataType(emailTemplateVo); // do it before set Original Template Id
		emailTemplateVo.setOrigTemplateId(emailTemplateVo.getTemplateId());
		updateReloadFlags();
		return rowsUpadted;
	}

	public synchronized int deleteByTemplateId(String templateId) {
		String sql = "delete from EmailTemplate where TemplateId=?";
		Object[] parms = new Object[] {templateId};
		int rowsDeleted = getJdbcTemplate().update(sql, parms);
		deleteMsgDataType(templateId);
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
				emailTemplateVo.getIsHtml() ? Constants.YES_CODE : Constants.NO_CODE,
				emailTemplateVo.getListType(),
				emailTemplateVo.getDeliveryOption(),
				emailTemplateVo.getSelectCriteria(),
				emailTemplateVo.getEmbedEmailId(),
				emailTemplateVo.getIsBuiltIn(),
				bytes
			};
		
		String sql = "INSERT INTO EmailTemplate (" +
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
		insertMsgDataType(emailTemplateVo);
		updateReloadFlags();
		return rowsInserted;
	}
	
	/*
	 * define methods that sync up template id to MsgDataType table.
	 */
	private final String DataType = MsgDataType.TEMPLATE_ID;
	private int insertMsgDataType(EmailTemplateVo vo) {
		int rowsInserted = 0;
		// 1) retrieve the record
		MsgDataTypeVo typeVo = getMsgDataTypeDao().getByTypeValuePair(DataType, vo.getTemplateId());
		if (typeVo == null) { // not found, insert
			typeVo = new MsgDataTypeVo();
			typeVo.setDataType(DataType);
			typeVo.setDataTypeValue(vo.getTemplateId());
			rowsInserted = getMsgDataTypeDao().insert(typeVo);
		}
		return rowsInserted;
	}
	
	private int deleteMsgDataType(String templateId) {
		int rowsDeleted = 0;
		// 1) retrieve the record
		MsgDataTypeVo vo = getMsgDataTypeDao().getByTypeValuePair(DataType, templateId);
		if (vo != null) { // found the record, delete
			rowsDeleted = getMsgDataTypeDao().deleteByPrimaryKey(vo.getRowId());
		}
		return rowsDeleted;
	}
	
	private int updateMsgDataType(EmailTemplateVo vo) {
		int rowsUpadted = 0;
		MsgDataTypeVo typeVo = getMsgDataTypeDao().getByTypeValuePair(DataType,
				vo.getOrigTemplateId());
		if (typeVo != null) { // record found, update
			typeVo.setDataTypeValue(vo.getTemplateId());
			rowsUpadted = getMsgDataTypeDao().update(typeVo);
		}
		else { // original record not found, check the new record and
				// insert one if it does not exist
			rowsUpadted = insertMsgDataType(vo);
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
	private MsgDataTypeDao msgDataTypeDao = null;
	MsgDataTypeDao getMsgDataTypeDao() {
		return msgDataTypeDao;
	}
	
	protected int retrieveRowId() {
		return getJdbcTemplate().queryForInt(getRowIdSql());
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
