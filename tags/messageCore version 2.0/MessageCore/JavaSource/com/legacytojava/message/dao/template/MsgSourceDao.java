package com.legacytojava.message.dao.template;

import java.util.List;

import com.legacytojava.message.vo.template.MsgSourceVo;

public interface MsgSourceDao {
	public MsgSourceVo getByPrimaryKey(String msgSourceId);
	public List<MsgSourceVo> getByFromAddrId(long fromAddrId);
	public int update(MsgSourceVo msgSourceVo);
	public int deleteByPrimaryKey(String msgSourceId);
	public int deleteByFromAddrId(long fromAddrId);
	public int insert(MsgSourceVo msgSourceVo);
}
