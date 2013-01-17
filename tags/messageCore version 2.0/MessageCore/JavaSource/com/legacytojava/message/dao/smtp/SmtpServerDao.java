package com.legacytojava.message.dao.smtp;

import java.util.List;

import com.legacytojava.message.vo.SmtpConnVo;

public interface SmtpServerDao {
	public SmtpConnVo getByPrimaryKey(String serverName);
	public List<SmtpConnVo> getAll(boolean onlyActive);
	public List<SmtpConnVo> getAllForTrial(boolean onlyActive);
	public List<SmtpConnVo> getByServerType(String serverType, boolean onlyActive);
	public List<SmtpConnVo> getBySslFlag(boolean useSSL, boolean onlyActive);
	public List<SmtpConnVo> getBySslFlagForTrial(boolean useSSL, boolean onlyActive);
	public int update(SmtpConnVo smtpConnVo);
	public int deleteByPrimaryKey(String serverName);
	public int insert(SmtpConnVo smtpConnVo);
}
