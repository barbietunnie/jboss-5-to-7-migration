package com.es.bo.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.MailingListDao;
import com.es.dao.address.SubscriptionDao;
import com.es.data.constant.CodeType;
import com.es.msgbean.MessageBean;
import com.es.msgbean.MessageContext;
import com.es.vo.address.MailingListVo;
import com.es.vo.address.SubscriptionVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class SubscribeToListTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(SubscribeToListTest.class);
	
	@Resource
	private SubscribeToList task;
	@Resource
	private MailingListDao mListDao;
	@Resource
	private SubscriptionDao subscriptionDao;

	@BeforeClass
	public static void SubscribeToListPrepare() {
	}

	@Test
	public void testSubscribeToList() {
		MessageBean mBean = new MessageBean();
		String fromaddr = "event.alert@localhost";
		List<MailingListVo> lists = mListDao.getAll(true);
		MailingListVo mList = lists.get(0);
		String toaddr = mList.getEmailAddr();
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("subscribe");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("testUser");

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		System.out.println("Verifying Results ##################################################################");
		// verify results
		assertFalse(ctx.getEmailAddrIdList().isEmpty());
		logger.info("EmailAddrId from MesageContext = " + ctx.getEmailAddrIdList());
		SubscriptionVo sub = subscriptionDao.getByPrimaryKey(ctx.getEmailAddrIdList().get(0), mList.getListId());
		assertNotNull(sub);
		assertTrue(fromaddr.equals(sub.getEmailAddr()));
		assertTrue(CodeType.YES_CODE.getValue().equals(sub.getSubscribed()));
		assertTrue(mList.getListId().equals(sub.getListId()));
	}
}
