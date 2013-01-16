package com.legacytojava.msgui.bean;

import org.apache.log4j.Logger;

import com.legacytojava.jbatch.common.ProductKey;
import com.legacytojava.message.dao.client.ClientDao;
import com.legacytojava.msgui.util.SpringUtil;

public class EnterProductKeyBean {
	static final Logger logger = Logger.getLogger(EnterProductKeyBean.class);
	private String name = null;
	private String productKey = null;
	private String message = null;
	
	private ClientDao clientDao = null;
	
	public String enterProductKey() {
		message = null;
		if (!ProductKey.validateKey(productKey)) {
			message = "Invalid Product Key.";
			return "enterkey.failed";
		}
		int rowsUpdated = getClientDao().updateSystemKey(productKey);
		logger.info("enterProductKey() - rows updated: " + rowsUpdated);
		return "enterkey.saved";
	}
	
	private ClientDao getClientDao() {
		if (clientDao == null)
			clientDao = (ClientDao) SpringUtil.getWebAppContext().getBean("clientDao");
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
