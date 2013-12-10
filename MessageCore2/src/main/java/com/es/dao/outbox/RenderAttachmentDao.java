package com.es.dao.outbox;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.db.metadata.MetaDataUtil;
import com.es.vo.outbox.RenderAttachmentVo;

@Component("renderAttachmentDao")
public class RenderAttachmentDao extends AbstractDao {
	
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
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderAttachmentVo);
		String sql = MetaDataUtil.buildInsertStatement("Render_Attachment", renderAttachmentVo);
		int rowsUpadted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
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
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(renderAttachmentVo);
		String sql = MetaDataUtil.buildUpdateStatement("Render_Attachment", renderAttachmentVo);
		int rowsInserted = getNamedParameterJdbcTemplate().update(sql, namedParameters);
		return rowsInserted;
	}
	
}
