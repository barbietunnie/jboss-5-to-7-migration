package com.legacytojava.message.ejb.customer;
import javax.ejb.Remote;

import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;

@Remote
public interface CustomerSignupRemote {
	public int signupOnly(CustomerVo vo) throws DataValidationException;
	public int signupAndSubscribe(CustomerVo vo, String listId) throws DataValidationException;
	public int addToList(String emailAddr, String listId) throws DataValidationException;
	public int removeFromList(String emailAddr, String listId) throws DataValidationException;
}
