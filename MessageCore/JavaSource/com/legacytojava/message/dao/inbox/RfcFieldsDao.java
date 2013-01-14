package com.legacytojava.message.dao.inbox;

import java.util.List;

import com.legacytojava.message.vo.inbox.RfcFieldsVo;

public interface RfcFieldsDao {
	public RfcFieldsVo getByPrimaryKey(long msgId, String rfcType);
	public List<RfcFieldsVo> getByMsgId(long msgId);
	public int update(RfcFieldsVo rfcFieldsVo);
	public int deleteByPrimaryKey(long msgId, String rfcType);
	public int deleteByMsgId(long msgId);
	public int insert(RfcFieldsVo rfcFieldsVo);
}
