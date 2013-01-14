package com.legacytojava.message.ejb.emailaddr;
import javax.ejb.Local;

import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

@Local
public interface EmailAddrLocal {
	public EmailAddrVo findByAddrId(int addrId);
	public EmailAddrVo findByAddress(String address);
	public int insert(EmailAddrVo emailAddrVo);
	public int update(EmailAddrVo emailAddrVo);
	public int deleteByAddrId(int addrId);
	public int deleteByAddress(String address);
}
