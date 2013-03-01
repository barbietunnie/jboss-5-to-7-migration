package com.legacytojava.message.bo.outbox;

import java.text.ParseException;

import javax.mail.internet.AddressException;

import com.legacytojava.message.bean.MessageBean;
import com.legacytojava.message.bo.template.RenderRequest;
import com.legacytojava.message.bo.template.RenderResponse;
import com.legacytojava.message.exception.DataValidationException;

public interface MsgOutboxBo {

	public long saveRenderData(RenderResponse rsp) throws DataValidationException;

	public MessageBean getMessageByPK(long renderId) throws AddressException,
			DataValidationException, ParseException;

	public RenderRequest getRenderRequestByPK(long renderId) throws DataValidationException;
}
