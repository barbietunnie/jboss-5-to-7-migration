package com.es.dao.sender;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.es.core.util.ProductKey;
import com.es.core.util.ProductUtil;
import com.es.core.util.SpringUtil;
import com.es.core.util.TimestampUtil;
import com.es.data.constant.Constants;
import com.es.vo.comm.SenderVo;

public final class SenderUtil {
	static final Logger logger = Logger.getLogger(SenderUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private static SenderDao senderDao = null;
	private static final int TRIAL_DAYS = 30;
	
	private SenderUtil() {
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
		String systemId = getSenderDao().getSystemId();
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
				date = TimestampUtil.db2ToTimestamp(db2ts);
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
	 * @return true if either productkey.txt or sender.SystemKey is valid.
	 */
	public static boolean isProductKeyValid() {
		String key = getSenderDao().getSystemKey();
		return (ProductKey.validateKey(key) | ProductUtil.isProductKeyValid());
	}

	public static SenderVo getDefaultSenderVo() {
		return getSiteProfile(Constants.DEFAULT_SENDER_ID);
	}
	
	public static SenderVo getSenderVo(String senderId) {
		return getSiteProfile(senderId);
	}
	
	public static SenderVo getSiteProfile(String senderId) {
		if (StringUtils.isEmpty(senderId)) {
			senderId = Constants.DEFAULT_SENDER_ID;
		}
		SenderVo vo = getSenderDao().getBySenderId(senderId);
		if (vo != null) {
			return vo;
		}
		else {
			vo = getSenderDao().getBySenderId(Constants.DEFAULT_SENDER_ID);
			if (vo != null) {
				return vo;
			}
			else {
				throw new RuntimeException("SenderData table missing: " + Constants.DEFAULT_SENDER_ID);
			}
		}
	}
	
	private static SenderDao getSenderDao() {
		if (senderDao == null) {
			senderDao = SpringUtil.getAppContext().getBean(SenderDao.class);
		}
		return senderDao;
	}
}
