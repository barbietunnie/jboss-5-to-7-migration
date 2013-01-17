package com.legacytojava.message.dao.template;

import java.sql.Timestamp;
import java.util.List;

import com.legacytojava.message.vo.template.ClientVariableVo;

public interface ClientVariableDao {
	public ClientVariableVo getByPrimaryKey(String clientId, String variableName, Timestamp startTime);
	public ClientVariableVo getByBestMatch(String clientId, String variableName, Timestamp startTime);
	public List<ClientVariableVo> getByVariableName(String variableName);
	public List<ClientVariableVo> getCurrentByClientId(String clientId);
	public int update(ClientVariableVo clientVariableVo);
	public int deleteByPrimaryKey(String clientId, String variableName, Timestamp startTime);
	public int deleteByVariableName(String variableName);
	public int deleteByClientId(String clientId);
	public int insert(ClientVariableVo clientVariableVo);
}
