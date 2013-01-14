package com.legacytojava.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;
import com.legacytojava.message.vo.emailaddr.MailingListVo;
import com.legacytojava.message.vo.emailaddr.SubscriptionVo;

import javax.mail.internet.InternetAddress;

import org.springframework.test.annotation.Rollback;

import com.legacytojava.message.dao.emailaddr.MailingListDao;
import com.legacytojava.message.dao.emailaddr.SubscriptionDao;

public class SubscribeBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo subscribeBo ;
	@Resource
	private TaskBaseBo unsubscribeBo;
	@Resource
	private SubscriptionDao subscriptionDao;
	@Resource
	private MailingListDao mailingListDao;
	private String testFromAddress = "test@test.com";
	private String mailingListAddr = "demolist2@localhost";
	private enum Action {
		Subscribe, Unsubscribe
	}
	
	@Test
	@Rollback(true)
	public void unsubscribe() throws Exception {
		prepare(Action.Subscribe);
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setFrom(InternetAddress.parse(testFromAddress));
		messageBean.setTo(InternetAddress.parse(mailingListAddr));
		messageBean.setSubject("unsubscribe");
		subscribeBo.setTaskArguments("$"+EmailAddressType.FROM_ADDR);
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		unsubscribeBo.process(messageBean);
		verifyDataRecord(Constants.NO_CODE);
	}
	@Test
	@Rollback(true)
	public void subscribe() throws Exception {
		prepare(Action.Unsubscribe);
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setFrom(InternetAddress.parse(testFromAddress));
		messageBean.setTo(InternetAddress.parse(mailingListAddr));
		messageBean.setSubject("subscribe");
		subscribeBo.setTaskArguments("$"+EmailAddressType.FROM_ADDR);
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		subscribeBo.process(messageBean);
		verifyDataRecord(Constants.YES_CODE);
	}
	private void prepare(Action action) {
		EmailAddrVo addrVo = selectEmailAddrByAddress(testFromAddress);
		assertNotNull(addrVo);
		List<MailingListVo> list = mailingListDao.getByAddress(mailingListAddr);
		assertTrue(list.size()>0);
		if (action.equals(Action.Subscribe)) {
			subscriptionDao.subscribe(addrVo.getEmailAddrId(), list.get(0).getListId());
		}
		else {
			subscriptionDao.unsubscribe(addrVo.getEmailAddrId(), list.get(0).getListId());
		}
	}
	private void verifyDataRecord(String subscribed) {
		EmailAddrVo addrVo = selectEmailAddrByAddress(testFromAddress);
		assertNotNull(addrVo);
		List<MailingListVo> list = mailingListDao.getByAddress(mailingListAddr);
		assertTrue(list.size()>0);
		SubscriptionVo vo = subscriptionDao.getByAddrAndListId(addrVo.getEmailAddr(), list.get(0).getListId());
		assertNotNull(vo);
		assertTrue(subscribed.equals(vo.getSubscribed()));
	}
}
