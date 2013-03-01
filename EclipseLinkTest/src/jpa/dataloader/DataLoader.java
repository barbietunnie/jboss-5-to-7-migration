package jpa.dataloader;

import jpa.util.SpringUtil;

public class DataLoader {

	public static void main(String[] args) {
		DataLoader loader = new DataLoader();
		try {
			loader.loadAllTables();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public DataLoader() {
		// to trigger JPA to generate DDL (create tables, etc.)
		SpringUtil.getAppContext().refresh();
	}
	
	void loadAllTables() {
		new ClientDataLoader().loadData();
		new IdTokensDataLoader().loadData();
		new EmailAddressLoader().loadData();
		new UserDataLoader().loadData();
		new CustomerDataLoader().loadData();
		new VariableDataLoader().loadData();
		new MailingListDataLoader().loadData();
		new EmailVariableLoader().loadData();
		new EmailTemplateLoader().loadData();
		new RuleDataLoader().loadData();
		new RuleActionLoader().loadData();
		new TemplateDataLoader().loadData();
		new MessageInboxLoader().loadData();
		new MobileCarrierLoader().loadData();
		new MessageRenderedLoader().loadData();
		new MailInboxLoader().loadData();
	}
}
