package com.legacytojava.message.dao.client;

import static com.legacytojava.message.constant.Constants.DEFAULT_CLIENTID;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.ProductUtil;
import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.common.ProductKey;
import com.legacytojava.jbatch.common.TimestampUtil;
import com.legacytojava.message.util.StringUtil;
import com.legacytojava.message.vo.ClientVo;

public final class ClientUtil {
	static final Logger logger = Logger.getLogger(ClientUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private static ClientDao clientDao = null;
	private static final int TRIAL_DAYS = 30;
	
	private ClientUtil() {
		// static only
	}
	
	public static void main(String[] args){
		try {
			System.out.println("Trial Ended? " + isTrialPeriodEnded());
			System.out.println("ProductKey Valid? " + isProductKeyValid());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static boolean isTrialPeriodEnded() {
		String systemId = getClientDao().getSystemId();
		if (systemId != null) {
			String db2ts = null;
			try {
				db2ts = TimestampUtil.decStrToDb2(systemId);
			}
			catch (NumberFormatException e) {
				logger.error("Failed to convert the SystemId: " + systemId, e);
				return true;
			}
			Date date = null;
			try {
				date = TimestampUtil.db2ToDate(db2ts);
			}
			catch (NumberFormatException e) {
				logger.error("Failed to parse the timestamp: " + db2ts, e);
				return true;
			}
			Calendar now = Calendar.getInstance();
			Calendar exp = Calendar.getInstance();
			exp.setTime(date);
			exp.add(Calendar.DAY_OF_YEAR, TRIAL_DAYS);
			if (now.before(exp)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check product key
	 * @return true if either productkey.txt or clients.SystemKey is valid.
	 */
	public static boolean isProductKeyValid() {
		String key = getClientDao().getSystemKey();
		return (ProductKey.validateKey(key) | ProductUtil.isProductKeyValid());
	}

	public static ClientVo getDefaultClientVo() {
		return getSiteProfile(DEFAULT_CLIENTID);
	}
	
	public static ClientVo getClientVo(String clientId) {
		return getSiteProfile(clientId);
	}
	
	public static ClientVo getSiteProfile(String clientId) {
		if (StringUtil.isEmpty(clientId)) {
			clientId = DEFAULT_CLIENTID;
		}
		ClientVo vo = getClientDao().getByClientId(clientId);
		if (vo != null) {
			return vo;
		}
		else {
			vo = getClientDao().getByClientId(DEFAULT_CLIENTID);
			if (vo != null) {
				return vo;
			}
			else {
				throw new RuntimeException("Clients table missing: " + DEFAULT_CLIENTID);
			}
		}
	}
	
	private static ClientDao getClientDao() {
		if (clientDao == null) {
			clientDao = (ClientDao)SpringUtil.getDaoAppContext().getBean("clientDao");
		}
		return clientDao;
	}
}
