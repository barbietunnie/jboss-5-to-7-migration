package com.legacytojava.message.ejb.customer;
import javax.ejb.Remote;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;

@Remote
public interface CustomerRemote {
	public CustomerVo getCustomerByCustId(String custId) throws DataValidationException;
	public CustomerVo getCustomerByEmailAddress(String emailAddr);
	public int insertCustomer(CustomerVo vo) throws DataValidationException;
	public int updateCustomer(CustomerVo vo) throws DataValidationException;
	public int deleteCustomer(String custId) throws DataValidationException;
	public int deleteByEmailAddr(String emailAddr) throws DataValidationException;
}
