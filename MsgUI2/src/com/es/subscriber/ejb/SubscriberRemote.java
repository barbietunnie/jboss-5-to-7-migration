package com.es.subscriber.ejb;
import java.util.List;

import javax.ejb.Remote;

import jpa.exception.DataValidationException;
import jpa.model.SubscriberData;

@Remote
public interface SubscriberRemote {
	public List<SubscriberData> getAllSubscribers();
	public SubscriberData getSubscriberById(String subrId) throws DataValidationException;
	public SubscriberData getSubscriberByEmailAddress(String emailAddr);
	public void insertSubscriber(SubscriberData vo) throws DataValidationException;
	public void updateSubscriber(SubscriberData vo) throws DataValidationException;
	public void deleteSubscriber(SubscriberData vo) throws DataValidationException;
}
