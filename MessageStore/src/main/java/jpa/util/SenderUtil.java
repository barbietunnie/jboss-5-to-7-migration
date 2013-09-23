package jpa.util;

import java.util.Calendar;
import java.util.Date;

import jpa.service.SenderDataService;

import org.apache.log4j.Logger;

public final class SenderUtil {
	static final Logger logger = Logger.getLogger(SenderUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private static SenderDataService senderService = null;
	private static final int TRIAL_DAYS = 30;
	
	private SenderUtil() {
		// static only
	}
	
	public static void main(String[] args){
		try {
			logger.info("Trial Ended? " + isTrialPeriodEnded());
			logger.info("ProductKey Valid? " + isProductKeyValid());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static SenderDataService getSenderDataService() {
		if(senderService == null) {
			senderService = (SenderDataService) SpringUtil.getAppContext().getBean("senderDataService");
		}
		return senderService;
	}

	public static boolean isTrialPeriodEnded() {
		String systemId = getSenderDataService().getSystemId();
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
				java.sql.Timestamp tms = TimestampUtil.db2ToTimestamp(db2ts);
				date = new Date(tms.getTime());
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
	 * @return true if either productkey.txt or senders.SystemKey is valid.
	 */
	public static boolean isProductKeyValid() {
		String key = getSenderDataService().getSystemKey();
		return (ProductKey.validateKey(key) | ProductUtil.isProductKeyValid());
	}

//	public SenderVo getDefaultSenderVo() {
//		return getSiteProfile(DEFAULT_SENDER_ID);
//	}
//	
//	public SenderVo getSenderVo(String senderId) {
//		return getSiteProfile(senderId);
//	}
//	
//	public SenderVo getSiteProfile(String senderId) {
//		if (StringUtil.isEmpty(senderId)) {
//			senderId = DEFAULT_SENDER_ID;
//		}
//		SenderVo vo = getSenderDao().getBySenderId(senderId);
//		if (vo != null) {
//			return vo;
//		}
//		else {
//			vo = getSenderDao().getBySenderId(DEFAULT_SENDER_ID);
//			if (vo != null) {
//				return vo;
//			}
//			else {
//				throw new RuntimeException("Senders table missing: " + DEFAULT_SENDER_ID);
//			}
//		}
//	}
}
