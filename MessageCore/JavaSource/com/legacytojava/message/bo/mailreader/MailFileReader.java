package com.legacytojava.message.bo.mailreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.jbatch.queue.JmsProcessor;
import com.legacytojava.message.bean.BodypartUtil;
import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bean.MessageBeanUtil;
import com.legacytojava.message.bean.MessageNode;
import com.legacytojava.message.constant.CarrierCode;

public class MailFileReader {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MailFileReader.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public static void main(String[] args){
		String filePath = "F:/pkgs/SavedMailStreams/aim/mail17_bounced.txt";
		filePath = "F:/pkgs/SavedMailStreams/aim/mail18.txt";
		//filePath = "F:/pkgs/workspace/MailReader/data/rfc3798_MDN_sample.txt";
		//filePath = "F:/pkgs/workspace/MailReader/data/rfc3464_DSN_sample4.txt";
		filePath = "F:/pkgs/workspace/MailReader/data/hard_bounce.txt";
		try {
			MailFileReader fReader = new MailFileReader();
			//MessageBean msgBean = fReader.start(filePath);
			MessageBean msgBean = fReader.readMessageBean(filePath);
			List<MessageNode> mNodes = BodypartUtil.retrieveAttachments(msgBean);
			logger.info("Number of Attachments: " + mNodes.size());
			logger.info("******************************");
			logger.info("MessageBean created:" + LF + msgBean);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		System.exit(0);
	}
	
	MessageBean start(String filePath) throws MessagingException, IOException, JMSException {
		JmsTransactionManager jmsTransactionManager = (JmsTransactionManager) SpringUtil
				.getAppContext().getBean("jmsTransactionManager");
		JmsProcessor jmsProcessor = (JmsProcessor) SpringUtil.getAppContext().getBean("jmsProcessor");
		Queue queue = (Queue) SpringUtil.getAppContext().getBean("mailReaderOutput");
		
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		jmsProcessor.getJmsTemplate().setDefaultDestination(queue);

		MessageBean msgBean = readMessageBean(filePath);
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		
		TransactionStatus tStatus = null;
		try {
			tStatus = jmsTransactionManager.getTransaction(definition);
			String msgId = jmsProcessor.writeMsg(msgBean, "MailReader");
			logger.info("JMS message written. Message Id returned: " + msgId);
			jmsTransactionManager.commit(tStatus);
			
			return msgBean;
		}
		catch (JMSException e) {
			logger.error("JMSException caught", e);
			tStatus.setRollbackOnly();
			throw e;
		}
	}
	
	private MessageBean readMessageBean(String filePath) throws MessagingException, IOException {
		byte[] mailStream = readFromFile(filePath);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
	
	private byte[] readFromFile(String filePath) throws IOException {
		File file = new File(filePath);
		if (file.exists() && file.isFile()) {
			FileInputStream fis = new FileInputStream(file);
			int fileLen = fis.available();
			logger.info("File Length: " + fileLen);
			byte[] fileContent = new byte[fileLen];
			fis.read(fileContent);
			fis.close();
			return fileContent;
		}
		throw new FileNotFoundException("File " + filePath + " not found");
	}
}
