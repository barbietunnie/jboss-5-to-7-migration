package com.legacytojava.message.bo.template;

import java.text.ParseException;

import javax.mail.internet.AddressException;

import com.legacytojava.message.exception.DataValidationException;

public interface RenderBo {
	public RenderResponse getRenderedEmail(RenderRequest req) throws DataValidationException,
			ParseException, AddressException;

	public RenderResponse getRenderedBody(RenderRequest req) throws DataValidationException,
			ParseException;

	public RenderResponse getRenderedMisc(RenderRequest req) throws DataValidationException;

	public RenderResponse getRenderedSubj(RenderRequest req) throws DataValidationException,
			ParseException;

	public RenderResponse getRenderedAddrs(RenderRequest req) throws DataValidationException,
			AddressException;

	public RenderResponse getRenderedXHdrs(RenderRequest req) throws DataValidationException;
}
