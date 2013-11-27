package com.es.dao.outbox;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.es.vo.outbox.RenderAttachmentVo;

@Component("renderAttachmentDao")
public class RenderAttachmentDao {
	
	@Autowired
	private DataSource msgDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(msgDataSource);
		}
		return jdbcTemplate;
	}

	public RenderAttachmentVo getByPrimaryKey(long renderId, int attchmntSeq) {
		String sql = 
			"select * " +
			"from " +
				"Render_Attachment where renderId=? and attchmntSeq=? ";
		
		Object[] parms = new Object[] {renderId, attchmntSeq};
		try {
			RenderAttachmentVo vo = getJdbcTemplate().queryForObject(sql, parms, 
					new BeanPropertyRowMapper<RenderAttachmentVo>(RenderAttachmentVo.class));
			return vo;
		}
		catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public List<RenderAttachmentVo> getByRenderId(long renderId) {
		String sql = 
			"select * " +
			" from " +
				" Render_Attachment where renderId=? " +
			" order by attchmntSeq";
		Object[] parms = new Object[] {renderId};
		List<RenderAttachmentVo> list = getJdbcTemplate().query(sql, parms, 
				new BeanPropertyRowMapper<RenderAttachmentVo>(RenderAttachmentVo.class));
		return list;
	}
	
	public int update(RenderAttachmentVo renderAttachmentVo) {
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderAttachmentVo.getAttchmntName());
		fields.add(renderAttachmentVo.getAttchmntType());
		fields.add(renderAttachmentVo.getAttchmntDisp());
		fields.add(renderAttachmentVo.getAttchmntValue());
		fields.add(renderAttachmentVo.getRenderId());
		fields.add(renderAttachmentVo.getAttchmntSeq());
		
		String sql =
			"update Render_Attachment set " +
				"AttchmntName=?, " +
				"AttchmntType=?, " +
				"AttchmntDisp=?, " +
				"AttchmntValue=? " +
			" where " +
				" renderId=? and attchmntSeq=? ";
		
		int rowsUpadted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpadted;
	}
	
	public int deleteByPrimaryKey(long renderId, int attchmntSeq) {
		String sql = 
			"delete from Render_Attachment where renderId=? and attchmntSeq=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderId+"");
		fields.add(attchmntSeq+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int deleteByRenderId(long renderId) {
		String sql = 
			"delete from Render_Attachment where renderId=? ";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderId+"");
		
		int rowsDeleted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsDeleted;
	}
	
	public int insert(RenderAttachmentVo renderAttachmentVo) {
		String sql = 
			"INSERT INTO Render_Attachment (" +
			"RenderId, " +
			"AttchmntSeq, " +
			"AttchmntName, " +
			"AttchmntType, " +
			"AttchmntDisp, " +
			"AttchmntValue " +
			") VALUES (" +
				" ?, ?, ?, ?, ?, ? " +
				")";
		
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(renderAttachmentVo.getRenderId());
		fields.add(renderAttachmentVo.getAttchmntSeq());
		fields.add(renderAttachmentVo.getAttchmntName());
		fields.add(renderAttachmentVo.getAttchmntType());
		fields.add(renderAttachmentVo.getAttchmntDisp());
		fields.add(renderAttachmentVo.getAttchmntValue());
		
		int rowsInserted = getJdbcTemplate().update(sql, fields.toArray());
		return rowsInserted;
	}
	
	protected String getRowIdSql() {
		return "select last_insert_id()";
	}
}
