package com.legacytojava.message.ejb.rule;
import javax.ejb.Local;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.inbox.MsgInboxVo;

@Local
public interface MsgInboxLocal {
	public long saveMessage(MessageBean messageBean) throws DataValidationException;
	public MsgInboxVo getMessageByPK(long msgId);
	public void parseMessage(MessageBean msgBean) throws DataValidationException;
}
