package com.legacytojava.message.dao.action;

import java.util.List;

import com.legacytojava.message.vo.action.MsgDataTypeVo;

public interface MsgDataTypeDao {
	public MsgDataTypeVo getByTypeValuePair(String type, String value);
	public MsgDataTypeVo getByPrimaryKey(int rowId);
	public List<MsgDataTypeVo> getByDataType(String dataType);
	public List<String> getDataTypes();
	public int update(MsgDataTypeVo msgDataTypeVo);
	public int deleteByPrimaryKey(int rowId);
	public int deleteByDataType(String dataType);
	public int insert(MsgDataTypeVo msgDataTypeVo);
}
