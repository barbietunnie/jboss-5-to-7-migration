package com.legacytojava.message.ejb.customer;
import javax.ejb.Local;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;

@Local
public interface CustomerLocal {
	public CustomerVo getCustomerByCustId(String custId) throws DataValidationException;
	public CustomerVo getCustomerByEmailAddress(String emailAddr);
	public int insertCustomer(CustomerVo vo) throws DataValidationException;
	public int updateCustomer(CustomerVo vo) throws DataValidationException;
	public int deleteCustomer(String custId) throws DataValidationException;
	public int deleteByEmailAddr(String emailAddr) throws DataValidationException;
}
