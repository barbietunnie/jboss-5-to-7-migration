package com.legacytojava.message.dao.template;

import java.sql.Timestamp;
import java.util.List;

import com.legacytojava.message.vo.template.GlobalVariableVo;

public interface GlobalVariableDao {
	public GlobalVariableVo getByPrimaryKey(String variableName, Timestamp startTime);
	public GlobalVariableVo getByBestMatch(String variableName, Timestamp startTime);
	public List<GlobalVariableVo> getByVariableName(String variableName);
	public List<GlobalVariableVo> getCurrent();
	public int update(GlobalVariableVo globalVariableVo);
	public int deleteByPrimaryKey(String variableName, Timestamp startTime);
	public int deleteByVariableName(String variableName);
	public int insert(GlobalVariableVo globalVariableVo);
}
