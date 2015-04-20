package com.es.ejb.mailinglist;
import java.util.Map;

import javax.ejb.Local;

import jpa.exception.DataValidationException;
import jpa.exception.OutOfServiceException;
import jpa.exception.TemplateNotFoundException;

@Local
public interface MailingListLocal {
	public int sendMail(String toAddr, Map<String, String> variables,
			String templateId) throws DataValidationException,
			TemplateNotFoundException, OutOfServiceException;

	public int broadcast(String templateId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException;

	public int broadcast(String templateId, String listId)
			throws OutOfServiceException, TemplateNotFoundException,
			DataValidationException;
}
