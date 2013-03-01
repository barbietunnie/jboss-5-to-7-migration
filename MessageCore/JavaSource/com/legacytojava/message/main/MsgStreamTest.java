package com.legacytojava.message.main;

import javax.mail.Message;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.test.BoTestBase;
import com.legacytojava.message.dao.inbox.MsgStreamDao;
import com.legacytojava.message.vo.outbox.MsgStreamVo;

public class MsgStreamTest extends BoTestBase {

	public void testActivate() throws Exception {
		MsgStreamDao msgStreamDao = (MsgStreamDao) factory.getBean("msgStreamDao");

		long msgId = 6L;
		MsgStreamVo msgStreamVo = msgStreamDao.getByPrimaryKey(msgId);
		
		Message msg = createMimeMessage(msgStreamVo.getMsgStream());
		MessageBean messageBean = createMessageBean(msg);
		
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
			logger.debug("BodyContentType: " + messageBean.getBodyContentType());
			logger.debug("BodyContent: " + messageBean.getBody());
			logger.debug("BodyNode: " + LF + messageBean.getBodyNode().toString(0));
		}
	}

}
