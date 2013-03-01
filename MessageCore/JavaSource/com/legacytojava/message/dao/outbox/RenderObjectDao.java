package com.legacytojava.message.dao.outbox;

import java.util.List;

import com.legacytojava.message.vo.outbox.RenderObjectVo;

public interface RenderObjectDao {
	public RenderObjectVo getByPrimaryKey(long renderId, String variableName);
	public List<RenderObjectVo> getByRenderId(long renderId);
	public int update(RenderObjectVo renderObjectVo);
	public int deleteByPrimaryKey(long renderId, String variableName);
	public int deleteByRenderId(long renderId);
	public int insert(RenderObjectVo renderObjectVo);
}
