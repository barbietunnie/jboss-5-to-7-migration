package com.legacytojava.message.ejb.emailaddr;
import javax.ejb.Remote;

import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

@Remote
public interface EmailAddrRemote {
	public EmailAddrVo findByAddrId(int addrId);
	public EmailAddrVo findByAddress(String address);
	public int insert(EmailAddrVo emailAddrVo);
	public int update(EmailAddrVo emailAddrVo);
	public int deleteByAddrId(int addrId);
	public int deleteByAddress(String address);
}
