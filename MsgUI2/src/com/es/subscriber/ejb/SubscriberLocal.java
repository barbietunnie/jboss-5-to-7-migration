package com.es.subscriber.ejb;
import java.util.List;

import javax.ejb.Local;

import jpa.exception.DataValidationException;
import jpa.model.SubscriberData;

@Local
public interface SubscriberLocal {
	public List<SubscriberData> getAllSubscribers();
	public SubscriberData getSubscriberById(String subrId) throws DataValidationException;
	public SubscriberData getSubscriberByEmailAddress(String emailAddr);
	public void insertSubscriber(SubscriberData vo) throws DataValidationException;
	public void updateSubscriber(SubscriberData vo) throws DataValidationException;
	public void deleteSubscriber(SubscriberData vo) throws DataValidationException;
}
