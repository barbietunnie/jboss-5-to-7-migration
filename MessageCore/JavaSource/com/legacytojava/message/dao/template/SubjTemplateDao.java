package com.legacytojava.message.dao.template;

import java.sql.Timestamp;
import java.util.List;

import com.legacytojava.message.vo.template.SubjTemplateVo;

public interface SubjTemplateDao {
	public SubjTemplateVo getByPrimaryKey(String templateId, String clientId, Timestamp startTime);
	public SubjTemplateVo getByBestMatch(String templateId, String clientId, Timestamp startTime);
	public List<SubjTemplateVo> getByTemplateId(String templateId);
	public List<SubjTemplateVo> getByClientId(String clientId);
	public int update(SubjTemplateVo subjTemplateVo);
	public int deleteByPrimaryKey(String templateId, String clientId, Timestamp startTime);
	public int deleteByTemplateId(String templateId);
	public int deleteByClientId(String clientId);
	public int insert(SubjTemplateVo subjTemplateVo);
}
