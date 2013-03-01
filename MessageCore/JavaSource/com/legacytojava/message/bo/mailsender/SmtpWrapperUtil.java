package com.legacytojava.message.bo.mailsender;

import java.util.ArrayList;
import java.util.List;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.pool.NamedPools;
import com.legacytojava.jbatch.pool.ObjectPool;
import com.legacytojava.jbatch.smtp.SmtpConnection;
import com.legacytojava.message.dao.client.ClientUtil;
import com.legacytojava.message.dao.smtp.SmtpServerDao;
import com.legacytojava.message.vo.SmtpConnVo;

public class SmtpWrapperUtil {
	private static SmtpServerDao smtpServerDao = null;
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
			List<SmtpConnVo> smtpConnVos = getSmtpConnVos(false);
			smtpPools =getNamedPools(smtpConnVos);
			List<SmtpConnVo> secuConnVos = getSmtpConnVos(true);
			secuPools =getNamedPools(secuConnVos);
			lastGetTime = currTime;
		}
	}

	/*
	 * returns a list of named pools or an empty list
	 */
	private static NamedPools getNamedPools(List<SmtpConnVo> smtpConnVos) {
		List<ObjectPool> objPools = new ArrayList<ObjectPool>();
		for (int i = 0; i < smtpConnVos.size(); i++) {
			SmtpConnVo smtpConnVo = smtpConnVos.get(i);
			ObjectPool smtpPool = new ObjectPool(smtpConnVo);
			objPools.add(smtpPool);
		}
		NamedPools pools = new NamedPools(objPools);
		return pools;
	}

	/*
	 * returns a list or SmtpConnVo's or an empty list 
	 */
	private static List<SmtpConnVo> getSmtpConnVos(boolean isSecure) {
		List<SmtpConnVo> list = null;
		if (ClientUtil.isTrialPeriodEnded() && !ClientUtil.isProductKeyValid()) {
			list = getSmtpServerDao().getBySslFlagForTrial(isSecure, true);
		}
		else {
			list = getSmtpServerDao().getBySslFlag(isSecure, true);
		}
		return list;
	}
	
	public static SmtpServerDao getSmtpServerDao() {
		if (smtpServerDao == null) {
			smtpServerDao = (SmtpServerDao) SpringUtil.getDaoAppContext().getBean("smtpServerDao");
		}
		return smtpServerDao;
	}
}
