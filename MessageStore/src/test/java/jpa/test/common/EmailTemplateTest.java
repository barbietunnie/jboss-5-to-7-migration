package jpa.test.common;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.MailingListType;
import jpa.model.SenderData;
import jpa.model.EmailTemplate;
import jpa.model.MailingList;
import jpa.service.common.EmailTemplateService;
import jpa.service.common.SenderDataService;
import jpa.service.maillist.MailingListService;
import jpa.util.StringUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional(propagation=Propagation.REQUIRED)
public class EmailTemplateTest {
	static Logger logger = Logger.getLogger(EmailTemplateTest.class);
	
	final String testTemplateId = "jpa test template id";
	
	@BeforeClass
	public static void EmailTemplatePrepare() {
	}

	@Autowired
	EmailTemplateService service;
	@Autowired
	SenderDataService senderService;
	@Autowired
	MailingListService mlistService;

	@Test
	public void emailTemplateService() {
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		List<MailingList> mlist = mlistService.getAll(false);
		assertFalse(mlist.isEmpty());
		assertNotNull(mlist.get(0).getListEmailAddr());
		assertNotNull(mlist.get(0).getSenderData());
		// test insert
		EmailTemplate var1 = new EmailTemplate();
		var1.setSenderData(sender);
		var1.setMailingList(mlist.get(0));
		var1.setTemplateId(testTemplateId);
		var1.setDeliveryOption(MailingListDeliveryType.ALL_ON_LIST.getValue());
		var1.setListType(MailingListType.TRADITIONAL.getValue());
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(var1);
		
		EmailTemplate var2 = service.getByTemplateId(testTemplateId);
		assertNotNull(var2);
		logger.info("EmailTemplate: " + StringUtil.prettyPrint(var2,1));

		List<EmailTemplate> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		assertNotNull(list1.get(0).getMailingList());
		
		List<EmailTemplate> list2 = service.getByMailingListId(list1.get(0).getMailingList().getListId());
		assertFalse(list2.isEmpty());
		
		// test insert
		EmailTemplate var3 = createNewInstance(list2.get(0));
		var3.setTemplateId(var3.getTemplateId()+"_v2");
		service.insert(var3);
		assertNotNull(service.getByTemplateId(var3.getTemplateId()));
		// end of test insert
		
		service.delete(var3);
		try {
			service.getByTemplateId(var3.getTemplateId());
			fail();
		}
		catch (NoResultException e) {}
		
		// test deleteByPrimaryKey
		EmailTemplate var5 = createNewInstance(var2);
		var5.setTemplateId(var2.getTemplateId() + "_v5");
		service.insert(var5);
		var5 = service.getByTemplateId(var5.getTemplateId());
		service.delete(var5);
		try {
			service.getByTemplateId(var5.getTemplateId());
			fail();
		}
		catch (NoResultException e) {}
		
		// test update
		EmailTemplate var6 = createNewInstance(var2);
		var6.setTemplateId(var2.getTemplateId() + "_v6");
		service.insert(var6);
		assertNotNull((var6=service.getByTemplateId(var6.getTemplateId())));
		var6.setBodyText("new test value");
		service.update(var6);
		EmailTemplate var_updt = service.getByTemplateId(var6.getTemplateId());
		assertTrue("new test value".equals(var_updt.getBodyText()));
		logger.info("EmailTemplate: " + StringUtil.prettyPrint(var6,1));
		// end of test update
		
		service.delete(var6);
		try {
			service.getByRowId(var6.getRowId());
			fail();
		}
		catch (NoResultException e) {
			// expected
		}
	}
	
	private EmailTemplate createNewInstance(EmailTemplate orig) {
		EmailTemplate dest = new EmailTemplate();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
