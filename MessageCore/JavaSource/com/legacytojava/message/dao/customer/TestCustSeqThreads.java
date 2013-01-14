package com.legacytojava.message.dao.customer;

import com.legacytojava.jbatch.SpringUtil;

public class TestCustSeqThreads implements Runnable {
	final static String LF = System.getProperty("line.separator", "\n");
	static CustSequenceDao custSequenceDao = null;
	
	public static void main(String[] args) {
		custSequenceDao = (CustSequenceDao) SpringUtil.getDaoAppContext().getBean("custSequenceDao");

		try {
			TestCustSeqThreads test = new TestCustSeqThreads();
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
		for (int i = 0; i < 20; i++) {
			long nextVal = custSequenceDao.findNextValue();
			System.out.println(Thread.currentThread().getName() + " - " + nextVal);
		}
	}
}
