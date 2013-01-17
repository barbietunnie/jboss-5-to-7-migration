package com.legacytojava.msgui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.legacytojava.message.util.EmailAddrUtil;

public class EmailAddressValidator implements Validator {
   public void validate(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (emailAddr == null || emailAddr.trim().length() == 0)
			return;
		if (!EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
			FacesMessage message = com.legacytojava.msgui.util.Messages.getMessage(
					"com.legacytojava.msgui.messages", "invalidEmailAddress", null);
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}
}
