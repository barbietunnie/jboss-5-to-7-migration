package com.es.ejb.mailinglist;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import jpa.exception.DataValidationException;
import jpa.exception.OutOfServiceException;
import jpa.exception.TemplateNotFoundException;

@Remote
public interface MailingListRemote {
	public List<jpa.model.MailingList> getActiveLists();
	
	public int sendMail(String toAddr, Map<String, String> variables,
			String templateId) throws DataValidationException,
			TemplateNotFoundException, OutOfServiceException;

	public int broadcast(String templateId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException;

	public int broadcast(String templateId, String listId)
			throws OutOfServiceException, TemplateNotFoundException,
			DataValidationException;
}
