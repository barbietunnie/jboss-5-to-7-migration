package com.es.ejb.idtokens;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService (targetNamespace = "http://com.es.ws.idtokens/wsdl")
public interface IdTokensWs {

	@WebMethod
	public IdTokensVo getBySenderId(String senderId);
	
	@WebMethod
	public List<IdTokensVo> getAll();
}
