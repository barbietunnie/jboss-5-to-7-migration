package com.legacytojava.message.ejb.client;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.ejb.idtokens.IdTokensRemote;
import com.legacytojava.message.util.LookupUtil;
import com.legacytojava.message.vo.IdTokensVo;

public class IdTokensClient {
	public static boolean TestInsert = false;
	public static void main(String[] args){
		try {
			IdTokensRemote idTokens = (IdTokensRemote)LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/IdTokens!com.legacytojava.message.ejb.idtokens.IdTokensRemote");
			IdTokensClient test = new IdTokensClient();
			test.select(idTokens);
			test.update(idTokens);
			if (TestInsert) {
				test.insert(idTokens);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	private void select(IdTokensRemote idTokens) throws RemoteException {
		List<?> emailIds = idTokens.findAll();
		for (Iterator<?> it=emailIds.iterator(); it.hasNext();) {
			IdTokensVo idTokensVo = (IdTokensVo)it.next();
			System.out.println("IdTokensDao: "+idTokensVo);
		}
	}
	
	private void update(IdTokensRemote idTokens) throws RemoteException {
		IdTokensVo idTokensVo = idTokens.findByClientId(Constants.DEFAULT_CLIENTID);
		if (idTokensVo!=null) {
			idTokensVo.setDescription("For Default Sender");
			idTokens.update(idTokensVo);
			System.out.println("IdTokensDao: "+idTokensVo);
		}
	}
	
	private void insert(IdTokensRemote idTokens) throws RemoteException {
		IdTokensVo idTokensVo = idTokens.findByClientId(Constants.DEFAULT_CLIENTID);
		if (idTokensVo!=null) {
			idTokensVo.setClientId("JBatchCorp");
			idTokens.insert(idTokensVo);
			System.out.println("IdTokensDao: "+idTokensVo);
		}
	}
}
