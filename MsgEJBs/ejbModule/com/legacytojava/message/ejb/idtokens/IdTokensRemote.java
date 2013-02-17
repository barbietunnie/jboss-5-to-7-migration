package com.legacytojava.message.ejb.idtokens;
import java.util.List;

import javax.ejb.Remote;

import com.legacytojava.message.vo.IdTokensVo;

@Remote
public interface IdTokensRemote {
	public IdTokensVo findByClientId(String senderId);
	public List<IdTokensVo> findAll();
	public int insert(IdTokensVo idTokensVo);
	public int update(IdTokensVo idTokensVo);
	public int delete(String senderId);
	public com.legacytojava.message.jpa.model.IdTokens getByClientId(String senderId);
}
