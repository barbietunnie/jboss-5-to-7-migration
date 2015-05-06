package com.es.ejb.tracking;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService (targetNamespace = "http://com.es.ws.sbsrtracking/wsdl")
public interface SbsrTrackingWs {

	@WebMethod
	public int updateOpenCount(@WebParam(name="trackingId") int trkRowId);
	@WebMethod
	public int updateClickCount(@WebParam(name="trackingId") int trkRowId);
	
	@WebMethod
	public int updateMsgOpenCount(@WebParam(name="emailAddrId") int emailAddrRowId, @WebParam(name="listId") String listId);
	@WebMethod
	public int updateMsgClickCount(@WebParam(name="emailAddrId") int emailAddrRowId, @WebParam(name="listId") String listId);	
}
