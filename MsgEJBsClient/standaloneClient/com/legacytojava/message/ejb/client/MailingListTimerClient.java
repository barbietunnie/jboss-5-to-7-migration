package com.legacytojava.message.ejb.client;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import com.legacytojava.message.ejb.mailinglist.MailingListTimerRemote;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;
import com.legacytojava.message.util.LookupUtil;

/**
 * this class tests both MailingListTimer EJB's
 */
public class MailingListTimerClient {
	public static void main(String[] args){
		try {
			MailingListTimerClient mailingListClient = new MailingListTimerClient();
			mailingListClient.invokeEJBs();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private void invokeEJBs() throws RemoteException, CreateException, OutOfServiceException,
			TemplateNotFoundException, DataValidationException, NamingException {
		MailingListTimerRemote timer = (MailingListTimerRemote) LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/MailingListTimer!com.legacytojava.message.ejb.mailinglist.MailingListTimerRemote");
		timer.scheduleTimerTasks();
	}
	
}
