package com.legacytojava.message.dao.emailaddr;

import java.util.List;

import com.legacytojava.message.vo.emailaddr.EmailVariableVo;

public interface EmailVariableDao {
	public static final String SYSTEM_VARIABLE = "S";
	public static final String CUSTOMER_VARIABLE = "C";
	
	public EmailVariableVo getByName(String variableName);
	public List<EmailVariableVo> getAll();
	public List<EmailVariableVo> getAllForTrial();
	public List<EmailVariableVo> getAllCustomVariables();
	public List<EmailVariableVo> getAllBuiltinVariables();
	public String getByQuery(String query, long addrId);
	public int update(EmailVariableVo emailVariableVo);
	public int deleteByName(String variableName);
	public int insert(EmailVariableVo emailVariableVo);
}
