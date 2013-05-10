package jpa.msgui.bean;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.msgui.util.SpringUtil;
import jpa.service.SenderDataService;
import jpa.util.ProductKey;

import org.apache.log4j.Logger;

public class EnterProductKeyBean {
	static final Logger logger = Logger.getLogger(EnterProductKeyBean.class);
	private String name = null;
	private String productKey = null;
	private String message = null;
	
	private SenderDataService clientDao = null;
	
	public String enterProductKey() {
		message = null;
		if (!ProductKey.validateKey(productKey)) {
			message = "Invalid Product Key.";
			return "enterkey.failed";
		}
		SenderData data = getSenderDataService().getBySenderId(Constants.DEFAULT_SENDER_ID);
		data.setSystemKey(productKey);
		getSenderDataService().update(data);
		logger.info("enterProductKey() - rows updated: " + 1);
		return "enterkey.saved";
	}
	
	private SenderDataService getSenderDataService() {
		if (clientDao == null)
			clientDao = (SenderDataService) SpringUtil.getWebAppContext().getBean("senderDataService");
		return clientDao;
	}
    
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProductKey() {
		return productKey;
	}

	public void setProductKey(String productKey) {
		this.productKey = productKey;
	}
}