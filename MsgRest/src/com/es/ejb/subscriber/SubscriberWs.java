package com.es.ejb.subscriber;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import jpa.model.Subscription;

import com.es.ejb.ws.vo.SubscriberDataVo;

@WebService (targetNamespace = "http://com.es.ws.subscriber/wsdl")
public interface SubscriberWs {

	@WebMethod
	public Subscription subscribe(@WebParam(name="emailAddr") String emailAddr, @WebParam(name="listId") String listId);
	
	@WebMethod
	public Subscription unSubscribe(@WebParam(name="emailAddr") String emailAddr, @WebParam(name="listId") String listId);
	
	@WebMethod
	public SubscriberDataVo getSubscriberData(@WebParam(name="emailAddr") String emailAddr);
	
	@WebMethod
	public void addSubscriber(SubscriberDataVo vo);
}
