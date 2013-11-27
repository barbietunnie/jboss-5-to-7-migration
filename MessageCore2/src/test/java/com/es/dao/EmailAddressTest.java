package com.es.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.es.dao.address.EmailAddressDao;
import com.es.vo.address.EmailAddressVo;
import com.es.vo.comm.PagingVo;
import com.es.vo.comm.PagingVo.PageAction;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/spring-core-config.xml"})
@TransactionConfiguration(transactionManager="msgTransactionManager", defaultRollback=true)
@Transactional
public class EmailAddressTest {
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	private EmailAddressDao emailAddrDao;
	final String insertEmailAddr = "jdoe2@test.com"; 

	@BeforeClass
	public static void EmailAddrPrepare() throws Exception {
	}
	
	@Test
	public void insertSelectDelete() {
		try {
			EmailAddressVo vo = insert();
			assertNotNull(vo);
			EmailAddressVo vo2 = selectByAddress(vo.getEmailAddr());
			assertNotNull(vo2);
			EmailAddressVo vo3 = selectByAddrId(vo2.getEmailAddrId());
			assertNotNull(vo3);
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setUpdtTime(vo2.getUpdtTime());
			assertTrue(vo3.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 4);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
			PagingVo pgvo = new PagingVo();
			pgvo.setPageAction(PageAction.FIRST);
			List<EmailAddressVo> pgList = emailAddrDao.getEmailAddrsWithPaging(pgvo);
			System.out.println("getEmailAddrsWithPaging() - Rows = " + pgList.size());
			assertTrue(pgList.size()>0);
			long previewId = emailAddrDao.getEmailAddrIdForPreview();
			assertTrue(previewId>0);
		}
		catch (Exception e) {
			EmailAddressVo vo = new EmailAddressVo();
			vo.setEmailAddr(insertEmailAddr);
			delete(vo);
			e.printStackTrace();
			fail();
		}
	}

	private EmailAddressVo selectByAddress(String emailAddr) {
		EmailAddressVo addrVo = emailAddrDao.findSertAddress(emailAddr);
		System.out.println("EmailAddressDao - selectByAddress: "+LF+addrVo);
		return addrVo;
	}
	
	private EmailAddressVo selectByAddrId(long emailAddrId) {
		EmailAddressVo emailAddr = emailAddrDao.getByAddrId(emailAddrId);
		if (emailAddr!=null) {
			System.out.println("EmailAddressDao - selectByAddrId: "+LF+emailAddr);
		}
		return emailAddr;
	}
	
	private int update(EmailAddressVo vo) {
		EmailAddressVo emailAddr = emailAddrDao.findSertAddress(vo.getEmailAddr());
		int rowsUpdated = 0;
		if (emailAddr!=null) {
			emailAddr.setStatusId("A");
			emailAddr.setBounceCount(emailAddr.getBounceCount() + 1);
			rowsUpdated = emailAddrDao.update(emailAddr);
			rowsUpdated += emailAddrDao.updateLastRcptTime(emailAddr.getEmailAddrId());
			rowsUpdated += emailAddrDao.updateLastSentTime(emailAddr.getEmailAddrId());
			rowsUpdated += emailAddrDao.updateBounceCount(emailAddr);
			System.out.println("EmailAddressDao - rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int delete(EmailAddressVo emailAddrVo) {
		int rowsDeleted = emailAddrDao.deleteByAddress(emailAddrVo.getEmailAddr());
		System.out.println("EmailAddressDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private EmailAddressVo insert() {
		EmailAddressVo emailAddrVo = emailAddrDao.findSertAddress("jsmith@test.com");
		emailAddrVo.setEmailAddr(insertEmailAddr);
		try {
			emailAddrDao.insert(emailAddrVo);
			System.out.println("EmailAddressDao - insert: "+emailAddrVo);
		}
		catch (org.springframework.dao.DataIntegrityViolationException e) {
			System.out.println("DataIntegrityViolationException caught: " + e);
			//e.printStackTrace();
		}
		return emailAddrVo;
	}
}
