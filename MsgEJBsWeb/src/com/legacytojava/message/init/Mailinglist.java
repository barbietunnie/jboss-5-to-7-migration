package com.legacytojava.message.init;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.legacytojava.message.ejb.mailinglist.MailingListLocal;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;

public class Mailinglist {
	protected static final Logger logger = Logger.getLogger(Mailinglist.class);

	//@EJB(beanInterface=MailingListRemote.class)
	private MailingListLocal mailingList;

	public Mailinglist() {
		mailingList = (MailingListLocal) LookupUtil.lookupLocalEjb("java:app/MsgEJBs/MailingList!com.legacytojava.message.ejb.mailinglist.MailingListLocal");
	}

	public int subscribe(String emailAddr, String listId) throws DataValidationException {
		int emailsSent = mailingList.subscribe(emailAddr, listId);
		logger.info("subscribe() - Email: " + emailAddr + " subscribed to: " + listId);
		return emailsSent;
	}
	
	public int unSubscribe(String emailAddr, String listId) throws DataValidationException {
		int emailsSent = mailingList.unSubscribe(emailAddr, listId);
		logger.info("unSubscribe() - Email: " + emailAddr + " removed from: " + listId);
		return emailsSent;
	}
	
	public void optInRequest(String emailAddr, String listId) throws DataValidationException {
		int mailsSent = mailingList.optInRequest(emailAddr, listId);
		if (mailsSent > 0) {
			logger.info("Email address " + emailAddr + " has been optIn'ed to " + listId);
		}
		else {
			logger.info("Email address " + emailAddr + " was already optIn'ed to " + listId);
		}
	}

	public void optInConfirm(String emailAddr, String listId) throws DataValidationException {
		int mailsSent = mailingList.optInConfirm(emailAddr, listId);
		if (mailsSent > 0) {
			logger.info("Email address " + emailAddr + " has been confirmed to " + listId);
		}
		else {
			logger.info("Email address " + emailAddr + " was already confirmed to " + listId);
		}
	}

	public int sendMail(String toAddr, VariableDto[] variables, String templateId)
			throws DataValidationException, TemplateNotFoundException, OutOfServiceException {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; variables != null && i < variables.length; i++) {
			VariableDto dto = variables[i];
			map.put(dto.getName(), dto.getValue());
		}
		int emailsSent = mailingList.sendMail(toAddr, map, templateId);
		logger.info("sendMail() - Email sent to: " + toAddr + ", TemplateId: " + templateId);
		return emailsSent;
	}
	
	public int updateOpenCount(long emailAddrId, String listId) throws DataValidationException {
		int emailsSent = mailingList.updateOpenCount(emailAddrId, listId);
		logger.info("updateOpenCount() - updated open count for " + emailAddrId + "/" + listId);
		return emailsSent;
	}
	
	public int updateClickCount(long emailAddrId, String listId) throws DataValidationException {
		int emailsSent = mailingList.updateClickCount(emailAddrId, listId);
		logger.info("updateClickCount() - updated click count for " + emailAddrId + "/" + listId);
		return emailsSent;
	}
	
	public int updateMsgOpenCount(long broadcastMsgId) {
		int emailsSent = mailingList.updateMsgOpenCount(broadcastMsgId);
		logger.info("updateMsgOpenCount() - updated open count for " + broadcastMsgId);
		return emailsSent;
	}
	
	public int updateMsgClickCount(long broadcastMsgId) {
		int emailsSent = mailingList.updateMsgClickCount(broadcastMsgId);
		logger.info("updateMsgClickCount() - updated click count for " + broadcastMsgId);
		return emailsSent;
	}

	public static void main(String[] args) {
		Mailinglist mlist = new Mailinglist();
		try {
			mlist.subscribe("jsmith@test.com", "SMPLLST2");
			mlist.unSubscribe("jsmith@test.com", "SMPLLST2");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
