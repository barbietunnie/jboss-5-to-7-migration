package com.legacytojava.message.dao.emailaddr;

import java.util.List;

import com.legacytojava.message.vo.emailaddr.UnsubCommentsVo;

public interface UnsubCommentsDao {
	public UnsubCommentsVo getByPrimaryKey(int rowId);
	public List<UnsubCommentsVo> getAll();
	public List<UnsubCommentsVo> getByEmailAddrId(long emailAddrId);
	public List<UnsubCommentsVo> getByListId(String listId);
	public int update(UnsubCommentsVo unsubCommentsVo);
	public int deleteByPrimaryKey(int rowId);
	public int deleteByEmailAddrId(long emailAddrId);
	public int insert(UnsubCommentsVo unsubCommentsVo);
}
