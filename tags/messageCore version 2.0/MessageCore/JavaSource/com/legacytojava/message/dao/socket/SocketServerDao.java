package com.legacytojava.message.dao.socket;

import java.util.List;

import com.legacytojava.message.vo.SocketServerVo;

public interface SocketServerDao {
	public SocketServerVo getByPrimaryKey(String serverName);
	public List<SocketServerVo> getAll(boolean onlyActive);
	public int update(SocketServerVo socketServerVo);
	public int deleteByPrimaryKey(String serverName);
	public int insert(SocketServerVo socketServerVo);
}
