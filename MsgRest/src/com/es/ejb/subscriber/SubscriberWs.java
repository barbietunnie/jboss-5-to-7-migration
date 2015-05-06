package com.es.ejb.subscriber;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import jpa.model.Subscription;

@WebService (targetNamespace = "http://com.es.ws.subscriber/wsdl")
public interface SubscriberWs {

	@WebMethod
	public Subscription subscribe(@WebParam(name="emailAddr") String emailAddr, @WebParam(name="listId") String listId);
	
	@WebMethod
	public Subscription unSubscribe(@WebParam(name="emailAddr") String emailAddr, @WebParam(name="listId") String listId);
}
