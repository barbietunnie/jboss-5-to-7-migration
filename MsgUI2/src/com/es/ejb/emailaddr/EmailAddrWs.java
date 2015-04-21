package com.es.ejb.emailaddr;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService (targetNamespace = "http://com.es.ws.emailaddr/wsdl")
public interface EmailAddrWs {

	@WebMethod
	public EmailAddrVo getOrAddAddress(String address);
	
	@WebMethod
	public int delete(String address);
}
