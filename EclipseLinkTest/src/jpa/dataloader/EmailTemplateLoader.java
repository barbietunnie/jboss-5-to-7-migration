package jpa.dataloader;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.EmailTemplateEnum;
import jpa.model.ClientData;
import jpa.model.EmailTemplate;
import jpa.model.MailingList;
import jpa.model.SchedulesBlob;
import jpa.service.ClientDataService;
import jpa.service.EmailTemplateService;
import jpa.service.MailingListService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class EmailTemplateLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(EmailTemplateLoader.class);
	private EmailTemplateService service;
	private ClientDataService clientService;
	private MailingListService mlistService;

	public static void main(String[] args) {
		EmailTemplateLoader loader = new EmailTemplateLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (EmailTemplateService) SpringUtil.getAppContext().getBean("emailTemplateService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		mlistService = (MailingListService) SpringUtil.getAppContext().getBean("mailingListService");
		startTransaction();
		try {
			loadEmailTemplates();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadEmailTemplates() {
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		
		for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
			if (tmp.getMailingList().isProd()) continue;
			MailingList mlist = mlistService.getByListId(tmp.getMailingList().name());
			EmailTemplate data = new EmailTemplate();
			data.setClientData(client);
			data.setMailingList(mlist);
			data.setTemplateId(tmp.name());
			data.setSubject(tmp.getSubject());
			data.setBodyText(tmp.getBodyText());
			data.setHtml(tmp.isHtml());
			data.setListType(tmp.getListType().getValue());
			data.setDeliveryOption(tmp.getDeliveryType().getValue());
			data.setBuiltin(tmp.isBuiltin());
			data.setIsEmbedEmailId(tmp.getIsEmbedEmailId()); // use system default when null
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			SchedulesBlob blob1 = new SchedulesBlob();
			data.setSchedulesBlob(blob1);
			service.insert(data);
		}

		logger.info("EntityManager persisted the record.");
	}
	
	void loadProdEmailTemplates() {
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);

		for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
			if (!tmp.getMailingList().isProd()) continue;
			MailingList mlist = mlistService.getByListId(tmp.getMailingList().name());
			EmailTemplate data = new EmailTemplate();
			data.setClientData(client);
			data.setMailingList(mlist);
			data.setTemplateId(tmp.name());
			data.setSubject(tmp.getSubject());
			data.setBodyText(tmp.getBodyText());
			data.setHtml(tmp.isHtml());
			data.setListType(tmp.getListType().getValue());
			data.setDeliveryOption(tmp.getDeliveryType().getValue());
			data.setBuiltin(tmp.isBuiltin());
			data.setIsEmbedEmailId(tmp.getIsEmbedEmailId()); // use system default when null
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			SchedulesBlob blob1 = new SchedulesBlob();
			data.setSchedulesBlob(blob1);
			service.insert(data);
		}

		logger.info("EntityManager persisted the record.");
	}
}

