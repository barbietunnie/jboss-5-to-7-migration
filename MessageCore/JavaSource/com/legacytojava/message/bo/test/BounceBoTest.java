package com.legacytojava.message.bo.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;
import com.legacytojava.message.constant.EmailAddressType;
import com.legacytojava.message.dao.emailaddr.EmailAddrDao;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

public class BounceBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo bounceBo;
	@Resource
	private EmailAddrDao emailAddrDao;
	@Test
	public void bounce() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		bounceBo.setTaskArguments("$"+EmailAddressType.FINAL_RCPT_ADDR+",$"+EmailAddressType.FROM_ADDR);
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		// get "before" bounce count for From address
		EmailAddrVo addrVo = selectByAddress(messageBean.getFromAsString());
		assertNotNull(addrVo);
		int beforeFromCount = addrVo.getBounceCount();
		// end of "before" bounce count
		Long addrsUpdated = (Long)bounceBo.process(messageBean);
		assertTrue(addrsUpdated>=0);
		// now verify the database record
		addrVo = selectByAddress(messageBean.getFromAsString());
		assertNotNull(addrVo);
		assertTrue(addrVo.getBounceCount()>beforeFromCount);
		if (StringUtils.isNotBlank(messageBean.getFinalRcpt())) {
			addrVo = selectByAddress(messageBean.getFinalRcpt());
			assertNotNull(addrVo);
			assertTrue(addrVo.getBounceCount()>0);
		}
	}
	private EmailAddrVo selectByAddress(String emailAddr) {
		EmailAddrVo addrVo = emailAddrDao.findByAddress(emailAddr);
		logger.info("EmailAddrDao - selectByAddress: "+LF+addrVo);
		return addrVo;
	}
}
