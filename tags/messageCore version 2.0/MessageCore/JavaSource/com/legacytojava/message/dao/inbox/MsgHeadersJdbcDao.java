package com.legacytojava.message.dao.inbox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.inbox.MsgHeadersVo;

@Component("msgHeadersDao")
public class MsgHeadersJdbcDao implements MsgHeadersDao {
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class MsgHeadersMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			MsgHeadersVo msgHeadersVo = new MsgHeadersVo();
			
			msgHeadersVo.setMsgId(rs.getLong("MsgId"));
			msgHeadersVo.setHeaderSeq(rs.getInt("HeaderSeq"));
			msgHeadersVo.setHeaderName(rs.getString("HeaderName"));
			msgHeadersVo.setHeaderValue(rs.getString("HeaderValue"));
			
			return msgHeadersVo;
		}
	}

	public MsgHeadersVo getByPrimaryKey(long msgId, int headerSeq) {
		String sql = 
			"select * " +
			"from " +
				"MsgHeaders where msgid=? and headerSeq=? ";
		
		Object[] parms = new Object[] {msgId+"",headerSeq+""};
		List<?> list = (List<?>)getJdbcTemplate().query(sql, parms, new MsgHeadersMapper());
		if (list.size()>0)
			return (MsgHeadersVo)list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<MsgHeadersVo> getByMsgId(long msgId) {
		String sql = 
			"select * " +
			" from " +
				" MsgHeaders where msgId=? " +
			" order by headerSeq";
		Object[] parms = new Object[] {msgId+""};
		List<MsgHeadersVo> list = (List<MsgHeadersVo>)getJdbcTemplate().query(sql, parms, new MsgHeadersMapper());
		return list;
	}
	
	public int update(MsgHeadersVo msgHeadersVo) {
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(StringUtils.left(msgHeadersVo.getHeaderName(), 100));
		fields.add(msgHeadersVo.getHeaderValue());
		fields.add(msgHeadersVo.getMsgId()+"");
		fields.add(msgHeadersVo.getHeaderSeq()+"");
		
		String sql =
			"update MsgHeaders set " +
				"HeaderName=?, " +
				"HeaderValue=? " +
			" where " +
				" msgid=? and headerSeq=?  ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long msgId, int headerSeq) {
		String sql = 
			"delete from MsgHeaders where msgid=? and headerSeq=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		fields.add(headerSeq+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByMsgId(long msgId) {
		String sql = 
			"delete from MsgHeaders where msgid=? ";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(MsgHeadersVo msgHeadersVo) {
		String sql = 
			"INSERT INTO MsgHeaders (" +
			"MsgId, " +
			"HeaderSeq, " +
			"HeaderName, " +
			"HeaderValue " +
			") VALUES (" +
				" ?, ?, ?, ? " +
				")";
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(msgHeadersVo.getMsgId()+"");
		fields.add(msgHeadersVo.getHeaderSeq()+"");
		fields.add(StringUtils.left(msgHeadersVo.getHeaderName(), 100));
		fields.add(msgHeadersVo.getHeaderValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
