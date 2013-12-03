package com.es.bo.mlist;

import javax.mail.MessagingException;

import com.es.core.util.EmailSender;

public class TestMailingList {

	public static void main(String[] args) {
		TestMailingList test = new TestMailingList();
		try {
			test.subscribe();
			Thread.sleep(20000);
			test.unsubscribe();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void subscribe() throws MessagingException {
		EmailSender.send("testuser@test.com", "demolist1@localhost", "subscribe", "");
	}

	void unsubscribe() throws MessagingException {
		EmailSender.send("testuser@test.com", "demolist1@localhost", "unsubscribe", "");
	}

}
