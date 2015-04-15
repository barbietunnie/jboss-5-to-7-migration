package com.es.idtokens.ejb;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import javax.ejb.Local;

import jpa.model.IdTokens;

@Local
public interface IdTokensLocal {
	public Future<?> stayBusy(CountDownLatch ready);
	public IdTokens findBySenderId(String senderId);
	public List<IdTokens> findAll();
	public void insert(IdTokens idTokens);
	public void update(IdTokens idTokens);
	public void delete(String senderId);
}
