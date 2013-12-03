package com.es.bo.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.es.core.util.SpringUtil;
import com.es.dao.mailbox.MailBoxDao;
import com.es.vo.comm.MailBoxVo;

public class MailReaderMain {
	static final Logger logger = Logger.getLogger( MailReaderMain.class);

	public static void main(String[] args) {
		MailBoxDao mailBoxDao = SpringUtil.getAppContext().getBean(MailBoxDao.class);
		List<MailBoxVo> mboxes = mailBoxDao.getAll(true);
		List<Thread> threads = new ArrayList<Thread>();
		Random random = new Random();
		for (MailBoxVo mbox : mboxes) {
			try {
				//mbox.setFromTimer(true);
				MailReaderBo reader = new MailReaderBo(mbox);
				Thread thread = new Thread(reader, mbox.getUserId() + "@" + mbox.getHostName());
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
