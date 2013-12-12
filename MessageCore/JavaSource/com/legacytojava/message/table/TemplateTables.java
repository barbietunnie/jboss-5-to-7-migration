package com.legacytojava.message.table;

import java.sql.SQLException;

import com.legacytojava.message.constant.CarrierCode;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.StatusIdCode;
import com.legacytojava.message.main.CreateTableBase;

public class TemplateTables extends CreateTableBase {
	/** Creates a new instance of TemplateTables 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public TemplateTables() throws ClassNotFoundException, SQLException {
		init();
	}

	public void dropTables() {
		try {
			stm.execute("DROP TABLE MSGSOURCE");
			System.out.println("Dropped MSGSOURCE Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE TEMPLATEVARIABLE");
			System.out.println("Dropped TEMPLATEVARIABLE Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE CLIENTVARIABLE");
			System.out.println("Dropped CLIENTVARIABLE Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE GLOBALVARIABLE");
			System.out.println("Dropped GLOBALVARIABLE Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE SUBJTEMPLATE");
			System.out.println("Dropped SUBJTEMPLATE Table...");
		}
		catch (SQLException e) {
		}
		try {
			stm.execute("DROP TABLE BODYTEMPLATE");
			System.out.println("Dropped BODYTEMPLATE Table...");
		}
		catch (SQLException e) {
		}
	}

	public void createTables() throws SQLException {
		createSUBJTEMPLATETable();
		createBODYTEMPLATETable();
		createGLOBALVARIABLETable();
		createCLIENTVARIABLETable();
		createTEMPLATEVARIABLETable();
		createMSGSOURCETable();
	}
	
	public void loadTestData() throws SQLException {
		// dummy method to satisfy super class
	}
	
	void createSUBJTEMPLATETable() throws SQLException {
		try {
			stm.execute("CREATE TABLE SUBJTEMPLATE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "TemplateId varchar(16) NOT NULL, "
				+ "ClientId varchar(16), "
				+ "StartTime timestamp NOT NULL, "
				+ "Description varchar(100), "
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', " // A - active, V - verification, I - inactive
				+ "TemplateValue varchar(255), "
				+ "PRIMARY KEY (RowId), "
				+ "FOREIGN KEY (ClientId) REFERENCES CLIENTS (ClientId) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (ClientId), "
				+ "UNIQUE INDEX (TemplateId,ClientId,StartTime)) ENGINE=InnoDB");
			System.out.println("Created SUBJTEMPLATE Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createBODYTEMPLATETable() throws SQLException {
		try {
			stm.execute("CREATE TABLE BODYTEMPLATE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "TemplateId varchar(16) NOT NULL, "
				+ "ClientId varchar(16), "
				+ "StartTime timestamp NOT NULL, "
				+ "Description varchar(100), "
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', "
				+ "ContentType varchar(100) NOT NULL, " // content mime type
				+ "TemplateValue text, "
				+ "PRIMARY KEY (RowId), "
				+ "FOREIGN KEY (ClientId) REFERENCES CLIENTS (ClientId) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (ClientId), "
				+ "UNIQUE INDEX (TemplateId,ClientId,StartTime)) ENGINE=InnoDB");
			System.out.println("Created BODYTEMPLATE Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createGLOBALVARIABLETable() throws SQLException {
		try {
			stm.execute("CREATE TABLE GLOBALVARIABLE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp NOT NULL, "
				+ "VariableFormat varchar(50), " 
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - Xheader, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', "
				// A - Active, I - Inactive
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + Constants.YES_CODE + "', "
				// allow override value to be supplied at runtime, Y/N/M, M=Mandatory
				+ "Required char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', "
				// required to be present in body template
				+ "VariableValue varchar(255), "
				+ "PRIMARY KEY (RowId), "
				+ "INDEX (VariableName), "
				+ "UNIQUE INDEX (VariableName,StartTime)) ENGINE=InnoDB");
			System.out.println("Created GLOBALVARIABLE Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createCLIENTVARIABLETable() throws SQLException {
		try {
			stm.execute("CREATE TABLE CLIENTVARIABLE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "ClientId varchar(16) NOT NULL, "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp NOT NULL, "
				+ "VariableFormat varchar(50), "
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - Xheader, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', "
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + Constants.YES_CODE + "', "
				// allow override value to be supplied at runtime
				+ "Required char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', "
				// required to present in body template
				+ "VariableValue text, "
				+ "PRIMARY KEY (RowId), "
				+ "FOREIGN KEY (ClientId) REFERENCES CLIENTS (ClientId) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (ClientId), "
				+ "INDEX (VariableName), "
				+ "UNIQUE INDEX (ClientId,VariableName,StartTime)) ENGINE=InnoDB");
			System.out.println("Created CLIENTVARIABLE Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createTEMPLATEVARIABLETable() throws SQLException {
		try {
			stm.execute("CREATE TABLE TEMPLATEVARIABLE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "TemplateId varchar(16) NOT NULL, "
				+ "ClientId varchar(16), "
				+ "VariableName varchar(26) NOT NULL, "
				+ "StartTime timestamp NOT NULL, "
				+ "VariableFormat varchar(50), "
				+ "VariableType char(1) NOT NULL, "
				// T - text, N - numeric, D - DateField/time,
				// A - address, X - X header, L - LOB(Attachment)
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', "
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + Constants.YES_CODE + "', "
				// allow override value to be supplied at runtime
				+ "Required char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', "
				// required to present in body template
				+ "VariableValue text, "
				+ "PRIMARY KEY (RowId), "
				+ "INDEX (VariableName), "
				+ "FOREIGN KEY (ClientId) REFERENCES CLIENTS (ClientId) ON DELETE CASCADE ON UPDATE CASCADE, "
				+ "INDEX (ClientId), "
				+ "UNIQUE INDEX (TemplateId,ClientId,VariableName,StartTime)"
				+ ") ENGINE=InnoDB");
			System.out.println("Created TEMPLATEVARIABLE Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	void createMSGSOURCETable() throws SQLException {
		try {
			stm.execute("CREATE TABLE MSGSOURCE ( "
				+ "RowId int AUTO_INCREMENT not null, "
				+ "MsgSourceId varchar(16) NOT NULL, "
				+ "Description varchar(100), "
				+ "StatusId char(1) NOT NULL DEFAULT '" + StatusIdCode.ACTIVE + "', "
				+ "FromAddrId bigint NOT NULL, "
				+ "ReplyToAddrId bigint, "
				+ "SubjTemplateId varchar(16) NOT NULL, "
				+ "BodyTemplateId varchar(16) NOT NULL, "
				+ "TemplateVariableId varchar(16), "
				+ "ExcludingIdToken char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', "
				// Y - No email id will be embedded into message
				+ "CarrierCode char(1) NOT NULL DEFAULT '" + CarrierCode.SMTPMAIL + "', "
				// Internet, WebMail, Internal Routing, ...
				+ "AllowOverride char(1) NOT NULL DEFAULT '" + Constants.YES_CODE + "', "
				// allow override templates, addrs to be supplied at runtime
				+ "SaveMsgStream char(1) NOT NULL DEFAULT '" + Constants.YES_CODE + "', "
				// Y - save rendered smtp message stream to MSGSTREAM
				+ "ArchiveInd char(1) NOT NULL DEFAULT '" + Constants.NO_CODE + "', "
				// Y - archive the rendered messages
				+ "PurgeAfter int, " // in month
				+ "UpdtTime datetime NOT NULL, "
				+ "UpdtUserId varchar(10) NOT NULL, "
				+ "PRIMARY KEY (RowId), "
				+ "UNIQUE INDEX (MsgSourceId), "
				+ "FOREIGN KEY (FromAddrId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (FromAddrId), "
				+ "FOREIGN KEY (ReplyToAddrId) REFERENCES EMAILADDR (EmailAddrId) ON DELETE SET NULL ON UPDATE CASCADE, "
				+ "INDEX (ReplyToAddrId), "
				+ "FOREIGN KEY (TemplateVariableId) REFERENCES TEMPLATEVARIABLE (TemplateId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (TemplateVariableId), "
				+ "FOREIGN KEY (SubjTemplateId) REFERENCES SUBJTEMPLATE (TemplateId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (SubjTemplateId), "
				+ "FOREIGN KEY (BodyTemplateId) REFERENCES BODYTEMPLATE (TemplateId) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+ "INDEX (BodyTemplateId) "
				+ ") ENGINE=InnoDB");
			System.out.println("Created MSGSOURCE Table...");
		}
		catch (SQLException e) {
			System.err.println("SQL Error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			TemplateTables ct = new TemplateTables();
			ct.dropTables();
			ct.createTables();
			ct.loadTestData();
			ct.wrapup();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}