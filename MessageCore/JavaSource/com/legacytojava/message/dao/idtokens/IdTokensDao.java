package com.legacytojava.message.dao.idtokens;

import java.util.List;

import com.legacytojava.message.vo.IdTokensVo;

public interface IdTokensDao {
	public IdTokensVo getByClientId(String clientId);
	public List<IdTokensVo> getAll();
	public int update(IdTokensVo idTokensVo);
	public int delete(String clientId);
	public int insert(IdTokensVo idTokensVo);
}
