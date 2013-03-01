package com.legacytojava.message.main;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.dao.emailaddr.EmailTemplateDao;
import com.legacytojava.message.dao.emailaddr.SchedulesBlob;
import com.legacytojava.message.dao.emailaddr.SchedulesBlob.DateWrapper;
import com.legacytojava.message.util.BlobUtil;
import com.legacytojava.message.vo.emailaddr.EmailTemplateVo;

public class CalendarTest {
	protected static final Logger logger = Logger.getLogger(CalendarTest.class);
	private EmailTemplateDao emailTemplateDao;
	
	public static void main(String[] args) {
		CalendarTest test = new CalendarTest();
		test.startTasks();
	}
	
	private void startTasks() {
		emailTemplateDao = (EmailTemplateDao) SpringUtil.getDaoAppContext().getBean("emailTemplateDao");
		List<EmailTemplateVo> templates = emailTemplateDao.getAll();
		for (Iterator<EmailTemplateVo> it = templates.iterator(); it.hasNext();) {
			EmailTemplateVo vo = (EmailTemplateVo) it.next();
			SchedulesBlob blob = vo.getSchedulesBlob();
			if (blob == null) {
				logger.info("scheduleTimerTasks() - SchedulesBlob is null for templateId: "
						+ vo.getTemplateId());
				continue;
			}
			blob.setTemplateId(vo.getTemplateId());
			// schedule weekly tasks
			if (blob.getWeekly() != null) {
				for (int i = 0; i < blob.getWeekly().length; i++) {
					Calendar cal = getCalendar(blob);
					setDayOfWeek(cal, Integer.parseInt(blob.getWeekly()[i]), blob.getStartHour());
					SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
					clone.setTimerEvent(SchedulesBlob.Events.WEEKLY);
					logger.info("Added Timer for " + clone.getTemplateId() + " "
							+ clone.getTimerEvent().toString() + " day " + clone.getWeekly()[i]);
				}
			}
			// schedule biweekly tasks
			if (blob.getBiweekly() != null) {
				for (int i = 0; i < blob.getBiweekly().length; i++) {
					Calendar cal = getCalendar(blob);
					setDayOfWeek(cal, Integer.parseInt(blob.getBiweekly()[i]), blob.getStartHour());
					SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
					clone.setTimerEvent(SchedulesBlob.Events.BIWEEKLY);
					logger.info("Added Timer for " + clone.getTemplateId() + " "
							+ clone.getTimerEvent().toString() + " day " + clone.getBiweekly()[i]);
				}
			}
			// schedule monthly tasks
			if (blob.getMonthly() != null) {
				for (int i = 0; i < blob.getMonthly().length; i++) {
					Calendar cal = getCalendar(blob);
					setDayOfMonth(cal, Integer.parseInt(blob.getMonthly()[i]), blob.getStartHour());
					SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
					clone.setTimerEvent(SchedulesBlob.Events.MONTHLY);
					logger.info("Added Timer for " + clone.getTemplateId() + " "
							+ clone.getTimerEvent().toString() + " day " + clone.getMonthly()[i]);
				}
			}
			// schedule end of month tasks
			if (blob.getEndOfMonth()) {
				Calendar cal = getCalendar(blob);
				int dayOfMonth = setMaxDayOfMonth(cal, blob.getStartHour(), -0);
				SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
				clone.setTimerEvent(SchedulesBlob.Events.END_OF_MONTH);
				logger.info("Added Timer for " + clone.getTemplateId() + " "
						+ clone.getTimerEvent().toString() + " day " + dayOfMonth);
			}
			// schedule end of month minus 1 day tasks
			if (blob.getEomMinus1Day()) {
				Calendar cal = getCalendar(blob);
				int dayOfMonth = setMaxDayOfMonth(cal, blob.getStartHour(), -1);
				SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
				clone.setTimerEvent(SchedulesBlob.Events.EOM_MINUS_1DAY);
				logger.info("Added Timer for " + clone.getTemplateId() + " "
						+ clone.getTimerEvent().toString() + " day " + dayOfMonth);
			}
			// schedule end of month minus 2 day tasks
			if (blob.getEomMinus2Day()) {
				Calendar cal = getCalendar(blob);
				int dayOfMonth = setMaxDayOfMonth(cal, blob.getStartHour(), -2);
				SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
				clone.setTimerEvent(SchedulesBlob.Events.EOM_MINUS_2DAY);
				logger.info("Added Timer for " + clone.getTemplateId() + " "
						+ clone.getTimerEvent().toString() + " day " + dayOfMonth);
			}
			// schedule dates from the list
			for (int i = 0; i < blob.getDateList().length; i++) {
				DateWrapper scheduled = (DateWrapper) blob.getDateList()[i];
				if (scheduled != null && scheduled.getDate() != null) {
					Calendar scheduledTime = Calendar.getInstance();
					scheduledTime.setTime(scheduled.getDate());
					setHoursMinutes(scheduledTime, blob);
					Calendar currTime = Calendar.getInstance();
					if (scheduledTime.compareTo(currTime) > 0) {
					    SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
					    clone.setTimerEvent(SchedulesBlob.Events.DATE_LIST);
						logger.info("Added Timer for " + clone.getTemplateId() + " "
								+ clone.getTimerEvent().toString() + " date: "
								+ scheduledTime.getTime());
					}
					else {
						logger.warn(SchedulesBlob.Events.DATE_LIST.toString() + " - timer["
								+ (i + 1) + "] for \"" + blob.getTemplateId() + "\" has expired: "
								+ scheduledTime.getTime() + ", timer ignored.");
					}
				}
			}
		}
	}

