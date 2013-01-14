package com.legacytojava.message.ejb.customer;
import javax.ejb.Local;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;

@Local
public interface CustomerSignupLocal {
	public int signupOnly(CustomerVo vo) throws DataValidationException;
	public int signupAndSubscribe(CustomerVo vo, String listId) throws DataValidationException;
	public int addToList(String emailAddr, String listId) throws DataValidationException;
	public int removeFromList(String emailAddr, String listId) throws DataValidationException;
}
