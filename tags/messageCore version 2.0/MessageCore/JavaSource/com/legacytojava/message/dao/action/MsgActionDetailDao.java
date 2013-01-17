package com.legacytojava.message.dao.action;

import java.util.List;

import com.legacytojava.message.vo.action.MsgActionDetailVo;

public interface MsgActionDetailDao {
	public MsgActionDetailVo getByActionId(String actionId);
	public MsgActionDetailVo getByPrimaryKey(int rowId);
	public List<MsgActionDetailVo> getAll();
	public List<String> getActionIds();
	public int update(MsgActionDetailVo msgActionDetailVo);
	public int deleteByActionId(String actionId);
	public int deleteByPrimaryKey(int rowId);
	public int insert(MsgActionDetailVo msgActionDetailVo);
}
