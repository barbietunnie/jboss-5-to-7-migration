package com.legacytojava.message.ejb.client;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.legacytojava.message.ejb.emailaddr.EmailAddrRemote;
import com.legacytojava.message.ejb.mailinglist.MailingListRemote;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.exception.OutOfServiceException;
import com.legacytojava.message.exception.TemplateNotFoundException;
import com.legacytojava.message.util.LookupUtil;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

/**
 * this class tests both MailingList EJB's
 */
public class MailingListClient {
	static final Logger logger = Logger.getLogger(MailingListClient.class);
	public static void main(String[] args) {
		MailingListClient mailingListClient = new MailingListClient();
		try {
			if (true) {
				mailingListClient.broadcast("SampleNewsletter2");
			}
			if (true) {
				String toAddr = "testto@localhost";
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("CustomerName", "List Subscriber");
				mailingListClient.sendMail(toAddr, vars, "SampleNewsletter2");
			}
			if (true) {
				mailingListClient.unSubscribe("twang@localhost", "SMPLLST1");
			}
			if (true) {
				mailingListClient.subscribe("twang@localhost", "SMPLLST1");
			}
			if (true) {
				mailingListClient.updateClickCount("jsmith@test.com", "SMPLLST1");
			}
			if (true) {
				mailingListClient.optInRequest("test2@test.com", "SMPLLST1");
			}
			if (true) {
				mailingListClient.optInConfirm("test2@test.com", "SMPLLST1");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void sendMail(String toAddr, Map<String, String> variables, String templateId)
			throws NamingException, RemoteException, CreateException, DataValidationException,
			TemplateNotFoundException, OutOfServiceException {
		MailingListRemote mailingList = getMailingListRemote();
		int mailsSent = mailingList.sendMail(toAddr, variables, templateId);
		if (mailsSent > 0) {
			logger.info("Email sent to: " + toAddr);
		}
		else {
			logger.error("sendMail() - Failed to send email to: " + toAddr);
		}
	}

	public void broadcast(String templateId) throws NamingException, RemoteException,
			CreateException, OutOfServiceException, TemplateNotFoundException,
			DataValidationException {
		MailingListRemote mailingList = getMailingListRemote();
		int mailsSent = mailingList.broadcast(templateId);
		logger.info("Number of Email Addresses broadcasted to: " + mailsSent);
	}

	public void subscribe(String emailAddr, String listId) throws NamingException, RemoteException,
			CreateException, DataValidationException {
		MailingListRemote mailingList = getMailingListRemote();
		int mailsSent = mailingList.subscribe(emailAddr, listId);
		if (mailsSent > 0) {
			logger.info("Email address " + emailAddr + " has been subscribed to " + listId);
		}
		else {
			logger.info("Email address " + emailAddr + " was already subscribed to " + listId);
		}
	}

	public void unSubscribe(String emailAddr, String listId) throws NamingException,
			RemoteException, CreateException, DataValidationException {
		MailingListRemote mailingList = getMailingListRemote();
		int mailsSent = mailingList.unSubscribe(emailAddr, listId);
		if (mailsSent > 0) {
			logger.info("Email address " + emailAddr + " has been removed to " + listId);
		}
		else {
			logger.info("Email address " + emailAddr + " was already removed to " + listId);
		}
	}
	
	public void optInRequest(String emailAddr, String listId) throws NamingException, RemoteException,
			CreateException, DataValidationException {
		MailingListRemote mailingList = getMailingListRemote();
		int mailsSent = mailingList.optInRequest(emailAddr, listId);
		if (mailsSent > 0) {
			logger.info("Email address " + emailAddr + " has been optIn'ed to " + listId);
		}
		else {
			logger.info("Email address " + emailAddr + " was already optIn'ed to " + listId);
		}
	}

	public void optInConfirm(String emailAddr, String listId) throws NamingException,
			RemoteException, CreateException, DataValidationException {
		MailingListRemote mailingList = getMailingListRemote();
		int mailsSent = mailingList.optInConfirm(emailAddr, listId);
		if (mailsSent > 0) {
			logger.info("Email address " + emailAddr + " has been confirmed to " + listId);
		}
		else {
			logger.info("Email address " + emailAddr + " was already confirmed to " + listId);
		}
	}

	public void updateClickCount(String addr, String listId) throws NamingException, RemoteException,
			CreateException, DataValidationException {
		EmailAddrRemote emailAddr = (EmailAddrRemote)LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/EmailAddr!com.legacytojava.message.ejb.emailaddr.EmailAddrRemote");
		MailingListRemote mailingList = getMailingListRemote();
		EmailAddrVo addrVo = emailAddr.findByAddress(addr);
		int rowsUpdated = mailingList.updateClickCount(addrVo.getEmailAddrId(), listId);
		logger.info("updateClickCount() rows updated: " + rowsUpdated);
	}
	
	MailingListRemote getMailingListRemote() {
		return (MailingListRemote) LookupUtil.lookupRemoteEjb("ejb:MailEngineEar/MsgEJBs/MailingList!com.legacytojava.message.ejb.mailinglist.MailingListRemote");
	}

}
