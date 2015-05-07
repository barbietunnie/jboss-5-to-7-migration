package com.es.ejb.mailsender;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService (targetNamespace = "http://com.es.ws.mailsender/wsdl")
public interface MailSenderWs {

	@WebMethod
	public void sendMail(@WebParam(name = "fromAddress") String fromAddr,
			@WebParam(name = "toAddress") String toAddr,
			@WebParam(name = "subject") String subject,
			@WebParam(name = "body") String body);
	
	@WebMethod
	public void sendMailToSite(@WebParam(name = "siteId") String siteId,
			@WebParam(name = "fromAddress") String fromAddr,
			@WebParam(name = "subject") String subject,
			@WebParam(name = "body") String body);
}
