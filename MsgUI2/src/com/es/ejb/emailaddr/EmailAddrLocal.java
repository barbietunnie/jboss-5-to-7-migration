package com.es.ejb.emailaddr;
import javax.ejb.Local;

@Local
public interface EmailAddrLocal {
	public EmailAddrVo findByAddress(String address);
	public int deleteByAddress(String address);
}
