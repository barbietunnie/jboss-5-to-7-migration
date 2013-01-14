package com.legacytojava.message.dao.emailaddr;

import java.util.List;

import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;

public interface EmailTemplateDao {
	public EmailTemplateVo getByTemplateId(String templateId);
	public List<EmailTemplateVo> getByListId(String listId);
	public List<EmailTemplateVo> getAll();
	public List<EmailTemplateVo> getAllForTrial();
	public int update(EmailTemplateVo emailTemplateVo);
	public int deleteByTemplateId(String templateId);
	public int insert(EmailTemplateVo emailTemplateVo);
}
