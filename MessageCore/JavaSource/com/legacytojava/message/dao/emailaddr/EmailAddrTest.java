package com.legacytojava.message.dao.emailaddr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.legacytojava.message.vo.PagingVo;
import com.legacytojava.message.vo.emailaddr.EmailAddrVo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-mysql-config.xml", "/spring-common-config.xml"})
@TransactionConfiguration(transactionManager="mysqlTransactionManager", defaultRollback=true)
@Transactional
public class EmailAddrTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private EmailAddrDao emailAddrDao;
	final String insertEmailAddr = "jdoe2@test.com"; 

	@BeforeClass
	public static void EmailAddrPrepare() throws Exception {
	}
	
	@Test
	public void insertSelectDelete() {
		try {
			EmailAddrVo vo = insert();
			assertNotNull(vo);
			EmailAddrVo vo2 = selectByAddress(vo.getEmailAddr());
			assertNotNull(vo2);
			EmailAddrVo vo3 = selectByAddrId(vo2.getEmailAddrId());
			assertNotNull(vo3);
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setUpdtTime(vo2.getUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 4);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			EmailAddrVo vo = new EmailAddrVo();
			vo.setEmailAddr(insertEmailAddr);
			delete(vo);
		}
	}

	private EmailAddrVo selectByAddress(String emailAddr) {
		EmailAddrVo addrVo = emailAddrDao.findByAddress(emailAddr);
		System.out.println("EmailAddrDao - selectByAddress: "+LF+addrVo);
		return addrVo;
	}
	
	private EmailAddrVo selectByAddrId(long emailAddrId) {
		EmailAddrVo emailAddr = emailAddrDao.getByAddrId(emailAddrId);
		if (emailAddr!=null) {
			System.out.println("EmailAddrDao - selectByAddrId: "+LF+emailAddr);
		}
		emailAddrDao.getEmailAddrsWithPaging(new PagingVo());
		return emailAddr;
	}
	
	private int update(EmailAddrVo vo) {
		EmailAddrVo emailAddr = emailAddrDao.findByAddress(vo.getEmailAddr());
		int rowsUpdated = 0;
		if (emailAddr!=null) {
			emailAddr.setStatusId("A");
			emailAddr.setBounceCount(emailAddr.getBounceCount() + 1);
			rowsUpdated = emailAddrDao.update(emailAddr);
			rowsUpdated += emailAddrDao.updateLastRcptTime(emailAddr.getEmailAddrId());
			rowsUpdated += emailAddrDao.updateLastSentTime(emailAddr.getEmailAddrId());
			rowsUpdated += emailAddrDao.updateBounceCount(emailAddr);
			System.out.println("EmailAddrDao - rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int delete(EmailAddrVo emailAddrVo) {
		int rowsDeleted = emailAddrDao.deleteByAddress(emailAddrVo.getEmailAddr());
		System.out.println("EmailAddrDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private EmailAddrVo insert() {
		EmailAddrVo emailAddrVo = emailAddrDao.findByAddress("jsmith@test.com");
		emailAddrVo.setEmailAddr(insertEmailAddr);
		try {
			emailAddrDao.insert(emailAddrVo);
			System.out.println("EmailAddrDao - insert: "+emailAddrVo);
		}
		catch (org.springframework.dao.DataIntegrityViolationException e) {
			System.out.println("DataIntegrityViolationException caught: " + e);
			//e.printStackTrace();
		}
		return emailAddrVo;
	}
}
