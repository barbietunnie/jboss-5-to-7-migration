package jpa.dataloader;

import jpa.data.preload.SmtpServerEnum;
import jpa.model.SmtpServer;
import jpa.service.msgout.SmtpServerService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class SmtpServerLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(SmtpServerLoader.class);
	private SmtpServerService service;

	public static void main(String[] args) {
		SmtpServerLoader loader = new SmtpServerLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (SmtpServerService) SpringUtil.getAppContext().getBean("smtpServerService");
		startTransaction();
		try {
			loadSmtpServers();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadSmtpServers() {
		for (SmtpServerEnum mc : SmtpServerEnum.values()) {
			SmtpServer in = new SmtpServer();
			in.setSmtpHostName(mc.getSmtpHost());
			in.setSmtpPortNumber(mc.getSmtpPort());
			in.setServerName(mc.getServerName());
			in.setDescription(mc.getDescription());
			in.setUseSsl(mc.isUseSsl());
			in.setUserId(mc.getUserId());
			in.setUserPswd(mc.getUserPswd());
			in.setPersistence(mc.isPersistence());
			in.setStatusId(mc.getStatus().getValue());
			in.setServerType(mc.getServerType().getValue());
			in.setNumberOfThreads(mc.getNumberOfThreads());
			in.setMaximumRetries(mc.getMaximumRetries());
			in.setRetryFrequence(mc.getRetryFreq());
			in.setMinimumWait(mc.getMinimumWait());
			in.setAlertAfter(mc.getAlertAfter());
			in.setAlertLevel(mc.getAlertLevel());
			in.setMessageCount(mc.getMessageCount());
			service.insert(in);
		}
		logger.info("EntityManager persisted the record.");
	}
}

