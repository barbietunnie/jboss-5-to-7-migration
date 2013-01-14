package com.legacytojava.message.ejb.mailinglist;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.dao.client.ReloadFlagsDao;
import com.legacytojava.message.vo.ReloadFlagsVo;

/**
 * Session Bean implementation class CheckSchedulesTimer
 */
@Stateless(mappedName = "ejb/CheckSchedulesTimer")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(CheckSchedulesTimerRemote.class)
@Local(CheckSchedulesTimerLocal.class)
public class CheckSchedulesTimer implements CheckSchedulesTimerRemote, CheckSchedulesTimerLocal {
	protected static final Logger logger = Logger.getLogger(CheckSchedulesTimer.class);
	@Resource
	SessionContext context;
	private ReloadFlagsDao reloadFlagsDao;
	private ReloadFlagsVo reloadFlagsVo = null;
	private final static long DELAY = 5 * 60 * 1000; // 5 minutes
	private final static long INTERVAL = 5 * 60 * 1000;
    /**
     * Default constructor. 
     */
    public CheckSchedulesTimer() {
    	//reloadFlagsDao = (ReloadFlagsDao)SpringUtil.getAppContext().getBean("reloadFlagsDao");
    }

    private ReloadFlagsDao getReloadFlagsDao() {
    	if (reloadFlagsDao == null) {
    		reloadFlagsDao = (ReloadFlagsDao)SpringUtil.getAppContext().getBean("reloadFlagsDao");
    	}
    	return reloadFlagsDao;
    }

    public void startTimers() {
		logger.info("Entering startTimers()... ");
		stopTimers();
		TimerService timerService = context.getTimerService();
		timerService.createTimer(DELAY, INTERVAL, "CheckSchedulesTimer");
		logger.info("Added timer to first expire in " + (DELAY / (60 * 1000)) + " minutes.");
	}

	public void stopTimers() {
		logger.info("Entering stopTimers()... ");
		TimerService timerService = context.getTimerService();
		Collection<?> timers = timerService.getTimers();
		if (timers == null) return;
		for (Iterator<?> it=timers.iterator(); it.hasNext(); ) {
			Timer timer = (Timer)it.next();
			stopTimer(timer);
		}
	}
	
	private void stopTimer(Timer timer) {
		if (timer != null) {
			try {
				timer.cancel();
			}
			catch (NoSuchObjectLocalException e) {
				logger.error("NoSuchObjectLocalException caught", e);
			}
			catch (IllegalStateException e) {
				logger.error("IllegalStateException caught", e);
			}
		}
	}

	@EJB(beanInterface=MailingListTimerLocal.class)
	private MailingListTimerLocal listTimer;
	
	@Timeout
	public void ejbTimeout(Timer timer) {
		//if (isDebugEnabled)
		//	logger.debug("Entering ejbTimeout() - " + timer.getInfo());
		ReloadFlagsVo vo = getReloadFlagsDao().select();
		if (reloadFlagsVo != null && vo != null) {
			if (reloadFlagsVo.getSchedules() < vo.getSchedules() ||
					reloadFlagsVo.getTemplates() < vo.getTemplates()) {
				logger.info("Schedules have been changed, reload all timer ejbs...");
				reloadFlagsVo.setSchedules(vo.getSchedules());
				reloadFlagsVo.setTemplates(vo.getTemplates());
				listTimer.scheduleTimerTasks();
			}
		}
	}
}
