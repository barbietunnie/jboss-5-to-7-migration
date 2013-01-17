package com.legacytojava.message.bo.inbox;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.inbox.MsgInboxVo;

public interface MsgInboxBo {

	public long saveMessage(MessageBean messageBean) throws DataValidationException;

	public MsgInboxVo getMessageByPK(long msgId);
	
	public MsgInboxVo getAllDataByMsgId(long msgId);
}
