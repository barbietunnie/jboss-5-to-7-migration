package com.legacytojava.message.ejb.client;

import java.rmi.RemoteException;

import com.legacytojava.message.ejb.emailaddr.EmailAddrRemote;
import com.legacytojava.message.util.LookupUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

public class EmailAddrClient {
	public static boolean TestInsert = false;
	public static void main(String[] args){
		try {
			EmailAddrRemote emailAddr = (EmailAddrRemote)LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/EmailAddr!com.legacytojava.message.ejb.emailaddr.EmailAddrRemote");
			EmailAddrClient test = new EmailAddrClient();
			test.selectByAddrId(emailAddr);
			test.select(emailAddr);
			test.update(emailAddr);
			if (TestInsert) {
				test.insert(emailAddr);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private void selectByAddrId(EmailAddrRemote emailAddr) throws RemoteException {
		EmailAddrVo emailAddrVo = emailAddr.findByAddrId(2);
		if (emailAddr!=null) {
			System.out.println("EmailAddrDao: "+emailAddrVo);
		}
	}
	
	private void select(EmailAddrRemote emailAddr) throws RemoteException {
		EmailAddrVo emailAddrVo = emailAddr.findByAddress("jwang@test.com");
		if (emailAddr!=null) {
			System.out.println("EmailAddrDao: "+emailAddrVo);
		}
	}
	
	private void update(EmailAddrRemote emailAddr) throws RemoteException {
		EmailAddrVo emailAddrVo = emailAddr.findByAddress("jwang@test.com");
		if (emailAddrVo!=null) {
			emailAddrVo.setStatusId("A");
			emailAddr.update(emailAddrVo);
			System.out.println("EmailAddrDao: "+emailAddrVo);
		}
	}
	
	private void insert(EmailAddrRemote emailAddr) throws RemoteException {
		EmailAddrVo emailAddrVo = emailAddr.findByAddress("jwang@test.com");
		if (emailAddrVo!=null) {
			emailAddrVo.setEmailAddr("jwang2@test.com");
			emailAddr.insert(emailAddrVo);
			System.out.println("EmailAddrDao: "+emailAddrVo);
		}
	}
}
