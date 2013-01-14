package com.legacytojava.message.bo.mailinglist;

import java.util.Map;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;

public interface MailingListBo {
	public int broadcast(String templateId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException;
	
	public int broadcast(String templateId, String listId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException;
	
	public int send(String toAddr, Map<String, String> variables, String templateId)
			throws DataValidationException, TemplateNotFoundException, OutOfServiceException;
	
	public int subscribe(String emailAddr, String listId) throws DataValidationException;

	public int unSubscribe(String emailAddr, String listId) throws DataValidationException;
	
	public int optInRequest(String emailAddr, String listId) throws DataValidationException;
	
	public int optInConfirm(String emailAddr, String listId) throws DataValidationException;
	
	public int updateSentCount(long emailAddrId, String listId) throws DataValidationException;
	
	public int updateOpenCount(long emailAddrId, String listId) throws DataValidationException;
	
	public int updateClickCount(long emailAddrId, String listId) throws DataValidationException;
	
	public int updateSentCount(long msgId, int count);
	
	public int updateOpenCount(long msgId, int count);
	
	public int updateClickCount(long msgId, int count);
}
