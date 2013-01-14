package com.legacytojava.message.bo.mailsender;

import java.util.List;
import java.util.Vector;

import com.legacytojava.jbatch.pool.NamedPools;
import com.legacytojava.jbatch.pool.ObjectPool;
import com.legacytojava.jbatch.smtp.SmtpConnection;

public class TestNamedPool implements Runnable {

	static NamedPools pools = SmtpWrapperUtil.getSmtpNamedPools();
	static int poolSize;
	static Vector<SmtpConnection> conns = new Vector<SmtpConnection>();
	
	static {
		List<ObjectPool> objPools = pools.getPools();
		int _size = 0;
		for (int i = 0; i < objPools.size(); i++) {
			ObjectPool pool = objPools.get(i);
			_size += pool.getSize();
		}
		poolSize = _size + 10;
	}
	
	String runMode;
	
	public static void main(String[] args) {
		Thread test1 = new Thread(new TestNamedPool("get"));
		Thread test2 = new Thread(new TestNamedPool("put"));
		try {
			test1.start();
			Thread.sleep(1000);
			test2.start();
			test1.join();
			test2.join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public TestNamedPool(String runMode) {
		this.runMode = runMode;
		System.out.println("Pool Size = " + poolSize + ", RunMode = " + runMode);
	}
	
 	public void run() {
 		if ("get".equals(runMode)) {
			try {
				for (int i = 0; i < poolSize; i++) {
					SmtpConnection conn = (SmtpConnection) pools.getConnection();
					conn.testConnection(true);
					conns.add(conn);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
 		}
 		else if ("put".equals(runMode)) {
			try {
				for (int i = 0; i < poolSize; i++) {
					Thread.sleep(50);
					while (conns.size() == 0) {
						Thread.sleep(100);
					}
					SmtpConnection conn = conns.remove(0);
					pools.returnConnection(conn);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
 		}
	}
}
