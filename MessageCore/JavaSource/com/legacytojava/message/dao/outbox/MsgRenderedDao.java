package com.legacytojava.message.dao.outbox;

import java.util.List;

import com.legacytojava.message.vo.outbox.MsgRenderedVo;

public interface MsgRenderedDao {
	public MsgRenderedVo getByPrimaryKey(long renderId);
	public MsgRenderedVo getLastRecord();
	public List<MsgRenderedVo> getByMsgSourceId(String msgSourceId);
	public int update(MsgRenderedVo msgRenderedVo);
	public int deleteByPrimaryKey(long renderId);
	public int insert(MsgRenderedVo msgRenderedVo);
}
