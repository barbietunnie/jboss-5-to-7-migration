package com.legacytojava.message.bo.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.TaskBaseBo;

public class ToCsrBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo toCsrBo;
	@Test
	public void toCsr() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		toCsrBo.setTaskArguments("$CUSTOMER_CARE_INPUT");
		String jmsMsgId = (String)toCsrBo.process(messageBean);
		assertNotNull(jmsMsgId);
		assertTrue(jmsMsgId.startsWith("ID:"));
	}
}
