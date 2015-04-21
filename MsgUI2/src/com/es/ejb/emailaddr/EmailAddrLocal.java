package com.es.ejb.emailaddr;
import javax.ejb.Local;

import jpa.model.EmailAddress;

@Local
public interface EmailAddrLocal {
	public EmailAddress findSertAddress(String address);
	public int delete(String address);
}
