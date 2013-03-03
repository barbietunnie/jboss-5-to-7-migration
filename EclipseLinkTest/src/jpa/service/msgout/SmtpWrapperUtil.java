package jpa.service.msgout;

import java.util.ArrayList;
import java.util.List;

import jpa.model.SmtpServer;
import jpa.util.SpringUtil;

public class SmtpWrapperUtil {
	private static SmtpServerService smtpServerDao = null;
	private static java.util.Date lastGetTime = new java.util.Date();
	private static NamedPools smtpPools = null;
	private static NamedPools secuPools = null;
	
	public static void main(String[] args) {
		NamedPools pools = getSmtpNamedPools();
		for (String name :pools.getNames()) {
			System.out.println("Pool name: " + name);
		}
		List<ObjectPool> objPools = pools.getPools();
		int _size = 0;
		for (int i = 0; i < objPools.size(); i++) {
			ObjectPool pool = objPools.get(i);
			_size += pool.getSize();
		}
		System.out.println("Total Connections: " + _size);
		SmtpConnection[] conns = new SmtpConnection[_size];
		try {
			for (int i = 0; i < _size; i++) {
				conns[i] = (SmtpConnection) pools.getConnection();
				conns[i].testConnection(true);
			}
			for (int i = 0; i < _size; i++) {
				pools.returnConnection(conns[i]);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public synchronized static NamedPools getSmtpNamedPools() {
		refreshPools();
		return smtpPools;
	}
	
	public synchronized static NamedPools getSecuNamedPools() {
		refreshPools();
		return secuPools;
	}

	public synchronized static void clearSmtpNamedPools() {
		if (smtpPools != null) {
			if (!smtpPools.isEmpty())
				smtpPools.close();
			smtpPools = null;
		}
	}

	public synchronized static void clearSecuNamedPools() {
		if (secuPools != null) {
			if (!secuPools.isEmpty())
				secuPools.close();
			secuPools = null;
		}
	}

	private static void refreshPools() {
		java.util.Date currTime = new java.util.Date();
		if (smtpPools == null || secuPools == null || (smtpPools.isEmpty() && secuPools.isEmpty())
				|| (currTime.getTime() - lastGetTime.getTime()) > (15 * 60 * 1000)) {
			clearSmtpNamedPools();
			clearSecuNamedPools();
			List<SmtpServer> smtpConnVos = getSmtpServers(false);
			smtpPools =getNamedPools(smtpConnVos);
			List<SmtpServer> secuConnVos = getSmtpServers(true);
			secuPools =getNamedPools(secuConnVos);
			lastGetTime = currTime;
		}
	}

	/*
	 * returns a list of named pools or an empty list
	 */
	private static NamedPools getNamedPools(List<SmtpServer> smtpConnVos) {
		List<ObjectPool> objPools = new ArrayList<ObjectPool>();
		for (int i = 0; i < smtpConnVos.size(); i++) {
			SmtpServer smtpConnVo = smtpConnVos.get(i);
			ObjectPool smtpPool = new ObjectPool(smtpConnVo);
			objPools.add(smtpPool);
		}
		NamedPools pools = new NamedPools(objPools);
		return pools;
	}

	/*
	 * returns a list or SmtpServer's or an empty list 
	 */
	private static List<SmtpServer> getSmtpServers(boolean isSecure) {
		List<SmtpServer> list = null;
		list = getSmtpServerService().getAll(true, isSecure);
		return list;
	}
	
	public static SmtpServerService getSmtpServerService() {
		if (smtpServerDao == null) {
			smtpServerDao = (SmtpServerService) SpringUtil.getAppContext().getBean("smtpServerService");
		}
		return smtpServerDao;
	}
}
