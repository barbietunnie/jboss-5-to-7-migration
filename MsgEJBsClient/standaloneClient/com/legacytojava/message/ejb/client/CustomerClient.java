package com.legacytojava.message.ejb.client;

import java.rmi.RemoteException;

import javax.ejb.CreateException;

import com.legacytojava.message.ejb.customer.CustomerRemote;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;
import com.legacytojava.message.util.LookupUtil;
import com.legacytojava.message.vo.CustomerVo;

/**
 * this class tests both CustomerSignup EJB's
 */
public class CustomerClient {
	public static void main(String[] args){
		try {
			CustomerClient customerClient = new CustomerClient();
			customerClient.invokeEJBs();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private void invokeEJBs() throws RemoteException, CreateException, OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		CustomerRemote customer = (CustomerRemote) LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/Customer!com.legacytojava.message.ejb.customer.CustomerRemote");

		int rowsAffected = 0;
		CustomerVo vo = customer.getCustomerByCustId("test");
		//vo.setEmailAddr("test@test.com");
		vo.setCityName("Raleigh");
		rowsAffected = customer.updateCustomer(vo);
		System.out.println("Number of records affected: " + rowsAffected);
	}
	
}
