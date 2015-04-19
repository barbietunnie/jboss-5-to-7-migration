package com.es.subscriber.ejb;
import java.util.List;

import javax.ejb.Remote;

import jpa.exception.DataValidationException;
import jpa.model.SubscriberData;
import jpa.model.Subscription;

@Remote
public interface SubscriberRemote {
	public List<SubscriberData> getAllSubscribers();
	public SubscriberData getSubscriberById(String subrId) throws DataValidationException;
	public SubscriberData getSubscriberByEmailAddress(String emailAddr);

	public Subscription subscribe(String emailAddr, String listId) throws DataValidationException;
	public Subscription unSubscriber(String emailAddr, String listId) throws DataValidationException;

	public void insertSubscriber(SubscriberData vo) throws DataValidationException;
	public void updateSubscriber(SubscriberData vo) throws DataValidationException;
	public void deleteSubscriber(SubscriberData vo) throws DataValidationException;
	
	public Subscription optInRequest(String emailAddr, String listId) throws DataValidationException;
	public Subscription optInConfirm(String emailAddr, String listId) throws DataValidationException;
}
