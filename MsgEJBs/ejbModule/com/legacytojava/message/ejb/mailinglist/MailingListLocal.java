package com.legacytojava.message.ejb.mailinglist;
import java.util.Map;

import javax.ejb.Local;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;

@Local
public interface MailingListLocal {
	public int sendMail(String toAddr, Map<String, String> variables, String templateId)
			throws DataValidationException, TemplateNotFoundException, OutOfServiceException;
	public int broadcast(String templateId) throws OutOfServiceException,
	TemplateNotFoundException, DataValidationException;
	public int broadcast(String templateId, String listId) throws OutOfServiceException,
	TemplateNotFoundException, DataValidationException;
	public int subscribe(String emailAddr, String listId) throws DataValidationException;
	public int unSubscribe(String emailAddr, String listId) throws DataValidationException;
	public int optInRequest(String emailAddr, String listId) throws DataValidationException;
	public int optInConfirm(String emailAddr, String listId) throws DataValidationException;
	public int updateOpenCount(long emailAddrId, String listId) throws DataValidationException;
	public int updateClickCount(long emailAddrId, String listId) throws DataValidationException;
	public int updateMsgOpenCount(long msgId);
	public int updateMsgClickCount(long msgId);
}
