package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.data.preload.RuleNameEnum;
import jpa.model.ClientData;
import jpa.model.EmailAddr;
import jpa.model.MessageInbox;
import jpa.model.RuleLogic;
import jpa.service.ClientDataService;
import jpa.service.EmailAddrService;
import jpa.service.MessageInboxService;
import jpa.service.RuleLogicService;
import jpa.util.SpringUtil;

import org.apache.log4j.Logger;

public class MessageInboxLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(MessageInboxLoader.class);
	private MessageInboxService service;
	private ClientDataService clientService;
	private EmailAddrService addrService;
	private RuleLogicService logicService;

	public static void main(String[] args) {
		MessageInboxLoader loader = new MessageInboxLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = (MessageInboxService) SpringUtil.getAppContext().getBean("messageInboxService");
		clientService = (ClientDataService) SpringUtil.getAppContext().getBean("clientDataService");
		addrService = (EmailAddrService) SpringUtil.getAppContext().getBean("emailAddrService");
		logicService = (RuleLogicService) SpringUtil.getAppContext().getBean("ruleLogicService");
		startTransaction();
		try {
			loadMessageInbox();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadMessageInbox() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		ClientData client = clientService.getByClientId(Constants.DEFAULT_CLIENTID);

		MessageInbox data = new MessageInbox();
		data.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		data.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		data.setMsgSubject("Test Subject");
		data.setMsgPriority("2 (Normal)");
		data.setReceivedTime(updtTime);
		
		EmailAddr from = addrService.findSertAddress("jsmith@test.com");
		data.setFromAddress(from);
		data.setReplytoAddress(null);

		String to_addr = client.getReturnPathLeft() + "@" + client.getDomainName();
		EmailAddr to = addrService.findSertAddress(to_addr);
		data.setToAddress(to);
		data.setClientData(client);
		data.setCustomerData(null);
		data.setPurgeDate(null);
		data.setUpdtTime(updtTime);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data.setLockTime(null);
		data.setLockId(null);
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.name());
		data.setRuleLogic(logic);
		data.setMsgContentType("multipart/mixed");
		data.setBodyContentType("text/plain");
		data.setMsgBody("Test Message Body");
		data.setStatusId(MsgStatusCode.RECEIVED.getValue());
		service.insert(data);

		data = new MessageInbox();
		data.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		data.setMsgDirection(MsgDirectionCode.SENT.getValue());
		data.setMsgSubject("Test Broadcast Subject");
		data.setMsgPriority("2 (Normal)");
		data.setReceivedTime(updtTime);
		
		from = addrService.findSertAddress("demolist1@localhost");
		data.setFromAddress(from);
		data.setReplytoAddress(null);

		data.setToAddress(from);
		data.setClientData(client);
		data.setCustomerData(null);
		data.setPurgeDate(null);
		data.setUpdtTime(updtTime);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data.setLockTime(null);
		data.setLockId(null);
		
		logic = logicService.getByRuleName(RuleNameEnum.BROADCAST.name());
		data.setRuleLogic(logic);
		data.setMsgContentType("text/plain");
		data.setBodyContentType("text/plain");
		data.setMsgBody("Test Broadcast Message Body");
		data.setStatusId(MsgStatusCode.CLOSED.getValue());
		service.insert(data);

		logger.info("EntityManager persisted the record.");
	}
}

