package com.legacytojava.message.dao.mailbox;

import java.util.List;

import com.legacytojava.message.vo.MailBoxVo;

public interface MailBoxDao {
	public MailBoxVo getByPrimaryKey(String userId, String hostName);
	public List<MailBoxVo> getAll(boolean onlyActive);
	public List<MailBoxVo> getAllForTrial(boolean onlyActive);
	public int update(MailBoxVo mailBoxVo);
	public int deleteByPrimaryKey(String userId, String hostName);
	public int insert(MailBoxVo mailBoxVo);
}
