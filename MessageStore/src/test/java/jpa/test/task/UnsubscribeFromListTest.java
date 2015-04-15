package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.common.MailingListService;
import jpa.service.common.SubscriptionService;
import jpa.service.task.UnsubscribeFromList;
import jpa.util.StringUtil;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-jpa-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=false)
@Transactional
public class UnsubscribeFromListTest {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(UnsubscribeFromListTest.class);
	
	@Resource
	private UnsubscribeFromList task;
	@Resource
	private MailingListService listService;
	@Resource
	private SubscriptionService subService;

	@BeforeClass
	public static void UnsubscribeFromListPrepare() {
	}

	private int rowId;
	private List<MailingList> lists = null;
	private String fromaddr = "event.alert@localhost";
	
	@Before
	@Rollback(false)
	public void prepare() {
		MessageBean mBean = new MessageBean();
		lists = listService.getAll(true);
		String toaddr = lists.get(0).getListEmailAddr();
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("unsubscribe");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("testUser");

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		assertFalse(ctx.getRowIds().isEmpty());
		rowId = ctx.getRowIds().get(0);
	}
	
	@Test
	public void testUnsubscribeFromList() throws Exception {
		logger.info("in testUnsubscribeFromList() method...");
		// verify results
		Subscription sub = subService.getByRowId(rowId);
		logger.info("Subscription record: " + StringUtil.prettyPrint(sub, 2));
		assertTrue(fromaddr.equals(sub.getEmailAddr().getAddress()));
		//assertFalse(sub.isSubscribed());
		assertTrue(lists.get(0).getListId().equals(sub.getMailingList().getListId()));
	}
}
