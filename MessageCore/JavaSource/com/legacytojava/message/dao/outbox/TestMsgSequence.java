package com.legacytojava.message.dao.outbox;

import com.legacytojava.jbatch.SpringUtil;

public class TestMsgSequence implements Runnable {
	final static String LF = System.getProperty("line.separator", "\n");
	static MsgSequenceDao msgSequenceDao = null;
	
	public static void main(String[] args) {
		msgSequenceDao = (MsgSequenceDao) SpringUtil.getDaoAppContext().getBean("msgSequenceDao");

		try {
			TestMsgSequence test = new TestMsgSequence();
			Thread thread1 = new Thread(test);
			Thread thread2 = new Thread(test);
			Thread thread3 = new Thread(test);
			Thread thread4 = new Thread(test);
			thread1.start();
			thread2.start();
			thread3.start();
			thread4.start();
			thread1.join();
			thread2.join();
			thread3.join();
			thread4.join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void run() {
		for (int i=0; i<20; i++) {
			long nextVal = msgSequenceDao.findNextValue();
			System.out.println(Thread.currentThread().getName() + " - " + nextVal);
		}
	}
}