	private Calendar getCalendar(SchedulesBlob blob) {
		Calendar cal = Calendar.getInstance();
		setHoursMinutes(cal, blob);
		return cal;
	}

	private void setHoursMinutes(Calendar cal, SchedulesBlob blob) {
		cal.set(Calendar.HOUR, blob.getStartHour());
		cal.set(Calendar.MINUTE, blob.getStartMinute());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	private void setDayOfWeek(Calendar cal, int dayOfWeek, int hourOfDay) {
		int calDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int calHourOfDay = currentHour();
		if (dayOfWeek < calDayOfWeek || (dayOfWeek == calDayOfWeek && hourOfDay <= calHourOfDay)) {
			cal.add(Calendar.DATE, 7);
			logger.info("setDayOfWeek() - rolled calendar forward a week: " + cal.getTime());
		}
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		logger.info("setDayOfWeek() - Timer to first expire at: " + cal.getTime());
	}

	private void setDayOfMonth(Calendar cal, int dayOfMonth, int hourOfDay) {
		int calDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		int calHourOfDay = currentHour();
		if (dayOfMonth < calDayOfMonth
				|| (dayOfMonth == calDayOfMonth && hourOfDay <= calHourOfDay)) {
			cal.add(Calendar.MONTH, 1);
			logger.info("setDayOfMonth() - rolled calendar forward a month: " + cal.getTime());
		}
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		logger.info("setDayOfMonth() - Timer to first expire at: " + cal.getTime());
	}

	private int setMaxDayOfMonth(Calendar cal, int hourOfDay, int minusDays) {
		int calDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		int calHourOfDay = currentHour();
		int dayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) + minusDays;
		if (dayOfMonth < calDayOfMonth
				|| (dayOfMonth == calDayOfMonth && hourOfDay <= calHourOfDay)) {
			cal.add(Calendar.MONTH, 1);
			dayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) + minusDays;
			logger.info("setMaxDayOfMonth() - rolled calendar forward a month: " + cal.getTime());
		}
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		logger.info("setMaxDayOfMonth() - Timer to first expire at: " + cal.getTime());
		return dayOfMonth;
	}
	
	int currentHour() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public EmailTemplateDao getEmailTemplateDao() {
		return emailTemplateDao;
	}

	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}

}
