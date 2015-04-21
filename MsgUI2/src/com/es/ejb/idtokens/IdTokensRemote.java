package com.es.ejb.idtokens;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.ejb.Remote;

@Remote
public interface IdTokensRemote {
	public Future<?> stayBusy(CountDownLatch ready);
	public IdTokensVo findBySenderId(String senderId);
	public List<IdTokensVo> findAll();
}
