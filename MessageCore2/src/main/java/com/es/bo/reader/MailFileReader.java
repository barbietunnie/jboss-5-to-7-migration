package com.es.bo.reader;

import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import com.es.bo.inbox.MessageParserBo;
import com.es.bo.task.TaskSchedulerBo;
import com.es.core.util.FileUtil;
import com.es.core.util.SpringUtil;
import com.es.data.constant.CarrierCode;
import com.es.exception.DataValidationException;
import com.es.exception.TemplateException;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageBeanUtil;
import com.es.msgbean.MessageContext;

public class MailFileReader implements java.io.Serializable {
	private static final long serialVersionUID = -7542897465313801472L;
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MailFileReader.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public static void main(String[] args){
		String filePath = "bouncedmails";
		String fileName = "BouncedMail_1.txt";
		SpringUtil.beginTransaction();
		try {
			MailFileReader fReader = new MailFileReader();
			MessageBean msgBean = fReader.read(filePath, fileName);
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
	
	MessageBean read(String filePath, String fileName) throws MessagingException,
			IOException, DataValidationException, TemplateException {
		MessageBean msgBean = readIntoMessageBean(filePath, fileName);
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		//msgBean.setMsgRefId(1L); // triggers "deliveryError" task
		MessageParserBo parser = SpringUtil.getAppContext().getBean(MessageParserBo.class);
		msgBean.setRuleName(parser.parse(msgBean));
		TaskSchedulerBo taskBo = SpringUtil.getAppContext().getBean(TaskSchedulerBo.class);
		taskBo.scheduleTasks(new MessageContext(msgBean));
		return msgBean;
	}

	private MessageBean readIntoMessageBean(String filePath, String fileName)
			throws MessagingException, IOException {
		byte[] mailStream = FileUtil.loadFromFile(filePath, fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
