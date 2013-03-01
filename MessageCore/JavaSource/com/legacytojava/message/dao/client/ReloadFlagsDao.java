package com.legacytojava.message.dao.client;

import com.legacytojava.message.vo.ReloadFlagsVo;

public interface ReloadFlagsDao {
	public ReloadFlagsVo select();
	public int update(ReloadFlagsVo vo);
	public int updateClientReloadFlag();
	public int updateRuleReloadFlag();
	public int updateActionReloadFlag();
	public int updateTemplateReloadFlag();
	public int updateScheduleReloadFlag();
}
