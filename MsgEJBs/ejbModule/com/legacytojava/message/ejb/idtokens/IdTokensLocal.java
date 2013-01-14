package com.legacytojava.message.ejb.idtokens;
import java.util.List;

import javax.ejb.Local;

import com.legacytojava.message.vo.IdTokensVo;

@Local
public interface IdTokensLocal {
	public IdTokensVo findByClientId(String senderId);
	public List<IdTokensVo> findAll();
	public int insert(IdTokensVo idTokensVo);
	public int update(IdTokensVo idTokensVo);
	public int delete(String senderId);
}
