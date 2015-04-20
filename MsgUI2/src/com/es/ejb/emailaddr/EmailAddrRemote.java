package com.es.ejb.emailaddr;
import javax.ejb.Remote;

@Remote
public interface EmailAddrRemote {
	public EmailAddrVo findByAddress(String address);
	public int deleteByAddress(String address);
}
