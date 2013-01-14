package com.legacytojava.message.bo.customer;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.legacytojava.jbatch.JbMain;
import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.dao.customer.CustomerDao;
import com.legacytojava.message.exception.DataValidationException;
import com.legacytojava.message.vo.CustomerVo;

/*
 * !!! Do not modify this class to use SpringJunit as it will cause DB deadlock.
 */
public class CustomerSignupBoTest {
	static final Logger logger = Logger.getLogger(CustomerSignupBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	private CustomerSignupBo signUpBo = null;
	private CustomerDao customerDao = null;
	@BeforeClass
	public static void CustomerSignupBoPrepare() {
		JbMain.getInstance();
	}
	@Test
	public void testCustomerSignupBo() throws Exception {
		try {
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setName("customerSignUp");
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			PlatformTransactionManager txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("mysqlTransactionManager");
			TransactionStatus status = txmgr.getTransaction(def);
	
			CustomerVo vo = getCustomerDao().getByCustId("test");
			vo.setCustId(vo.getCustId()+"_v2");
			CustomerVo vo2 = getCustomerDao().getByCustId(vo.getCustId());
			if (vo2!=null) { // delete the customer record to make the test repeatable
				getCustomerDao().delete(vo2.getCustId());
			}
			vo.setEmailAddr("jwang@localhost");
			int rows = signUp(vo, "SMPLLST1"); // this is where the deadlock occurred.
			assertEquals(rows, 1);
			rows = removeFromList(vo.getEmailAddr(), "SMPLLST1");
			assertEquals(rows, 1);
			rows = addToList(vo.getEmailAddr(), "SMPLLST1");
			assertEquals(rows, 1);
			
			// rollback transaction
			txmgr.rollback(status);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	int signUp(CustomerVo vo, String listId) throws DataValidationException {
		int rows = getCustomerSignupBo().signUpAndSubscribe(vo, listId);
		logger.info("Number of rows added: " + rows);
		return rows;
	}

	int addToList(String emailAddr, String listId) throws DataValidationException {
		int rows = getCustomerSignupBo().addToList(emailAddr, listId);
		logger.info("Number of rows added: " + rows);
		return rows;
	}

	int removeFromList(String emailAddr, String listId) throws DataValidationException {
		int rows = getCustomerSignupBo().removeFromList(emailAddr, listId);
		logger.info("Number of rows removed: " + rows);
		return rows;
	}
	
	public CustomerSignupBo getCustomerSignupBo() {
		if (signUpBo==null) {
			signUpBo = (CustomerSignupBo) JbMain.getBatchAppContext().getBean("customerSignupBo");
		}
		return signUpBo;
	}

	public CustomerDao getCustomerDao() {
		if (customerDao==null) {
			customerDao = (CustomerDao) JbMain.getBatchAppContext().getBean("customerDao");
		}
		return customerDao;
	}
}
