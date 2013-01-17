package com.legacytojava.message.dao.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.constant.Constants;
import com.legacytojava.message.vo.CustomerVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql_ds-config.xml", "/spring-dao-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class CustomerTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private CustomerDao customerDao;
	final String defaultCustId = "test";

	@BeforeClass
	public static void CustomerPrepare() throws Exception {
	}

	@Test
	public void insertSelectDelete() {
		try {
			List<CustomerVo> list = selectByClientId(Constants.DEFAULT_CLIENTID);
			assertTrue(list.size()>0);
			CustomerVo vo0 = selectByEmailAddrId(1L);
			assertNotNull(vo0);
			CustomerVo vo = insert();
			assertNotNull(vo);
			CustomerVo vo2 = selectByCustId(vo);
			assertNotNull(vo2);
			// sync-up next 3 fields since differences are expected
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setPrimaryKey(vo2.getPrimaryKey());
			// end of sync-up
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo2);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			CustomerVo vo = new CustomerVo();
			vo.setCustId(defaultCustId+"_1");
			delete(vo);
		}
	}

	private CustomerVo selectByCustId(CustomerVo vo) {
		CustomerVo customer = customerDao.getByCustId(vo.getCustId());
		if (customer!=null) {
			System.out.println("CustomerDao - selectByCustId: "+LF+customer);
		}
		return customer;
	}
	
	private CustomerVo selectByEmailAddrId(long emailId) {
		CustomerVo vo = customerDao.getByEmailAddrId(emailId);
		if (vo != null) {
			System.out.println("CustomerDao - selectEmailAddrId: "+LF+vo);
		}
		return vo;
	}
	
	private List<CustomerVo> selectByClientId(String clientId) {
		List<CustomerVo> list = (List<CustomerVo>)customerDao.getByClientId(clientId);
		for (int i=0; i<list.size(); i++) {
			CustomerVo customer = list.get(i);
			System.out.println("CustomerDao - selectClientId: "+LF+customer);
		}
		return list;
	}
	
	private int update(CustomerVo vo) {
		CustomerVo customer = customerDao.getByCustId(vo.getCustId());
		int rows = 0;
		if (customer!=null) {
			customer.setStatusId("A");
			rows = customerDao.update(customer);
			System.out.println("CustomerDao - update: rows updated: "+ rows);
		}
		return rows;
	}
	
	private CustomerVo insert() {
		CustomerVo customer = customerDao.getByCustId(defaultCustId);
		if (customer!=null) {
			customer.setCustId(customer.getCustId()+"_1");
			customer.setEmailAddr("test."+customer.getEmailAddr());
			customer.setBirthDate(new java.util.Date());
			customerDao.insert(customer);
			System.out.println("CustomerDao - insert: "+customer);
			return customer;
		}
		return null;
	}
	
	private int delete(CustomerVo customerVo) {
		int rowsDeleted = customerDao.delete(customerVo.getCustId());
		System.out.println("CustomerDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
}
