package com.legacytojava.message.dao.client;

import java.util.List;

import com.legacytojava.message.vo.ClientVo;

public interface ClientDao {
	public ClientVo getByClientId(String clientId);
	public ClientVo getByDomainName(String domainName);
	public List<ClientVo> getAll();
	public List<ClientVo> getAllForTrial();
	public int update(ClientVo clientVo);
	public int delete(String clientId);
	public int insert(ClientVo clientVo);
	public String getSystemId();
	public String getSystemKey();
	public int updateSystemKey(String key);
}
