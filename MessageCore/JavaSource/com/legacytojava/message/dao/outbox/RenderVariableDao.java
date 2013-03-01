package com.legacytojava.message.dao.outbox;

import java.util.List;

import com.legacytojava.message.vo.outbox.RenderVariableVo;

public interface RenderVariableDao {
	public RenderVariableVo getByPrimaryKey(long renderId, String variableName);
	public List<RenderVariableVo> getByRenderId(long renderId);
	public int update(RenderVariableVo renderVariableVo);
	public int deleteByPrimaryKey(long renderId, String variableName);
	public int deleteByRenderId(long renderId);
	public int insert(RenderVariableVo renderVariableVo);
}
