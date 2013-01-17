package com.legacytojava.message.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.dao.inbox.MsgStreamDao;
import com.legacytojava.message.vo.outbox.MsgStreamVo;

public class MessageBeanTest {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MessageBeanTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public static void main(String[] args){
		AbstractApplicationContext factory = SpringUtil.getAppContext();
		MsgStreamDao msgStreamDao = (MsgStreamDao)factory.getBean("msgStreamDao");
		
		try {
			MessageBeanTest test = new MessageBeanTest();
			MessageBean msgBean = null;
			long msgId = 1L;
			boolean readFromDB = false;
			if (readFromDB) {
				msgBean = test.testReadFromDatabase(msgStreamDao, msgId);
			}
			String filePath = "F:/pkgs/SavedMailStreams/aim/mail4.txt";
			if (true)
				msgBean = test.testReadFromFile(filePath);

			msgBean.setToPlainText(true);
			List<MessageNode> mNodes = BodypartUtil.retrieveAttachments(msgBean);
			logger.info("Number of Attachments: " + mNodes.size());
			logger.info("******************************");
			logger.info("MessageBean created:" + LF + msgBean);

			MessageBeanUtil.createMimeMessage(msgBean);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		System.exit(0);
	}
	
	private MessageBean testReadFromDatabase(MsgStreamDao msgStreamDao, long msgId) throws MessagingException {
		byte[] mailStream = readFromDatabase(msgStreamDao, msgId);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
	
	private MessageBean testReadFromFile(String filePath) throws MessagingException, IOException {
		byte[] mailStream = readFromFile(filePath);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
	
	private byte[] readFromDatabase(MsgStreamDao msgStreamDao, long msgId) {
		MsgStreamVo msgStreamVo = (MsgStreamVo)msgStreamDao.getByPrimaryKey(msgId);
		System.out.println("MsgStreamDao - getByPrimaryKey: "+LF+msgStreamVo);
		return msgStreamVo.getMsgStream();
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
