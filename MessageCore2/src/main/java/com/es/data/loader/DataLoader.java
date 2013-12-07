package com.es.data.loader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.es.core.util.SpringUtil;

public class DataLoader {

	public static void main(String[] args) {
		DataLoader loader = new DataLoader();
		try {
			loader.createAllTables();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public DataLoader() {
		SpringUtil.getAppContext().refresh();
	}
	
	public void createAllTables() throws DataAccessException {
		List<AbstractTableBase> creators = buildCreatorList();
		for (int i=(creators.size()-1); i>=0; i--) {
			AbstractTableBase creator = creators.get(i);
			creator.dropTables();
		}
		for (int i=0; i<creators.size(); i++) {
			AbstractTableBase creator = creators.get(i);
			creator.createTables();
		}
		for (AbstractTableBase creator : creators) {
			SpringUtil.beginTransaction();
			creator.loadTestData();
			SpringUtil.commitTransaction();
		}
	}
	
	List<AbstractTableBase> buildCreatorList() {
		List<AbstractTableBase> loaders = new ArrayList<AbstractTableBase>();
		loaders.add(new SenderTable());
		loaders.add(new IdTokensTable());
		loaders.add(new EmailAddrTables());
		loaders.add(new UserTable());
		loaders.add(new SubscriberTable());
		loaders.add(new RuleTables());
		loaders.add(new RuleActionTables());
		loaders.add(new TemplateTables());
		loaders.add(new InboxTables());
		loaders.add(new MailboxTable());
		loaders.add(new SmtpTable());
		return loaders;
	}
}
