package jpa.dataloader;

import jpa.constant.Constants;
import jpa.constant.EmailVariableType;
import jpa.constant.StatusId;
import jpa.model.EmailVariable;
import jpa.service.EmailVariableService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class EmailVariableLoader implements AbstractDataLoader {
	static final Logger logger = Logger.getLogger(EmailVariableLoader.class);
	private EmailVariableService service;

	public static void main(String[] args) {
		EmailVariableLoader loader = new EmailVariableLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (EmailVariableService) SpringUtil.getAppContext().getBean("emailVariableService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadEmailVariables();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	private void loadEmailVariables() {
		EmailVariable data = new EmailVariable();
		data.setVariableName("CustomerName");
		data.setVariableType(EmailVariableType.Custom.getValue());
		data.setTableName("customer_data");
		data.setColumnName("FirstName,LastName");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(false);
		data.setDefaultValue("Valued Customer");
		data.setVariableQuery("SELECT CONCAT(c.FirstName, ' ', c.LastName) as ResultStr " +
			"FROM customer_data c, email_addr e " +
			"where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1;");
		data.setVariableProcName("jpa.external.CustomerNameResolver");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);


		data = new EmailVariable();
		data.setVariableName("CustomerFirstName");
		data.setVariableType(EmailVariableType.Custom.getValue());
		data.setTableName("customer_data");
		data.setColumnName("FirstName");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(false);
		data.setDefaultValue("Valued Customer");
		data.setVariableQuery("SELECT c.FirstName as ResultStr " +
				"FROM customer_data c, email_addr e " +
				"where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1;");
		data.setVariableProcName("jpa.external.CustomerNameResolver");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("CustomerLastName");
		data.setVariableType(EmailVariableType.Custom.getValue());
		data.setTableName("customer_data");
		data.setColumnName("LastName");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(false);
		data.setDefaultValue("Valued Customer");
		data.setVariableQuery("SELECT c.LastName as ResultStr " +
				"FROM customer_data c, email_addr e " +
				"where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1;");
		data.setVariableProcName("jpa.external.CustomerNameResolver");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("CustomerAddress");
		data.setVariableType(EmailVariableType.Custom.getValue());
		data.setTableName("customer_data");
		data.setColumnName("StreetAddress");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(false);
		data.setDefaultValue("");
		data.setVariableQuery("SELECT CONCAT_WS(',',c.StreetAddress2,c.StreetAddress) as ResultStr " +
				"FROM customer_data c, email_addr e " +
				"where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1;");
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("CustomerCityName");
		data.setVariableType(EmailVariableType.Custom.getValue());
		data.setTableName("customer_data");
		data.setColumnName("CityName");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(false);
		data.setDefaultValue("");
		data.setVariableQuery("SELECT c.CityName as ResultStr " +
				"FROM customer_data c, email_addr e " +
				"where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1;");
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("CustomerStateCode");
		data.setVariableType(EmailVariableType.Custom.getValue());
		data.setTableName("customer_data");
		data.setColumnName("StateCode");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(false);
		data.setDefaultValue("");
		data.setVariableQuery("SELECT CONTAC_WS(',',c.StateCode,c.ProvinceName) as ResultStr " +
				"FROM customer_data c, email_addr e " +
				"where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1;");
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("CustomerZipCode");
		data.setVariableType(EmailVariableType.Custom.getValue());
		data.setTableName("customer_data");
		data.setColumnName("ZipCode");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(false);
		data.setDefaultValue("");
		data.setVariableQuery("SELECT CONCAT_WS('-',c.ZipCode5,ZipCode4) as ResultStr " +
				"FROM customer_data c, email_addr e " +
				"where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1;");
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("CustomerCountry");
		data.setVariableType(EmailVariableType.Custom.getValue());
		data.setTableName("customer_data");
		data.setColumnName("Country");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(false);
		data.setDefaultValue("");
		data.setVariableQuery("SELECT c.Country as ResultStr " +
				"FROM customer_data c, email_addr e " +
				"where e.Row_Id=c.EmailAddrRowId and e.Row_Id=?1;");
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		/*
		 * System variables 
		 */
		data = new EmailVariable();
		data.setVariableName("EmailOpenCountImgTag");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("<img src='${WebSiteUrl}/msgopen.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new EmailVariable();
		data.setVariableName("EmailClickCountImgTag");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("<img src='${WebSiteUrl}/msgclick.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("EmailUnsubscribeImgTag");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("<img src=='${WebSiteUrl}/msgunsub.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}' alt='' height='1' width='1'>");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("EmailTrackingTokens");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("FooterWithUnsubLink");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue(LF + "<p>To unsubscribe from this mailing list, " + LF +
				"<a target='_blank' href='${WebSiteUrl}/MsgUnsubPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}'>click here</a>.</p>"
				+ LF);
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("FooterWithUnsubAddr");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue(LF + "To unsubscribe from this mailing list, send an e-mail to: ${MailingListAddress}" + LF +
				"with \"unsubscribe\" (no quotation marks) in the subject." + LF);
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("SubscribeURL");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("${WebSiteUrl}/subscribe.jsp?sbsrid=${SubscriberAddressId}");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("ConfirmationURL");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("${WebSiteUrl}/confirmsub.jsp?sbsrid=${_EncodedSubcriberId}&listids=${_SubscribedListIds}&sbsraddr=${SubscriberAddress}");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		
		data = new EmailVariable();
		data.setVariableName("UnsubscribeURL");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("${WebSiteUrl}/unsubscribe.jsp?sbsrid=${_EncodedSubcriberId}&listids=${_SubscribedListIds}&sbsraddr=${SubscriberAddress}");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("UserProfileURL");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("${WebSiteUrl}/userprofile.jsp?sbsrid=${SubscriberAddressId}");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("TellAFriendURL");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("${WebSiteUrl}/referral.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new EmailVariable();
		data.setVariableName("SiteLogoURL");
		data.setVariableType(EmailVariableType.System.getValue());
		data.setTableName("");
		data.setColumnName("");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setBuiltin(true);
		data.setDefaultValue("${WebSiteUrl}/images/logo.gif");
		data.setVariableQuery(null);
		data.setVariableProcName(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		logger.info("EntityManager persisted the record.");
	}
	
}

