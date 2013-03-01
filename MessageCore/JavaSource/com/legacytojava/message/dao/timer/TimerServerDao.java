package com.legacytojava.message.dao.timer;

import java.util.List;

import com.legacytojava.message.vo.TimerServerVo;

public interface TimerServerDao {
	public TimerServerVo getByPrimaryKey(String serverName);
	public List<TimerServerVo> getAll(boolean onlyActive);
	public int update(TimerServerVo timerServerVo);
	public int deleteByPrimaryKey(String serverName);
	public int insert(TimerServerVo timerServerVo);
}
