package com.es.ejb.emailaddr;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService (targetNamespace = "http://com.es.ws.emailaddr/wsdl")
public interface EmailAddrWs {

	@WebMethod
	public EmailAddrVo findByAddress(String address);
	
	@WebMethod
	public int deleteByAddress(String address);
}
