package jpa.service.msgin;

import java.io.IOException;

import javax.mail.MessagingException;

import jpa.constant.CarrierCode;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.service.message.MessageParserBo;
import jpa.util.SpringUtil;
import jpa.util.TestUtil;

import org.apache.log4j.Logger;

public class MailFileReader {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MailFileReader.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public static void main(String[] args){
		String filePath = "jpa/test/bouncedmails";
		String fileName = "BouncedMail_1.txt";
		SpringUtil.startTransaction();
		try {
			MailFileReader fReader = new MailFileReader();
			//MessageBean msgBean = fReader.start(filePath);
			MessageBean msgBean = fReader.start(filePath, fileName);
			logger.info("Number of Attachments: " + msgBean.getAttachCount());
			logger.info("******************************");
			logger.info("MessageBean created:" + LF + msgBean);
			SpringUtil.commitTransaction();
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			SpringUtil.clearTransaction();
		}
		System.exit(0);
	}
	
	MessageBean start(String filePath, String fileName) throws MessagingException, IOException {
		MessageBean msgBean = readMessageBean(filePath, fileName);
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		MessageParserBo parser = (MessageParserBo) SpringUtil.getAppContext().getBean("messageParserBo");
		msgBean.setRuleName(parser.parse(msgBean));
		// TODO save message
		return msgBean;
	}

	private MessageBean readMessageBean(String filePath, String fileName) throws MessagingException, IOException {
		byte[] mailStream = TestUtil.loadFromFile(filePath, fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
