package com.lagacytojava.timerejb.client;

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.legacytojava.timerejb.MailReaderRemote;

public class MailReaderClient {
	public static void main(String[] args){
		try {
			MailReaderRemote reader = (MailReaderRemote) lookupRemoteEjb("ejb:MailReaderEar/TimerEJB/MailReader!com.legacytojava.timerejb.MailReaderRemote");
			MailReaderClient test = new MailReaderClient();
			test.startTimer(reader);
			Thread.sleep(25 * 1000);
			test.stopTimer(reader);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private void startTimer(MailReaderRemote reader) throws RemoteException, CreateException,
			NamingException {
		reader.startMailReader(5); // start mail reader in 5 seconds
		System.out.println("MailReaderClient.startTimer: start timer in 5 seconds.");
	}
	
	private void stopTimer(MailReaderRemote reader) throws RemoteException, CreateException {
		reader.stopMailReader(); // stop timer
		System.out.println("MailReaderClient.stopTimer: stop timer.");
	}

    private static Context ctx = null;
	public static Object lookupRemoteEjb(String jndiName) {
		try {
			if (ctx == null) {
				Hashtable<String,String> jndiProperties = new Hashtable<String,String>();
				jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
				ctx = new InitialContext(jndiProperties);
			}
			return ctx.lookup(jndiName);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
}
