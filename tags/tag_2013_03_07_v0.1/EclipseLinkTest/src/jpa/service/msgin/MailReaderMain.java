package jpa.service.msgin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import jpa.model.MailInbox;
import jpa.util.SpringUtil;

public class MailReaderMain {
	static final Logger logger = Logger.getLogger( MailReaderMain.class);

	public static void main(String[] args) {
		MailInboxService mailBoxDao = (MailInboxService) SpringUtil.getAppContext().getBean("mailInboxService");
		List<MailInbox> mboxes = mailBoxDao.getAll(true);
		List<Thread> threads = new ArrayList<Thread>();
		Random random = new Random();
		for (MailInbox mbox : mboxes) {
			try {
				//mbox.setFromTimer(true);
				MailReaderBo reader = new MailReaderBo(mbox);
				Thread thread = new Thread(reader, mbox.getMailInboxPK().toString());
				threads.add(thread);
				try {
					thread.start();
					Thread.sleep(1000 + random.nextInt(5000));
				}
				catch (Exception e) {
					logger.error("Exception caught", e);
				}
			}
			finally {
			}
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {}
		}
		System.exit(0);
	}

}
