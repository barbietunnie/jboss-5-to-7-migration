package com.legacytojava.message.dao.customer;

import java.util.List;

import com.legacytojava.message.vo.CustomerVo;
import com.legacytojava.message.vo.PagingCustomerVo;

public interface CustomerDao {
	public CustomerVo getByCustId(String custId);
	public List<CustomerVo> getByClientId(String clientId);
	public CustomerVo getByEmailAddrId(long emailAddrId);
	public CustomerVo getByEmailAddress(String emailAddr);
	public List<CustomerVo> getAll();
	public int getCustomerCount(PagingCustomerVo vo);
	public List<CustomerVo> getCustomersWithPaging(PagingCustomerVo vo);
	public int update(CustomerVo customerVo);
	public int delete(String custId);
	public int deleteByEmailAddr(String emailAddr);
	public int insert(CustomerVo customerVo);
}
