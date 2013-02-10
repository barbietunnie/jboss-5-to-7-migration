package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.TableColumnName;
import jpa.model.ClientData;
import jpa.model.RuleDataValue;
import jpa.service.ClientDataService;
import jpa.service.RuleDataValueService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class ActionDataLoader implements AbstractDataLoader {
	static final Logger logger = Logger.getLogger(ActionDataLoader.class);
	private RuleDataValueService propService;
	private ClientDataService clientService;

	public static void main(String[] args) {
		ActionDataLoader loader = new ActionDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		propService = (RuleDataValueService) SpringUtil.getAppContext().getBean("RuleDataValueService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("loader_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
		TransactionStatus status = txmgr.getTransaction(def);
		try {
			loadMsgDataTypes();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			txmgr.commit(status);
		}
	}

	void loadMsgDataTypes() throws SQLException {
		ClientData cd = clientService.getByClientId(Constants.DEFAULT_CLIENTID);
		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());
		String jndiProperties = 
				"java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory" + LF +
				"java.naming.provider.url=jnp:////localhost:2099" + LF +
				"java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces";

		RuleDataValue data = null;
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + EmailAddrType.FROM_ADDR.getValue(), "MessageBean");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + EmailAddrType.TO_ADDR.getValue(), "MessageBean");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + EmailAddrType.CC_ADDR.getValue(), "MessageBean");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + EmailAddrType.BCC_ADDR.getValue(), "MessageBean");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + EmailAddrType.FINAL_RCPT_ADDR.getValue(), "MessageBean");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + EmailAddrType.ORIG_RCPT_ADDR.getValue(), "MessageBean");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + EmailAddrType.FORWARD_ADDR.getValue(), "MessageBean");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + TableColumnName.SECURITY_DEPT_ADDR, "clientDao");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + TableColumnName.CUSTOMER_CARE_ADDR, "clientDao");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + TableColumnName.RMA_DEPT_ADDR, "clientDao");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + TableColumnName.VIRUS_CONTROL_ADDR, "clientDao");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + TableColumnName.SPAM_CONTROL_ADDR, "clientDao");
		propService.insert(data);
		data = new RuleDataValue("EMAIL_ADDRESS", "$" + TableColumnName.CHALLENGE_HANDLER_ADDR, "clientDao");
		propService.insert(data);
		data = new RuleDataValue("QUEUE_NAME", "$RMA_REQUEST_INPUT", "rmaRequestInputJmsTemplate");
		propService.insert(data);
		data = new RuleDataValue("QUEUE_NAME", "$CUSTOMER_CARE_INPUT", "customerCareInputJmsTemplate");
		propService.insert(data);
		data = new RuleDataValue(RuleDataValue.TEMPLATE_ID, "SubscribeByEmailReply", jndiProperties);
		propService.insert(data);
		logger.info("EntityManager persisted the record.");
	}
	
}

