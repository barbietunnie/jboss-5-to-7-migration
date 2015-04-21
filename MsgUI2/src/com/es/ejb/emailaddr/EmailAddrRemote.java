package com.es.ejb.emailaddr;
import javax.ejb.Remote;

@Remote
public interface EmailAddrRemote {
	public EmailAddrVo findSertAddress(String address);
	public int deleteByAddress(String address);
}
