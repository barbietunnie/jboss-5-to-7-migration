package com.legacytojava.message.main;

import java.sql.SQLException;

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

public class UpdateRecordsForProd {

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
			UpdateRecordsForProd create = new UpdateRecordsForProd();
			create.init();
			create.updateRecords();
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
	
	void updateRecords() throws SQLException {
		clientTable.updateClient4Prod();
		smtpTable.UpdateSmtpData4Prod();
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
