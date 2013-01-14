package com.legacytojava.message.dao.inbox;

import java.util.List;

import com.legacytojava.message.vo.inbox.MsgHeadersVo;

public interface MsgHeadersDao {
	public MsgHeadersVo getByPrimaryKey(long msgId, int headerSeq);
	public List<MsgHeadersVo> getByMsgId(long msgId);
	public int update(MsgHeadersVo msgHeadersVo);
	public int deleteByPrimaryKey(long msgId, int headerSeq);
	public int deleteByMsgId(long msgId);
	public int insert(MsgHeadersVo msgHeadersVo);
}
