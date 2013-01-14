package com.legacytojava.message.ejb.sendmail;
import java.text.ParseException;

import javax.ejb.Local;
import javax.mail.internet.AddressException;

import com.legacytojava.message.bo.template.RenderRequest;
import com.legacytojava.message.bo.template.RenderResponse;
import com.legacytojava.message.exception.DataValidationException;

@Local
public interface RenderLocal {
	public RenderResponse getRenderedEmail(RenderRequest req) throws AddressException,
	DataValidationException, ParseException;
}
