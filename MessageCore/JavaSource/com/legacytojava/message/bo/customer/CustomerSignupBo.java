package com.legacytojava.message.bo.customer;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;

public interface CustomerSignupBo {

	public int signUpOnly(CustomerVo vo) throws DataValidationException;
	
	public int signUpAndSubscribe(CustomerVo vo, String listId) throws DataValidationException;
	
	public int addToList(String emailAddr, String listId) throws DataValidationException;
	
	public int removeFromList(String emailAddr, String listId) throws DataValidationException;
}
