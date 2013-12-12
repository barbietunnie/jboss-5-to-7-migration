package com.legacytojava.message.main;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;

import com.legacytojava.message.table.ActionTables;
import com.legacytojava.message.table.ClientTable;
import com.legacytojava.message.table.CustomerTable;
import com.legacytojava.message.table.EmailAddrTable;
import com.legacytojava.message.table.IdTokensTable;
import com.legacytojava.message.table.InboxTables;
import com.legacytojava.message.table.LoadActionTables;
import com.legacytojava.message.table.LoadInboxTables;
import com.legacytojava.message.table.LoadTemplateTables;
import com.legacytojava.message.table.MailboxTable;
import com.legacytojava.message.table.SmtpTable;
import com.legacytojava.message.table.TemplateTables;
import com.legacytojava.message.table.TimerTable;
import com.legacytojava.message.table.UserTable;

public class CreateReleaseTables {

	ClientTable clientTable;
	EmailAddrTable emailAddrTable;
	CustomerTable customerTable;
	//RuleTables ruleTables;
	ActionTables actionTables;
	TemplateTables templateTables;
	InboxTables inboxTables;
	IdTokensTable idTokensTable;
	
	LoadActionTables loadActionTables;
	LoadInboxTables loadInboxTables;
	LoadTemplateTables loadTemplateTables;
	
	MailboxTable mailboxTable;
	SmtpTable smtpTable;
	TimerTable timerTable;
	UserTable userTable;
	
	public static void main(String[] args) {
		try {
			CreateReleaseTables create = new CreateReleaseTables();
			create.init();
			create.dropAllTables();
			create.createAllTables();
			create.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void init() throws SQLException, ClassNotFoundException {
		clientTable = new ClientTable();
		emailAddrTable = new EmailAddrTable();
		customerTable = new CustomerTable();
		//ruleTables = new RuleTables();
		actionTables = new ActionTables();
		templateTables = new TemplateTables();
		inboxTables = new InboxTables();
		idTokensTable = new IdTokensTable();
		
		loadActionTables = new LoadActionTables();
		loadInboxTables = new LoadInboxTables();
		loadTemplateTables = new LoadTemplateTables();
		
		mailboxTable = new MailboxTable();
		smtpTable = new SmtpTable();
		timerTable = new TimerTable();
		userTable = new UserTable();
	}
	
	void dropAllTables() {
		userTable.dropTables();
		timerTable.dropTables();
		smtpTable.dropTables();
		mailboxTable.dropTables();
		idTokensTable.dropTables();
		inboxTables.dropTables();
		templateTables.dropTables();
		actionTables.dropTables();
		//ruleTables.dropTables();
		customerTable.dropTables();
		emailAddrTable.dropTables();
		clientTable.dropTables();
	}
	
	void createAllTables() throws SQLException {
		createAllTables(null);
	}
	
	void createAllTables(ApplicationContext ctx) throws SQLException {
		clientTable.createTables();
		clientTable.loadReleaseData();
		
		emailAddrTable.createTables();
		emailAddrTable.loadReleaseData();
		
		customerTable.createTables();
		customerTable.loadReleaseData();
		
		//ruleTables.createTables();
		//ruleTables.loadReleaseData();
		
		actionTables.createTables();
		actionTables.loadReleaseData();
		loadActionTables.loadData(ctx);
		
		templateTables.createTables();
		loadTemplateTables.loadData(ctx);
		
		inboxTables.createTables();
		loadInboxTables.loadData(ctx);
		
		idTokensTable.createTables();
		idTokensTable.loadReleaseData();
		
		mailboxTable.createTables();
		mailboxTable.loadReleaseData();
		
		smtpTable.createTables();
		smtpTable.loadReleaseData();
		
		timerTable.createTables();
		timerTable.loadReleaseData();
		
		userTable.createTables();
		userTable.loadReleaseData();
		
		// build ClientVariable records
		clientTable.updateAllClients();
		// build MsgDataType records for TemplateId data type
		emailAddrTable.updateTemplates();
		// insert mailboxes to EmailAddr table
		mailboxTable.updateEmailAddrTable();
	}
	
	void wrapup() {
		clientTable.wrapup();
		emailAddrTable.wrapup();
		customerTable.wrapup();
		//ruleTables.wrapup();
		actionTables.wrapup();
		templateTables.wrapup();
		inboxTables.wrapup();
		idTokensTable.wrapup();
		
		mailboxTable.wrapup();
		smtpTable.wrapup();
		timerTable.wrapup();
		userTable.wrapup();
	}
}
