package com.es.ejb.idtokens;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.es.ejb.vo.IdTokensVo;

@WebService (targetNamespace = "http://com.es.ws.idtokens/wsdl")
public interface IdTokensWs {

	@WebMethod
	public IdTokensVo getBySenderId(String senderId);
	
	@WebMethod
	public List<IdTokensVo> getAll();
	
	@WebMethod
	public void update(IdTokensVo vo);
}
