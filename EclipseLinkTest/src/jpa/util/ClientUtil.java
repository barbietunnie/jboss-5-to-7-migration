package jpa.util;

import java.util.Calendar;
import java.util.Date;

import jpa.service.ClientDataService;

import org.apache.log4j.Logger;

public final class ClientUtil {
	static final Logger logger = Logger.getLogger(ClientUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private static ClientDataService clientService = null;
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

	private static ClientDataService getClientDataService() {
		if(clientService == null) {
			clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		}
		return clientService;
	}

	public static boolean isTrialPeriodEnded() {
		String systemId = getClientDataService().getSystemId();
		if (systemId != null) {
			String db2ts = null;
			try {
				db2ts = TimestampUtil.decimalStringToDb2(systemId);
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
		String key = getClientDataService().getSystemKey();
		return (ProductKey.validateKey(key) | ProductUtil.isProductKeyValid());
	}

//	public ClientVo getDefaultClientVo() {
//		return getSiteProfile(DEFAULT_CLIENTID);
//	}
//	
//	public ClientVo getClientVo(String clientId) {
//		return getSiteProfile(clientId);
//	}
//	
//	public ClientVo getSiteProfile(String clientId) {
//		if (StringUtil.isEmpty(clientId)) {
//			clientId = DEFAULT_CLIENTID;
//		}
//		ClientVo vo = getClientDao().getByClientId(clientId);
//		if (vo != null) {
//			return vo;
//		}
//		else {
//			vo = getClientDao().getByClientId(DEFAULT_CLIENTID);
//			if (vo != null) {
//				return vo;
//			}
//			else {
//				throw new RuntimeException("Clients table missing: " + DEFAULT_CLIENTID);
//			}
//		}
//	}
}
